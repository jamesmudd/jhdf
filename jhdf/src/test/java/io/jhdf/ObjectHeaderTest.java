package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.jhdf.ObjectHeader.ObjectHeaderV1;

public class ObjectHeaderTest {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@BeforeEach
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
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
	public void testObjectHeaderOnGroup() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 800); // dataset_group header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(800L)));
		assertThat(oh.getMessages().size(), is(equalTo(7)));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		assertThat(ohV1.getReferenceCount(), is(equalTo(1)));
	}

	@Test
	public void testObjectHeaderOnFloat16Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 7272); // float16 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(7272L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		assertThat(ohV1.getReferenceCount(), is(equalTo(1)));
	}

	@Test
	public void testObjectHeaderOnFloat32Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 7872); // float32 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(7872L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		assertThat(ohV1.getReferenceCount(), is(equalTo(1)));
	}

	@Test
	public void testObjectHeaderOnFloat64Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 10240); // float64 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(10240L)));
		assertThat(oh.getMessages().size(), is(equalTo(7)));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		assertThat(ohV1.getReferenceCount(), is(equalTo(1)));
	}

	@Test
	public void testObjectHeaderOnInt8Dataset() throws IOException {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, 11176); // int8 header

		assertThat(oh.getVersion(), is(equalTo(1)));
		assertThat(oh.getAddress(), is(equalTo(11176L)));
		assertThat(oh.getMessages().size(), is(equalTo(6)));

		// V1 specific methods
		ObjectHeaderV1 ohV1 = (ObjectHeaderV1) oh;
		// Two refs this one and a hard link /links/hard_link_to_int8
		assertThat(ohV1.getReferenceCount(), is(equalTo(2)));
	}

	@Test
	public void testLazyObjectHeader() throws Exception {
		FileChannel spyFc = Mockito.spy(fc);
		LazyInitializer<ObjectHeader> lazyObjectHeader = ObjectHeader.lazyReadObjectHeader(spyFc, sb, 11176); // int8
																												// header
		// Creating the lazy object header should not touch the file
		Mockito.verifyZeroInteractions(spyFc);

		// Get the actual header should cause the file to be read
		lazyObjectHeader.get();

		// Check the file was read
		verify(spyFc, Mockito.atLeastOnce()).read(any(ByteBuffer.class), anyLong());

		// Ensure nothing else was done to the file
		Mockito.verifyNoMoreInteractions(spyFc);
	}

}
