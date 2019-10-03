/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.links;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.jhdf.HdfFile;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfBrokenLinkException;
import io.jhdf.exceptions.HdfException;

class LinksTest {

	final String testFileUrl = this.getClass().getResource("../test_file.hdf5").getFile();

	@Test
	void testSoftLink() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Node softLinkNode = hdfFile.getByPath("/links_group/soft_link_to_int8");
			Link softLink = (Link) softLinkNode;
			assertThat(softLink.isLink(), is(true));
			assertThat(softLink.isBrokenLink(), is(false));
			assertThat(softLink.isGroup(), is(false));
			assertThat(softLink.getPath(), is(equalTo("/links_group/soft_link_to_int8")));
			assertThat(softLink.getTargetPath(), is(equalTo("/datasets_group/int/int8")));
			assertThat(softLink.getTarget(), is(notNullValue()));
			assertThat(softLink.getHdfFile(), is(sameInstance(hdfFile)));
			assertThat(softLink.getFile(), is(sameInstance(file)));
			assertThat(softLink.getType(), is(equalTo(NodeType.DATASET)));
			assertThat(softLink.toString(),
					is(equalTo("SoftLink [name=soft_link_to_int8, target=/datasets_group/int/int8]")));
			assertThat(softLink.getAddress(), is(equalTo(10904L)));
			assertThat(softLink.getName(), is(equalTo("soft_link_to_int8")));
			assertThat(softLink.getParent(), is(sameInstance(hdfFile.getByPath("/links_group"))));
			assertThat(softLink.getAttributes(), isA(Map.class));
			assertThat(softLink.getAttribute("missing_attribute"), is(nullValue()));
			assertThat(softLink.isAttributeCreationOrderTracked(), is(false));
		}
	}

	@Test
	void testSoftLinkWithInvalidPath() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Link softLink = new SoftLink("/non/existent/path", "broken_link", hdfFile);
			assertThat(softLink.isBrokenLink(), is(true));
			HdfException e = assertThrows(HdfBrokenLinkException.class, softLink::getTarget);
			assertThat(e.getMessage(),
					is(equalTo("Could not resolve link target '/non/existent/path' from link '/broken_link'")));
		}
	}

	@Test
	void testExternalLink() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Node softLinkNode = hdfFile.getByPath("/links_group/external_link");
			Link externalLink = (Link) softLinkNode;
			assertThat(externalLink.isLink(), is(true));
			assertThat(externalLink.isGroup(), is(false));
			assertThat(externalLink.getPath(), is(equalTo("/links_group/external_link")));
			assertThat(externalLink.getTargetPath(), is(equalTo("test_file_ext.hdf5:/external_dataset")));
			assertThat(externalLink.getTarget(), is(notNullValue()));
			assertThat(externalLink.getHdfFile(), is(sameInstance(hdfFile)));
			assertThat(externalLink.getFile(), is(sameInstance(file)));
			assertThat(externalLink.getType(), is(equalTo(NodeType.DATASET)));
			assertThat(externalLink.toString(), is(equalTo(
					"ExternalLink [name=external_link, targetFile=test_file_ext.hdf5, targetPath=/external_dataset]")));
			assertThat(externalLink.getAddress(), is(equalTo(195L)));
			assertThat(externalLink.getName(), is(equalTo("external_link")));
			assertThat(externalLink.getParent(), is(sameInstance(hdfFile.getByPath("/links_group"))));
			assertThat(externalLink.getAttributes(), isA(Map.class));
			assertThat(externalLink.getAttribute("missing_attribute"), is(nullValue()));
		}
	}

	@Test
	void testExternalLinkWithInvalidPath() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Link externalLink = new ExternalLink("test_file_ext.hdf5", "/non/existent/path", "broken_link", hdfFile);
			assertThat(externalLink.isBrokenLink(), is(true));
			HdfException e = assertThrows(HdfBrokenLinkException.class, externalLink::getTarget);
			assertThat(e.getMessage(), is(equalTo(
					"Could not resolve link target '/non/existent/path' in external file 'test_file_ext.hdf5' from link '/broken_link'")));
		}
	}

	@Test
	void testExternalLinkWithInvalidFile() {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Link externalLink = new ExternalLink("/missing_file.hdf5", "/non/existent/path", "broken_link", hdfFile);
			assertThat(externalLink.isBrokenLink(), is(true));
			HdfException e = assertThrows(HdfBrokenLinkException.class, externalLink::getTarget);
			assertThat(e.getMessage(), is(equalTo(
					"Could not resolve link target '/non/existent/path' in external file '/missing_file.hdf5' from link '/broken_link'")));
		}
	}

}
