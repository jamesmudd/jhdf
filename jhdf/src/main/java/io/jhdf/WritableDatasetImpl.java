/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.ChunkProvider;
import io.jhdf.api.DatasetCreationOptions;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.api.StreamingDataset;
import io.jhdf.api.WritableDataset;
import io.jhdf.dataset.chunked.Chunk;
import io.jhdf.dataset.chunked.indexing.ChunkImpl;
import io.jhdf.dataset.chunked.indexing.FixedArrayIndexWriter;
import io.jhdf.exceptions.HdfWritingException;
import io.jhdf.filter.ByteShuffleFilter;
import io.jhdf.filter.Filter;
import io.jhdf.filter.FilterManager;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.AttributeInfoMessage;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.object.message.FillValueMessage;
import io.jhdf.object.message.FilterPipelineMessage;
import io.jhdf.object.message.FilterPipelineMessage.FilterInfo;
import io.jhdf.object.message.Message;
import io.jhdf.storage.HdfFileChannel;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.jhdf.Utils.flatten;
import static io.jhdf.Utils.stripLeadingIndex;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

public class WritableDatasetImpl extends AbstractWritableNode implements StreamingDataset {

	private static final Logger logger = LoggerFactory.getLogger(WritableDatasetImpl.class);

	private final Object data;
	/** Supplies chunks on demand instead of holding the whole dataset, null when data is held in memory */
	private final ChunkProvider chunkProvider;
	/** Set for a dataset streamed a chunk at a time; its data and index are written before the object header is */
	private StreamingState streamingState;
	private final DataType dataType;

	private final DataSpace dataSpace;

	/** Chunk dimensions if chunked storage is used, null for contiguous storage */
	private final int[] chunkDimensions;
	private final List<DatasetCreationOptions.RequestedFilter> requestedFilters;

	/** The size in bytes actually used for the data after filtering, set once written */
	private long storageInBytes = -1;

	public WritableDatasetImpl(Object data, String name, Group parent) {
		this(data, name, parent, DatasetCreationOptions.DEFAULT);
	}

	public WritableDatasetImpl(Object data, String name, Group parent, DatasetCreationOptions options) {
		super(parent, name);
		this.data = data;
		this.chunkProvider = null;
		if (options == null) {
			options = DatasetCreationOptions.DEFAULT;
		}
		this.dataType = DataType.fromObject(data, options.isUnsigned());
		this.dataSpace = DataSpace.fromObject(data);
		this.chunkDimensions = resolveChunkDimensions(options);
		this.requestedFilters = chunkDimensions != null ? options.getFilters() : Collections.emptyList();
	}

	/**
	 * Creates a chunked dataset whose data is supplied a chunk at a time, so it is never held in memory as a whole.
	 *
	 * @param javaType the dataset's element type e.g. {@code double.class}
	 * @param dimensions the dataset's dimensions
	 * @param name the dataset name
	 * @param parent the parent group
	 * @param options must specify chunk dimensions
	 * @param chunkProvider supplies each chunk when the file is written
	 */
	public WritableDatasetImpl(Class<?> javaType, int[] dimensions, String name, Group parent,
							   DatasetCreationOptions options, ChunkProvider chunkProvider) {
		this(javaType, dimensions, name, parent, options, Objects.requireNonNull(chunkProvider,
			"chunkProvider cannot be null"), false);
	}

