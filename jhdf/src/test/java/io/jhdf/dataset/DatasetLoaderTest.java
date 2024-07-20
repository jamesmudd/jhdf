/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.ObjectHeader;
import io.jhdf.api.Group;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayout;
import io.jhdf.object.message.DataLayoutMessage;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatasetLoaderTest {

	@Test
	void testUnrecognisedDataLayoutMessage() {
		HdfBackingStorage hdfBackingStorage = mock(HdfFileChannel.class);
		ObjectHeader objectHeader = mock(ObjectHeader.class);
		String name = "test";
		Group group = mock(Group.class);

		when(objectHeader.getMessageOfType(DataLayoutMessage.class))
			.thenReturn(new UnknownDataLayoutMessage(new BitSet()));

		assertThrows(HdfException.class, () -> DatasetLoader.createDataset(hdfBackingStorage, objectHeader, name, group));
	}

	@Test
	void testFailureToReadDataset() {
		HdfBackingStorage hdfBackingStorage = mock(HdfFileChannel.class);
		ObjectHeader objectHeader = mock(ObjectHeader.class);
		String name = "test";
		Group group = mock(Group.class);

		when(objectHeader.getMessageOfType(DataLayoutMessage.class))
			.thenThrow(RuntimeException.class);

		assertThrows(HdfException.class, () -> DatasetLoader.createDataset(hdfBackingStorage, objectHeader, name, group));
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
