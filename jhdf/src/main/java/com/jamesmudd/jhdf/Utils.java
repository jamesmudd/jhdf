package com.jamesmudd.jhdf;

import java.nio.ByteBuffer;

public final class Utils {
	private static final char NULL = '\0';
	private static final long UNDEFINED_ADDRESS = -1;
	
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
		while (buffer.remaining() > 0)
		{
			char c = (char) buffer.get();
			if (c == NULL)
				break;
			sb.append(c);
		}
		return sb.toString();
	}
}
