package io.jhdf.dataset;

import io.jhdf.HdfFileChannel;
import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.invokeConstructor;

class DatasetLoaderTest {

	@Test
	void noInstances() {
		assertThrows(AssertionError.class, () -> invokeConstructor(DatasetLoader.class));
	}

	@Test
	void testUnrecognisedDataLayoutMessage() {
		HdfFileChannel hdfFileChannel = mock(HdfFileChannel.class);
		ObjectHeader objectHeader = mock(ObjectHeader.class);
		String name = "test";
		Group group = mock(Group.class);

		when(objectHeader.getMessageOfType(DataLayoutMessage.class))
				.thenReturn(new UnknownDataLayoutMessage(new BitSet()));

		assertThrows(HdfException.class, () -> DatasetLoader.createDataset(hdfFileChannel, objectHeader, name, group));
	}

	@Test
	void testFailureToReadDataset() {
		HdfFileChannel hdfFileChannel = mock(HdfFileChannel.class);
		ObjectHeader objectHeader = mock(ObjectHeader.class);
		String name = "test";
		Group group = mock(Group.class);

		when(objectHeader.getMessageOfType(DataLayoutMessage.class))
				.thenThrow(RuntimeException.class);

		assertThrows(HdfException.class, () -> DatasetLoader.createDataset(hdfFileChannel, objectHeader, name, group));
	}

	private static class UnknownDataLayoutMessage extends DataLayoutMessage {

		public UnknownDataLayoutMessage(BitSet flags) {
			super(flags);
		}

		@Override
		public DataLayout getDataLayout() {
			return null;
		}
	}
}