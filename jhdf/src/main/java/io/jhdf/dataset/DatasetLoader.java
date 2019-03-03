package io.jhdf.dataset;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.VariableLength;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV3;
import io.jhdf.object.message.DataLayoutMessage.ChunkedDataLayoutMessageV4;
import io.jhdf.object.message.DataLayoutMessage.CompactDataLayoutMessage;
import io.jhdf.object.message.DataLayoutMessage.ContigiousDataLayoutMessage;
import io.jhdf.object.message.DataTypeMessage;

public final class DatasetLoader {

	private DatasetLoader() {
		// No instances
	}

	public static Dataset createDataset(HdfFileChannel hdfFc, long address, String name,
			Group parent) {

		try {
			// Load the object header to determine the type of dataset to make
			final ObjectHeader oh = ObjectHeader.readObjectHeader(hdfFc, address);
			DataLayoutMessage dlm = oh.getMessageOfType(DataLayoutMessage.class);

			final DatasetBase dataset;
			if (dlm instanceof CompactDataLayoutMessage) {
				dataset = new CompactDataset(hdfFc, address, name, parent, oh);

			} else if (dlm instanceof ContigiousDataLayoutMessage) {
				dataset = new ContiguousDataset(hdfFc, address, name, parent, oh);

			} else if (dlm instanceof ChunkedDataLayoutMessageV3) {
				dataset = new ChunkedDatasetV3(hdfFc, address, name, parent, oh);

			} else if (dlm instanceof ChunkedDataLayoutMessageV4) {
				throw new UnsupportedHdfException("Chunked V4 dataset not supported");

			} else {
				throw new HdfException("Unreconised Dataset layout type: " + dlm.getClass().getCanonicalName());
			}

			// Check for variable size data type handle this separately as it uses global
			// heap
			DataType type = oh.getMessageOfType(DataTypeMessage.class).getDataType();

			if (type instanceof VariableLength) {
				// If its a variable length data type wrap the dataset
				return new VaribleLentghDataset(hdfFc, dataset, oh);
			} else {
				return dataset; // not wrapped
			}

		} catch (Exception e) {
			throw new HdfException("Failed to read dataset '" + name + "' at address '" + address + "'", e);
		}
	}

}
