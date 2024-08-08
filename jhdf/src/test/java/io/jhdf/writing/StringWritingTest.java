/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.writing;

import io.jhdf.HdfFile;
import io.jhdf.TestUtils;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Node;
import io.jhdf.api.WritiableDataset;
import io.jhdf.examples.TestAllFilesBase;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringWritingTest {

	private Path tempFile;

	private String prose = "Lorem ipsum odor amet, consectetuer adipiscing elit. Phasellus tempus turpis; proin sed est ornare odio. Tempor suspendisse dapibus quis pharetra adipiscing turpis; urna hendrerit. Nam lobortis inceptos ad ante dignissim sociosqu nec consectetur platea. Magnis per velit; posuere nunc id placerat. Dis curabitur sagittis penatibus inceptos molestie massa odio vehicula. Himenaeos inceptos egestas et platea ut condimentum. Senectus facilisi fusce semper elit commodo. Tellus primis ultrices sed risus quis.";

	@Test
	@Order(1)
	void writeStrings() throws Exception {
		tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		WritiableDataset scalarStringDataset = writableHdfFile.putDataset("scalarString", "scalarString");
		scalarStringDataset.putAttribute("scalarStringAttribute", "scalarString");

		WritiableDataset oneDStringDataset = writableHdfFile.putDataset("1DString", new String[]
			{"element 1", "element 2", "element 3", "element 4", "element 5"});
		oneDStringDataset.putAttribute("1DStringAttr", new String[]
			{"element 1", "element 2", "element 3", "element 4", "element 5"});

		WritiableDataset twoDStringDataset = writableHdfFile.putDataset("2DString", new String[][]{
			{"element 1,1", "element 1,2", "element 1,3", "element 1,4", "element 1,5"},
			{"element 2,1", "element 2,2", "element 2,3", "element 2,4", "element 2,5"}
		});
		twoDStringDataset.putAttribute("2DStringAttr", new String[][]{
			{"element 1,1", "element 1,2", "element 1,3", "element 1,4", "element 1,5"},
			{"element 2,1", "element 2,2", "element 2,3", "element 2,4", "element 2,5"}
		});

		writableHdfFile.putDataset("prose", StringUtils.split(prose));

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Node> datasets = hdfFile.getChildren();
			assertThat(datasets).hasSize(4);
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
