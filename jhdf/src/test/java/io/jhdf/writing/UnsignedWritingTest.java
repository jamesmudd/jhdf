/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.writing;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnsignedWritingTest {

	@Test
	void writeUnsignedDatasetAndAttribute() throws Exception {
		Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		writableHdfFile.putDataset("UnsignedShortDataset", new short[]{-1, 0, 1, 133}, true);
		writableHdfFile.putAttribute("UnsignedByteAttribute", (byte) -1, true);

		writableHdfFile.close();

		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			Dataset dataset = hdfFile.getDatasetByPath("UnsignedShortDataset");
			// unsigned shorts get promoted to int in jHDF
			assertThat(dataset.getJavaType()).isEqualTo(int.class);
			int[] data = (int[]) dataset.getData();
			assertThat(data).containsExactly(65535, 0, 1, 133);

			Attribute attribute = hdfFile.getAttribute("UnsignedByteAttribute");
			assertThat(attribute.getJavaType()).isEqualTo(Integer.class);
			assertThat(attribute.getData()).isEqualTo(255);
		}
	}

	@Test
	void unsignedNotSupportedForNonFixedPointTypes() throws Exception {
		Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		assertThatThrownBy(() -> writableHdfFile.putDataset("bad", 1.23, true))
			.isInstanceOf(HdfException.class);

		assertThatThrownBy(() -> writableHdfFile.putAttribute("bad", "notFixedPoint", true))
			.isInstanceOf(HdfException.class);

		writableHdfFile.close();
	}
}
