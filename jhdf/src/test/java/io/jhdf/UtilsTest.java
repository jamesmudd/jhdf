/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.stream.Stream;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilsTest {

	@Test
    void testToHex() {
		assertThat(Utils.toHex(88), is(equalTo("0x58")));
	}

	@Test
    void testToHexWithUndefinedAddress() {
		assertThat(Utils.toHex(Constants.UNDEFINED_ADDRESS), is(equalTo("UNDEFINED")));
	}

	@Test
    void testReadUntilNull() {
		ByteBuffer bb = ByteBuffer.allocate(4);
		byte[] b = new byte[] { 'H', 'D', 'F', Constants.NULL };
		bb.put(b);
		bb.rewind();
		assertThat(Utils.readUntilNull(bb), is(equalTo("HDF")));
	}

	@Test
    void testReadUntilNullThrowsIfNoNullIsFound() {
		ByteBuffer bb = ByteBuffer.allocate(3);
		byte[] b = new byte[] { 'H', 'D', 'F' };
		bb.put(b);
		bb.rewind();
		assertThrows(IllegalArgumentException.class, () -> Utils.readUntilNull(bb));
	}

	@Test
    void testReadUntilNullThrowsIfNonAlphanumericCharacterIsSeen() {
		ByteBuffer bb = ByteBuffer.allocate(3);
		byte[] b = new byte[] { 'H', 'D', ' ' };
		bb.put(b);
		bb.rewind();
		assertThrows(IllegalArgumentException.class, () -> Utils.readUntilNull(bb));
	}

	@Test
    void testValidNameReturnsTrue() {
		assertThat(Utils.validateName("hello"), is(true));
	}

	@Test
    void testNameContainingDotIsInvalid() {
		assertThat(Utils.validateName("hello."), is(false));
	}

	@Test
    void testNameContainingSlashIsInvalid() {
		assertThat(Utils.validateName("hello/"), is(false));
	}

	@Test
    void testNameContainingNonAsciiIsInvalid() {
		assertThat(Utils.validateName("helloμ"), is(false));
	}

	@Test
    void testMovingBufferAlreadyAtEightBytePositionDoesNothing() {
		ByteBuffer bb = ByteBuffer.allocate(11);
		// Buffer position is 0
		Utils.seekBufferToNextMultipleOfEight(bb);
		assertThat(bb.position(), is(equalTo(0)));

		// Move to 8 and test again
		bb.position(8);
		Utils.seekBufferToNextMultipleOfEight(bb);
		assertThat(bb.position(), is(equalTo(8)));
	}

	@Test
    void testMovingBufferToNextEightBytePosition() {
		ByteBuffer bb = ByteBuffer.allocate(20);
		bb.position(1);
		// Buffer position is 1 should be moved to 8
		Utils.seekBufferToNextMultipleOfEight(bb);
		assertThat(bb.position(), is(equalTo(8)));

		// Move to 14 should be moved to 16
		bb.position(14);
		Utils.seekBufferToNextMultipleOfEight(bb);
		assertThat(bb.position(), is(equalTo(16)));
	}

	@Test
    void testReadingOneByteToLong() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 1), is(equalTo(12L)));
		assertThat(bb.position(), is(1));
	}

	@Test
    void testReadingTwoBytesToLong() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 2), is(equalTo(12L)));
		assertThat(bb.position(), is(2));
	}

	@Test
    void testReadingThreeBytesToLong() {
		byte[] bytes = new byte[] { 0, 0, 1 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 3), is(equalTo(65536L)));
		assertThat(bb.position(), is(3));
	}

	@Test
    void testReadingThreeBytesToLongBigEndian() {
		byte[] bytes = new byte[] { 1, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(BIG_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 3), is(equalTo(65536L)));
		assertThat(bb.position(), is(3));
	}

	@Test
    void testReadingFourBytesToLong() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 4), is(equalTo(12L)));
		assertThat(bb.position(), is(4));
	}

	@Test
    void testReadingFiveBytesToLong() {
		byte[] bytes = new byte[] { 0, 0, 0, 0, 1 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 5), is(equalTo(4294967296L)));
		assertThat(bb.position(), is(5));
	}

	@Test
    void testReadingSixBytesToLong() {
		byte[] bytes = new byte[] { 0, 0, 0, 0, 0, 1 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 6), is(equalTo(1099511627776L)));
		assertThat(bb.position(), is(6));
	}

	@Test
    void testReadingSixBytesToInt() {
		byte[] bytes = new byte[] { 0, 0, 1, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 6), is(equalTo(65536)));
		assertThat(bb.position(), is(6));
	}

	@Test
    void testReadingSevenBytesToLong() {
		byte[] bytes = new byte[] { 0, 0, 0, 0, 0, 0, 1 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 7), is(equalTo(281474976710656L)));
		assertThat(bb.position(), is(7));
	}

	@Test
    void testReadingEightBytesToLong() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 8), is(equalTo(12L)));
		assertThat(bb.position(), is(8));
	}

	@Test
    void testReadingUnsupportedLengthLongThrows() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThrows(IllegalArgumentException.class, () -> Utils.readBytesAsUnsignedLong(bb, 9));
	}

	@Test
    void testReadingEightByteNegativeIntegerThrows() {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.rewind();
		bb.putInt(-462);
		bb.rewind();
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedLong(bb, 8));
	}

	@Test
    void testReadingEightByteNegativeLongThrows() {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.rewind();
		bb.putLong(-462);
		bb.rewind();
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedLong(bb, 8));
	}

	@Test
    void testReadingOneByteToInt() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 1), is(equalTo(12)));
		assertThat(bb.position(), is(1));
	}

	@Test
    void testReadingTwoBytesToInt() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 2), is(equalTo(12)));
		assertThat(bb.position(), is(2));
	}

	@Test
    void testReadingThreeBytesToInt() {
		byte[] bytes = new byte[] { 0, 0, 1 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 3), is(equalTo(65536)));
		assertThat(bb.position(), is(3));
	}

	@Test
    void testReadingFourBytesToInt() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 4), is(equalTo(12)));
		assertThat(bb.position(), is(4));
	}

	@Test
    void testReadingFourBytesToIntThatCantBeUnsigned() {
		byte[] bytes = new byte[] { -127, -127, -127, -127 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedInt(bb, 4));
	}

	@Test
    void testReadingEightBytesToInt() {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 8), is(equalTo(12)));
		assertThat(bb.position(), is(8));
	}

	@Test
    void testReadingUnsupportedLengthThrows() {
		byte[] bytes = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThrows(IllegalArgumentException.class, () -> Utils.readBytesAsUnsignedInt(bb, 9));
	}

	@Test
    void testReadingLargeLongThrows() {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(Long.MAX_VALUE);
		bb.rewind();
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedInt(bb, 8));
	}

	@Test
    void testReadingUndefinedAddressSpecialCaseWorks() {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(Constants.UNDEFINED_ADDRESS);
		bb.rewind();
		assertThat(Utils.readBytesAsUnsignedLong(bb, 8), is(equalTo(Constants.UNDEFINED_ADDRESS)));
	}

	@Test
    void testCreatingSubBuffer() {
		byte[] ints = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ByteBuffer bb = ByteBuffer.wrap(ints);

		ByteBuffer subBuffer = Utils.createSubBuffer(bb, 3);
		// Check new buffer is of the right length
		assertThat(subBuffer.limit(), is(equalTo(3)));
		// Check original buffer position is moved on
		assertThat(bb.position(), is(equalTo(3)));
	}

	@Test
    void testBitsToInt() {
		BitSet bits = new BitSet();
		assertThat(Utils.bitsToInt(bits, 0, 8), is(equalTo(0)));
		bits.set(0, true);
		assertThat(Utils.bitsToInt(bits, 0, 1), is(equalTo(1)));
		assertThat(Utils.bitsToInt(bits, 0, 5), is(equalTo(1)));
		bits.set(1, true);
		assertThat(Utils.bitsToInt(bits, 0, 2), is(equalTo(3)));
		bits.set(2, true);
		assertThat(Utils.bitsToInt(bits, 0, 3), is(equalTo(7)));
		// Test just high bit set
		bits.set(0, 7, false);
		bits.set(7, true);
		assertThat(Utils.bitsToInt(bits, 0, 8), is(equalTo(128)));
		assertThat(Utils.bitsToInt(bits, 0, 7), is(equalTo(0)));
		assertThat(Utils.bitsToInt(bits, 5, 3), is(equalTo(4)));
	}

	@Test
    void testBitsToIntThrowsWithNegativeLength() {
		BitSet bits = new BitSet(8);
		assertThrows(IllegalArgumentException.class, () -> Utils.bitsToInt(bits, 3, -1)); // Should throw
	}

	@Test
    void testBytesNeededToHoldNumber() {
		// Edge case 0
		assertThat(Utils.bytesNeededToHoldNumber(0), is(equalTo(1)));

		// Simple cases
		assertThat(Utils.bytesNeededToHoldNumber(1), is(equalTo(1)));
		assertThat(Utils.bytesNeededToHoldNumber(2), is(equalTo(1)));
		// On the 1 byte boundary
		assertThat(Utils.bytesNeededToHoldNumber(255), is(equalTo(1)));
		assertThat(Utils.bytesNeededToHoldNumber(256), is(equalTo(2)));
		// On the 2 byte boundary
		assertThat(Utils.bytesNeededToHoldNumber(65535), is(equalTo(2)));
		assertThat(Utils.bytesNeededToHoldNumber(65536), is(equalTo(3)));
	}

	@Test
    void testBytesNeededToHoldNumberThrowsWithNegative() {
		assertThrows(IllegalArgumentException.class, () -> Utils.bytesNeededToHoldNumber(-123));
	}

	@ParameterizedTest
	@MethodSource("chunkIndexToChunkOffsetTestCases")
	void test(int chunkIndex, int[] chunkDimensions, int[] datasetDimensions, int[] expectedChunkOffset) {
		assertThat(Utils.chunkIndexToChunkOffset(chunkIndex, chunkDimensions, datasetDimensions), is(equalTo(expectedChunkOffset)));
	}

	private static Stream<Arguments> chunkIndexToChunkOffsetTestCases() {
		return Stream.of(
				//1D
				Arguments.of(0, new int[]{2}, new int[]{5}, new int[] {0}),
				Arguments.of(1, new int[]{2}, new int[]{5}, new int[] {2}),
				Arguments.of(2, new int[]{2}, new int[]{5}, new int[] {4}),
				Arguments.of(3, new int[]{2}, new int[]{5}, new int[] {6}), // This is outside the data

				// 2D
				// Mismatched in both directions i.e dataset dims not a multiple of chunk dims
				Arguments.of(0, new int[]{2,3}, new int[]{7,5}, new int[] {0,0}),
				Arguments.of(1, new int[]{2,3}, new int[]{7,5}, new int[] {0,3}),
				Arguments.of(2, new int[]{2,3}, new int[]{7,5}, new int[] {2,0}),
				Arguments.of(3, new int[]{2,3}, new int[]{7,5}, new int[] {2,3}),
				Arguments.of(4, new int[]{2,3}, new int[]{7,5}, new int[] {4,0}),
				Arguments.of(5, new int[]{2,3}, new int[]{7,5}, new int[] {4,3}),
				Arguments.of(6, new int[]{2,3}, new int[]{7,5}, new int[] {6,0}),
				Arguments.of(7, new int[]{2,3}, new int[]{7,5}, new int[] {6,3}), // This is outside the data

				// Matched in both dims
				Arguments.of(0, new int[]{3,5}, new int[]{9,20}, new int[] {0,0}),
				Arguments.of(1, new int[]{3,5}, new int[]{9,20}, new int[] {0,5}),
				Arguments.of(2, new int[]{3,5}, new int[]{9,20}, new int[] {0,10}),
				Arguments.of(3, new int[]{3,5}, new int[]{9,20}, new int[] {0,15}),
				Arguments.of(4, new int[]{3,5}, new int[]{9,20}, new int[] {3,0}),
				Arguments.of(5, new int[]{3,5}, new int[]{9,20}, new int[] {3,5}),
				Arguments.of(6, new int[]{3,5}, new int[]{9,20}, new int[] {3,10}),
				Arguments.of(7, new int[]{3,5}, new int[]{9,20}, new int[] {3,15}),
				Arguments.of(8, new int[]{3,5}, new int[]{9,20}, new int[] {6,0}),
				Arguments.of(9, new int[]{3,5}, new int[]{9,20}, new int[] {6,5}),
				Arguments.of(10, new int[]{3,5}, new int[]{9,20}, new int[] {6,10}),
				Arguments.of(11, new int[]{3,5}, new int[]{9,20}, new int[] {6,15}),

				// 3D
				Arguments.of(0, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {0,0,0}),
				Arguments.of(1, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {0,0,4}),
				Arguments.of(2, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {0,3,0}),
				Arguments.of(3, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {0,3,4}),
				Arguments.of(4, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {2,0,0}),
				Arguments.of(5, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {2,0,4}),
				Arguments.of(6, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {2,3,0}),
				Arguments.of(7, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {2,3,4}),
				Arguments.of(8, new int[]{2,3,4}, new int[]{7,5,6}, new int[] {4,0,0})
		);
	}
}
