package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class ObjectHeader {
  private static final Logger logger = LoggerFactory.getLogger(ObjectHeader.class);

  /** The location of this B tree in the file */
  private final long address;
  /** Type of node. 0 = group, 1 = data */
  private final byte version;
  /** Level of the node 0 = leaf */
  private final short numberOfMessages;
  /** Level of the node 0 = leaf */
  private final int referenceCount;


  public ObjectHeader(FileChannel fc, long address) {
    this.address = address;

    try {
      int headerSize = 12;
      ByteBuffer header = ByteBuffer.allocate(headerSize);
      header.order(LITTLE_ENDIAN);
      fc.read(header, address);
      long location = address + headerSize;
      header.rewind();

      // Version
      version = header.get();

      // Skip reserved byte
      header.get();

      // Number of messages
      numberOfMessages = header.getShort();

      // Reference Count
      referenceCount = header.getInt();

      // Size of the messages
      headerSize = header.getInt();

      header = ByteBuffer.allocate(headerSize);
      header.order(LITTLE_ENDIAN);
      fc.read(header, location);

      header.rewind();
      for (int i = 0; i < numberOfMessages; i++) {
        short messageType = header.getShort();
        short dataSize = header.getShort();
        logger.debug("messageType = {}, dataSize = {}", messageType, dataSize);
        BitSet flags = BitSet.valueOf(new byte[] {header.get()});
        for (int j = 0; j < 8; j++) {
          System.out.println(j + " = " + flags.get(j));
        }
        // Skip 3 reserved bytes
        header.get(new byte[3]);
        header.get(new byte[dataSize]);
      }
      logger.debug("Read object header from: {}", Utils.toHex(address));

    } catch (Exception e) {
      throw new HdfException("Failed to read object header at: " + Utils.toHex(address), e);
    }
  }
}
