package io.jhdf.dataset;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.AbstractNode;
import io.jhdf.Superblock;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.btree.BTreeV1;
import io.jhdf.btree.BTreeV1.BTreeV1Data;
import io.jhdf.btree.BTreeV1.BTreeV1Data.Chunk;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.OrderedDataType;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV3;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ContigiousDataLayoutMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;

public class DatasetImpl extends AbstractNode implements Dataset {
	private static final Logger logger = LoggerFactory.getLogger(DatasetImpl.class);

	private final FileChannel fc;
	private final Superblock sb;

	public DatasetImpl(FileChannel fc, Superblock sb, long address, String name, Group parent) {
		super(fc, sb, address, name, parent);
		this.fc = fc;
		this.sb = sb;
	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	@Override
	public ByteBuffer getDataBuffer() {
		// First get the raw buffer
		final ByteBuffer dataBuffer;
		DataLayoutMessage dataLayoutMessage = getHeaderMessage(DataLayoutMessage.class);
		if (dataLayoutMessage instanceof CompactDataLayoutMessage) {
			dataBuffer = ((CompactDataLayoutMessage) dataLayoutMessage).getDataBuffer();
		} else if (dataLayoutMessage instanceof ContigiousDataLayoutMessage) {
			ContigiousDataLayoutMessage contigiousDataLayoutMessage = (ContigiousDataLayoutMessage) dataLayoutMessage;
			try {
				dataBuffer = fc.map(MapMode.READ_ONLY, contigiousDataLayoutMessage.getAddress(),
						contigiousDataLayoutMessage.getSize());
			} catch (IOException e) {
				throw new HdfException("Failed to map data buffer for dataset '" + getPath() + "'", e);
			}
		} else if (dataLayoutMessage instanceof ChunkedDataLayoutMessageV3) {
			ChunkedDataLayoutMessageV3 chunkedLayout = (ChunkedDataLayoutMessageV3) dataLayoutMessage;
			BTreeV1Data bTree = BTreeV1.createDataBTree(fc, sb, chunkedLayout.getBTreeAddress(),
					getDimensions().length);

			byte[] dataArray = new byte[0];

			for (Chunk chunk : bTree.getChunks()) {
				int size = chunk.getSize();
				ByteBuffer bb = ByteBuffer.allocate(size);
				try {
					fc.read(bb, chunk.getAddress());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dataArray = ArrayUtils.addAll(dataArray, bb.array());
			}
			return ByteBuffer.wrap(dataArray);

//			throw new UnsupportedHdfException("Chunked datasets not supported yet");
		} else {
			throw new HdfException(
					"Unsupported data layout. Layout class is: " + dataLayoutMessage.getClass().getCanonicalName());
		}

		// Convert to the correct endiness
		final DataType dataType = getHeaderMessage(DataTypeMessage.class).getDataType();
		if (dataType instanceof OrderedDataType) {
			final ByteOrder order = (((OrderedDataType) dataType).getByteOrder());
			dataBuffer.order(order);
			logger.debug("Set buffer oder of '{}' to {}", getPath(), order);
		}

		return dataBuffer;
	}

	@Override
	public long getSize() {
		DataSpace dataSpace = getHeaderMessage(DataSpaceMessage.class).getDataSpace();
		return dataSpace.getTotalLentgh();
	}

	@Override
	public long getDiskSize() {
		final DataType dataType = getHeaderMessage(DataTypeMessage.class).getDataType();
		return getSize() * dataType.getSize();
	}

	@Override
	public int[] getDimensions() {
		return getHeaderMessage(DataSpaceMessage.class).getDataSpace().getDimensions();
	}

	@Override
	public Optional<int[]> getMaxSize() {
		DataSpace dataSpace = getHeaderMessage(DataSpaceMessage.class).getDataSpace();
		if (dataSpace.isMaxSizesPresent()) {
			return Optional.of(dataSpace.getMaxSizes());
		} else {
			return Optional.empty();
		}
	}

	@Override
	public DataLayout getDataLayout() {
		return getHeaderMessage(DataLayoutMessage.class).getDataLayout();
	}

	@Override
	public Class<?> getJavaType() {
		DataType dataType = getDataType();
		return dataType.getJavaType();
	}

	private DataType getDataType() {
		return getHeaderMessage(DataTypeMessage.class).getDataType();
	}

	@Override
	public Object getData() {
		logger.debug("Getting data for '{}'...", getPath());
		return DatasetReader.readDataset(getDataType(), getDataBuffer(), getDimensions());
	}

	@Override
	public String toString() {
		return "DatasetImpl [path=" + getPath() + "]";
	}

}
