package com.jamesmudd.jhdf;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamesmudd.jhdf.exceptions.HdfException;
import com.jamesmudd.jhdf.exceptions.UnsupportedHdfException;

public abstract class Superblock {
	private static final Logger logger = LoggerFactory.getLogger(Superblock.class);

	private static final byte[] HDF5_FILE_SIGNATURE = new byte[] { -119, 72, 68, 70, 13, 10, 26, 10 };
	private static final int HDF5_FILE_SIGNATURE_LENGTH = HDF5_FILE_SIGNATURE.length;

	public abstract int getVersionOfSuperblock();

	public abstract int getVersionNumberOfTheFileFreeSpaceInformation();

	public abstract int getVersionOfRootGroupSymbolTableEntry();

	public abstract int getVersionOfSharedHeaderMessageFormat();

	public abstract int getSizeOfOffsets();

	public abstract int getSizeOfLengths();

	public abstract int getGroupLeafNodeK();

	public abstract int getGroupInternalNodeK();

	public abstract long getBaseAddressByte();

	public abstract long getAddressOfGlobalFreeSpaceIndex();

	public abstract long getEndOfFileAddress();

	public abstract long getDriverInformationBlockAddress();

	public abstract long getRootGroupSymbolTableAddress();

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

	public static Superblock readSuperblock(FileChannel fc, long address) {

		final boolean verifiedSignature = verifySignature(fc, address);
		if (!verifiedSignature) {
			throw new HdfException("Superblock didn't contain valid signature");
		}

		// Signature is ok read rest of Superblock
		long fileLocation = address + HDF5_FILE_SIGNATURE_LENGTH;

		ByteBuffer version = ByteBuffer.allocate(1);
		try {
			fc.read(version, fileLocation);
		} catch (IOException e) {
			throw new HdfException("Failed to read superblock at address = " + Utils.toHex(address));
		}
		version.rewind();

		// Version # of Superblock
		final byte versionOfSuperblock = version.get();
		logger.trace("Version of superblock is = {}", versionOfSuperblock);

		switch (versionOfSuperblock) {
		case 0:
		case 1:
			return new SuperblockV0V1(fc, fileLocation);

		default:
			throw new UnsupportedHdfException(
					"Superblock version is not supported. Detected version = " + versionOfSuperblock);
		}
	}

	private static class SuperblockV0V1 extends Superblock {

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

		public SuperblockV0V1(FileChannel fc, long address) {
			try {

				ByteBuffer header = ByteBuffer.allocate(12);
				fc.read(header, address);
				address += 12;

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
				address += 4;

				// TODO for version 1 Indexed Storage Internal Node K

				int nextSectionSize = 4 * sizeOfOffsets;
				header = ByteBuffer.allocate(nextSectionSize);
				fc.read(header, address);
				address += nextSectionSize;
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
				rootGroupSymbolTableAddress = address;
				logger.trace("rootGroupSymbolTableAddress= {}", rootGroupSymbolTableAddress);

			} catch (Exception e) {
				throw new HdfException("Failed to read superblock", e);
			}

		}

		/**
		 * @return the versionOfSuperblock
		 */
		@Override
		public int getVersionOfSuperblock() {
			return versionOfSuperblock;
		}

		/**
		 * @return the versionNumberOfTheFileFreeSpaceInformation
		 */
		@Override
		public int getVersionNumberOfTheFileFreeSpaceInformation() {
			return versionNumberOfTheFileFreeSpaceInformation;
		}

		/**
		 * @return the versionOfRootGroupSymbolTableEntry
		 */
		@Override
		public int getVersionOfRootGroupSymbolTableEntry() {
			return versionOfRootGroupSymbolTableEntry;
		}

		/**
		 * @return the versionOfSharedHeaderMessageFormat
		 */
		@Override
		public int getVersionOfSharedHeaderMessageFormat() {
			return versionOfSharedHeaderMessageFormat;
		}

		/**
		 * @return the sizeOfOffsets
		 */
		@Override
		public int getSizeOfOffsets() {
			return sizeOfOffsets;
		}

		/**
		 * @return the sizeOfLengths
		 */
		@Override
		public int getSizeOfLengths() {
			return sizeOfLengths;
		}

		/**
		 * @return the groupLeafNodeK
		 */
		@Override
		public int getGroupLeafNodeK() {
			return groupLeafNodeK;
		}

		/**
		 * @return the groupInternalNodeK
		 */
		@Override
		public int getGroupInternalNodeK() {
			return groupInternalNodeK;
		}

		/**
		 * @return the baseAddressByte
		 */
		@Override
		public long getBaseAddressByte() {
			return baseAddressByte;
		}

		/**
		 * @return the addressOfGlobalFreeSpaceIndex
		 */
		@Override
		public long getAddressOfGlobalFreeSpaceIndex() {
			return addressOfGlobalFreeSpaceIndex;
		}

		/**
		 * @return the endOfFileAddress
		 */
		@Override
		public long getEndOfFileAddress() {
			return endOfFileAddress;
		}

		/**
		 * @return the driverInformationBlockAddress
		 */
		@Override
		public long getDriverInformationBlockAddress() {
			return driverInformationBlockAddress;
		}

		/**
		 * @return the rootGroupSymbolTableAddress
		 */
		@Override
		public long getRootGroupSymbolTableAddress() {
			return rootGroupSymbolTableAddress;
		}
	}
}
