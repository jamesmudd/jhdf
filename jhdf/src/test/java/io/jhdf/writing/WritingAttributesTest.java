package io.jhdf.writing;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritiableDataset;
import io.jhdf.examples.TestAllFilesBase;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WritingAttributesTest {

	private static Path tempFile;

	@BeforeAll
	static void beforeAll() throws IOException {
		tempFile = Files.createTempFile(null, ".hdf5");
	}
	@Test
	@Order(5)
	void writeAttributes() throws Exception {
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

		// TODO floating point attributes

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
		}
	}

	@Test
	@Order(6)
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

	@Test
	@Order(5)
	void writeScalarAttributes() {
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		// Scalar
		writableHdfFile.putAttribute("groupScalarByteAttribute", 10);
		writableHdfFile.putAttribute("groupScalarByteObjAttribute", new Byte("20"));
		writableHdfFile.putAttribute("groupScalarShortAttribute", 133);
		writableHdfFile.putAttribute("groupScalarShortObjAttribute", new Short("123"));
		writableHdfFile.putAttribute("groupScalarIntAttribute", -32455);
		writableHdfFile.putAttribute("groupScalarIntObjAttribute", new Integer(9455));
		writableHdfFile.putAttribute("groupScalarLongAttribute", 83772644);
		writableHdfFile.putAttribute("groupScalarLongObjAttribute", new Long(837726444324L));
		writableHdfFile.putAttribute("groupScalarFloatAttribute", -2847372.324242);
		writableHdfFile.putAttribute("groupScalarFloatObjAttribute", new Float(372.324242));
		writableHdfFile.putAttribute("groupScalarDoubleAttribute", 21342.324);
		writableHdfFile.putAttribute("groupScalarDoubleObjAttribute", new Double(3421342.324113));

		WritiableDataset intDataset = writableHdfFile.putDataset("intData",  new int[] {1,2,3});

		intDataset.putAttribute("datasetByteAttribute", 10);
		intDataset.putAttribute("datasetByteObjAttribute", new Byte("20"));
		intDataset.putAttribute("datasetShortAttribute", 133);
		intDataset.putAttribute("datasetShortObjAttribute", new Short("123"));
		intDataset.putAttribute("datasetIntAttribute", -32455);
		intDataset.putAttribute("datasetIntObjAttribute", new Integer(9455));
		intDataset.putAttribute("datasetLongAttribute", 83772644);
		intDataset.putAttribute("datasetLongObjAttribute", new Long(837726444324L));
		intDataset.putAttribute("datasetFloatAttribute", -2847372.324242);
		intDataset.putAttribute("datasetFloatObjAttribute", new Float(372.324242));
		intDataset.putAttribute("datasetDoubleAttribute", 21342.324);
		intDataset.putAttribute("datasetDoubleObjAttribute", new Double(3421342.324113));

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Attribute> attributes = hdfFile.getAttributes();
			assertThat(attributes).hasSize(12);
//			Attribute attribute = attributes.get("rootAttribute");
//			assertThat(attribute.getData()).isEqualTo(new int[] {1,2,3});
//			assertThat(attribute.getDimensions()).isEqualTo(new int[] {3});
//			assertThat(attribute.getJavaType()).isEqualTo(int.class);

			// Just check thw whole file is readable
			TestAllFilesBase.verifyAttributes(hdfFile);
			TestAllFilesBase.recurseGroup(hdfFile);
		}
	}

	@Test
	@Order(6)
	@EnabledIfH5DumpAvailable
	void readScalarAttributesWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}

	@Test
	@Order(7)
	void write1DAttributes() {
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		// 1D
		writableHdfFile.putAttribute("group1DByteAttribute", new byte[]{10,20,30});
		writableHdfFile.putAttribute("group1DByteObjAttribute", new Byte[]{10, 20, 30});
		writableHdfFile.putAttribute("group1DShortAttribute", new short[]{133, 222, 444});
		writableHdfFile.putAttribute("group1DShortObjAttribute", new Short[]{133, 222, 444});
		writableHdfFile.putAttribute("group1DIntAttribute", new int[]{-32455,121,244});
		writableHdfFile.putAttribute("group1DIntObjAttribute", new Integer[]{-32455,121,244});
		writableHdfFile.putAttribute("group1DLongAttribute", new long[]{83772644,234242});
		writableHdfFile.putAttribute("group1DLongObjAttribute", new Long[]{837726444324L, 843589389219L});
		writableHdfFile.putAttribute("group1DFloatAttribute", new float[]{-2847372.324242f, -2453.213424f});
		writableHdfFile.putAttribute("group1DFloatObjAttribute", new Float[]{372.324242f, -2434.32324f});
		writableHdfFile.putAttribute("group1DDoubleAttribute", new double[]{21342.324, -232342.434});
		writableHdfFile.putAttribute("group1DDoubleObjAttribute", new Double[]{3421342.324113, 54366.324223});

		WritiableDataset intDataset = writableHdfFile.putDataset("intData",  new int[] {1,2,3});

		intDataset.putAttribute("dataset1DByteAttribute", new byte[]{10,20,30});
		intDataset.putAttribute("dataset1DByteObjAttribute", new Byte[]{10, 20, 30});
		intDataset.putAttribute("dataset1DShortAttribute", new short[]{133, 222, 444});
		intDataset.putAttribute("dataset1DShortObjAttribute", new Short[]{133, 222, 444});
		intDataset.putAttribute("dataset1DIntAttribute", new int[]{-32455,121,244});
		intDataset.putAttribute("dataset1DIntObjAttribute", new Integer[]{-32455,121,244});
		intDataset.putAttribute("dataset1DLongAttribute", new long[]{83772644,234242});
		intDataset.putAttribute("dataset1DLongObjAttribute", new Long[]{837726444324L, 843589389219L});
		intDataset.putAttribute("dataset1DFloatAttribute", new float[]{-2847372.324242f, -2453.213424f});
		intDataset.putAttribute("dataset1DFloatObjAttribute", new Float[]{372.324242f, -2434.32324f});
		intDataset.putAttribute("dataset1DDoubleAttribute", new double[]{21342.324, -232342.434});
		intDataset.putAttribute("dataset1DDoubleObjAttribute", new Double[]{3421342.324113, 54366.324223});

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Attribute> attributes = hdfFile.getAttributes();
			assertThat(attributes).hasSize(12);
//			Attribute attribute = attributes.get("rootAttribute");
//			assertThat(attribute.getData()).isEqualTo(new int[] {1,2,3});
//			assertThat(attribute.getDimensions()).isEqualTo(new int[] {3});
//			assertThat(attribute.getJavaType()).isEqualTo(int.class);

			// Just check thw whole file is readable
			TestAllFilesBase.verifyAttributes(hdfFile);
			TestAllFilesBase.recurseGroup(hdfFile);
		}
	}

	@Test
	@Order(8)
	@EnabledIfH5DumpAvailable
	void read1DAttributesWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}

	@Test
	@Order(9)
	void writeNDAttributes() {
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		// nD
		writableHdfFile.putAttribute("group3DByteAttribute", new byte[][][]{
				{{10,20,30},{10,20,30},{10,20,30}},
				{{100,-123,33},{35,11,-35},{-43,22,99}}});
		writableHdfFile.putAttribute("group2DByteObjAttribute", new Byte[][]{
				{1,2,3,4,5},
				{6,7,8,9,10}});
		writableHdfFile.putAttribute("group3DShortAttribute", new short[][][]{
				{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
				{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
		writableHdfFile.putAttribute("group3DShortObjAttribute", new Short[][][]{
				{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
				{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
		writableHdfFile.putAttribute("group3DIntAttribute", new int[][][]{
				{{-32455,121,244},{-32455,121,244}},
				{{452345,-91221,42244},{-355,121321,244099}},
				{{5341,-43121,-210044},{-200455,121112,224244}}});
		writableHdfFile.putAttribute("group3DIntObjAttribute", new Integer[][][]{
				{{-32455,121,244},{-32455,121,244}},
				{{452345,-91221,42244},{-355,121321,244099}},
				{{5341,-43121,-210044},{-200455,121112,224244}}});
		writableHdfFile.putAttribute("group3DLongAttribute", new long[][][]{
				{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
				{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
				{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
		writableHdfFile.putAttribute("group3DLongObjAttribute", new Long[][][]{
				{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
				{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
				{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
		writableHdfFile.putAttribute("group3DFloatAttribute", new float[][][]{
				{{-2847372.324242f, -442453.213424f}, {847372.324242f, -2453.213424f}},
				{{-372.42f, 53.13424f}, {-117372.324242f, -992453.213424f}},
				{{7372.324242f, -2453.213424f}, {237372.324242f, -2453.21333424f}},
				{{555847372.324242f, 82453.213424f}, {-28847372.324242f, -2453.213435324f}}});
		writableHdfFile.putAttribute("group3DFloatObjAttribute", new Float[][][]{
				{{-2847372.324242f, -442453.213424f}, {847372.324242f, -2453.213424f}},
				{{-372.42f, 53.13424f}, {-117372.324242f, -992453.213424f}},
				{{7372.324242f, -2453.213424f}, {237372.324242f, -2453.21333424f}},
				{{555847372.324242f, 82453.213424f}, {-28847372.324242f, -2453.213435324f}}});
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


		WritiableDataset intDataset = writableHdfFile.putDataset("intData",  new int[] {1,2,3});

		intDataset.putAttribute("group3DByteAttribute", new byte[][][]{
				{{10,20,30},{10,20,30},{10,20,30}},
				{{100,-123,33},{35,11,-35},{-43,22,99}}});
		intDataset.putAttribute("group2DByteObjAttribute", new Byte[][]{
				{1,2,3,4,5},
				{6,7,8,9,10}});
		intDataset.putAttribute("group3DShortAttribute", new short[][][]{
				{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
				{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
		intDataset.putAttribute("group3DShortObjAttribute", new Short[][][]{
				{{133, 222, 444}, {133, 222, 444}, {133, 222, 444}},
				{{-343, 22, -2444}, {1000, -2, 421}, {3, -52, 94}}});
		intDataset.putAttribute("group3DIntAttribute", new int[][][]{
				{{-32455,121,244},{-32455,121,244}},
				{{452345,-91221,42244},{-355,121321,244099}},
				{{5341,-43121,-210044},{-200455,121112,224244}}});
		intDataset.putAttribute("group3DIntObjAttribute", new Integer[][][]{
				{{-32455,121,244},{-32455,121,244}},
				{{452345,-91221,42244},{-355,121321,244099}},
				{{5341,-43121,-210044},{-200455,121112,224244}}});
		intDataset.putAttribute("group3DLongAttribute", new long[][][]{
				{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
				{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
				{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
		intDataset.putAttribute("group3DLongObjAttribute", new Long[][][]{
				{{837726444324L, 11843589389219L}, {1836444324L, 9843589389219L}, {11837726444324L, 5843589389219L}},
				{{26444324L, -9219L}, {-6444324L, 349843589389219L}, {726444324L, -843589389219L}},
				{{834L, -1119L}, {14324L, -989389219L}, {7726444324L, -5389211L}}});
		intDataset.putAttribute("group3DFloatAttribute", new float[][][]{
				{{-2847372.324242f, -442453.213424f}, {847372.324242f, -2453.213424f}},
				{{-372.42f, 53.13424f}, {-117372.324242f, -992453.213424f}},
				{{7372.324242f, -2453.213424f}, {237372.324242f, -2453.21333424f}},
				{{555847372.324242f, 82453.213424f}, {-28847372.324242f, -2453.213435324f}}});
		intDataset.putAttribute("group3DFloatObjAttribute", new Float[][][]{
				{{-2847372.324242f, -442453.213424f}, {847372.324242f, -2453.213424f}},
				{{-372.42f, 53.13424f}, {-117372.324242f, -992453.213424f}},
				{{7372.324242f, -2453.213424f}, {237372.324242f, -2453.21333424f}},
				{{555847372.324242f, 82453.213424f}, {-28847372.324242f, -2453.213435324f}}});
		intDataset.putAttribute("group3DDoubleAttribute", new double[][][]{
				{{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
				{{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
				{{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
				{{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});
		intDataset.putAttribute("group3DDoubleObjAttribute", new Double[][][]{
				{{-2847372.324242, -442453.213424}, {847372.324242, -2453.213424}},
				{{-372.42, 53.13424}, {-117372.324242, -992453.213424}},
				{{7372.324242, -2453.213424}, {237372.324242, -2453.21333424}},
				{{555847372.324242, 82453.213424}, {-28847372.324242, -2453.213435324}}});

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Attribute> attributes = hdfFile.getAttributes();
			assertThat(attributes).hasSize(12);
//			Attribute attribute = attributes.get("rootAttribute");
//			assertThat(attribute.getData()).isEqualTo(new int[] {1,2,3});
//			assertThat(attribute.getDimensions()).isEqualTo(new int[] {3});
//			assertThat(attribute.getJavaType()).isEqualTo(int.class);

			// Just check thw whole file is readable
			TestAllFilesBase.verifyAttributes(hdfFile);
			TestAllFilesBase.recurseGroup(hdfFile);
		}
	}

	@Test
	@Order(10)
	@EnabledIfH5DumpAvailable
	void readNDAttributesWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}
}
