/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2022 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.exceptions.HdfFilterException;
import io.jhdf.exceptions.UnsupportedHdfException;
import net.jpountz.lz4.LZ4Factory;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.xerial.snappy.BitShuffle;
import org.xerial.snappy.BitShuffleType;

import java.io.IOException;
import java.nio.ByteBuffer;

import static io.jhdf.filter.BitShuffleFilter.LZ4_COMPRESSION;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class BitShuffleFilterTest {

	@Test
	void testCompareToReference() throws IOException {
		ByteBuffer data = ByteBuffer.allocateDirect(2);
		data.put((byte) 1);
		data.put((byte) 2);
		data.flip();
		ByteBuffer shuffled = ByteBuffer.allocateDirect(2);

		BitShuffle.shuffle(data, BitShuffleType.BYTE, shuffled);

		ByteBuffer unshuffled = ByteBuffer.allocateDirect(2);
		BitShuffle.unshuffle(shuffled, BitShuffleType.BYTE	, unshuffled);

		// Check shuffle+unshuffle worked
		assertThat(unshuffled.get(), Matchers.is((byte) 1));
		assertThat(unshuffled.get(), Matchers.is((byte) 2));

		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
		byte[] shuffledBytes = new byte[2];
		shuffled.get(shuffledBytes);
		byte[] unshuffledBytes = new byte[2];
		bitShuffleFilter.unshuffle(shuffledBytes, 1, unshuffledBytes);

		assertThat(unshuffledBytes[0], Matchers.is((byte) 1));
		assertThat(unshuffledBytes[1], Matchers.is((byte) 2));
	}

	@Test
	void testCompareToReference2() throws IOException {
		ByteBuffer data = ByteBuffer.allocateDirect(2);
		data.put((byte) 7);
		data.put((byte) 45);
		data.flip();
		ByteBuffer shuffled = ByteBuffer.allocateDirect(2);

		BitShuffle.shuffle(data, BitShuffleType.BYTE, shuffled);

		ByteBuffer unshuffled = ByteBuffer.allocateDirect(2);
		BitShuffle.unshuffle(shuffled, BitShuffleType.BYTE	, unshuffled);

		// Check shuffle+unshuffle worked
		assertThat(unshuffled.get(), Matchers.is((byte) 7));
		assertThat(unshuffled.get(), Matchers.is((byte) 45));

		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
		byte[] shuffledBytes = new byte[2];
		shuffled.get(shuffledBytes);
		byte[] unshuffledBytes = new byte[2];
		bitShuffleFilter.unshuffle(shuffledBytes, 1, unshuffledBytes);

		assertThat(unshuffledBytes[0], Matchers.is((byte) 7));
		assertThat(unshuffledBytes[1], Matchers.is((byte) 45));
	}

	@Test
	void testCompareToReference3() throws IOException {
		ByteBuffer data = ByteBuffer.allocateDirect(2);
		data.put((byte) 7);
		data.put((byte) 45);
		data.flip();
		ByteBuffer shuffled = ByteBuffer.allocateDirect(2);

		BitShuffle.shuffle(data, BitShuffleType.SHORT, shuffled);

		ByteBuffer unshuffled = ByteBuffer.allocateDirect(2);
		BitShuffle.unshuffle(shuffled, BitShuffleType.SHORT	, unshuffled);

		// Check shuffle+unshuffle worked
		assertThat(unshuffled.get(), Matchers.is((byte) 7));
		assertThat(unshuffled.get(), Matchers.is((byte) 45));

		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
		byte[] shuffledBytes = new byte[2];
		shuffled.get(shuffledBytes);
		byte[] unshufledBytes = new byte[2];
		bitShuffleFilter.unshuffle(shuffledBytes, 2, unshufledBytes);

		assertThat(unshufledBytes[0], Matchers.is((byte) 7));
		assertThat(unshufledBytes[1], Matchers.is((byte) 45));
	}

	@Test
	void testCompareToReference4() throws IOException {
		ByteBuffer data = ByteBuffer.allocateDirect(8);
		data.putInt(7);
		data.putInt(32455);
		data.flip();
		ByteBuffer shuffled = ByteBuffer.allocateDirect(8);

		BitShuffle.shuffle(data, BitShuffleType.INT, shuffled);

		ByteBuffer unshuffled = ByteBuffer.allocateDirect(8);
		BitShuffle.unshuffle(shuffled, BitShuffleType.INT	, unshuffled);

		// Check shuffle+unshuffle worked
		assertThat(unshuffled.getInt(), Matchers.is(7));
		assertThat(unshuffled.getInt(), Matchers.is(32455));

		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
		byte[] shuffledBytes = new byte[8];
		shuffled.get(shuffledBytes);
		byte[] unshufledBytes = new byte[8];
		bitShuffleFilter.unshuffle(shuffledBytes, 4, unshufledBytes);

		ByteBuffer wrap = ByteBuffer.wrap(unshufledBytes);
		assertThat(wrap.getInt(), Matchers.is(7));
		assertThat(wrap.getInt(), Matchers.is(32455));
	}

	@Test
	void testCompareToReference5() throws IOException {
		ByteBuffer data = ByteBuffer.allocateDirect(13);
		data.put((byte) 1);
		data.put((byte) 2);
		data.put((byte) 3);
		data.put((byte) 4);
		data.put((byte) 5);
		data.put((byte) 6);
		data.put((byte) 7);
		data.put((byte) 8);
		data.put((byte) 9);
		data.put((byte) 10);
		data.put((byte) 11);
		data.put((byte) 12);
		data.put((byte) 13);
		data.flip();
		ByteBuffer shuffled = ByteBuffer.allocateDirect(13);

		BitShuffle.shuffle(data, BitShuffleType.BYTE, shuffled);

		ByteBuffer unshuffled = ByteBuffer.allocateDirect(13);
		BitShuffle.unshuffle(shuffled, BitShuffleType.BYTE	, unshuffled);

		// Check shuffle+unshuffle worked
		for (byte i = 1; i < unshuffled.capacity(); i++) {
			assertThat(unshuffled.get(), Matchers.is(i));
		}

		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
		byte[] shuffledBytes = new byte[13];
		shuffled.get(shuffledBytes);
		byte[] unshufledBytes = new byte[13];
		bitShuffleFilter.unshuffle(shuffledBytes, 1, unshufledBytes);

		assertThat(unshufledBytes[0], Matchers.is((byte) 1));
		assertThat(unshufledBytes[1], Matchers.is((byte) 2));
	}

	@Test
	void testCompareToReference6() throws IOException {
		ByteBuffer data = ByteBuffer.allocateDirect(10);
		data.put((byte) 4);
		data.put((byte) 4);
		data.put((byte) 4);
		data.put((byte) 4);

		data.put((byte) 4);
		data.put((byte) 4);
		data.put((byte) 4);
		data.put((byte) 4);

		data.put((byte) 4);
		data.put((byte) 4);


		data.flip();
		ByteBuffer shuffled = ByteBuffer.allocateDirect(10);

		BitShuffle.shuffle(data, BitShuffleType.BYTE, shuffled);

		ByteBuffer unshuffled = ByteBuffer.allocateDirect(10);
		BitShuffle.unshuffle(shuffled, BitShuffleType.BYTE	, unshuffled);

		// Check shuffle+unshuffle worked
		assertThat(unshuffled.get(), Matchers.is((byte) 4));
		assertThat(unshuffled.get(), Matchers.is((byte) 4));

		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
		byte[] shufledBytes = new byte[10];
		shuffled.get(shufledBytes);
		byte[] unshufledBytes = new byte[10];
		bitShuffleFilter.unshuffle(shufledBytes, 1, unshufledBytes);

		assertThat(unshufledBytes[0], Matchers.is((byte) 4));
		assertThat(unshufledBytes[1], Matchers.is((byte) 4));

		byte[] unshuffledReference = new byte[10];
		unshuffled.rewind();
		unshuffled.get(unshuffledReference);

		assertThat(toObject(unshufledBytes), Matchers.arrayContaining(toObject(unshuffledReference)));
	}

	@Test
	void unsupportedCompressionThrows() {
		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();

		// Completely unknown filter type 6
		final int[] filterData = new int[]{0, 0, 4, 0, 6};
		assertThrows(HdfFilterException.class, () -> bitShuffleFilter.decode(new byte[0], filterData));

		// Currently, ZSTD is unsupported. Remove when support is added
		final int[] zstdFilterData = new int[]{0, 0, 4, 0, BitShuffleFilter.ZSTD_COMPRESSION};
		assertThrows(UnsupportedHdfException.class, () -> bitShuffleFilter.decode(new byte[0], zstdFilterData));
	}

	@Test
	void testLazyInitFailure() {
		ByteBuffer buffer = ByteBuffer.allocate(64);
		buffer.putLong(64); // Total size
		buffer.putInt(16); // Block size
		buffer.putInt(8); // first block size

		try(MockedStatic<LZ4Factory> lz4FactoryMock = mockStatic(LZ4Factory.class)) {
			lz4FactoryMock.when(LZ4Factory::fastestJavaInstance).thenThrow(new RuntimeException("test"));
			BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
			assertThrows(HdfFilterException.class, () -> bitShuffleFilter.decode(buffer.array(), new int[]{0,0,1,16, LZ4_COMPRESSION}));
		}
	}
}
