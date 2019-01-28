package io.jhdf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.OrderedDataType;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV3;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ContigiousDataLayoutMessage;
import io.jhdf.object.message.DataSpace;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.object.message.Message;

public class DatasetImpl extends AbstractNode implements Dataset {
	private static final Logger logger = LoggerFactory.getLogger(DatasetImpl.class);

	private final LazyInitializer<Map<String, AttributeMessage>> attributes;
	private final LazyInitializer<ObjectHeader> header;
	private final FileChannel fc;

	public DatasetImpl(FileChannel fc, Superblock sb, long address, String name, Group parent) {
		super(address, name, parent);
		this.fc = fc;

		try {
			header = ObjectHeader.lazyReadObjectHeader(fc, sb, address);

			// Attributes
			attributes = new AttributesLazyInitializer(header);
		} catch (Exception e) {
			throw new HdfException("Error reading dataset '" + getPath() + "' at address " + address, e);
		}

	}

	private <T extends Message> T getHeaderMessage(Class<T> clazz) {
		try {
			return header.get().getMessageOfType(clazz);
		} catch (ConcurrentException e) {
			throw new HdfException("Failed to get header message of type '" + clazz.hashCode() + "' for dataset '"
					+ getPath() + "' at address '" + getAddress() + "'", e);
		}
	}

	@Override
	public NodeType getType() {
		return NodeType.DATASET;
	}

	@Override
	public Map<String, AttributeMessage> getAttributes() {
		try {
			return attributes.get();
		} catch (ConcurrentException e) {
			throw new HdfException(
					"Failed to load attributes for dataset '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
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
			throw new UnsupportedHdfException("Chunked datasets not supported yet");
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
