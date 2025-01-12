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
import io.jhdf.api.Node;
import io.jhdf.api.WritiableDataset;
import io.jhdf.examples.TestAllFilesBase;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
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
class BooleanWritingTest {

	private Path tempFile;

	@Test
	@Order(1)
	void writeBooleans() throws Exception {
		tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		WritiableDataset scalarTrueDataset = writableHdfFile.putDataset("scalarTrueBoolean", true);
		WritiableDataset scalarFalseDataset = writableHdfFile.putDataset("scalarFalseBoolean", false);
		scalarTrueDataset.putAttribute("scalarTrueAttribute", true);
		scalarFalseDataset.putAttribute("scalarFalseAttribute", false);

		WritiableDataset oneDBoolean = writableHdfFile.putDataset("1DBoolean", new boolean[]
			{true, false, true, false, true, false});
		oneDBoolean.putAttribute("1DBooleanAttr", new boolean[]
			{true, false, true, false, true, false});

		WritiableDataset oneDObjBoolean = writableHdfFile.putDataset("1DObjBoolean", new Boolean[]
			{true, false, true, false, true, false});
		oneDObjBoolean.putAttribute("1DObjBooleanAttr", new Boolean[]
			{true, false, true, false, true, false});

		WritiableDataset twoDBooleanDataset = writableHdfFile.putDataset("2DBoolean", new boolean[][]{
			{true, false, true, false, true, false},
			{false, true, false, true, false, true}});

		twoDBooleanDataset.putAttribute("2DBooleanAttr", new boolean[][]{
			{true, false, true, false, true, false},
			{false, true, false, true, false, true}});

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Node> datasets = hdfFile.getChildren();
			assertThat(datasets).hasSize(5);

			// Just check thw whole file is readable
			TestAllFilesBase.verifyAttributes(hdfFile);
			TestAllFilesBase.recurseGroup(hdfFile);

			TestUtils.compareGroups(writableHdfFile, hdfFile);
		}
	}

	@Test
	@Order(2)
	@EnabledIfH5DumpAvailable
	void readBooleanDatasetsWithH5Dump() throws Exception {
		// Read with h5dump
		HDF5FileXml hdf5FileXml = H5Dump.dumpAndParse(tempFile);

		// Read with jhdf
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			// Compare
			H5Dump.assetXmlAndHdfFileMatch(hdf5FileXml, hdfFile);
		}
	}
}
