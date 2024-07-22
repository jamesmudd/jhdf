/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.DataLayout;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WritableDatasetImplTest {

	@Test
	void testGettingSize() {
		int[] data = new int[] {1,2,3};
		WritableDatasetImpl writableDataset = new WritableDatasetImpl(data, "ints", null);
		assertThat(writableDataset.getSize()).isEqualTo(3);
		assertThat(writableDataset.getSizeInBytes()).isEqualTo(3 * 4);
		assertThat(writableDataset.getStorageInBytes()).isEqualTo(3 * 4);
	}
	@Test
	void testGettingData() {
		int[][] data = new int[][] {{1,2,3}, {4,5,6}};
		WritableDatasetImpl writableDataset = new WritableDatasetImpl(data, "ints", null);
		assertThat(writableDataset.getData()).isEqualTo(new int[][] {{1,2,3}, {4,5,6}});
		assertThat(writableDataset.getDataFlat()).isEqualTo(ArrayUtils.toObject(new int[] {1,2,3, 4,5,6}));
		assertThat(writableDataset.getDimensions()).isEqualTo(new int[]{2,3});
		assertThat(writableDataset.getMaxSize()).isEqualTo(new long[]{2,3});
		assertThat(writableDataset.getJavaType()).isEqualTo(int.class);
		assertThat(writableDataset.getDataLayout()).isEqualTo(DataLayout.CONTIGUOUS);
		assertThrows(HdfException.class, () ->
			writableDataset.getData(new long[] {1, 2}, new int[] {1, 2}));
		assertThat(writableDataset.getFillValue()).isNull();
		assertThat(writableDataset.getFilters()).isEmpty();
		assertThat(writableDataset.getDataType()).isNotNull();
	}

	@Test
	void testGettingFlags() {
		int[][] data = new int[][] {{1,2,3}, {4,5,6}};
		WritableDatasetImpl writableDataset = new WritableDatasetImpl(data, "ints", null);
		assertThat(writableDataset.isScalar()).isFalse();
		assertThat(writableDataset.isEmpty()).isFalse();
		assertThat(writableDataset.isLink()).isFalse();
		assertThat(writableDataset.isGroup()).isFalse();
		assertThat(writableDataset.isVariableLength()).isFalse();
		assertThat(writableDataset.isCompound()).isFalse();
		assertThat(writableDataset.isAttributeCreationOrderTracked()).isFalse();
		assertThat(writableDataset.getType()).isEqualTo(NodeType.DATASET);
	}

}
