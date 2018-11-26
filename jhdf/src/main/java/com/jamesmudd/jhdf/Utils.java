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
   * Reads ASCII string from the buffer until a null character is reached. This will read from the
   * buffers current position. After the method the buffer position will be after the null
   * character.
   * 
   * @param buffer to read from
   * @return the string read from the buffer
   * @throws IllegalArgumentException if the end of the buffer if reached before and null terminator
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
   * Check the provided name to see if it is valid for a HDF5 identifier. Checks name only contains
   * ASCII characters and does not contain '/' or '.' which are reserver characters.
   * 
   * @param name To check if valid
   * @return <code>true</code> if this is a valid HDF5 name, <code>false</code> otherwise
   */
  public static boolean validateName(String name) {
    return ASCII.canEncode(name) && !name.contains("/") && !name.contains(".");
  }
}