	/**
	 * Creates a chunked dataset with no data, either pulled from a {@link ChunkProvider} or pushed a chunk at a
	 * time through {@link StreamingDataset#writeChunk(long[], Object)}.
	 */
	WritableDatasetImpl(Class<?> javaType, int[] dimensions, String name, Group parent,
						DatasetCreationOptions options, ChunkProvider chunkProvider, boolean streaming) {
		super(parent, name);
		this.data = null;
		this.chunkProvider = chunkProvider;
		Objects.requireNonNull(javaType, "javaType cannot be null");
		if (dimensions == null || dimensions.length == 0) {
			throw new HdfWritingException("Dimensions must be provided for a chunk provider dataset");
		}
		for (int dimension : dimensions) {
			if (dimension < 1) {
				throw new HdfWritingException("Dimensions " + Arrays.toString(dimensions) + " must all be positive");
			}
		}
		if (options == null || !options.isChunked()) {
			throw new HdfWritingException("Chunk dimensions must be specified to write a dataset from a chunk provider");
		}
		// A single element of the requested type and rank is enough to derive the data type and a dataspace of the
		// right rank, without allocating anything proportional to the dataset. It must not be zero length in any
		// dimension because the type is found by walking into the array.
		final int[] oneOfEach = new int[dimensions.length];
		Arrays.fill(oneOfEach, 1);
		final Object prototype = Array.newInstance(javaType, oneOfEach);
		this.dataType = DataType.fromObject(prototype, options.isUnsigned());
		this.dataSpace = DataSpace.modifyDimensions(DataSpace.fromObject(prototype), dimensions);
		this.chunkDimensions = resolveChunkDimensions(options);
		this.requestedFilters = options.getFilters();
	}

	private int[] resolveChunkDimensions(DatasetCreationOptions options) {
		if (!options.isChunked()) {
			return null;
		}

		final int[] datasetDimensions = dataSpace.getDimensions();
		if (datasetDimensions.length == 0) {
			throw new HdfWritingException("Chunked storage cannot be used with scalar datasets");
		}

		int[] resolvedChunkDimensions = options.getChunkDimensions();
		if (resolvedChunkDimensions == null) {
			// Filters requested without chunk dimensions so write the dataset as a single chunk
			resolvedChunkDimensions = datasetDimensions;
		}

		if (resolvedChunkDimensions.length != datasetDimensions.length) {
			throw new HdfWritingException("Chunk dimensions " + Arrays.toString(resolvedChunkDimensions)
				+ " must have the same rank as the dataset dimensions " + Arrays.toString(datasetDimensions));
		}
		for (int i = 0; i < resolvedChunkDimensions.length; i++) {
			if (resolvedChunkDimensions[i] < 1 || resolvedChunkDimensions[i] > datasetDimensions[i]) {
				throw new HdfWritingException("Chunk dimensions " + Arrays.toString(resolvedChunkDimensions)
					+ " must be in the range 1 - dataset dimensions " + Arrays.toString(datasetDimensions));
			}
		}

		try {
			final long chunkSizeInBytes = getChunkSizeInBytes(resolvedChunkDimensions);
			if (chunkSizeInBytes > Integer.MAX_VALUE) {
				throw new HdfWritingException("Chunk size in bytes [" + chunkSizeInBytes + "] is too large. Maximum is ["
					+ Integer.MAX_VALUE + "] bytes, use smaller chunk dimensions");
			}
		} catch (ArithmeticException e) {
			throw new HdfWritingException("Chunk size in bytes overflows for chunk dimensions "
				+ Arrays.toString(resolvedChunkDimensions), e);
		}

		// Slicing chunks out of memory encodes the full dataset into a single buffer first, so it is bounded by
		// what a byte[] can hold. Supplying chunks one at a time never does that, whether they are pulled from a
		// ChunkProvider or pushed through a StreamingDataset, so there the limit is per chunk (checked above).
		if (data != null && dataSpace.getTotalLength() * dataType.getSize() > Integer.MAX_VALUE) {
			throw new HdfWritingException("Dataset is too large to write chunked. Maximum is ["
				+ Integer.MAX_VALUE + "] bytes, supply the data a chunk at a time with a "
				+ ChunkProvider.class.getSimpleName() + " or a " + StreamingDataset.class.getSimpleName()
				+ " to write a larger dataset");
		}

		return resolvedChunkDimensions;
	}

	private long getChunkSizeInBytes(int[] chunkDims) {
		long chunkElements = 1;
		for (int chunkDim : chunkDims) {
			chunkElements = Math.multiplyExact(chunkElements, chunkDim);
		}
		return Math.multiplyExact(chunkElements, dataType.getSize());
	}

