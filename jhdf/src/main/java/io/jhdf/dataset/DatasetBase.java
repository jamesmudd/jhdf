package io.jhdf.dataset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.AbstractNode;
import io.jhdf.ObjectHeader;
import io.jhdf.Superblock;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.OrderedDataType;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;

public abstract class DatasetBase extends AbstractNode implements Dataset {
	private static final Logger logger = LoggerFactory.getLogger(DatasetBase.class);

	protected final FileChannel fc;
	protected final Superblock sb;
	protected final ObjectHeader oh;

	public DatasetBase(FileChannel fc, Superblock sb, long address, String name, Group parent, ObjectHeader oh) {
		super(fc, sb, address, name, parent);
		this.fc = fc;
		this.sb = sb;
		this.oh = oh;
	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	protected void convertToCorrectEndiness(ByteBuffer bb) {
		final DataType dataType = getHeaderMessage(DataTypeMessage.class).getDataType();
		if (dataType instanceof OrderedDataType) {
			final ByteOrder order = (((OrderedDataType) dataType).getByteOrder());
			bb.order(order);
			logger.debug("Set buffer oder of '{}' to {}", getPath(), order);
		}
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

	protected DataType getDataType() {
		return getHeaderMessage(DataTypeMessage.class).getDataType();
	}

	@Override
	public Object getData() {
		logger.debug("Getting data for '{}'...", getPath());
		return DatasetReader.readDataset(getDataType(), getDataBuffer(), getDimensions());
	}

	@Override
	public String toString() {
		return "DatasetBase [path=" + getPath() + "]";
	}

}