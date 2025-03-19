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
import io.jhdf.TestUtils;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import io.jhdf.examples.TestAllFilesBase;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatasetWritingTest {

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ScalarDatasets {
		private Path tempFile;

        @Test
		@Order(1)
        void writeScalarDatasets() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

            // Scalar
            writableHdfFile.putDataset("ScalarByteDataset", (byte) 10);
            writableHdfFile.putDataset("ScalarShortDataset",  (short) 133);
            writableHdfFile.putDataset("ScalarIntDataset", -32455);
            writableHdfFile.putDataset("ScalarLongDataset", 83772644L);
            writableHdfFile.putDataset("ScalarFloatDataset", -78472.324F);
            writableHdfFile.putDataset("ScalarDoubleDataset", 2134922.321114);

            // Actually flush and write everything
            writableHdfFile.close();

            // Now read it back
            try (HdfFile hdfFile = new HdfFile(tempFile)) {
                Map<String, Node> datasets = hdfFile.getChildren();
                assertThat(datasets).hasSize(6);
				// Verify scalar
				for (Node node : datasets.values()) {
					Dataset dataset = (Dataset) node;
					assertThat(dataset.isScalar());
					assertThat(dataset.getDimensions()).isEqualTo(ArrayUtils.EMPTY_INT_ARRAY);
				}

				// Just check thw whole file is readable
				TestAllFilesBase.verifyAttributes(hdfFile);
                TestAllFilesBase.recurseGroup(hdfFile);

				TestUtils.compareGroups(writableHdfFile, hdfFile);
            }
        }

        @Test
        @Order(2)
        @EnabledIfH5DumpAvailable
        void readScalarDatasetsWithH5Dump() throws Exception {
            // Read with h5dump
            HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

            // Read with jhdf
            try (HdfFile hdfFile = new HdfFile(tempFile)) {
                // Compare
                H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
			}
        }
	}

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class OneDDatasets {
		private Path tempFile;

        @Test
		@Order(1)
        void write1DDatasets() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
            WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

            // 1D
			writableHdfFile.putDataset("1DByteDataset", new byte[]{10, 20, 30});
			writableHdfFile.putDataset("1DByteObjDataset", new Byte[]{10, 20, 30});
			writableHdfFile.putDataset("1DShortDataset", new short[]{133, 222, 444});
			writableHdfFile.putDataset("1DShortObjDataset", new Short[]{133, 222, 444});
			writableHdfFile.putDataset("1DIntDataset", new int[]{-32455, 121, 244});
			writableHdfFile.putDataset("1DIntObjDataset", new Integer[]{-32455, 121, 244});
			writableHdfFile.putDataset("1DLongDataset", new long[]{83772644, 234242});
			writableHdfFile.putDataset("1DLongObjDataset", new Long[]{837726444324L, 843589389219L});
			writableHdfFile.putDataset("1DFloatDataset", new float[]{-282.3242f, -9453.214f});
			writableHdfFile.putDataset("1DFloatObjDataset", new Float[]{372.324242f, -2434.32324f});
			writableHdfFile.putDataset("1DDoubleDataset", new double[]{21342.324, -232342.434});
			writableHdfFile.putDataset("1DDoubleObjDataset", new Double[]{3421342.324113, 54366.324223});

			// Actually flush and write everything
			writableHdfFile.close();

			// Now read it back
            try (HdfFile hdfFile = new HdfFile(tempFile)) {
				// Just check thw whole file is readable
				TestAllFilesBase.verifyAttributes(hdfFile);
                TestAllFilesBase.recurseGroup(hdfFile);

				TestUtils.compareGroups(writableHdfFile, hdfFile);
			}
        }

        @Test
        @Order(2)
        @EnabledIfH5DumpAvailable
        void read1DDatasetWithH5Dump() throws Exception {
            // Read with h5dump
            HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

            // Read with jhdf
            try (HdfFile hdfFile = new HdfFile(tempFile)) {
                // Compare
                H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
            }
        }
    }

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class NDDataset {
		private Path tempFile;

        @Test
		@Order(1)
        void writeNDDataset() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

            // nD
            writableHdfFile.putDataset("3DByteDataset", new byte[][][]{
                    {{10, 20, 30}, {10, 20, 30}, {10, 20, 30}},
                    {{100, -123, 33}, {35, 11, -35}, {-43, 22, 99}}});
            writableHdfFile.putDataset("2DByteObjDataset", new Byte[][]{
                    {1, 2, 3, 4, 5},
                    {6, 7, 8, 9, 10}});
            writableHdfFile.putDataset("3DShortDataset", new short[][][]{
                    {{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
                    {{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
            writableHdfFile.putDataset("3DShortObjDataset", new Short[][][]{
                    {{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
                    {{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
            writableHdfFile.putDataset("3DIntDataset", new int[][][]{
                    {{-32455, 121, 244}, {-32455, 121, 244}},
                    {{452345, -91221, 42244}, {-355, 121321, 244099}},
                    {{5341, -43121, -210044}, {-200455, 121112, 224244}}});
            writableHdfFile.putDataset("3DIntObjDataset", new Integer[][][]{
                    {{-32455, 121, 244}, {-32455, 121, 244}},
                    {{452345, -91221, 42244}, {-355, 121321, 244099}},
                    {{5341, -43121, -210044}, {-200455, 121112, 224244}}});
            writableHdfFile.putDataset("3DLongDataset", new long[][][]{
                    {{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
                    {{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
                    {{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
            writableHdfFile.putDataset("3DLongObjDataset", new Long[][][]{
                    {{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
                    {{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
                    {{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
            writableHdfFile.putDataset("3DFloatDataset", new float[][][]{
                    {{-7372.3242f, -442453.213424f}, {8473.242f, -2453.213424f}},
                    {{-372.42f, 53.13424f}, {-11732.3242f, -9924.4f}},
                    {{7372.324f, -2453.24f}, {237372.324242f, -2453.21333424f}},
                    {{5558473.242f, 82453.213424f}, {-288473.242f, -2453.213435324f}}});
            writableHdfFile.putDataset("3DFloatObjDataset", new Float[][][]{
                    {{-7372.3242f, -442453.213424f}, {8473.242f, -2453.213424f}},
                    {{-372.42f, 53.13424f}, {-11732.3242f, -9924.4f}},
                    {{7372.324f, -2453.24f}, {237372.324242f, -2453.21333424f}},
                    {{5558473.242f, 82453.213424f}, {-288473.242f, -2453.213435324f}}});
            writableHdfFile.putDataset("3DDoubleDataset", new double[][][]{
                    {{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
                    {{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
                    {{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
                    {{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});
            writableHdfFile.putDataset("3DDoubleObjDataset", new Double[][][]{
                    {{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
                    {{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
                    {{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
                    {{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});

            // Actually flush and write everything
            writableHdfFile.close();

            // Now read it back
            try (HdfFile hdfFile = new HdfFile(tempFile)) {
                // Just check thw whole file is readable
                TestAllFilesBase.verifyAttributes(hdfFile);
                TestAllFilesBase.recurseGroup(hdfFile);

				TestUtils.compareGroups(writableHdfFile, hdfFile);
			}
        }

        @Test
        @Order(2)
        @EnabledIfH5DumpAvailable
        void readNDDatasetWithH5Dump() throws Exception {
            // Read with h5dump
            HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

            // Read with jhdf
            try (HdfFile hdfFile = new HdfFile(tempFile)) {
                // Compare
                H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
            }
        }
	}

	@Test
	void testNoDatasetNameFails() throws IOException {
		Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		assertThrows(IllegalArgumentException.class,
			() -> writableHdfFile.putDataset(null, 111));

		assertThrows(IllegalArgumentException.class,
			() -> writableHdfFile.putDataset("", 111));
	}

	@Test
	void testNullDatasFails() throws IOException {
		Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		assertThrows(NullPointerException.class,
			() -> writableHdfFile.putDataset("test", null));
	}
}
