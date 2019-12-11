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

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfBrokenLinkException;
import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinksTest {

    private File file;
    private HdfFile hdfFile;

    @BeforeEach
    void setUp() throws URISyntaxException {
        URI uri = this.getClass().getResource("/hdf5/test_file.hdf5").toURI();
        hdfFile = new HdfFile(uri);
        file = new File(uri);
    }

    @AfterEach
    void tearDown() {
        hdfFile.close();
    }

    @Test
    void testSoftLink() {
        Group linkGroup = (Group) hdfFile.getChild("links_group");
        Node softLinkNode = linkGroup.getChild("soft_link_to_int8");
        Link softLink = (Link) softLinkNode;
        assertThat(softLink.isLink(), is(true));
        assertThat(softLink.isBrokenLink(), is(false));
        assertThat(softLink.isGroup(), is(false));
        assertThat(softLink.getPath(), is(equalTo("/links_group/soft_link_to_int8")));
        assertThat(softLink.getTargetPath(), is(equalTo("/datasets_group/int/int8")));
        assertThat(softLink.getTarget(), is(notNullValue()));
        assertThat(softLink.getHdfFile(), is(sameInstance(hdfFile)));
        assertThat(softLink.getFile(), is(file));
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

    @Test
    void testSoftLinkWithInvalidPath() {
        Link softLink = new SoftLink("/non/existent/path", "broken_link", hdfFile);
        assertThat(softLink.isBrokenLink(), is(true));
        HdfException e = assertThrows(HdfBrokenLinkException.class, softLink::getTarget);
        assertThat(e.getMessage(),
                is(equalTo("Could not resolve link target '/non/existent/path' from link '/broken_link'")));
    }

    @Test
    void testExternalLink() {
        Group linkGroup = (Group) hdfFile.getChild("links_group");
        Link externalLink = (Link) linkGroup.getChild("external_link");
        assertThat(externalLink.isLink(), is(true));
        assertThat(externalLink.isGroup(), is(false));
        assertThat(externalLink.getPath(), is(equalTo("/links_group/external_link")));
        assertThat(externalLink.getTargetPath(), is(equalTo("test_file_ext.hdf5:/external_dataset")));
        assertThat(externalLink.getTarget(), is(notNullValue()));
        assertThat(externalLink.getHdfFile(), is(sameInstance(hdfFile)));
        assertThat(externalLink.getFile(), is(file));
        assertThat(externalLink.getType(), is(equalTo(NodeType.DATASET)));
        assertThat(externalLink.toString(), is(equalTo(
                "ExternalLink [name=external_link, targetFile=test_file_ext.hdf5, targetPath=/external_dataset]")));
        assertThat(externalLink.getAddress(), is(equalTo(195L)));
        assertThat(externalLink.getName(), is(equalTo("external_link")));
        assertThat(externalLink.getParent(), is(sameInstance(hdfFile.getByPath("/links_group"))));
        assertThat(externalLink.getAttributes(), isA(Map.class));
        assertThat(externalLink.getAttribute("missing_attribute"), is(nullValue()));

    }

    @Test
    void testExternalLinkWithInvalidPath() {
        Link externalLink = new ExternalLink("test_file_ext.hdf5", "/non/existent/path", "broken_link", hdfFile);
        assertThat(externalLink.isBrokenLink(), is(true));
        HdfException e = assertThrows(HdfBrokenLinkException.class, externalLink::getTarget);
        assertThat(e.getMessage(), is(equalTo(
                "Could not resolve link target '/non/existent/path' in external file 'test_file_ext.hdf5' from link '/broken_link'")));

    }

    @Test
    void testExternalLinkWithInvalidFile() {
        Link externalLink = new ExternalLink("/missing_file.hdf5", "/non/existent/path", "broken_link", hdfFile);
        assertThat(externalLink.isBrokenLink(), is(true));
        HdfException e = assertThrows(HdfBrokenLinkException.class, externalLink::getTarget);
        assertThat(e.getMessage(), is(equalTo(
                "Could not resolve link target '/non/existent/path' in external file '/missing_file.hdf5' from link '/broken_link'")));

    }

    @Test
    void testTraversingSoftLinkToDataset() {
    	Dataset dataset = hdfFile.getDatasetByPath("/datasets_group/int/int32");
    	Dataset datasetThroughLink = hdfFile.getDatasetByPath("/links_group/soft_link_to_group/int32");
		assertThat(datasetThroughLink.getData(), is(equalTo(dataset.getData())));
    }

	@Test
	void testTraversingHardLinkToDataset() {
		Dataset dataset = hdfFile.getDatasetByPath("/datasets_group/int/int8");
		Dataset datasetThroughLink = hdfFile.getDatasetByPath("/links_group/hard_link_to_int8");
		assertThat(datasetThroughLink.getData(), is(equalTo(dataset.getData())));
	}

	@Test
	void testTraversingExternalLinkToDataset() {
		Dataset datasetThroughLink = hdfFile.getDatasetByPath("/links_group/external_link");
		assertThat(datasetThroughLink.getData(), is(notNullValue()));
	}
}
