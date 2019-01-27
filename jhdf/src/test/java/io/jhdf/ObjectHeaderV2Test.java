package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jhdf.ObjectHeader.ObjectHeaderV2;

public class ObjectHeaderV2Test {

	/** This will need to be updated each time the test files are regenerated */
	private static final long TIMESTAMP = 1548609278L;

	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@BeforeEach
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file2.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
	}

	@AfterEach
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testRootGroupObjectHeaderV2() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 48); // Root group header

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
	public void testDatasetsGroupObjectHeaderV2() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 195); // Root group header

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
	public void testObjectHeaderOnFloat16Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 608); // float16 header

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
	public void testObjectHeaderOnFloat32Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 892); // float32 header

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
	public void testObjectHeaderOnFloat64Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 1176); // float64 header

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
	public void testObjectHeaderOnInt8Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 1655); // int8 header

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