	@Override
	public long getSize() {
		return dataSpace.getTotalLength();
	}

	@Override
	public long getSizeInBytes() {
		return getSize() * dataType.getSize();
	}

	@Override
	public long getStorageInBytes() {
		if (storageInBytes >= 0) {
			return storageInBytes;
		}
		// Not written yet so the storage used (e.g. after compression) is not known
		return getSizeInBytes();
	}

	@Override
	public int[] getDimensions() {
		return dataSpace.getDimensions();
	}

	@Override
	public long[] getDimensionsAsLong() {
		return dataSpace.getDimensionsAsLong();
	}

	@Override
	public boolean isScalar() {
		if (isEmpty()) {
			return false;
		}
		return getDimensions().length == 0;
	}

	@Override
	public boolean isEmpty() {
		return data == null && chunkProvider == null && streamingState == null;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public boolean isVariableLength() {
		return false;
	}

	@Override
	public long[] getMaxSize() {
		return dataSpace.getMaxSizes();
	}

	@Override
	public DataLayout getDataLayout() {
		if (isChunked()) {
			return DataLayout.CHUNKED;
		}
		return DataLayout.CONTIGUOUS;
	}

	private boolean isChunked() {
		return chunkDimensions != null;
	}

	@Override
	public Object getData() {
		requireDataInMemory();
		return data;
	}

	@Override
	public Object getDataFlat() {
		requireDataInMemory();
		return flatten(data);
	}

	@Override
	public Object getDataFlat(long[] sliceOffset, int[] sliceDimensions) {
		throw new HdfWritingException("Slicing a writable dataset not supported");
	}

	@Override
	public Object getData(long[] sliceOffset, int[] sliceDimensions) {
		throw new HdfWritingException("Slicing a writable dataset not supported");
	}

	@Override
	public Class<?> getJavaType() {
		final Class<?> type = dataType.getJavaType();
		// For scalar datasets the returned type will be the wrapper class because
		// getData returns Object
		if (isScalar() && type.isPrimitive()) {
			return primitiveToWrapper(type);
		}
		return type;
	}

	@Override
	public DataType getDataType() {
		return dataType;
	}

	@Override
	public Object getFillValue() {
		return null;
	}

	@Override
	public List<PipelineFilterWithData> getFilters() {
		if (requestedFilters.isEmpty()) {
			return Collections.emptyList();
		}
		return FilterManager.getPipeline(resolveFilterInfos()).getFilters();
	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	@Override
	public boolean isGroup() {
		return false;
	}

	@Override
	public File getFile() {
		return getParent().getFile();
	}

	@Override
	public Path getFileAsPath() {
		return getParent().getFileAsPath();
	}

	@Override
	public HdfFile getHdfFile() {
		return getParent().getHdfFile();
	}

	@Override
	public long getAddress() {
		throw new HdfWritingException("Address not known until written");
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return false;
	}

	@Override
	public long write(HdfFileChannel hdfFileChannel, long position) {
		logger.info("Writing dataset [{}] at position [{}]", getPath(), position);
		List<Message> messages = new ArrayList<>();
		messages.add(DataTypeMessage.create(this.dataType));
		messages.add(DataSpaceMessage.create(this.dataSpace));
		messages.add(FillValueMessage.NO_FILL);

		final List<FilterInfo> filterInfos = resolveFilterInfos();
		if (!filterInfos.isEmpty()) {
			messages.add(FilterPipelineMessage.create(filterInfos));
		}

		// The address and sizes are only known once the data is written, so a placeholder message with the same
		// encoded length is used to find the object header size then swapped for the real message
		final DataLayoutMessage placeholder = createDataLayoutMessagePlaceholder(filterInfos);
		messages.add(placeholder);

		if(!getAttributes().isEmpty()) {
			AttributeInfoMessage attributeInfoMessage = AttributeInfoMessage.create();
			messages.add(attributeInfoMessage);
			for (Map.Entry<String, Attribute> attribute : getAttributes().entrySet()) {
				logger.info("Writing attribute [{}]", attribute.getKey());
				AttributeMessage attributeMessage = AttributeMessage.create(attribute.getKey(), attribute.getValue());
				messages.add(attributeMessage);
			}
		}

		ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
		int ohSize = objectHeader.toBuffer().limit();

		// Now know where we will write the data
		final long dataAddress = position + ohSize;

		final DataLayoutMessage dataLayoutMessage;
		final long endPosition;
		if (streamingState != null) {
			if (!streamingState.closed) {
				throw new HdfWritingException("Dataset [" + getPath() + "] was still streaming when the file was"
					+ " closed. Close the dataset once every chunk has been written.");
			}
			// The chunks and the index were written as they arrived, so only the object header is left
			dataLayoutMessage = streamingState.dataLayoutMessage;
			endPosition = dataAddress;
		} else if (isChunked()) {
			final ChunkedDataResult result = writeChunkedData(hdfFileChannel, dataAddress, filterInfos);
			dataLayoutMessage = result.dataLayoutMessage;
			endPosition = result.endPosition;
			// Leave the channel positioned at the end of this dataset matching the contiguous path
			hdfFileChannel.position(endPosition);
		} else {
			final long dataSize = writeData(hdfFileChannel, dataAddress);
			dataLayoutMessage = ContiguousDataLayoutMessage.create(dataAddress, dataSize);
			endPosition = dataAddress + dataSize;
		}

		// Now switch placeholder for real data layout message, in place so the object header size is unchanged
		messages.set(messages.indexOf(placeholder), dataLayoutMessage);

		objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);

		hdfFileChannel.write(objectHeader.toBuffer(), position);

		return endPosition;
	}

	/**
	 * Builds the filter specifications used to write this dataset filling in dataset dependent settings.
	 */
	private List<FilterInfo> resolveFilterInfos() {
		if (requestedFilters.isEmpty()) {
			return Collections.emptyList();
		}
		final List<FilterInfo> filterInfos = new ArrayList<>(requestedFilters.size());
		for (DatasetCreationOptions.RequestedFilter requestedFilter : requestedFilters) {
			final Filter filter = requestedFilter.getFilter();
			int[] filterData = requestedFilter.getFilterData();
			if (filter.getId() == ByteShuffleFilter.ID && filterData.length == 0) {
				// The shuffle filter needs the element size, set it now the dataset is known. Matches the
				// behaviour of H5Z_shuffle set_local
				filterData = new int[]{dataType.getSize()};
			}
			filterInfos.add(new FilterInfo(filter.getId(), filter.getName(), false, filterData));
		}
		return filterInfos;
	}

	private DataLayoutMessage createDataLayoutMessagePlaceholder(List<FilterInfo> filterInfos) {
		if (isChunked()) {
			return createChunkedDataLayoutMessage(Constants.UNDEFINED_ADDRESS, 0, !filterInfos.isEmpty());
		}
		return ContiguousDataLayoutMessage.create(Constants.UNDEFINED_ADDRESS, Constants.UNDEFINED_ADDRESS);
	}

	private ChunkedDataLayoutMessageV4 createChunkedDataLayoutMessage(long address, int filteredChunkSize, boolean filtered) {
		// The layout message chunk dimensions have the dataset element size appended
		final int[] layoutChunkDimensions = ArrayUtils.add(chunkDimensions, dataType.getSize());
		if (getTotalChunks() == 1) {
			if (filtered) {
				return ChunkedDataLayoutMessageV4.createFilteredSingleChunk(address, layoutChunkDimensions, filteredChunkSize);
			}
			return ChunkedDataLayoutMessageV4.createSingleChunk(address, layoutChunkDimensions);
		}
		return ChunkedDataLayoutMessageV4.createFixedArray(address, layoutChunkDimensions, calculatePageBits(getTotalChunks()));
	}

	private int getTotalChunks() {
		return Utils.totalChunks(getDimensions(), chunkDimensions);
	}

	/**
	 * The page size for fixed array indices. 2^10 = 1024 elements matching the HDF5 library default, grown if
	 * needed so the fixed array is always unpaged.
	 */
	private static int calculatePageBits(int totalChunks) {
		final int bitsNeeded = 32 - Integer.numberOfLeadingZeros(totalChunks - 1); // ceil(log2(totalChunks))
		return Math.max(10, bitsNeeded);
	}

	/**
	 * Supplies the raw, unfiltered, fully padded bytes of a single chunk.
	 * <p>
	 * Chunks are written to the file one at a time already; the only thing forcing the whole dataset into memory
	 * is where those bytes come from. Naming that dependency lets a chunk be produced on demand instead of
	 * sliced out of a buffer holding everything.
	 *
	 * @see #inMemoryChunkSource(int[], int, int)
	 */
	@FunctionalInterface
	interface ChunkSource {
		/**
		 * @param chunkOffset the offset of the chunk within the dataset
		 * @return exactly {@code chunkSizeInBytes} bytes, zero padded where the chunk overhangs the dataset
		 */
		byte[] chunkBytes(long[] chunkOffset);
	}

	/**
	 * A {@link ChunkSource} that slices chunks out of the whole dataset encoded in memory.
	 */
	private ChunkSource inMemoryChunkSource(int[] datasetDimensions, int elementSize, int chunkSizeInBytes) {
		final byte[] flatData = dataType.encodeData(data).array();
		return chunkOffset -> extractChunk(flatData, datasetDimensions, chunkDimensions, chunkOffset, elementSize,
			chunkSizeInBytes);
	}

	/**
	 * A {@link ChunkSource} that asks the {@link ChunkProvider} for each chunk as it is written.
	 */
	private ChunkSource providerChunkSource(int chunkSizeInBytes) {
		return chunkOffset -> {
			final Object chunkData = chunkProvider.getChunk(chunkOffset);
			if (chunkData == null) {
				throw new HdfWritingException("No data supplied for the chunk at offset "
					+ Arrays.toString(chunkOffset) + " of dataset [" + getPath() + "]");
			}
			final byte[] chunkBytes = dataType.encodeData(chunkData).array();
			if (chunkBytes.length != chunkSizeInBytes) {
				throw new HdfWritingException("The chunk at offset " + Arrays.toString(chunkOffset) + " of dataset ["
					+ getPath() + "] encoded to [" + chunkBytes.length + "] bytes, expected [" + chunkSizeInBytes
					+ "] bytes for chunk dimensions " + Arrays.toString(chunkDimensions));
			}
			return chunkBytes;
		};
	}

	private void requireDataInMemory() {
		if (chunkProvider != null) {
			throw new HdfWritingException("Dataset [" + getPath() + "] is written from a "
				+ ChunkProvider.class.getSimpleName() + " so its data is not held in memory");
		}
		if (streamingState != null) {
			throw new HdfWritingException("Dataset [" + getPath() + "] is streamed a chunk at a time so its data is"
				+ " not held in memory");
		}
	}

	/**
	 * Everything a dataset needs while its chunks are arriving: where to put them, what has arrived, and the layout
	 * message the object header will need once it is all in.
	 */
	private final class StreamingState {
		private final HdfFileChannel hdfFileChannel;
		private final FileSpace fileSpace;
		private final List<FilterInfo> filterInfos;
		private final int chunkSizeInBytes;
		private final int totalChunks;
		/** Indexed by chunk index rather than appended to, so chunks may arrive in any order */
		private final Chunk[] chunks;
		private final long firstAddress;
		private DataLayoutMessage dataLayoutMessage;
		private boolean closed;

		private StreamingState(HdfFileChannel hdfFileChannel, FileSpace fileSpace) {
			this.hdfFileChannel = hdfFileChannel;
			this.fileSpace = fileSpace;
			this.filterInfos = resolveFilterInfos();
			this.chunkSizeInBytes = Math.toIntExact(getChunkSizeInBytes(chunkDimensions));
			this.totalChunks = getTotalChunks();
			this.chunks = new Chunk[totalChunks];
			this.firstAddress = fileSpace.nextAddress();
		}
	}

	/**
	 * Starts streaming, so chunks can be written before the file's tree exists.
	 */
	void startStreaming(HdfFileChannel hdfFileChannel, FileSpace fileSpace) {
		if (streamingState != null) {
			throw new HdfWritingException("Dataset [" + getPath() + "] is already streaming");
		}
		this.streamingState = new StreamingState(hdfFileChannel, fileSpace);
	}

	@Override
	public void writeChunk(long[] chunkOffset, Object chunkData) {
		final StreamingState state = requireStreaming();
		if (state.closed) {
			throw new HdfWritingException("Dataset [" + getPath() + "] is closed so cannot take more chunks");
		}
		Objects.requireNonNull(chunkData, "chunk data cannot be null");
		final int chunkIndex = chunkIndexOf(chunkOffset);
		if (state.chunks[chunkIndex] != null) {
			throw new HdfWritingException("The chunk at offset " + Arrays.toString(chunkOffset) + " of dataset ["
				+ getPath() + "] has already been written");
		}

		byte[] chunkBytes = dataType.encodeData(chunkData).array();
		if (chunkBytes.length != state.chunkSizeInBytes) {
			throw new HdfWritingException("The chunk at offset " + Arrays.toString(chunkOffset) + " of dataset ["
				+ getPath() + "] encoded to [" + chunkBytes.length + "] bytes, expected [" + state.chunkSizeInBytes
				+ "] bytes for chunk dimensions " + Arrays.toString(chunkDimensions));
		}
		chunkBytes = applyFilters(chunkBytes, state.filterInfos);

		final long address = state.fileSpace.reserve(chunkBytes.length);
		writeFully(state.hdfFileChannel, ByteBuffer.wrap(chunkBytes), address);
		state.chunks[chunkIndex] = new ChunkImpl(address, chunkBytes.length, chunkOffset.clone());
	}

	@Override
	public void close() {
		final StreamingState state = requireStreaming();
		if (state.closed) {
			return;
		}

		for (int chunkIndex = 0; chunkIndex < state.totalChunks; chunkIndex++) {
			if (state.chunks[chunkIndex] == null) {
				throw new HdfWritingException("No data was written for the chunk at offset "
					+ Arrays.toString(Utils.chunkIndexToChunkOffset((long) chunkIndex, chunkDimensions, getDimensions()))
					+ " of dataset [" + getPath() + "]");
			}
		}

		final boolean filtered = !state.filterInfos.isEmpty();
		if (state.totalChunks == 1) {
			final Chunk chunk = state.chunks[0];
			state.dataLayoutMessage = createChunkedDataLayoutMessage(chunk.getAddress(), chunk.getSize(), filtered);
		} else {
			final long fixedArrayAddress = state.fileSpace.nextAddress();
			final ByteBuffer fixedArrayBuffer = FixedArrayIndexWriter.createFixedArray(Arrays.asList(state.chunks),
				fixedArrayAddress, state.chunkSizeInBytes, filtered, calculatePageBits(state.totalChunks));
			state.fileSpace.reserve(fixedArrayBuffer.limit());
			writeFully(state.hdfFileChannel, fixedArrayBuffer, fixedArrayAddress);
			state.dataLayoutMessage = createChunkedDataLayoutMessage(fixedArrayAddress, 0, filtered);
		}

		this.storageInBytes = state.fileSpace.nextAddress() - state.firstAddress;
		state.closed = true;
		logger.info("Finished streaming dataset [{}]. Chunks [{}], storage size [{}] bytes",
			getPath(), state.totalChunks, storageInBytes);
	}

	private StreamingState requireStreaming() {
		if (streamingState == null) {
			throw new HdfWritingException("Dataset [" + getPath() + "] is not streamed a chunk at a time");
		}
		return streamingState;
	}

	private int chunkIndexOf(long[] chunkOffset) {
		final int[] datasetDimensions = getDimensions();
		if (chunkOffset.length != datasetDimensions.length) {
			throw new HdfWritingException("Chunk offset " + Arrays.toString(chunkOffset) + " does not match the "
				+ datasetDimensions.length + " dimensions of dataset [" + getPath() + "]");
		}
		int chunkIndex = 0;
		for (int dimension = 0; dimension < datasetDimensions.length; dimension++) {
			final long offset = chunkOffset[dimension];
			if (offset < 0 || offset >= datasetDimensions[dimension] || offset % chunkDimensions[dimension] != 0) {
				throw new HdfWritingException("Chunk offset " + Arrays.toString(chunkOffset) + " is not the start of"
					+ " a chunk of dataset [" + getPath() + "] with chunk dimensions "
					+ Arrays.toString(chunkDimensions));
			}
			final int chunksInDimension =
				(datasetDimensions[dimension] + chunkDimensions[dimension] - 1) / chunkDimensions[dimension];
			chunkIndex = chunkIndex * chunksInDimension + Math.toIntExact(offset / chunkDimensions[dimension]);
		}
		return chunkIndex;
	}

	private static final class ChunkedDataResult {
		private final DataLayoutMessage dataLayoutMessage;
		private final long endPosition;

		private ChunkedDataResult(DataLayoutMessage dataLayoutMessage, long endPosition) {
			this.dataLayoutMessage = dataLayoutMessage;
			this.endPosition = endPosition;
		}
	}

	private ChunkedDataResult writeChunkedData(HdfFileChannel hdfFileChannel, long dataAddress, List<FilterInfo> filterInfos) {
		logger.info("Writing chunked data for dataset [{}] at position [{}]", getPath(), dataAddress);

		final int[] datasetDimensions = getDimensions();
		final int elementSize = dataType.getSize();
		final int chunkSizeInBytes = Math.toIntExact(getChunkSizeInBytes(chunkDimensions));
		final int totalChunks = getTotalChunks();
		final boolean filtered = !filterInfos.isEmpty();

		final ChunkSource chunkSource = chunkProvider == null
			? inMemoryChunkSource(datasetDimensions, elementSize, chunkSizeInBytes)
			: providerChunkSource(chunkSizeInBytes);

		final List<Chunk> chunks = new ArrayList<>(totalChunks);
		long address = dataAddress;
		for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
			final long[] chunkOffset = Utils.chunkIndexToChunkOffset((long) chunkIndex, chunkDimensions, datasetDimensions);

			byte[] chunkBytes = chunkSource.chunkBytes(chunkOffset);
			chunkBytes = applyFilters(chunkBytes, filterInfos);

			writeFully(hdfFileChannel, ByteBuffer.wrap(chunkBytes), address);
			chunks.add(new ChunkImpl(address, chunkBytes.length, chunkOffset));
			address += chunkBytes.length;
		}

		this.storageInBytes = address - dataAddress;

		final DataLayoutMessage dataLayoutMessage;
		long endPosition = address;
		if (totalChunks == 1) {
			final Chunk chunk = chunks.get(0);
			dataLayoutMessage = createChunkedDataLayoutMessage(chunk.getAddress(), chunk.getSize(), filtered);
		} else {
			// Multiple chunks so write a fixed array index pointing at them
			final long fixedArrayAddress = address;
			final ByteBuffer fixedArrayBuffer = FixedArrayIndexWriter.createFixedArray(chunks, fixedArrayAddress,
				chunkSizeInBytes, filtered, calculatePageBits(totalChunks));
			endPosition = fixedArrayAddress + fixedArrayBuffer.limit();
			writeFully(hdfFileChannel, fixedArrayBuffer, fixedArrayAddress);
			dataLayoutMessage = createChunkedDataLayoutMessage(fixedArrayAddress, 0, filtered);
		}

		logger.info("Finished writing chunked data for dataset [{}]. Chunks [{}], storage size [{}] bytes",
			getPath(), totalChunks, storageInBytes);
		return new ChunkedDataResult(dataLayoutMessage, endPosition);
	}

