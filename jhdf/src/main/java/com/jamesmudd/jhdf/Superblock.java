package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Superblock {
	
	private static final byte[] HDF5_FILE_SIGNATURE = new byte[] {-119, 72, 68, 70, 13, 10, 26, 10};
	
	private final int versionOfSuperblock;

	private final int versionNumberOfTheFileFreeSpaceInformation;
	
	private final int versionOfRootGroupSymbolTableEntry;
	
	private final int versionOfSharedHeaderMessageFormat;

	private final int sizeOfOffsets;

	private final int sizeOfLengths;

	private final int groupLeafNodeK;

	private final long baseAddressByte;

	private final long addressOfGlobalFreeSpaceIndex;

	private final long endOfFileAddress;

	private final long driverInformationBlockAddress;

	private final long rootGroupSymbolTableEntry;
	
	public Superblock(RandomAccessFile file) throws IOException {
		// Reset the file pointer to the start to read the superblock
		file.seek(0); // TODO Can be at offsets into the file 0, 512, 1024, 2048, and so on.

		// Format Signature
		byte[] formatSignitureByte = new byte[8];
		file.read(formatSignitureByte);

		// Verify signature
		if (!Arrays.equals(HDF5_FILE_SIGNATURE, formatSignitureByte)) {
			throw new IOException("File is not HDF5. File signature not matched");
		}
		
		// Version # of Superblock
		versionOfSuperblock = file.readUnsignedByte();
		System.out.println("Version of superblock is = " + versionOfSuperblock);

		if (versionOfSuperblock == 0 || versionOfSuperblock == 1) {

			// Version # of File Free-space Storage
			versionNumberOfTheFileFreeSpaceInformation = file.readUnsignedByte();
			System.out.println("Version Number of the File Free-Space Information: "
					+ versionNumberOfTheFileFreeSpaceInformation);

			// Version # of Root Group Symbol Table Entry
			versionOfRootGroupSymbolTableEntry = file.readUnsignedByte();
			System.out.println("Version # of Root Group Symbol Table Entry: " + versionOfRootGroupSymbolTableEntry);

			file.skipBytes(1);
			
			// Version # of Shared Header Message Format
			versionOfSharedHeaderMessageFormat = file.readUnsignedByte();
			System.out.println("Version # of Shared Header Message Format: " + versionOfSharedHeaderMessageFormat);

			// Size of Offsets
			sizeOfOffsets = file.readUnsignedByte();
			System.out.println("Size of Offsets: " + sizeOfOffsets);

			// Size of Lengths
			sizeOfLengths = file.readUnsignedByte();
			System.out.println("Size of Lengths: " + sizeOfLengths);
			
			file.skipBytes(1);

			// Group Leaf Node K
			byte[] twoBytes = new byte[2];
			file.read(twoBytes);
			groupLeafNodeK = ByteBuffer.wrap(twoBytes).order(LITTLE_ENDIAN).getShort();
			System.out.println("groupLeafNodeK = " + groupLeafNodeK);

			// Group Internal Node K
			file.read(twoBytes);
			int groupInternalNodeK = ByteBuffer.wrap(twoBytes).order(LITTLE_ENDIAN).getShort();
			System.out.println("groupInternalNodeK = " + groupInternalNodeK);

			// File Consistency Flags
			file.skipBytes(4);

			// Group Internal Node K
//			file.read(twoBytes);
//			int indexedStorageInternalNodeK = ByteBuffer.wrap(twoBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
//			System.out.println("indexedStorageInternalNodeK = " + indexedStorageInternalNodeK);
//			
//			file.skipBytes(2);
			
			byte[] offsetBytes = new byte[sizeOfOffsets];
			
			// Base Address
			file.read(offsetBytes);
			baseAddressByte = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("baseAddressByte = " + baseAddressByte);

			// Address of Global Free-space Index
			file.read(offsetBytes);
			addressOfGlobalFreeSpaceIndex = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("addressOfGlobalFreeSpaceIndex = " + addressOfGlobalFreeSpaceIndex);

			// End of File Address
			file.read(offsetBytes);
			endOfFileAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("endOfFileAddress = " + endOfFileAddress);

			// Driver Information Block Address
			file.read(offsetBytes);
			driverInformationBlockAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("driverInformationBlockAddress = " + driverInformationBlockAddress);

			// Root Group Symbol Table Entry
			byte[] fourBytes = new byte[4];
			file.read(fourBytes);
			rootGroupSymbolTableEntry = ByteBuffer.wrap(fourBytes).order(LITTLE_ENDIAN).getInt();
			System.out.println("rootGroupSymbolTableEntry=" + rootGroupSymbolTableEntry);

		} // end of version 0 and 1
		else if (versionOfSuperblock == 2) {
			throw new IOException("version 2 is not implemented yet!");
		} else {
			throw new IOException("Verison " + versionOfSuperblock + " is not supported!");
		}

	}

	public int getVersionOfSuperblock() {
		return versionOfSuperblock;
	}

	public int getVersionNumberOfTheFileFreeSpaceInformation() {
		return versionNumberOfTheFileFreeSpaceInformation;
	}

	public int getVersionOfRootGroupSymbolTableEntry() {
		return versionOfRootGroupSymbolTableEntry;
	}

	public int getVersionOfSharedHeaderMessageFormat() {
		return versionOfSharedHeaderMessageFormat;
	}

	public int getSizeOfOffsets() {
		return sizeOfOffsets;
	}

	public int getSizeOfLengths() {
		return sizeOfLengths;
	}

	public int getGroupLeafNodeK() {
		return groupLeafNodeK;
	}

	public long getBaseAddressByte() {
		return baseAddressByte;
	}

	public long getAddressOfGlobalFreeSpaceIndex() {
		return addressOfGlobalFreeSpaceIndex;
	}

	public long getEndOfFileAddress() {
		return endOfFileAddress;
	}

	public long getDriverInformationBlockAddress() {
		return driverInformationBlockAddress;
	}

	public long getRootGroupSymbolTableEntry() {
		return rootGroupSymbolTableEntry;
	}

}
