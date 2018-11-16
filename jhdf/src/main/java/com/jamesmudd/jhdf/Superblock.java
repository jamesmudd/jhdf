package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import com.jamesmudd.jhdf.exceptions.HdfException;

public class Superblock {

	private static final byte[] HDF5_FILE_SIGNATURE = new byte[] { -119, 72, 68, 70, 13, 10, 26, 10 };

	private final int versionOfSuperblock;
	private final int versionNumberOfTheFileFreeSpaceInformation;
	private final int versionOfRootGroupSymbolTableEntry;
	private final int versionOfSharedHeaderMessageFormat;
	private final int sizeOfOffsets;
	private final int sizeOfLengths;
	private final int groupLeafNodeK;
	private final int groupInternalNodeK;
	private final long baseAddressByte;
	private final long addressOfGlobalFreeSpaceIndex;
	private final long endOfFileAddress;
	private final long driverInformationBlockAddress;
	private final long rootGroupSymbolTableAddress;

	public Superblock(RandomAccessFile file, long offset) {
		try {

			final boolean verifiedSignature = verifySignature(file, offset);
			if (!verifiedSignature) {
				throw new HdfException("Superblock didn't contain valid signature");
			}

			// Signature is ok read rest of Superblock
			long fileLocation = offset + 8; // signature is 8 bytes
			FileChannel fc = file.getChannel();

			ByteBuffer header = ByteBuffer.allocate(12);
			header.order(LITTLE_ENDIAN);

			fc.read(header, fileLocation);
			fileLocation += 12;
			header.rewind();

			// Version # of Superblock
			versionOfSuperblock = header.get();
			System.out.println("Version of superblock is = " + versionOfSuperblock);

			if (versionOfSuperblock != 0) {
				throw new HdfException("Only superblock version 0 si currently supported");
			}

			// Version # of File Free-space Storage
			versionNumberOfTheFileFreeSpaceInformation = header.get();
			System.out.println(
					"Version Number of the File Free-Space Information: " + versionNumberOfTheFileFreeSpaceInformation);

			// Version # of Root Group Symbol Table Entry
			versionOfRootGroupSymbolTableEntry = header.get();
			System.out.println("Version # of Root Group Symbol Table Entry: " + versionOfRootGroupSymbolTableEntry);

			// Skip reserved byte
			header.get();

			// Version # of Shared Header Message Format
			versionOfSharedHeaderMessageFormat = header.get();
			System.out.println("Version # of Shared Header Message Format: " + versionOfSharedHeaderMessageFormat);

			// Size of Offsets
			sizeOfOffsets = header.get();
			System.out.println("Size of Offsets: " + sizeOfOffsets);

			// Size of Lengths
			sizeOfLengths = header.get();
			System.out.println("Size of Lengths: " + sizeOfLengths);

			// Skip reserved byte
			header.get();

			// Group Leaf Node K
			groupLeafNodeK = header.getShort();
			System.out.println("groupLeafNodeK = " + groupLeafNodeK);

			// Group Internal Node K
			groupInternalNodeK = header.getShort();
			System.out.println("groupInternalNodeK = " + groupInternalNodeK);

			// File Consistency Flags (skip)
			fileLocation += 4;

			byte[] offsetBytes = new byte[sizeOfOffsets];

			int nextSectionSize = 4 * sizeOfOffsets;
			header = ByteBuffer.allocate(nextSectionSize);
			fc.read(header, fileLocation);
			fileLocation += nextSectionSize;
			header.rewind();

			// Base Address
			header.get(offsetBytes);
			baseAddressByte = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("baseAddressByte = " + baseAddressByte);

			// Address of Global Free-space Index
			header.get(offsetBytes);
			addressOfGlobalFreeSpaceIndex = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("addressOfGlobalFreeSpaceIndex = " + addressOfGlobalFreeSpaceIndex);

			// End of File Address
			header.get(offsetBytes);
			endOfFileAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("endOfFileAddress = " + endOfFileAddress);

			// Driver Information Block Address
			header.get(offsetBytes);
			driverInformationBlockAddress = ByteBuffer.wrap(offsetBytes).order(LITTLE_ENDIAN).getLong();
			System.out.println("driverInformationBlockAddress = " + driverInformationBlockAddress);

			// Root Group Symbol Table Entry Address
			rootGroupSymbolTableAddress = offset + fileLocation;
			System.out.println("rootGroupSymbolTableAddress=" + rootGroupSymbolTableAddress);

		} catch (Exception e) {
			throw new HdfException("Failed to read superbloack", e);
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

	public int getGroupInternalNodeK() {
		return groupInternalNodeK;
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

	public long getRootGroupSymbolTableAddress() {
		return rootGroupSymbolTableAddress;
	}

	/**
	 * Checks if the file provided contains the HDF5 file signature at the given
	 * offset.
	 * 
	 * @param file   The file to test
	 * @param offset The offset into the file where the superblock starts
	 * @return <code>true</code> if signature is matched <code>false</code>
	 *         otherwise
	 */
	static boolean verifySignature(RandomAccessFile file, long offset) {

		FileChannel fc = file.getChannel();

		// Format Signature
		int signatureSize = 8;
		ByteBuffer signatureBuffer = ByteBuffer.allocate(signatureSize);

		try {
			fc.read(signatureBuffer, offset);
		} catch (IOException e) {
			throw new HdfException("Failed to read file", e);
		}
		signatureBuffer.rewind();

		byte[] formatSignitureByte = new byte[8];
		signatureBuffer.get(formatSignitureByte);

		// Verify signature
		return Arrays.equals(HDF5_FILE_SIGNATURE, formatSignitureByte);
	}

}
