package io.jhdf.dataset;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.ObjectHeader;
import io.jhdf.Superblock;
import io.jhdf.api.Group;
import io.jhdf.btree.BTreeV1;
import io.jhdf.btree.BTreeV1.BTreeV1Data;
import io.jhdf.btree.BTreeV1.BTreeV1Data.Chunk;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV3;

public class ChunkedDatasetV3 extends DatasetBase {
	private static final Logger logger = LoggerFactory.getLogger(ChunkedDatasetV3.class);

	private Map<ChunkOffsetKey, Chunk> chunkLookup;

	private final ChunkedDataLayoutMessageV3 layoutMessage;

	public ChunkedDatasetV3(FileChannel fc, Superblock sb, long address, String name, Group parent, ObjectHeader oh) {
		super(fc, sb, address, name, parent, oh);

		layoutMessage = getHeaderMessage(ChunkedDataLayoutMessageV3.class);
		createChunkLookup();
	}

	private void createChunkLookup() {
		// TODO convert to thread safe lazy
		if (chunkLookup == null) {
			BTreeV1Data bTree = BTreeV1.createDataBTree(fc, sb, layoutMessage.getBTreeAddress(),
					getDimensions().length);

			List<Chunk> chunks = bTree.getChunks();
			chunkLookup = new HashMap<>(chunks.size());
			for (Chunk chunk : chunks) {
				chunkLookup.put(new ChunkOffsetKey(chunk.getChunkOffset()), chunk);
			}
			logger.debug("Created chunk lookup for '{}'", getPath());
		}
	}

	@Override
	public ByteBuffer getDataBuffer() {

		// Need to load the full buffer into memory so create the array
		byte[] dataArray = new byte[toIntExact(getDiskSize())];
		logger.trace("Created data buffer for '{}' of size {} bytes", getPath(), dataArray.length);

		int elementSize = getDataType().getSize();
		byte[] elementBuffer = new byte[elementSize];

		for (int i = 0; i < dataArray.length; i += elementSize) {
			int[] dimensionedIndex = linearIndexToDimensionIndex(i / elementSize, getDimensions());
			long[] chunkOffset = getChunkOffset(dimensionedIndex);
			Chunk chunk = chunkLookup.get(new ChunkOffsetKey(chunkOffset));

			// Now figure out which element inside the chunk
			int[] insideChunk = new int[chunkOffset.length];
			for (int j = 0; j < chunkOffset.length; j++) {
				insideChunk[j] = (int) (dimensionedIndex[j] - chunkOffset[j]);
			}
			int insideChunkLinearOffset = dimensionIndexToLinearIndex(insideChunk, layoutMessage.getDimSizes());

			ByteBuffer bb = getDataBuffer(chunk);
			bb.position(insideChunkLinearOffset * elementSize);
			bb.get(elementBuffer);

			// Copy that data into the overall buffer
			System.arraycopy(elementBuffer, 0, dataArray, i, elementSize);
		}

		return ByteBuffer.wrap(dataArray);
	}

	private ByteBuffer getDataBuffer(Chunk chunk) {
		try {
			return fc.map(MapMode.READ_ONLY, chunk.getAddress(), chunk.getSize());
		} catch (IOException e) {
			throw new HdfException(
					"Failed to read chunk for dataset '" + getPath() + "' at address " + chunk.getAddress());
		}
	}

	private int[] linearIndexToDimensionIndex(int index, int[] dimensions) {
		int[] dimIndex = new int[dimensions.length];

		for (int i = dimIndex.length - 1; i >= 0; i--) {
			dimIndex[i] = index % dimensions[i];
			index = index / dimensions[i];
		}
		return dimIndex;
	}

	private int dimensionIndexToLinearIndex(int[] index, long[] dimensions) {
		int linear = 0;
		for (int i = 0; i < dimensions.length; i++) {
			int temp = index[i];
			for (int j = i + 1; j < dimensions.length; j++) {
				temp *= dimensions[j];
			}
			linear += temp;
		}
		return linear;
	}

	private long[] getChunkOffset(int[] dimensionedIndex) {
		long[] chunkOffset = new long[dimensionedIndex.length];
		for (int i = 0; i < chunkOffset.length; i++) {
			long temp = toIntExact(layoutMessage.getDimSizes()[i]);
			chunkOffset[i] = (dimensionedIndex[i] / temp) * temp;
		}
		return chunkOffset;
	}

	private class ChunkOffsetKey {
		final int hashcode;
		final long[] chunkOffset;

		private ChunkOffsetKey(long[] chunkOffset) {
			this.chunkOffset = chunkOffset;
			hashcode = ArrayUtils.hashCode(chunkOffset);
		}

		@Override
		public int hashCode() {
			return hashcode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChunkOffsetKey other = (ChunkOffsetKey) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return hashcode == other.hashcode;
		}

		private ChunkedDatasetV3 getEnclosingInstance() {
			return ChunkedDatasetV3.this;
		}

		@Override
		public String toString() {
			return "ChunkOffsetKey [chunkOffset=" + Arrays.toString(chunkOffset) + ", hashcode=" + hashcode + "]";
		}

	}

}
