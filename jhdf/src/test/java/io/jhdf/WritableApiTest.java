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

import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritiableDataset;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class WritableApiTest {

	@Test
	void testBuildingFile() {
		Path path = Paths.get("writing.hdf5");
		try (WritableHdfFile file = HdfFile.write(path)) {

			assertThat(file.getParent()).isNull();
			assertThat(file.getFile()).hasName("writing.hdf5");

			WritableGroup group1 = file.putGroup("group1");
			WritiableDataset datasetInGroup1 = group1.putDataset("datasetInGroup1", new int[]{1, 2, 3, 4, 5});

			WritiableDataset datasetInRoot = file.putDataset("datasetInRoot", new double[]{1, 2, 3, 4, 5});

			assertThat(file.getChildren()).hasSize(2);
			assertThat(file.getChildren()).containsEntry("group1", group1);
			assertThat(file.getChildren()).containsEntry("datasetInRoot", datasetInRoot);

			assertThat(file.getChild("group1")).isEqualTo(group1);
			assertThat(group1.getChildren()).hasSize(1);
			assertThat(group1.getChild("datasetInGroup1")).isEqualTo(datasetInGroup1);

			assertThat(datasetInRoot.getJavaType()).isEqualTo(double.class);
			assertThat(datasetInRoot.getDimensions()).isEqualTo(new int[]{5});

			assertThat(datasetInGroup1.getJavaType()).isEqualTo(int.class);
			assertThat(datasetInRoot.getDimensions()).isEqualTo(new int[]{5});

		}
	}
}
