package io.jhdf.filter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xerial.snappy.BitShuffle;
import org.xerial.snappy.BitShuffleType;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.hamcrest.MatcherAssert.assertThat;

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
		byte[] shufledBytes = new byte[2];
		shuffled.get(shufledBytes);
		byte[] unshufledBytes = new byte[2];
		bitShuffleFilter.unshuffle(shufledBytes, 8, unshufledBytes);

		System.out.println(new BigInteger(shufledBytes).toString(2));
		System.out.println(new BigInteger(unshufledBytes).toString(2));

		assertThat(unshufledBytes[0], Matchers.is((byte) 1));
		assertThat(unshufledBytes[1], Matchers.is((byte) 2));
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
		byte[] shufledBytes = new byte[2];
		shuffled.get(shufledBytes);
		byte[] unshufledBytes = new byte[2];
		bitShuffleFilter.unshuffle(shufledBytes, 8, unshufledBytes);

		System.out.println(new BigInteger(shufledBytes).toString(2));
		System.out.println(new BigInteger(unshufledBytes).toString(2));

		assertThat(unshufledBytes[0], Matchers.is((byte) 7));
		assertThat(unshufledBytes[1], Matchers.is((byte) 45));
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
		byte[] shufledBytes = new byte[2];
		shuffled.get(shufledBytes);
		byte[] unshufledBytes = new byte[2];
		bitShuffleFilter.unshuffle(shufledBytes, 16, unshufledBytes);

		System.out.println(new BigInteger(shufledBytes).toString(2));
		System.out.println(new BigInteger(unshufledBytes).toString(2));

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
		byte[] shufledBytes = new byte[8];
		shuffled.get(shufledBytes);
		byte[] unshufledBytes = new byte[8];
		bitShuffleFilter.unshuffle(shufledBytes, 32, unshufledBytes);

		System.out.println(new BigInteger(shufledBytes).toString(2));
		System.out.println(new BigInteger(unshufledBytes).toString(2));

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
//		assertThat(unshuffled.get(), Matchers.is((byte) 2));

		BitShuffleFilter bitShuffleFilter = new BitShuffleFilter();
		byte[] shufledBytes = new byte[13];
		shuffled.get(shufledBytes);
		byte[] unshufledBytes = new byte[13];
		bitShuffleFilter.unshuffle(shufledBytes, 8, unshufledBytes);

		System.out.println(new BigInteger(shufledBytes).toString(2));
		System.out.println(new BigInteger(unshufledBytes).toString(2));

		System.out.println(new BigInteger(shufledBytes).bitCount());
		System.out.println(new BigInteger(unshufledBytes).bitCount());

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
		bitShuffleFilter.unshuffle(shufledBytes, 8, unshufledBytes);

		System.out.println(new BigInteger(shufledBytes).toString(2));
		System.out.println(new BigInteger(unshufledBytes).toString(2));

		assertThat(unshufledBytes[0], Matchers.is((byte) 4));
		assertThat(unshufledBytes[1], Matchers.is((byte) 4));

		byte[] unshuffledReference = new byte[10];
		unshuffled.rewind();
		unshuffled.get(unshuffledReference);

		assertThat(toObject(unshufledBytes), Matchers.arrayContaining(toObject(unshuffledReference)));
	}

}
