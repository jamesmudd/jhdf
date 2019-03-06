package io.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.BitSet;

import org.junit.jupiter.api.Test;

public class UtilsTest {

	@Test
	public void testToHex() {
		assertThat(Utils.toHex(88), is(equalTo("0x58")));
	}

	@Test
	public void testToHexWithUndefinedAddress() {
		assertThat(Utils.toHex(Constants.UNDEFINED_ADDRESS), is(equalTo("UNDEFINED")));
	}

	@Test
	public void testReadUntilNull() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(4);
		byte[] b = new byte[] { 'H', 'D', 'F', Constants.NULL };
		bb.put(b);
		bb.rewind();
		assertThat(Utils.readUntilNull(bb), is(equalTo("HDF")));
	}

	@Test
	public void testReadUntilNullThrowsIfNoNullIsFound() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(3);
		byte[] b = new byte[] { 'H', 'D', 'F' };
		bb.put(b);
		bb.rewind();
		assertThrows(IllegalArgumentException.class, () -> Utils.readUntilNull(bb));
	}

	@Test
	public void testReadUntilNullThrowsIfNonAlphanumericCharacterIsSeen() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(3);
		byte[] b = new byte[] { 'H', 'D', ' ' };
		bb.put(b);
		bb.rewind();
		assertThrows(IllegalArgumentException.class, () -> Utils.readUntilNull(bb));
	}

	@Test
	public void testValidNameReturnsTrue() throws Exception {
		assertThat(Utils.validateName("hello"), is(true));
	}

	@Test
	public void testNameContainingDotIsInvalid() throws Exception {
		assertThat(Utils.validateName("hello."), is(false));
	}

	@Test
	public void testNameContainingSlashIsInvalid() throws Exception {
		assertThat(Utils.validateName("hello/"), is(false));
	}

	@Test
	public void testNameContainingNonAsciiIsInvalid() throws Exception {
		assertThat(Utils.validateName("helloμ"), is(false));
	}

	@Test
	public void testMovingBufferAlreadyAtEightBytePositionDoesNothing() throws Exception {
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
	public void testMovingBufferToNextEightBytePosition() throws Exception {
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
	public void testReadingOneByteToLong() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 1), is(equalTo(12L)));
		assertThat(bb.position(), is(1));
	}

	@Test
	public void testReadingTwoBytesToLong() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 2), is(equalTo(12L)));
		assertThat(bb.position(), is(2));
	}

	@Test
	public void testReadingThreeBytesToLong() throws Exception {
		byte[] bytes = new byte[] { 1, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 3), is(equalTo(65536L)));
		assertThat(bb.position(), is(3));
	}

	@Test
	public void testReadingFourBytesToLong() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 4), is(equalTo(12L)));
		assertThat(bb.position(), is(4));
	}

	@Test
	public void testReadingFiveBytesToLong() throws Exception {
		byte[] bytes = new byte[] { 1, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 5), is(equalTo(4294967296L)));
		assertThat(bb.position(), is(5));
	}

	@Test
	public void testReadingSixBytesToLong() throws Exception {
		byte[] bytes = new byte[] { 1, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 6), is(equalTo(1099511627776L)));
		assertThat(bb.position(), is(6));
	}

	@Test
	public void testReadingSevenBytesToLong() throws Exception {
		byte[] bytes = new byte[] { 1, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 7), is(equalTo(281474976710656L)));
		assertThat(bb.position(), is(7));
	}

	@Test
	public void testReadingEightBytesToLong() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedLong(bb, 8), is(equalTo(12L)));
		assertThat(bb.position(), is(8));
	}

	@Test
	public void testReadingUnsupportedLengthLongThrows() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThrows(IllegalArgumentException.class, () -> Utils.readBytesAsUnsignedLong(bb, 9));
	}

	@Test
	public void testReadingEightByteNegativeIntegerThrows() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.rewind();
		bb.putInt(-462);
		bb.rewind();
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedLong(bb, 8));
	}

	@Test
	public void testReadingEightByteNegativeLongThrows() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.rewind();
		bb.putInt(-462);
		bb.rewind();
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedLong(bb, 8));
	}

	@Test
	public void testReadingOneByteToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 1), is(equalTo(12)));
		assertThat(bb.position(), is(1));
	}

	@Test
	public void testReadingTwoBytesToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 2), is(equalTo(12)));
		assertThat(bb.position(), is(2));
	}

	@Test
	public void testReadingThreeBytesToInt() throws Exception {
		byte[] bytes = new byte[] { 1, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 3), is(equalTo(65536)));
		assertThat(bb.position(), is(3));
	}

	@Test
	public void testReadingFourBytesToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 4), is(equalTo(12)));
		assertThat(bb.position(), is(4));
	}

	@Test
	public void testReadingFourBytesToIntThatCantBeUnsigned() throws Exception {
		byte[] bytes = new byte[] { -127, -127, -127, -127 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedInt(bb, 4));
	}

	@Test
	public void testReadingEightBytesToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsUnsignedInt(bb, 8), is(equalTo(12)));
		assertThat(bb.position(), is(8));
	}

	@Test
	public void testReadingUnsupportedLengthThrows() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThrows(IllegalArgumentException.class, () -> Utils.readBytesAsUnsignedInt(bb, 6));
	}

	@Test
	public void testReadingLargeLongThrows() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(Long.MAX_VALUE);
		bb.rewind();
		assertThrows(ArithmeticException.class, () -> Utils.readBytesAsUnsignedInt(bb, 8));
	}

	@Test
	public void testReadingUndefinedAddressSpecialCaseWorks() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(Constants.UNDEFINED_ADDRESS);
		bb.rewind();
		assertThat(Utils.readBytesAsUnsignedLong(bb, 8), is(equalTo(Constants.UNDEFINED_ADDRESS)));
	}

	@Test
	public void testCreatingSubbuffer() throws Exception {
		byte[] ints = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ByteBuffer bb = ByteBuffer.wrap(ints);

		ByteBuffer subBuffer = Utils.createSubBuffer(bb, 3);
		// Check new buffer is of the right length
		assertThat(subBuffer.limit(), is(equalTo(3)));
		// Check original buffer position is moved on
		assertThat(bb.position(), is(equalTo(3)));
	}

	@Test
	public void testBitsToInt() throws Exception {
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
	public void testBitsToIntThrowsWithNegativeLength() throws Exception {
		BitSet bits = new BitSet(8);
		assertThrows(IllegalArgumentException.class, () -> Utils.bitsToInt(bits, 3, -1)); // Should throw
	}

	@Test
	public void testbytesNeededToHoldNumber() throws Exception {
		// Edge case 0
		assertThat(Utils.bytesNeededToHoldNumber(0), is(equalTo(1)));

		// Simple cases
		assertThat(Utils.bytesNeededToHoldNumber(1), is(equalTo(1)));
		assertThat(Utils.bytesNeededToHoldNumber(2), is(equalTo(1)));
		// On the 1 byte boundary
		assertThat(Utils.bytesNeededToHoldNumber(255), is(equalTo(1)));
		assertThat(Utils.bytesNeededToHoldNumber(256), is(equalTo(2)));
		// On the 2 byte boundry
		assertThat(Utils.bytesNeededToHoldNumber(65535), is(equalTo(2)));
		assertThat(Utils.bytesNeededToHoldNumber(65536), is(equalTo(3)));
	}

	@Test
	public void testbytesNeededToHoldNumberThrowsWithNegative() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> Utils.bytesNeededToHoldNumber(-123));
	}

}
