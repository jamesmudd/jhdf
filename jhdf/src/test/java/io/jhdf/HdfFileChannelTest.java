/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2026 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.HdfArithmeticException;
import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfFileChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HdfFileChannelTest {

	@Mock
	FileChannel fc;

	@Mock
	Superblock sb;

	// Under test
	private HdfFileChannel hdfFc;

	@BeforeEach
	void before() {
		// Setup mocks
		MockitoAnnotations.openMocks(this);
		Mockito.when(sb.getBaseAddressByte()).thenReturn(0L);

		// Setup test object
		hdfFc = new HdfFileChannel(fc, sb);
	}

	@SuppressWarnings("SameReturnValue")
	@Test
	void testReadingBuffer() throws IOException {

		Mockito.doAnswer(invocation -> {
			ByteBuffer bb = invocation.getArgument(0);
			bb.rewind();
			bb.put("TEST".getBytes(US_ASCII)); // Put test data in buffer
			return 4;
		}).when(fc).read(any(ByteBuffer.class), eq(3L));
		// The read ends exactly at the end of the file
		when(fc.size()).thenReturn(7L);

		// Read 4 bytes
		ByteBuffer bb = hdfFc.readBufferFromAddress(3, 4);
		assertThat(bb.capacity(), is(equalTo(4)));
		assertThat(US_ASCII.decode(bb).toString(), is(equalTo("TEST")));

	}

	@Test
	void testReadingBufferWithShortReads() throws IOException {
		when(fc.size()).thenReturn(20L);

		Mockito.doAnswer(invocation -> {
			ByteBuffer bb = invocation.getArgument(0);
			bb.put("HE".getBytes(US_ASCII));
			return 2;
		}).when(fc).read(any(ByteBuffer.class), eq(5L));

		Mockito.doAnswer(invocation -> {
			ByteBuffer bb = invocation.getArgument(0);
			bb.put("LL".getBytes(US_ASCII));
			return 2;
		}).when(fc).read(any(ByteBuffer.class), eq(7L));

		Mockito.doAnswer(invocation -> {
			ByteBuffer bb = invocation.getArgument(0);
			bb.put("O".getBytes(US_ASCII));
			return 1;
		}).when(fc).read(any(ByteBuffer.class), eq(9L));

		ByteBuffer bb = hdfFc.readBufferFromAddress(5, 5);
		assertThat(bb.capacity(), is(equalTo(5)));
		assertThat(US_ASCII.decode(bb).toString(), is(equalTo("HELLO")));
	}

	@Test
	void testReadingMoreBytesThanTheFileContainsThrows() throws IOException {
		when(fc.size()).thenReturn(100L);

		// A length field read from a corrupt file must not be used to size an allocation
		assertThrows(HdfException.class, () -> hdfFc.readBufferFromAddress(0, 1000));

		// Crucially nothing was read, and with the fix nothing was allocated either
		Mockito.verify(fc, Mockito.never()).read(any(ByteBuffer.class), anyLong());
	}

	@Test
	void testReadingPastTheEndOfTheFileThrows() throws IOException {
		when(fc.size()).thenReturn(100L);

		assertThrows(HdfException.class, () -> hdfFc.readBufferFromAddress(96, 5));

		Mockito.verify(fc, Mockito.never()).read(any(ByteBuffer.class), anyLong());
	}

	@Test
	void testReadingAtANegativeAddressThrows() throws IOException {
		when(fc.size()).thenReturn(100L);

		assertThrows(HdfException.class, () -> hdfFc.readBufferFromAddress(-1, 5));

		Mockito.verify(fc, Mockito.never()).read(any(ByteBuffer.class), anyLong());
	}

	@Test
	void testReadingAtAnAddressWhichOverflowsThrows() throws IOException {
		when(fc.size()).thenReturn(100L);
		when(sb.getBaseAddressByte()).thenReturn(Long.MAX_VALUE);

		// address + base address overflows, exact arithmetic detects this before the bounds check
		assertThrows(HdfArithmeticException.class, () -> hdfFc.readBufferFromAddress(1, 5));

		Mockito.verify(fc, Mockito.never()).read(any(ByteBuffer.class), anyLong());
	}

	@Test
	void testReadingAtANegativeRawAddressThrows() throws IOException {
		when(fc.size()).thenReturn(100L);

		// mapNoOffset can be called directly so a negative address must still be rejected by the bounds check
		assertThrows(HdfException.class, () -> hdfFc.mapNoOffset(-1, 5));

		Mockito.verify(fc, Mockito.never()).map(any(MapMode.class), anyLong(), anyLong());
	}

	@Test
	void testReadingChannelThrows() throws IOException {
		when(fc.size()).thenReturn(100L);
		when(fc.read(any(ByteBuffer.class), anyLong())).thenThrow(IOException.class);

		assertThrows(HdfException.class, () -> hdfFc.readBufferFromAddress(3, 4));
	}

	@Test
	@DisabledForJreRange(min = JRE.JAVA_17)
	void testMap() throws IOException {
		MappedByteBuffer mockMappedByteBuffer = mock(MappedByteBuffer.class);
		when(fc.map(any(MapMode.class), anyLong(), anyLong())).thenReturn(mockMappedByteBuffer);
		when(fc.size()).thenReturn(100L);

		assertThat(hdfFc.map(20, 10), is(sameInstance(mockMappedByteBuffer)));
	}

	@Test
	void testMapThrows() throws IOException {
		when(fc.map(any(MapMode.class), anyLong(), anyLong())).thenThrow(IOException.class);
		when(fc.size()).thenReturn(100L);

		assertThrows(HdfException.class, () -> hdfFc.map(20, 10));
	}

	@Test
	void testMapBeyondTheEndOfTheFileThrows() throws IOException {
		when(fc.size()).thenReturn(100L);

		assertThrows(HdfException.class, () -> hdfFc.map(96, 5));

		Mockito.verify(fc, Mockito.never()).map(any(MapMode.class), anyLong(), anyLong());
	}

	@Test
	void testMapReadWriteBeyondTheEndOfTheFileIsAllowed() throws IOException {
		when(fc.size()).thenReturn(100L);

		// Mapping beyond the end of the file is a valid way to grow it, only read only mappings are bounds checked
		hdfFc.mapNoOffset(96, 5, MapMode.READ_WRITE);

		Mockito.verify(fc).map(MapMode.READ_WRITE, 96, 5);
		Mockito.verify(fc, Mockito.never()).size();
	}

	@Test
	void testSize() throws IOException {
		when(fc.size()).thenReturn(12345L);
		assertThat(hdfFc.size(), is(equalTo(12345L)));
	}

	@Test
	void testSizeThrows() throws IOException {
		when(fc.size()).thenThrow(IOException.class);
		assertThrows(HdfException.class, () -> hdfFc.size());
	}

	@Test
	void testGetSuperblock() {
		assertThat(hdfFc.getSuperblock(), is(sameInstance(sb)));
	}

	@Test
	void testGetSizeOfOffsets() {
		when(sb.getSizeOfOffsets()).thenReturn(1);
		assertThat(hdfFc.getSizeOfOffsets(), is(equalTo(1)));
	}

	@Test
	void testGetSizeOfLengths() {
		when(sb.getSizeOfLengths()).thenReturn(1);
		assertThat(hdfFc.getSizeOfLengths(), is(equalTo(1)));
	}

	@Test
	void testGetUserBlockSize() {
		when(sb.getBaseAddressByte()).thenReturn(1L);
		assertThat(hdfFc.getUserBlockSize(), is(equalTo(1L)));
	}
}
