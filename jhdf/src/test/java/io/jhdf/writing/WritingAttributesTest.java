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
	void writeScalarAttributes() throws Exception {
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		writableHdfFile.putAttribute("groupByteAttribute", 10);
		writableHdfFile.putAttribute("groupShortAttribute", 133);
		writableHdfFile.putAttribute("groupIntAttribute", -32455);
		writableHdfFile.putAttribute("groupLongAttribute", 83772644);
		writableHdfFile.putAttribute("groupFloatAttribute", -2847372.324242);
		writableHdfFile.putAttribute("groupDoubleAttribute", 21342.324);


		WritiableDataset intDataset = writableHdfFile.putDataset("intData",  new int[] {1,2,3});

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
	void readScalarAttributesWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try(HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}
}
