/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.checksum;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link Fletcher32} checksum.
 * <p>
 * The expected checksum values are taken from real HDF5 files written by h5py
 * (which uses the HDF5 C library) with the 'fletcher32' filter, so they act as a
 * reference against an independent third-party implementation.
 */
class Fletcher32Test {

	private static int checksumOfHex(String hex) {
		return Fletcher32.checksum(HexFormat.of().parseHex(hex));
	}

	@Test
	void checksumMatchesHdf5ReferenceForInt32ChunkData() {
		// Raw chunk data (24 bytes) and checksum from a h5py written file
		String dataHex = "000000000100000002000000050000000600000007000000";
		assertThat(checksumOfHex(dataHex)).isEqualTo(0x5e001500);
	}

	@Test
	void checksumMatchesHdf5ReferenceForInt16ChunkData() {
		// Raw chunk data (14 bytes) and checksum from a h5py written file
		String dataHex = "0000010002000300040005000600";
		assertThat(checksumOfHex(dataHex)).isEqualTo(0x38001500);
	}

	@Test
	void checksumMatchesHdf5ReferenceForOddLengthChunkData() {
		// Raw chunk data (13 bytes, odd length) and checksum from a h5py written file
		String dataHex = "000102030405060708090a0b0c";
		assertThat(checksumOfHex(dataHex)).isEqualTo(0x707f2a24);
	}

	@Test
	void checksumOfEmptyDataIsZero() {
		assertThat(Fletcher32.checksum(new byte[0])).isZero();
	}

	@Test
	void checksumOfLargeDataMatchesReference() {
		// The int32 chunk data repeated 54 times (1296 bytes). This is long enough
		// to exercise the 360 word reduction blocks of the algorithm.
		byte[] base = HexFormat.of().parseHex("000000000100000002000000050000000600000007000000");
		byte[] data = new byte[base.length * 54]; // 54 repeats = 1296 bytes
		for (int i = 0; i < 54; i++) {
			System.arraycopy(base, 0, data, i * base.length, base.length);
		}
		// Reference computed with the HDF5 C implementation via h5py
		assertThat(Fletcher32.checksum(data)).isEqualTo(0x7d946e04);
	}

	@Test
	void checksumOfByteBufferMatchesByteArray() {
		String dataHex = "000000000100000002000000050000000600000007000000";
		byte[] bytes = HexFormat.of().parseHex(dataHex);

		assertThat(Fletcher32.checksum(ByteBuffer.wrap(bytes))).isEqualTo(Fletcher32.checksum(bytes));
	}

	@Test
	void checksumOfByteBufferDoesNotModifyPosition() {
		byte[] bytes = HexFormat.of().parseHex("000000000100000002000000050000000600000007000000");
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.position(4);

		int checksum = Fletcher32.checksum(buffer);

		assertThat(buffer.position()).isEqualTo(4);
		assertThat(checksum).isEqualTo(Fletcher32.checksum(bytes, 4, bytes.length - 4));
	}

	@Test
	void checksumOfSubArrayMatchesReference() {
		// The 13 byte odd length chunk data with a 2 byte prefix and 2 byte suffix
		byte[] data = HexFormat.of().parseHex("1234000102030405060708090a0b0c5678");
		assertThat(Fletcher32.checksum(data, 2, 13)).isEqualTo(0x707f2a24);
	}
}
