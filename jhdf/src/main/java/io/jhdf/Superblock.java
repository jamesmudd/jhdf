/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static io.jhdf.Utils.toHex;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public abstract class Superblock {
	private static final Logger logger = LoggerFactory.getLogger(Superblock.class);

	private static final byte[] HDF5_FILE_SIGNATURE = new byte[] { -119, 72, 68, 70, 13, 10, 26, 10 };
	private static final int HDF5_FILE_SIGNATURE_LENGTH = HDF5_FILE_SIGNATURE.length;

	public abstract int getVersionOfSuperblock();

	public abstract int getSizeOfOffsets();

	public abstract int getSizeOfLengths();

	public abstract long getBaseAddressByte();

	public abstract long getEndOfFileAddress();

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
		logger.trace("Verified superblock signature");

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
		logger.debug("Version of superblock is = {}", versionOfSuperblock);

		switch (versionOfSuperblock) {
		case 0:
		case 1:
			return new SuperblockV0V1(fc, fileLocation);
		case 2:
		case 3:
			return new SuperblockV2V3(fc, fileLocation);
		default:
			throw new UnsupportedHdfException(
					"Superblock version is not supported. Detected version = " + versionOfSuperblock);
		}
	}

	public static class SuperblockV0V1 extends Superblock {

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

		private SuperblockV0V1(FileChannel fc, long address) {
			try {

				ByteBuffer header = ByteBuffer.allocate(12);
				fc.read(header, address);
				address += 12;

				header.order(LITTLE_ENDIAN);
				header.rewind();

				// Version # of Superblock
				versionOfSuperblock = header.get();
				logger.trace("Version of superblock is = {}", versionOfSuperblock);

				if (versionOfSuperblock != 0 && versionOfSuperblock != 1) {
					throw new HdfException("Detected superblock version not 0 or 1");
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

				// Version 1
				if (versionOfSuperblock == 1) {
					// Skip Indexed Storage Internal Node K and zeros
					address += 4;
				}

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
				throw new HdfException("Failed to read superblock from address " + toHex(address), e);
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
		public int getVersionNumberOfTheFileFreeSpaceInformation() {
			return versionNumberOfTheFileFreeSpaceInformation;
		}

		/**
		 * @return the versionOfRootGroupSymbolTableEntry
		 */
		public int getVersionOfRootGroupSymbolTableEntry() {
			return versionOfRootGroupSymbolTableEntry;
		}

		/**
		 * @return the versionOfSharedHeaderMessageFormat
		 */
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
		public int getGroupLeafNodeK() {
			return groupLeafNodeK;
		}

		/**
		 * @return the groupInternalNodeK
		 */
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
		public long getDriverInformationBlockAddress() {
			return driverInformationBlockAddress;
		}

		/**
		 * @return the rootGroupSymbolTableAddress
		 */
		public long getRootGroupSymbolTableAddress() {
			return rootGroupSymbolTableAddress;
		}
	}

	public static class SuperblockV2V3 extends Superblock {

		private final int versionOfSuperblock;
		private final int sizeOfOffsets;
		private final int sizeOfLengths;
		private final long baseAddressByte;
		private final long superblockExtensionAddress;
		private final long endOfFileAddress;
		private final long rootGroupObjectHeaderAddress;

		private SuperblockV2V3(FileChannel fc, long address) {
			try {

				ByteBuffer header = ByteBuffer.allocate(4);
				fc.read(header, address);
				address += 4;

				header.order(LITTLE_ENDIAN);
				header.rewind();

				// Version # of Superblock
				versionOfSuperblock = header.get();
				logger.trace("Version of superblock is = {}", versionOfSuperblock);

				// Size of Offsets
				sizeOfOffsets = Byte.toUnsignedInt(header.get());
				logger.trace("Size of Offsets: {}", sizeOfOffsets);

				// Size of Lengths
				sizeOfLengths = Byte.toUnsignedInt(header.get());
				logger.trace("Size of Lengths: {}", sizeOfLengths);

				// TODO File consistency flags

				int nextSectionSize = 4 * sizeOfOffsets + 4;
				header = ByteBuffer.allocate(nextSectionSize);
				fc.read(header, address);
				address += nextSectionSize;
				header.order(LITTLE_ENDIAN);
				header.rewind();

				// Base Address
				baseAddressByte = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
				logger.trace("baseAddressByte = {}", baseAddressByte);

				// Superblock Extension Address
				superblockExtensionAddress = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
				logger.trace("addressOfGlobalFreeSpaceIndex = {}", superblockExtensionAddress);

				if (superblockExtensionAddress != Constants.UNDEFINED_ADDRESS) {
					throw new UnsupportedHdfException("Superblock extension is not supported");
				}

				// End of File Address
				endOfFileAddress = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
				logger.trace("endOfFileAddress = {}", endOfFileAddress);

				// Root Group Object Header Address
				rootGroupObjectHeaderAddress = Utils.readBytesAsUnsignedLong(header, sizeOfOffsets);
				logger.trace("rootGroupObjectHeaderAddress= {}", rootGroupObjectHeaderAddress);

				// TODO Superblock checksum

			} catch (Exception e) {
				throw new HdfException("Failed to read superblock from address " + toHex(address), e);
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
		 * @return the baseAddressByte
		 */
		@Override
		public long getBaseAddressByte() {
			return baseAddressByte;
		}

		/**
		 * @return the superblockExtensionAddress
		 */
		public long getSuperblockExtensionAddress() {
			return superblockExtensionAddress;
		}

		/**
		 * @return the endOfFileAddress
		 */
		@Override
		public long getEndOfFileAddress() {
			return endOfFileAddress;
		}

		/**
		 * @return the rootGroupObjectHeaderAddress
		 */
		public long getRootGroupObjectHeaderAddress() {
			return rootGroupObjectHeaderAddress;
		}
	}
}
