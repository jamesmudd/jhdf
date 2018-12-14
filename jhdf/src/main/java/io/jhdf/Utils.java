package io.jhdf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public final class Utils {
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
		if (address == Constants.UNDEFINED_ADDRESS) {
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
			if (c == Constants.NULL) {
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
	 * as an unsigned int. After this call the buffer position will be advanced by
	 * the specified length.
	 * <p>
	 * This is used in HDF5 to read "size of lengths" and "size of offsets"
	 * 
	 * @param buffer to read from
	 * @param lentgh the number of bytes to read
	 * @return the int value read from the buffer
	 * @throws ArithmeticException      if the data cannot be safely converted to an
	 *                                  unsigned int
	 * @throws IllegalArgumentException if the length requested is not supported;
	 */
	public static int readBytesAsUnsignedInt(ByteBuffer buffer, int lentgh) {
		switch (lentgh) {
		case 1:
			return Byte.toUnsignedInt(buffer.get());
		case 2:
			return Short.toUnsignedInt(buffer.getShort());
		case 4:
			int value = buffer.getInt();
			if (value < 0) {
				throw new ArithmeticException("Could not convert to unsigned");
			}
			return value;
		case 8:
			// Throws if the long can't be converted safely
			return Math.toIntExact(buffer.getLong());
		default:
			throw new IllegalArgumentException("Couldn't read " + lentgh + " bytes as int");
		}
	}

	/**
	 * This reads the requested number of bytes from the buffer and returns the data
	 * as an unsigned long. After this call the buffer position will be advanced by
	 * the specified length.
	 * <p>
	 * This is used in HDF5 to read "size of lengths" and "size of offsets"
	 * 
	 * @param buffer to read from
	 * @param lentgh the number of bytes to read
	 * @return the long value read from the buffer
	 * @throws ArithmeticException      if the data cannot be safely converted to an
	 *                                  unsigned long
	 * @throws IllegalArgumentException if the length requested is not supported;
	 */
	public static long readBytesAsUnsignedLong(ByteBuffer buffer, int lentgh) {
		switch (lentgh) {
		case 1:
			return Byte.toUnsignedLong(buffer.get());
		case 2:
			return Short.toUnsignedLong(buffer.getShort());
		case 4:
			return Integer.toUnsignedLong(buffer.getInt());
		case 8:
			long value = buffer.getLong();
			if (value < 0 && value != Constants.UNDEFINED_ADDRESS) {
				throw new ArithmeticException("Could not convert to unsigned");
			}
			return value;
		default:
			throw new IllegalArgumentException("Couldn't read " + lentgh + " bytes as int");
		}
	}

	/**
	 * Creates a new {@link ByteBuffer} of the specified length. The new buffer will
	 * start at the current position of the source buffer and will be of the
	 * specified length. The {@link ByteOrder} of the new buffer will be the same as
	 * the source buffer. After the call the source buffer position will be
	 * incremented by the length of the sub-buffer. The new buffer will share the
	 * backing data with the source buffer.
	 * 
	 * @param source the buffer to take the sub buffer from
	 * @param lentgh the size of the new sub-buffer
	 * @return the new sub buffer
	 */
	public static ByteBuffer createSubBuffer(ByteBuffer source, int lentgh) {
		ByteBuffer headerData = source.slice();
		headerData.limit(lentgh);
		headerData.order(source.order());

		// Move the buffer past this header
		source.position(source.position() + lentgh);
		return headerData;
	}
}
