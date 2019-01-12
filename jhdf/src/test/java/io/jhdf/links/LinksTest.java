package io.jhdf.links;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.jhdf.HdfFile;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;

class LinksTest {

	final String testFileUrl = this.getClass().getResource("../test_file.hdf5").getFile();

	@Test
	void testSoftLink() throws IOException {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Node softLinkNode = hdfFile.getByPath("/links_group/soft_link_to_int8");
			Link softLink = (Link) softLinkNode;
			assertThat(softLink.isLink(), is(true));
			assertThat(softLink.isGroup(), is(false));
			assertThat(softLink.getPath(), is(equalTo("/links_group/soft_link_to_int8")));
			assertThat(softLink.getTargetPath(), is(equalTo("/datasets_group/int/int8")));
			assertThat(softLink.getTarget(), is(notNullValue()));
			assertThat(softLink.getHdfFile(), is(sameInstance(hdfFile)));
			assertThat(softLink.getFile(), is(sameInstance(file)));
			assertThat(softLink.getType(), is(equalTo(NodeType.DATASET)));
			assertThat(softLink.toString(),
					is(equalTo("SoftLink [name=soft_link_to_int8, target=/datasets_group/int/int8]")));
		}
	}

	@Test
	void testSoftLinkWithInvalidPath() throws IOException {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Link softLink = new SoftLink("/non/exisitant/path", "broken_link", hdfFile);
			HdfException e = assertThrows(HdfException.class, () -> softLink.getTarget());
			assertThat(e.getMessage(),
					is(equalTo("Could not resolve link target '/non/exisitant/path' from link '/broken_link'")));
		}
	}

	@Test
	void testExternalLink() throws IOException {
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
		}
	}

	@Test
	void testExternalLinkWithInvalidPath() throws IOException {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Link externalLink = new ExternalLink("test_file_ext.hdf5", "/non/exisitant/path", "broken_link", hdfFile);
			HdfException e = assertThrows(HdfException.class, () -> externalLink.getTarget());
			assertThat(e.getMessage(), is(equalTo(
					"Could not resolve link target '/non/exisitant/path' in external file 'test_file_ext.hdf5' from link '/broken_link'")));
		}
	}

	@Test
	void testExternalLinkWithInvalidFile() throws IOException {
		File file = new File(testFileUrl);
		try (HdfFile hdfFile = new HdfFile(file)) {
			Link externalLink = new ExternalLink("/missing_file.hdf5", "/non/exisitant/path", "broken_link", hdfFile);
			HdfException e = assertThrows(HdfException.class, () -> externalLink.getTarget());
			assertThat(e.getMessage(), is(equalTo(
					"Could not resolve link target '/non/exisitant/path' in external file '/missing_file.hdf5' from link '/broken_link'")));
		}
	}

}
