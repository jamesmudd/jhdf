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
import io.jhdf.api.Dataset;
import io.jhdf.api.Node;
import io.jhdf.api.WritiableDataset;
import io.jhdf.examples.TestAllFilesBase;
import io.jhdf.h5dump.EnabledIfH5DumpAvailable;
import io.jhdf.h5dump.H5Dump;
import io.jhdf.h5dump.HDF5FileXml;
import org.apache.commons.lang3.RandomStringUtils;
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

	private String prose = "Lorem ipsum odor amet, consectetuer adipiscing elit. Cras auctor facilisi bibendum nibh " +
		"felis convallis ridiculus faucibus. Tempor magnis cursus vestibulum ligula turpis; nec quisque. Adipiscing " +
		"nam gravida quis vulputate vivamus. Nullam dui curabitur eget dui vulputate eget penatibus posuere. Hac felis" +
		" ipsum nam scelerisque etiam porttitor montes? Sociosqu lacinia habitant odio dolor maecenas leo odio. Est in" +
		" hac; nullam euismod gravida donec. Integer sit erat torquent turpis lobortis sociosqu duis magna. Primis " +
		"nunc varius, accumsan maximus massa arcu egestas tortor. Cursus netus lorem finibus ullamcorper velit semper " +
		"cursus. Viverra proin maximus ex scelerisque tellus augue. Suscipit tortor netus laoreet lectus scelerisque " +
		"nec. Aliquet arcu et euismod vehicula varius consequat pharetra posuere praesent. Aliquet non neque finibus " +
		"rutrum turpis suspendisse libero cursus. Montes integer et urna tortor, mus ligula facilisi nam. Luctus " +
		"nascetur sem finibus phasellus nulla lacus praesent montes facilisis. Senectus platea id ante hac mus " +
		"eleifend. Nostra sodales aenean; nascetur id ultricies habitant. Malesuada scelerisque nisl vitae rhoncus, a " +
		"quisque. Curabitur lacinia consectetur tempor habitant tincidunt mauris. Dui fames nunc taciti proin " +
		"suspendisse pulvinar enim ipsum. Venenatis urna justo sagittis sit massa ipsum. Et tristique fringilla ut; " +
		"habitasse litora elementum. Inceptos cras mus rhoncus commodo magnis cubilia. Felis potenti nec purus ad nam." +
		" Sociosqu rutrum ac condimentum eros at, rhoncus posuere purus. Metus eleifend purus sapien elit mollis " +
		"molestie aliquet mollis urna. Eros senectus lacus porta dictumst elit pulvinar. Aphasellus nam natoque ornare" +
		" elementum viverra nullam lacus mi. Auctor a varius nascetur; fusce porttitor rutrum venenatis. Sociosqu " +
		"cubilia duis efficitur urna eu ridiculus sociosqu at. Sed primis consequat duis leo convallis in arcu ante. " +
		"Ex diam suspendisse pellentesque et habitasse suscipit suscipit. Euismod feugiat ullamcorper hac dui sociosqu" +
		". Erat lacus justo condimentum justo; ultrices in quisque blandit. Ante mattis maecenas montes adipiscing " +
		"class ipsum blandit. Non sapien maximus sed vehicula natoque dignissim posuere? Eu enim nascetur diam mauris " +
		"eget finibus nisi pellentesque. Fusce eget elementum aliquet volutpat lorem ullamcorper facilisi quisque. " +
		"Facilisis eros iaculis, porta faucibus potenti commodo finibus. Praesent interdum himenaeos eleifend, tempor " +
		"ipsum parturient. Parturient habitant arcu vestibulum suspendisse, leo tellus hendrerit primis. Vulputate " +
		"conubia proin porttitor condimentum sagittis cubilia velit litora. Sagittis torquent efficitur ultricies " +
		"mattis feugiat at. Arcu a sed sagittis ornare id lacinia tellus class. Pretium rutrum ullamcorper, montes " +
		"eros laoreet curae. Proin turpis et ultrices; viverra platea ultricies hendrerit. Fusce pretium aliquet eros;" +
		" elit maecenas netus primis imperdiet nullam. Nisi magna convallis luctus habitasse congue venenatis? " +
		"Sagittis pellentesque fermentum purus mattis himenaeos per himenaeos. Faucibus dis purus nascetur sociosqu " +
		"vitae maximus sapien. Eget vehicula eget mattis a nostra montes. Suspendisse blandit congue enim posuere " +
		"porta lectus nullam habitasse. Parturient nulla turpis pharetra integer imperdiet. Egestas dapibus senectus " +
		"eget, dictumst justo quis faucibus. Ipsum condimentum nullam curae nunc fusce vel ac dis. Montes semper " +
		"maximus vel facilisis natoque auctor elementum mollis. Ullamcorper commodo rutrum parturient tincidunt " +
		"bibendum; class at risus torquent!";

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

		WritiableDataset proseDataset = writableHdfFile.putDataset("prose", StringUtils.split(prose));
		proseDataset.putAttribute("prose_attr", StringUtils.split(prose));

		// Actually flush and write everything
		writableHdfFile.close();

		// Now read it back
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			Map<String, Node> datasets = hdfFile.getChildren();
			assertThat(datasets).hasSize(4);

			String[] proseReadBackArray = (String[]) hdfFile.getDatasetByPath("prose").getData();
			String proseReadback = StringUtils.joinWith(" ", (Object[]) proseReadBackArray);
			assertThat(proseReadback).isEqualTo(prose);

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

	@Test
	@Order(3)
	// https://github.com/jamesmudd/jhdf/issues/641
	void writeVarStringAttributes() throws Exception {
		Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		// Write a dataset with string attributes
		WritiableDataset writiableDataset = writableHdfFile.putDataset("dataset", new String[] {"vv", "xx", "abcdef"});
		writiableDataset.putAttribute("labels", new String[] {"vv", "xx", "abcdef"});
		writiableDataset.putAttribute("units", new String[] {"", "1", "mm2"});
		writableHdfFile.close();

		// Now read it back
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			Dataset dataset = hdfFile.getDatasetByPath("dataset");
			assertThat(dataset.getData()).isEqualTo(new String[] {"vv", "xx", "abcdef"});

			// Expected :["vv", "xx", "abcdef"]
			// Actual   :["vv", "cdedf", ""]
			assertThat(dataset.getAttribute("labels").getData()).isEqualTo(new String[] {"vv", "xx", "abcdef"});

			// Expected :["", "1", "mm2"]
			// Actual   :["", "m2", ""]
			assertThat(dataset.getAttribute("units").getData()).isEqualTo(new String[] {"", "1", "mm2"});
		} finally {
			tempFile.toFile().delete();
		}
	}

	@Test()
	@Order(4)
	void writeReallyLongStrings() throws Exception {
		Path tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".hdf5");
		WritableHdfFile writableHdfFile = HdfFile.write(tempFile);

		// Write a dataset with string attributes
		String[] randomLongStringData = {
			RandomStringUtils.insecure().nextAlphanumeric(234, 456),
			RandomStringUtils.insecure().nextAlphanumeric(234, 456),
			RandomStringUtils.insecure().nextAlphanumeric(234, 456),
			RandomStringUtils.insecure().nextAlphanumeric(234, 456),
			RandomStringUtils.insecure().nextAlphanumeric(234, 456),
		};
		WritiableDataset writiableDataset = writableHdfFile.putDataset("dataset", randomLongStringData);
		writiableDataset.putAttribute("attr", randomLongStringData);
		writableHdfFile.close();

		// Now read it back
		try (HdfFile hdfFile = new HdfFile(tempFile)) {
			Dataset dataset = hdfFile.getDatasetByPath("dataset");
			assertThat(dataset.getData()).isEqualTo(randomLongStringData);
			assertThat(dataset.getAttribute("attr").getData()).isEqualTo(randomLongStringData);
		} finally {
			tempFile.toFile().delete();
		}
	}
}
