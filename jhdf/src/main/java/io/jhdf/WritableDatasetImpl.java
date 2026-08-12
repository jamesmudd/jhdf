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
import io.jhdf.api.DatasetCreationOptions;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
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

import static io.jhdf.Utils.flatten;
import static io.jhdf.Utils.stripLeadingIndex;

public class WritableDatasetImpl extends AbstractWritableNode implements WritableDataset {

	private static final Logger logger = LoggerFactory.getLogger(WritableDatasetImpl.class);

	private final Object data;
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
		if (options == null) {
			options = DatasetCreationOptions.DEFAULT;
		}
		this.dataType = DataType.fromObject(data, options.isUnsigned());
		this.dataSpace = DataSpace.fromObject(data);
		this.chunkDimensions = resolveChunkDimensions(options);
		this.requestedFilters = chunkDimensions != null ? options.getFilters() : Collections.emptyList();
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

		// Writing chunks encodes the full dataset into a single buffer first
		if (dataSpace.getTotalLength() * dataType.getSize() > Integer.MAX_VALUE) {
			throw new HdfWritingException("Dataset is too large to write chunked. Maximum is ["
				+ Integer.MAX_VALUE + "] bytes");
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
		return data == null;
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
		return data;
	}

	@Override
	public Object getDataFlat() {
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
			return Utils.primitiveToWrapper(type);
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
		if (isChunked()) {
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

		// Encode the full dataset into a flat row major buffer then slice it into chunks
		final byte[] flatData = dataType.encodeData(data).array();

		final List<Chunk> chunks = new ArrayList<>(totalChunks);
		long address = dataAddress;
		for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
			final long[] chunkOffset = Utils.chunkIndexToChunkOffset((long) chunkIndex, chunkDimensions, datasetDimensions);

			byte[] chunkBytes = extractChunk(flatData, datasetDimensions, chunkDimensions, chunkOffset, elementSize, chunkSizeInBytes);
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
