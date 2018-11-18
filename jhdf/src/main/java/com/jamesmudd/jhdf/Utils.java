package com.jamesmudd.jhdf;

import java.nio.ByteBuffer;

public final class Utils {
	public static final char NULL = '\0';
	public static final long UNDEFINED_ADDRESS = -1;

	private Utils() {
		// No instances
	}

	public static String toHex(long address) {
		if (address == UNDEFINED_ADDRESS) {
			return "UNDEFINED";
		}
		return "0x" + Long.toHexString(address);
	}

	public static String readUntilNull(ByteBuffer buffer) {
		StringBuilder sb = new StringBuilder(buffer.remaining());
		while (buffer.remaining() > 0) {
			char c = (char) buffer.get();
			if (c == NULL) {
				return sb.toString();
			}
			if (!Character.isLetterOrDigit(c)) {
				throw new IllegalArgumentException("Character '" + c + "' is not a letter or digit");
			}
			sb.append(c);
		}
		throw new IllegalArgumentException("End of buffer reached before NULL");
	}
}
