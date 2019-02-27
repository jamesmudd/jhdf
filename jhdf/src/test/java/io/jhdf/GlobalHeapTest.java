package io.jhdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.jhdf.exceptions.HdfException;

public class GlobalHeapTest {

	private GlobalHeap globalHeap;
	private FileChannel fc;
	private Superblock sb;

	@BeforeEach
	void setup() throws FileNotFoundException {
		String testFile = this.getClass().getResource("test_file.hdf5").getFile();
		RandomAccessFile raf = new RandomAccessFile(new File(testFile), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);

		globalHeap = new GlobalHeap(fc, sb, 2048);
	}

	@Test
	void testGettingObject() {
		ByteBuffer bb = globalHeap.getObjectData(1);

		assertThat(bb.limit(), is(equalTo(19)));
		//  Decode the data
		String data = StandardCharsets.US_ASCII.decode(bb).toString();
		assertThat(data, is(equalTo("my string attribute")));

	}

	@Test
	void testInvalidSignatureThrows() throws Exception {
		// Give address of local heap
		assertThrows(HdfException.class, () -> new GlobalHeap(fc, sb, 1384));
	}

	@Test
	void testGettingObjectOutOfRangeThrows() {
		assertThrows(IllegalArgumentException.class, () -> globalHeap.getObjectData(5));
	}

	@Test
	void testGettingReferencesToObject() {
		assertThat(globalHeap.getObjectReferenceCount(1), is(equalTo(0)));
	}

	@Test
	void testGettingReferencesToObjectOutOfRangeThrows() {
		assertThrows(IllegalArgumentException.class, () -> globalHeap.getObjectReferenceCount(5));
	}

	@Test
	void testToString() {
		assertThat(globalHeap.toString(), is(equalTo("GlobalHeap [address=2048, objects=1]")));
	}

	@Test
	void testInvalidVersionThrows() throws IOException {
		FileChannel mockFc = Mockito.mock(FileChannel.class);
		Mockito.doAnswer(invocation -> {
			ByteBuffer bb = (ByteBuffer) invocation.getArguments()[0];
			bb.rewind();
			bb.put("GCOL".getBytes()); // Match signature
			bb.put((byte) 4); // mismatch version
			return null;
		}).when(mockFc).read(Mockito.any(ByteBuffer.class), Mockito.anyLong());

		assertThrows(HdfException.class, () -> new GlobalHeap(mockFc, sb, 0));
	}
}
