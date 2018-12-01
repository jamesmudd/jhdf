package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testToHex() {
		assertThat(Utils.toHex(88), is(equalTo("0x58")));
	}

	@Test
	public void testToHexWithUndefinedAddress() {
		assertThat(Utils.toHex(Utils.UNDEFINED_ADDRESS), is(equalTo("UNDEFINED")));
	}

	@Test
	public void testReadUntilNull() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(4);
		byte[] b = new byte[] { 'H', 'D', 'F', Utils.NULL };
		bb.put(b);
		bb.rewind();
		assertThat(Utils.readUntilNull(bb), is(equalTo("HDF")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadUntilNullThrowsIfNoNullIsFound() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(3);
		byte[] b = new byte[] { 'H', 'D', 'F' };
		bb.put(b);
		bb.rewind();
		Utils.readUntilNull(bb);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadUntilNullThrowsIfNonAlphanumericCharacterIsSeen() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(3);
		byte[] b = new byte[] { 'H', 'D', ' ' };
		bb.put(b);
		bb.rewind();
		Utils.readUntilNull(bb);
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
	public void testReadingOneByteToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsInt(bb, 1), is(equalTo(12)));
		assertThat(bb.position(), is(1));
	}

	@Test
	public void testReadingTwoBytesToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsInt(bb, 2), is(equalTo(12)));
		assertThat(bb.position(), is(2));
	}

	@Test
	public void testReadingFourBytesToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsInt(bb, 4), is(equalTo(12)));
		assertThat(bb.position(), is(4));
	}

	@Test
	public void testReadingEightBytesToInt() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		assertThat(Utils.readBytesAsInt(bb, 8), is(equalTo(12)));
		assertThat(bb.position(), is(8));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadingUnsupportedLentghThrows() throws Exception {
		byte[] bytes = new byte[] { 12, 0, 0, 0, 0, 0, 0, 0 };
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
		Utils.readBytesAsInt(bb, 7); // should throw
	}

	@Test(expected = ArithmeticException.class)
	public void testReadingLargeLongThrows() throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(Long.MAX_VALUE);
		bb.rewind();
		Utils.readBytesAsInt(bb, 8);
	}

}
