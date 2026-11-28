/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.filter;

import io.jhdf.checksum.Fletcher32;
import io.jhdf.exceptions.HdfChecksumMismatchException;
import io.jhdf.exceptions.HdfFilterException;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@link FletcherChecksumFilter}.
 * <p>
 * The encoded test data is taken from real HDF5 files written by h5py (which
 * uses the HDF5 C library) with the 'fletcher32' filter, so the checksums act as
 * a reference against an independent third-party implementation.
 */
class FletcherChecksumFilterTest {

	private final FletcherChecksumFilter filter = new FletcherChecksumFilter();

	@Test
	void decodeVerifiesChecksumAndReturnsDataWithoutChecksum() {
		// Raw chunk (data + 4 byte little-endian checksum) from a h5py written file
		String encodedHex = "000000000100000002000000050000000600000007000000" + "0015005e";
		String dataHex = "000000000100000002000000050000000600000007000000";

		byte[] decoded = filter.decode(HexFormat.of().parseHex(encodedHex), new int[0]);

		assertThat(decoded).isEqualTo(HexFormat.of().parseHex(dataHex));
	}

	@Test
	void decodeVerifiesChecksumForOddLengthData() {
		// Raw chunk (data + 4 byte little-endian checksum) from a h5py written file
		String encodedHex = "000102030405060708090a0b0c" + "242a7f70";

		byte[] decoded = filter.decode(HexFormat.of().parseHex(encodedHex), new int[0]);

		assertThat(decoded).isEqualTo(HexFormat.of().parseHex("000102030405060708090a0b0c"));
	}

	@Test
	void decodeThrowsHdfChecksumMismatchExceptionWhenChecksumIsIncorrect() {
		// Data with a corrupted checksum
		String encodedHex = "000000000100000002000000050000000600000007000000" + "00000000";

		assertThatThrownBy(() -> filter.decode(HexFormat.of().parseHex(encodedHex), new int[0]))
			.isInstanceOf(HdfChecksumMismatchException.class)
			.hasMessageContaining("Checksum mismatch");
	}

	@Test
	void decodeThrowsHdfChecksumMismatchExceptionWhenDataIsCorrupted() {
		// Data with a corrupted byte but the original checksum
		String encodedHex = "0000000001000000020000000a0000000600000007000000" + "0015005e";

		assertThatThrownBy(() -> filter.decode(HexFormat.of().parseHex(encodedHex), new int[0]))
			.isInstanceOf(HdfChecksumMismatchException.class);
	}

	@Test
	void decodeAcceptsChecksumWrittenByPreV163Hdf5Library() {
		// Before v1.6.3 the checksum could be byte-swapped. The stored checksum is
		// the reversed word order of the correct checksum 0x5e001500.
		String encodedHex = "000000000100000002000000050000000600000007000000" + "15005e00";

		byte[] decoded = filter.decode(HexFormat.of().parseHex(encodedHex), new int[0]);

		assertThat(decoded).isEqualTo(HexFormat.of().parseHex("000000000100000002000000050000000600000007000000"));
	}

	@Test
	void decodeThrowsHdfFilterExceptionWhenBufferIsTooShort() {
		assertThatThrownBy(() -> filter.decode(new byte[]{1, 2, 3, 4}, new int[0]))
			.isInstanceOf(HdfFilterException.class)
			.hasMessageContaining("too short");
		assertThatThrownBy(() -> filter.decode(new byte[0], new int[0]))
			.isInstanceOf(HdfFilterException.class);
	}

	@Test
	void encodeAppendsChecksumOfData() {
		String dataHex = "000102030405060708090a0b0c";

		byte[] encoded = filter.encode(HexFormat.of().parseHex(dataHex), new int[0]);

		// The data is unchanged with the 4 byte little-endian checksum appended
		assertThat(encoded).isEqualTo(HexFormat.of().parseHex(dataHex + "242a7f70"));
	}

	@Test
	void encodeDecodeRoundTrip() {
		String dataHex = "000102030405060708090a0b0c";

		byte[] data = HexFormat.of().parseHex(dataHex);
		byte[] encoded = filter.encode(data, new int[0]);

		assertThat(encoded).hasSize(data.length + 4);
		assertThat(encoded[data.length])
			.isEqualTo((byte) Fletcher32.checksum(data))
			.isEqualTo((byte) 0x24);
		assertThat(filter.decode(encoded, new int[0])).isEqualTo(data);
	}

	@Test
	void encodeDecodeRoundTripForOddLengthData() {
		byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

		byte[] encoded = filter.encode(data, new int[0]);

		assertThat(filter.decode(encoded, new int[0])).isEqualTo(data);
	}

	@Test
	void getIdAndName() {
		assertThat(filter.getId()).isEqualTo(3);
		assertThat(filter.getName()).isEqualTo("fletcher32");
	}
}