	/**
	 * Applies the filters to a chunk in pipeline (encode) order. The filter instances come from the requested
	 * filters, the settings from the resolved filterInfos which is a parallel list.
	 */
	private byte[] applyFilters(byte[] chunkBytes, List<FilterInfo> filterInfos) {
		for (int i = 0; i < filterInfos.size(); i++) {
			chunkBytes = requestedFilters.get(i).getFilter().encode(chunkBytes, filterInfos.get(i).getData());
		}
		return chunkBytes;
	}

	/**
	 * Copies the data for one chunk out of the flat encoded dataset. Edge chunks are full sized with the area
	 * outside the dataset left zero filled.
	 */
	private static byte[] extractChunk(byte[] flatData, int[] datasetDimensions, int[] chunkDimensions, long[] chunkOffset, int elementSize, int chunkSizeInBytes) {
		final byte[] chunkBytes = new byte[chunkSizeInBytes];
		final int rank = datasetDimensions.length;
		final int fastestChunkDim = chunkDimensions[rank - 1];
		final int fastestOffset = Math.toIntExact(chunkOffset[rank - 1]);

		// The bytes to copy for each contiguous run, may be clipped by the edge of the dataset
		final int runLengthBytes = Math.min(fastestChunkDim, datasetDimensions[rank - 1] - fastestOffset) * elementSize;

		// Every combination of the chunk dimensions except the fastest is a contiguous run in the dataset
		final int[] runDimensions = Arrays.copyOf(chunkDimensions, rank - 1);
		final int runs = Arrays.stream(runDimensions).reduce(1, Math::multiplyExact);

		final int[] datasetIndex = new int[rank];
		runLoop:
		for (int run = 0; run < runs; run++) {
			final int[] runIndex = Utils.linearIndexToDimensionIndex(run, runDimensions);
			for (int dim = 0; dim < rank - 1; dim++) {
				final int index = Math.toIntExact(chunkOffset[dim]) + runIndex[dim];
				if (index >= datasetDimensions[dim]) {
					// This run is outside the dataset so leave the zero padding
					continue runLoop;
				}
				datasetIndex[dim] = index;
			}
			datasetIndex[rank - 1] = fastestOffset;

			final int sourceOffsetBytes = Utils.dimensionIndexToLinearIndex(datasetIndex, datasetDimensions) * elementSize;
			final int destinationOffsetBytes = run * fastestChunkDim * elementSize;
			System.arraycopy(flatData, sourceOffsetBytes, chunkBytes, destinationOffsetBytes, runLengthBytes);
		}
		return chunkBytes;
	}

	private static void writeFully(HdfFileChannel hdfFileChannel, ByteBuffer buffer, long address) {
		while (buffer.hasRemaining()) {
			address += hdfFileChannel.write(buffer, address);
		}
	}

	private long writeData(HdfFileChannel hdfFileChannel, long dataAddress) {
		logger.info("Writing data for dataset [{}] at position [{}]", getPath(), dataAddress);

		hdfFileChannel.position(dataAddress);

		dataType.writeData(data, getDimensions(), hdfFileChannel);

		return  dataSpace.getTotalLength() * dataType.getSize();
	}


	private static void writeDoubleData(Object data, int[] dims, ByteBuffer buffer, HdfFileChannel hdfFileChannel) {
		if (dims.length > 1) {
			for (int i = 0; i < dims[0]; i++) {
				Object newArray = Array.get(data, i);
				writeDoubleData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel);
			}
		} else {
			buffer.asDoubleBuffer().put((double[]) data);
			hdfFileChannel.write(buffer);
			buffer.clear();
		}
	}
}
