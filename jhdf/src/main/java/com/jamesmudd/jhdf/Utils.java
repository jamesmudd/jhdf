package com.jamesmudd.jhdf;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public final class Utils {
	public static final char NULL = '\0';
	public static final long UNDEFINED_ADDRESS = -1;

	private static final CharsetEncoder ASCII = StandardCharsets.US_ASCII.newEncoder();

	private Utils() {
		// No instances
	}

	/**
	 * Converts an address to a hex string for display
	 * 
	 * @param address to convert to Hex
	 * @return the address as a hex string
	 */
	public static String toHex(long address) {
		if (address == UNDEFINED_ADDRESS) {
			return "UNDEFINED";
		}
		return "0x" + Long.toHexString(address);
	}

	/**
	 * Reads ASCII string from the buffer until a null character is reached. This
	 * will read from the buffers current position. After the method the buffer
	 * position will be after the null character.
	 * 
	 * @param buffer to read from
	 * @return the string read from the buffer
	 * @throws IllegalArgumentException if the end of the buffer if reached before
	 *                                  and null terminator
	 */
	public static String readUntilNull(ByteBuffer buffer) {
		StringBuilder sb = new StringBuilder(buffer.remaining());
		while (buffer.hasRemaining()) {
			char c = (char) buffer.get();
			if (c == NULL) {
				return sb.toString();
			}
			sb.append(c);
		}
		throw new IllegalArgumentException("End of buffer reached before NULL");
	}

	/**
	 * Check the provided name to see if it is valid for a HDF5 identifier. Checks
	 * name only contains ASCII characters and does not contain '/' or '.' which are
	 * reserver characters.
	 * 
	 * @param name To check if valid
	 * @return <code>true</code> if this is a valid HDF5 name, <code>false</code>
	 *         otherwise
	 */
	public static boolean validateName(String name) {
		return ASCII.canEncode(name) && !name.contains("/") && !name.contains(".");
	}

	/**
	 * Moves the position of the {@link ByteBuffer} to the next position aligned on
	 * 8 bytes. If the buffer position is already a multiple of 8 the position will
	 * not be changed.
	 * 
	 * @param bb the buffer to be aligned
	 */
	public static void seekBufferToNextMultipleOfEight(ByteBuffer bb) {
		int pos = bb.position();
		if (pos % 8 == 0) {
			return; // Already on a 8 byte multiple
		}
		bb.position(pos + (8 - (pos % 8)));
	}

	/**
	 * This reads the requested number of bytes from the buffer and returns the data
	 * as an int.
	 * <p>
	 * This is used in HDF5 to read "size of lengths" and "size of offsets"
	 * 
	 * @param buffer to read from
	 * @param lentgh the number of bytes to read
	 * @return the int value read from the buffer
	 * @throws ArithmeticException      if the data cannot be safely converted to an
	 *                                  int
	 * @throws IllegalArgumentException if the length requested is not supported;
	 */
	public static int readBytesAsInt(ByteBuffer buffer, int lentgh) {
		switch (lentgh) {
		case 1:
			return buffer.get();
		case 2:
			return buffer.getShort();
		case 4:
			return buffer.getInt();
		case 8:
			// Throws if the long can't be converted safely
			return Math.toIntExact(buffer.getLong());
		default:
			throw new IllegalArgumentException("Couldn't read " + lentgh + " bytes as int");
		}
	}
}
