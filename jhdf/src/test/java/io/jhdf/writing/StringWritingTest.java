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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StringWritingTest {

	private Path tempFile;

	@Test
	@Order(1)
	void writeStrings() throws Exception {
		tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		writableHdfFile.putDataset("scalarString", "scalarString");
		writableHdfFile.putDataset("1DString", new String[]
			{"element 1", "element 2", "element 3", "element 4", "element 5"});
		writableHdfFile.putDataset("2DString", new String[][]{
			{"element 1,1", "element 1,2", "element 1,3", "element 1,4", "element 1,5"},
			{"element 2,1", "element 2,2", "element 2,3", "element 2,4", "element 2,5"}
		});


		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Node> datasets = hdfFile.getChildren();
			assertThat(datasets).hasSize(3);
			// Verify scalar
//			for (Node node : datasets.values()) {
//				Dataset dataset = (Dataset) node;
//				assertThat(dataset.isScalar());
//				assertThat(dataset.getDimensions()).isEqualTo(ArrayUtils.EMPTY_INT_ARRAY);
//			}

			// Just check thw whole file is readable
			TestAllFilesBase.verifyAttributes(hdfFile);
			TestAllFilesBase.recurseGroup(hdfFile);

			TestUtils.compareGroups(writableHdfFile, hdfFile);
		}
	}

	@Test
	@Order(2)
	@EnabledIfH5DumpAvailable
	void readStringDatasetsWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}
}
