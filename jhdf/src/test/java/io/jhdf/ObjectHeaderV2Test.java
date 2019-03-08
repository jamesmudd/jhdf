package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.ObjectHeader.ObjectHeaderV2;

public class ObjectHeaderV2Test {

	/** This will need to be updated each time the test files are regenerated */
	private static final long TIMESTAMP = 1548609278L;

	private HdfFileChannel hdfFc;

	@BeforeEach
	public void setUp() throws IOException, URISyntaxException {
		final URI testFileUri = this.getClass().getResource("test_file2.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfFc = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	public void after() {
		hdfFc.close();
	}

	@Test
	public void testRootGroupObjectHeaderV2() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfFc, 48); // Root group header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(48L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testDatasetsGroupObjectHeaderV2() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfFc, 195); // Root group header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(195L)));
		assertThat(oh.getMessages().size(), is(equalTo(9)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnFloat16Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfFc, 608); // float16 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(608L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnFloat32Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfFc, 892); // float32 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(892L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnFloat64Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfFc, 1176); // float64 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(1176L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

	@Test
	public void testObjectHeaderOnInt8Dataset() {
		ObjectHeader oh = ObjectHeader.readObjectHeader(hdfFc, 1655); // int8 header

		assertThat(oh.getVersion(), is(equalTo(2)));
		assertThat(oh.getAddress(), is(equalTo(1655L)));
		assertThat(oh.getMessages().size(), is(equalTo(5)));

		// V2 specific methods
		ObjectHeaderV2 ohV2 = (ObjectHeaderV2) oh;
		assertThat(ohV2.getAccessTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getBirthTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getChangeTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getModificationTime(), is(equalTo(TIMESTAMP)));
		assertThat(ohV2.getMaximumNumberOfCompactAttributes(), is(equalTo(-1)));
		assertThat(ohV2.getMaximumNumberOfDenseAttributes(), is(equalTo(-1)));
	}

}
