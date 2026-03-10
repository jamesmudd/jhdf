/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.dataset;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.checksum.ChecksumUtils;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.InvalidSliceHdfException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LargeDimensionSliceTest {

	private static final long LARGE_DIMENSION = 3_000_000_000L;

	@Test
	void sliceContiguousDatasetWithLargeDimension() throws IOException {
		Path tempFile = Files.createTempFile("large-dim-test", ".hdf5");
		byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		try (WritableHdfFile writableHdfFile = HdfFile.write(tempFile)) {
			writableHdfFile.putDataset("data", data);
		}

		byte[] fileBytes = Files.readAllBytes(tempFile);
		patchDatasetObjectHeader(fileBytes, LARGE_DIMENSION);

		try (HdfFile hdfFile = HdfFile.fromBytes(fileBytes)) {
			Dataset dataset = hdfFile.getDatasetByPath("data");

			assertThat(dataset.getDimensionsAsLong()).containsExactly(LARGE_DIMENSION);
			assertThatThrownBy(dataset::getDimensions)
				.isInstanceOf(ArithmeticException.class);

			byte[] slice1 = (byte[]) dataset.getData(new long[]{0}, new int[]{5});
			assertThat(slice1).containsExactly(1, 2, 3, 4, 5);

			byte[] slice2 = (byte[]) dataset.getData(new long[]{5}, new int[]{5});
			assertThat(slice2).containsExactly(6, 7, 8, 9, 10);
		}
	}

	@Test
	void sliceChunkedDatasetFromRealFile() {
		try (HdfFile hdfFile = HdfFile.fromInputStream(
				LargeDimensionSliceTest.class.getResourceAsStream("/hdf5/tiny_huge_1d_over_maxint.hdf5"))) {
			Dataset dataset = hdfFile.getDatasetByPath("huge_1d");

			assertThat(dataset.getDimensionsAsLong()).containsExactly(2_147_483_700L);
			assertThatThrownBy(dataset::getDimensions)
				.isInstanceOf(ArithmeticException.class);

			int[] slice = (int[]) dataset.getData(new long[]{2_147_483_655L}, new int[]{8});
			assertThat(slice).containsExactly(11, 22, 33, 44, 55, 66, 77, 88);
		}
	}

	@Test
	void getDataOnLargeDimensionDatasetThrowsHdfException() throws IOException {
		Path tempFile = Files.createTempFile("large-dim-error-test", ".hdf5");
		byte[] data = {1, 2, 3, 4, 5};
		try (WritableHdfFile writableHdfFile = HdfFile.write(tempFile)) {
			writableHdfFile.putDataset("data", data);
		}

		byte[] fileBytes = Files.readAllBytes(tempFile);
		patchDatasetObjectHeader(fileBytes, LARGE_DIMENSION);

		try (HdfFile hdfFile = HdfFile.fromBytes(fileBytes)) {
			Dataset dataset = hdfFile.getDatasetByPath("data");

			assertThatThrownBy(dataset::getData)
				.isInstanceOf(HdfException.class)
				.hasMessageContaining("Integer.MAX_VALUE")
				.hasMessageContaining("slices");

			assertThatThrownBy(dataset::getDataFlat)
				.isInstanceOf(HdfException.class)
				.hasMessageContaining("Integer.MAX_VALUE")
				.hasMessageContaining("slices");
		}
	}

	@Test
	void getDataOnLargeChunkedDatasetThrowsHdfException() {
		try (HdfFile hdfFile = HdfFile.fromInputStream(
				LargeDimensionSliceTest.class.getResourceAsStream("/hdf5/tiny_huge_1d_over_maxint.hdf5"))) {
			Dataset dataset = hdfFile.getDatasetByPath("huge_1d");

			assertThatThrownBy(dataset::getData)
				.isInstanceOf(HdfException.class)
				.hasMessageContaining("Integer.MAX_VALUE")
				.hasMessageContaining("slices");

			assertThatThrownBy(dataset::getDataFlat)
				.isInstanceOf(HdfException.class)
				.hasMessageContaining("Integer.MAX_VALUE")
				.hasMessageContaining("slices");
		}
	}

	@Test
	void overflowingSliceOffsetIsRejected() throws IOException {
		Path tempFile = Files.createTempFile("large-dim-overflow-slice-test", ".hdf5");
		byte[] data = {1, 2, 3, 4, 5};
		try (WritableHdfFile writableHdfFile = HdfFile.write(tempFile)) {
			writableHdfFile.putDataset("data", data);
		}

		byte[] fileBytes = Files.readAllBytes(tempFile);
		patchDatasetObjectHeader(fileBytes, Long.MAX_VALUE);

		try (HdfFile hdfFile = HdfFile.fromBytes(fileBytes)) {
			Dataset dataset = hdfFile.getDatasetByPath("data");

			assertThatThrownBy(() -> dataset.getData(new long[]{Long.MAX_VALUE - 5}, new int[]{10}))
				.isInstanceOf(InvalidSliceHdfException.class)
				.hasMessageContaining("Requested slice exceeds dataset");
		}
	}

	private static void patchDatasetObjectHeader(byte[] file, long newDimension) {
		int ohdrStart = findNthOhdr(file, 2);

		int flags = file[ohdrStart + 5] & 0xFF;
		int chunkSizeWidth = 1 << (flags & 0x03);
		int chunkSizeOffset = ohdrStart + 6;

		long chunk0Size = readLittleEndian(file, chunkSizeOffset, chunkSizeWidth);

		int msgStart = chunkSizeOffset + chunkSizeWidth;
		int msgEnd = msgStart + (int) chunk0Size;

		int pos = msgStart;
		while (pos + 4 <= msgEnd) {
			int msgType = file[pos] & 0xFF;
			int msgDataSize = (file[pos + 1] & 0xFF) | ((file[pos + 2] & 0xFF) << 8);
			int msgDataStart = pos + 4;

			if (msgType == 1) {
				writeLittleEndianLong(file, msgDataStart + 4, newDimension);
			} else if (msgType == 8) {
				writeLittleEndianLong(file, msgDataStart + 10, newDimension);
			}

			pos = msgDataStart + msgDataSize;
		}

		int checksumRegionLength = msgEnd - ohdrStart;
		byte[] checksumRegion = new byte[checksumRegionLength];
		System.arraycopy(file, ohdrStart, checksumRegion, 0, checksumRegionLength);
		int checksum = ChecksumUtils.checksum(checksumRegion);
		writeLittleEndianInt(file, msgEnd, checksum);
	}

	private static int findNthOhdr(byte[] file, int n) {
		int count = 0;
		for (int i = 0; i <= file.length - 4; i++) {
			if (file[i] == 'O' && file[i + 1] == 'H' && file[i + 2] == 'D' && file[i + 3] == 'R') {
				count++;
				if (count == n) {
					return i;
				}
			}
		}
		throw new IllegalStateException("Could not find OHDR signature #" + n);
	}

	private static long readLittleEndian(byte[] data, int offset, int width) {
		long value = 0;
		for (int i = 0; i < width; i++) {
			value |= (long) (data[offset + i] & 0xFF) << (i * 8);
		}
		return value;
	}

	private static void writeLittleEndianLong(byte[] data, int offset, long value) {
		for (int i = 0; i < 8; i++) {
			data[offset + i] = (byte) (value >>> (i * 8));
		}
	}

	private static void writeLittleEndianInt(byte[] data, int offset, int value) {
		data[offset] = (byte) value;
		data[offset + 1] = (byte) (value >>> 8);
		data[offset + 2] = (byte) (value >>> 16);
		data[offset + 3] = (byte) (value >>> 24);
	}
}
