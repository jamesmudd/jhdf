package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmudd.jhdf.exceptions.HdfException;

public class Superblock {
	private static final Logger logger = LoggerFactory.getLogger(Superblock.class);

	private static final byte[] HDF5_FILE_SIGNATURE = new byte[] { -119, 72, 68, 70, 13, 10, 26, 10 };
	private static final int HDF5_FILE_SIGNATURE_LENGTH = HDF5_FILE_SIGNATURE.length;

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

	public Superblock(FileChannel fc, long address) {
		try {

			final boolean verifiedSignature = verifySignature(fc, address);
			if (!verifiedSignature) {
				throw new HdfException("Superblock didn't contain valid signature");
			}

			// Signature is ok read rest of Superblock
			long fileLocation = address + HDF5_FILE_SIGNATURE_LENGTH;

			ByteBuffer header = ByteBuffer.allocate(12);
			fc.read(header, fileLocation);
			fileLocation += 12;

			header.order(LITTLE_ENDIAN);
			header.rewind();

			// Version # of Superblock
			versionOfSuperblock = header.get();
			logger.trace("Version of superblock is = {}", versionOfSuperblock);

			if (versionOfSuperblock != 0) {
				throw new HdfException("Only superblock version 0 is currently supported");
			}

			// Version # of File Free-space Storage
			versionNumberOfTheFileFreeSpaceInformation = header.get();
			logger.trace("Version Number of the File Free-Space Information: {}",
					versionNumberOfTheFileFreeSpaceInformation);

			// Version # of Root Group Symbol Table Entry
			versionOfRootGroupSymbolTableEntry = header.get();
			logger.trace("Version # of Root Group Symbol Table Entry: {}", versionOfRootGroupSymbolTableEntry);

			// Skip reserved byte
			header.position(header.position() + 1);

			// Version # of Shared Header Message Format
			versionOfSharedHeaderMessageFormat = header.get();
			logger.trace("Version # of Shared Header Message Format: {}", versionOfSharedHeaderMessageFormat);

			// Size of Offsets
			sizeOfOffsets = Byte.toUnsignedInt(header.get());
			logger.trace("Size of Offsets: {}", sizeOfOffsets);

			// Size of Lengths
			sizeOfLengths = Byte.toUnsignedInt(header.get());
			logger.trace("Size of Lengths: {}", sizeOfLengths);

			// Skip reserved byte
			header.position(header.position() + 1);

			// Group Leaf Node K
			groupLeafNodeK = Short.toUnsignedInt(header.getShort());
			logger.trace("groupLeafNodeK = {}", groupLeafNodeK);

			// Group Internal Node K
			groupInternalNodeK = Short.toUnsignedInt(header.getShort());
			logger.trace("groupInternalNodeK = {}", groupInternalNodeK);

			// File Consistency Flags (skip)
			fileLocation += 4;

			int nextSectionSize = 4 * sizeOfOffsets;
			header = ByteBuffer.allocate(nextSectionSize);
			fc.read(header, fileLocation);
			fileLocation += nextSectionSize;
			header.order(LITTLE_ENDIAN);
			header.rewind();

			// Base Address
			baseAddressByte = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
			logger.trace("baseAddressByte = {}", baseAddressByte);

			// Address of Global Free-space Index
			addressOfGlobalFreeSpaceIndex = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
			logger.trace("addressOfGlobalFreeSpaceIndex = {}", addressOfGlobalFreeSpaceIndex);

			// End of File Address
			endOfFileAddress = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
			logger.trace("endOfFileAddress = {}", endOfFileAddress);

			// Driver Information Block Address
			driverInformationBlockAddress = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
			logger.trace("driverInformationBlockAddress = {}", driverInformationBlockAddress);

			// Root Group Symbol Table Entry Address
			rootGroupSymbolTableAddress = address + fileLocation;
			logger.trace("rootGroupSymbolTableAddress= {}", rootGroupSymbolTableAddress);

		} catch (Exception e) {
			throw new HdfException("Failed to read superblock", e);
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
	 * @param fc     The file to test
	 * @param offset The offset into the file where the superblock starts
	 * @return <code>true</code> if signature is matched <code>false</code>
	 *         otherwise
	 */
	static boolean verifySignature(FileChannel fc, long offset) {

		// Format Signature
		ByteBuffer signatureBuffer = ByteBuffer.allocate(HDF5_FILE_SIGNATURE_LENGTH);

		try {
			fc.read(signatureBuffer, offset);
		} catch (IOException e) {
			throw new HdfException("Failed to read from address: " + Utils.toHex(offset), e);
		}

		// Verify signature
		return Arrays.equals(HDF5_FILE_SIGNATURE, signatureBuffer.array());
	}

}
