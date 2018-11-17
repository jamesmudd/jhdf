package com.jamesmudd.jhdf;

public final class Utils {
	private static final long UNDEFINED_ADDRESS = -1;
	
	private Utils() {
		// No instances
	}
	
	public static String toHex(long address) {
		if (address == UNDEFINED_ADDRESS) {
			return "UNDEFINED";
		}
		return Long.toHexString(address);
	}

}
