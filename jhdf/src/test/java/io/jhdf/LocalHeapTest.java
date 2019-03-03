package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocalHeapTest {
	private HdfFileChannel hdfFc;

	@BeforeEach
	public void setUp() throws Exception {
		final URI testFileUri = this.getClass().getResource("test_file.hdf5").toURI();
		FileChannel fc = FileChannel.open(Paths.get(testFileUri), StandardOpenOption.READ);
		Superblock sb = Superblock.readSuperblock(fc, 0);
		hdfFc = new HdfFileChannel(fc, sb);
	}

	@AfterEach
	public void after() throws IOException {
		hdfFc.close();
	}

	@Test
	public void testLocalHeap() {
		LocalHeap heap = new LocalHeap(hdfFc, 680);

		assertThat(heap.getVersion(), is(equalTo((short) 0)));
		assertThat(heap.getDataSegmentSize(), is(equalTo(88L)));
		assertThat(heap.getOffsetToHeadOfFreeList(), is(equalTo(56L)));
		assertThat(heap.getAddressOfDataSegment(), is(equalTo(712L)));
		assertThat(heap.toString(), is(equalTo(
				"LocalHeap [address=0x2a8, version=0, dataSegmentSize=88, offsetToHeadOfFreeList=56, addressOfDataSegment=0x2c8]")));
	}

	@Test
	public void testAccessingData() {
		LocalHeap heap = new LocalHeap(hdfFc, 680);
		ByteBuffer bb = heap.getDataBuffer();
		assertThat(bb.capacity(), is(equalTo(88)));
		// Test reading a name from the heap
		bb.position(8);
		assertThat(Utils.readUntilNull(bb), is("datasets_group"));
	}
}
