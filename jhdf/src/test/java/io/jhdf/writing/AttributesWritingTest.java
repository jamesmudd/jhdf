/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.writing;

import io.jhdf.HdfFile;
import io.jhdf.TestUtils;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritiableDataset;
import io.jhdf.examples.TestAllFilesBase;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AttributesWritingTest {

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class DataAttributes {
		private Path tempFile;
		@Test
		@Order(1)
		void writeAttributes() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

			WritableGroup intGroup = writableHdfFile.putGroup("intGroup");
			int[] intData1 = new int[]{-5, -4, -3, -2, -1, 0, 1,2,3,4,5 };
			WritiableDataset intDataset = intGroup.putDataset("intData1", intData1);

			writableHdfFile.putAttribute("rootAttribute", new int[] {1,2,3});

			intGroup.putAttribute("groupByteAttribute", new byte[] {1,2,3});
			intGroup.putAttribute("groupShortAttribute", new short[] {1,2,3});
			intGroup.putAttribute("groupIntAttribute", new int[] {1,2,3});
			intGroup.putAttribute("groupLongAttribute", new long[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
			intGroup.putAttribute("groupFloatAttribute", new float[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
			intGroup.putAttribute("groupDoubleAttribute", new double[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});

			intDataset.putAttribute("datasetByteAttribute", new byte[] {1,2,3});
			intDataset.putAttribute("datasetShortAttribute", new short[] {1,2,3});
			intDataset.putAttribute("datasetIntAttribute", new int[] {1,2,3});
			intDataset.putAttribute("datasetLongAttribute", new long[] {1,2,3});

			// Actually flush and write everything
			writableHdfFile.close();

			// Now read it back
			try(HdfFile hdfFile = new HdfFile(tempFile)) {
				Map<String, Attribute> attributes = hdfFile.getAttributes();
				assertThat(attributes).containsKeys("rootAttribute");
				Attribute attribute = attributes.get("rootAttribute");
				assertThat(attribute.getData()).isEqualTo(new int[] {1,2,3});
				assertThat(attribute.getDimensions()).isEqualTo(new int[] {3});
				assertThat(attribute.getJavaType()).isEqualTo(int.class);

				// Just check thw whole file is readable
				TestAllFilesBase.verifyAttributes(hdfFile);
				TestAllFilesBase.recurseGroup(hdfFile);

				TestUtils.compareGroups(writableHdfFile, hdfFile);
			}
		}

		@Test
		@Order(2)
		@EnabledIfH5DumpAvailable
		void readAttributesWithH5Dump() throws Exception {
			// Read with h5dump
			HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

			// Read with jhdf
			try(HdfFile hdfFile = new HdfFile(tempFile)) {
				// Compare
				H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
			}
		}
	}

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ScalarAttributes {
		private Path tempFile;

		@Test
		@Order(1)
		void writeScalarAttributes() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

			// Scalar
			writableHdfFile.putAttribute("groupScalarByteAttribute", (byte) 10);
			writableHdfFile.putAttribute("groupScalarShortAttribute", (short) 133);
			writableHdfFile.putAttribute("groupScalarIntAttribute", -32455);
			writableHdfFile.putAttribute("groupScalarLongAttribute", 83772644L);
			writableHdfFile.putAttribute("groupScalarFloatAttribute", -78472.324F);
			writableHdfFile.putAttribute("groupScalarDoubleAttribute", 21342.324);

			WritiableDataset intDataset = writableHdfFile.putDataset("intData", new int[]{1, 2, 3});

			intDataset.putAttribute("datasetByteAttribute", (byte) 10);
			intDataset.putAttribute("datasetShortAttribute", (short) 133);
			intDataset.putAttribute("datasetIntAttribute", -32455);
			intDataset.putAttribute("datasetLongAttribute", 83772644L);
			intDataset.putAttribute("datasetFloatAttribute", -78472.324F);
			intDataset.putAttribute("datasetDoubleAttribute", 21342.324);

			// Actually flush and write everything
			writableHdfFile.close();

			// Now read it back
			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				Map<String, Attribute> attributes = hdfFile.getAttributes();
				assertThat(attributes).hasSize(6);

				// Just check thw whole file is readable
				TestAllFilesBase.verifyAttributes(hdfFile);
				TestAllFilesBase.recurseGroup(hdfFile);
			}
		}

		@Test
		@Order(2)
		@EnabledIfH5DumpAvailable
		void readScalarAttributesWithH5Dump() throws Exception {
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
	class OneDAttributes {
		private Path tempFile;

		@Test
		@Order(1)
		void write1DAttributes() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

			// 1D
			writableHdfFile.putAttribute("group1DByteAttribute", new byte[]{10, 20, 30});
			writableHdfFile.putAttribute("group1DByteObjAttribute", new Byte[]{10, 20, 30});
			writableHdfFile.putAttribute("group1DShortAttribute", new short[]{133, 222, 444});
			writableHdfFile.putAttribute("group1DShortObjAttribute", new Short[]{133, 222, 444});
			writableHdfFile.putAttribute("group1DIntAttribute", new int[]{-32455, 121, 244});
			writableHdfFile.putAttribute("group1DIntObjAttribute", new Integer[]{-32455, 121, 244});
			writableHdfFile.putAttribute("group1DLongAttribute", new long[]{83772644, 234242});
			writableHdfFile.putAttribute("group1DLongObjAttribute", new Long[]{837726444324L, 843589389219L});
			writableHdfFile.putAttribute("group1DFloatAttribute", new float[]{-282.3242f, -9453.214f});
			writableHdfFile.putAttribute("group1DFloatObjAttribute", new Float[]{372.324242f, -2434.32324f});
			writableHdfFile.putAttribute("group1DDoubleAttribute", new double[]{21342.324, -232342.434});
			writableHdfFile.putAttribute("group1DDoubleObjAttribute", new Double[]{3421342.324113, 54366.324223});

			WritiableDataset intDataset = writableHdfFile.putDataset("intData", new int[]{1, 2, 3});

			intDataset.putAttribute("dataset1DByteAttribute", new byte[]{10, 20, 30});
			intDataset.putAttribute("dataset1DByteObjAttribute", new Byte[]{10, 20, 30});
			intDataset.putAttribute("dataset1DShortAttribute", new short[]{133, 222, 444});
			intDataset.putAttribute("dataset1DShortObjAttribute", new Short[]{133, 222, 444});
			intDataset.putAttribute("dataset1DIntAttribute", new int[]{-32455, 121, 244});
			intDataset.putAttribute("dataset1DIntObjAttribute", new Integer[]{-32455, 121, 244});
			intDataset.putAttribute("dataset1DLongAttribute", new long[]{83772644, 234242});
			intDataset.putAttribute("dataset1DLongObjAttribute", new Long[]{837726444324L, 843589389219L});
			intDataset.putAttribute("dataset1DFloatAttribute", new float[]{28473.72f, -7453.29f});
			intDataset.putAttribute("dataset1DFloatObjAttribute", new Float[]{28473.72f, -7453.29f});
			intDataset.putAttribute("dataset1DDoubleAttribute", new double[]{21342.324, -232342.434});
			intDataset.putAttribute("dataset1DDoubleObjAttribute", new Double[]{3421342.324113, 54366.324223});

			// Actually flush and write everything
			writableHdfFile.close();

			// Now read it back
			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				Map<String, Attribute> attributes = hdfFile.getAttributes();
				assertThat(attributes).hasSize(12);

				// Just check thw whole file is readable
				TestAllFilesBase.verifyAttributes(hdfFile);
				TestAllFilesBase.recurseGroup(hdfFile);

				TestUtils.compareGroups(writableHdfFile, hdfFile);
			}
		}

		@Test
		@Order(2)
		@EnabledIfH5DumpAvailable
		void read1DAttributesWithH5Dump() throws Exception {
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
	class NDAttributes {
		private Path tempFile;

		@Test
		@Order(1)
		void writeNDAttributes() throws Exception {
			tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
			WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

			// nD
			writableHdfFile.putAttribute("group3DByteAttribute", new byte[][][]{
					{{10, 20, 30}, {10, 20, 30}, {10, 20, 30}},
					{{100, -123, 33}, {35, 11, -35}, {-43, 22, 99}}});
			writableHdfFile.putAttribute("group2DByteObjAttribute", new Byte[][]{
					{1, 2, 3, 4, 5},
					{6, 7, 8, 9, 10}});
			writableHdfFile.putAttribute("group3DShortAttribute", new short[][][]{
					{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
					{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
			writableHdfFile.putAttribute("group3DShortObjAttribute", new Short[][][]{
					{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
					{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
			writableHdfFile.putAttribute("group3DIntAttribute", new int[][][]{
					{{-32455, 121, 244}, {-32455, 121, 244}},
					{{452345, -91221, 42244}, {-355, 121321, 244099}},
					{{5341, -43121, -210044}, {-200455, 121112, 224244}}});
			writableHdfFile.putAttribute("group3DIntObjAttribute", new Integer[][][]{
					{{-32455, 121, 244}, {-32455, 121, 244}},
					{{452345, -91221, 42244}, {-355, 121321, 244099}},
					{{5341, -43121, -210044}, {-200455, 121112, 224244}}});
			writableHdfFile.putAttribute("group3DLongAttribute", new long[][][]{
					{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
					{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
					{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
			writableHdfFile.putAttribute("group3DLongObjAttribute", new Long[][][]{
					{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
					{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
					{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
			writableHdfFile.putAttribute("group3DFloatAttribute", new float[][][]{
					{{-7372.3242f, -442453.213424f}, {8473.242f, -2453.213424f}},
					{{-372.42f, 53.13424f}, {-11732.3242f, -9924.4f}},
					{{7372.324f, -2453.24f}, {237372.324242f, -2453.21333424f}},
					{{5558473.242f, 82453.213424f}, {-288473.242f, -2453.213435324f}}});
			writableHdfFile.putAttribute("group3DFloatObjAttribute", new Float[][][]{
					{{-7372.3242f, -442453.213424f}, {8473.242f, -2453.213424f}},
					{{-372.42f, 53.13424f}, {-11732.3242f, -9924.4f}},
					{{7372.324f, -2453.24f}, {237372.324242f, -2453.21333424f}},
					{{5558473.242f, 82453.213424f}, {-288473.242f, -2453.213435324f}}});
			writableHdfFile.putAttribute("group3DDoubleAttribute", new double[][][]{
					{{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
					{{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
					{{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
					{{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});
			writableHdfFile.putAttribute("group3DDoubleObjAttribute", new Double[][][]{
					{{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
					{{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
					{{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
					{{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});


			WritiableDataset intDataset = writableHdfFile.putDataset("intData", new int[]{1, 2, 3});

			intDataset.putAttribute("dataset3DByteAttribute", new byte[][][]{
					{{10, 20, 30}, {10, 20, 30}, {10, 20, 30}},
					{{100, -123, 33}, {35, 11, -35}, {-43, 22, 99}}});
			intDataset.putAttribute("dataset2DByteObjAttribute", new Byte[][]{
					{1, 2, 3, 4, 5},
					{6, 7, 8, 9, 10}});
			intDataset.putAttribute("dataset3DShortAttribute", new short[][][]{
					{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
					{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
			intDataset.putAttribute("dataset3DShortObjAttribute", new Short[][][]{
					{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
					{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
			intDataset.putAttribute("dataset3DIntAttribute", new int[][][]{
					{{-32455, 121, 244}, {-32455, 121, 244}},
					{{452345, -91221, 42244}, {-355, 121321, 244099}},
					{{5341, -43121, -210044}, {-200455, 121112, 224244}}});
			intDataset.putAttribute("dataset3DIntObjAttribute", new Integer[][][]{
					{{-32455, 121, 244}, {-32455, 121, 244}},
					{{452345, -91221, 42244}, {-355, 121321, 244099}},
					{{5341, -43121, -210044}, {-200455, 121112, 224244}}});
			intDataset.putAttribute("dataset3DLongAttribute", new long[][][]{
					{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
					{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
					{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
			intDataset.putAttribute("dataset3DLongObjAttribute", new Long[][][]{
					{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
					{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
					{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
			intDataset.putAttribute("dataset3DFloatAttribute", new float[][][]{
					{{-7372.3242f, -442453.213424f}, {8473.242f, -2453.213424f}},
					{{-372.42f, 53.13424f}, {-11732.3242f, -9924.4f}},
					{{7372.324f, -2453.24f}, {237372.324242f, -2453.21333424f}},
					{{5558473.242f, 82453.213424f}, {-288473.242f, -2453.213435324f}}});
			intDataset.putAttribute("dataset3DFloatObjAttribute", new Float[][][]{
					{{-7372.3242f, -442453.213424f}, {8473.242f, -2453.213424f}},
					{{-372.42f, 53.13424f}, {-11732.3242f, -9924.224f}},
					{{7372.324f, -2453.24f}, {237372.324242f, -2453.21333424f}},
					{{5558473.242f, 82453.213424f}, {-288473.242f, -2453.213435324f}}});
			intDataset.putAttribute("dataset3DDoubleAttribute", new double[][][]{
					{{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
					{{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
					{{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
					{{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});
			intDataset.putAttribute("dataset3DDoubleObjAttribute", new Double[][][]{
					{{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
					{{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
					{{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
					{{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});

			// Actually flush and write everything
			writableHdfFile.close();

			// Now read it back
			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				Map<String, Attribute> attributes = hdfFile.getAttributes();
				assertThat(attributes).hasSize(12);

				// Just check thw whole file is readable
				TestAllFilesBase.verifyAttributes(hdfFile);
				TestAllFilesBase.recurseGroup(hdfFile);

				TestUtils.compareGroups(writableHdfFile, hdfFile);
			}
		}

		@Test
		@Order(2)
		@EnabledIfH5DumpAvailable
		void readNDAttributesWithH5Dump() throws Exception {
			// Read with h5dump
			HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

			// Read with jhdf
			try (HdfFile hdfFile = new HdfFile(tempFile)) {
				// Compare
				H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
			}
		}
	}
}
