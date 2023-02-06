/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.exceptions.HdfException;
import io.jhdf.storage.HdfFileChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

		// Read 4 bytes
		ByteBuffer bb = hdfFc.readBufferFromAddress(3, 4);
		assertThat(bb.capacity(), is(equalTo(4)));
		assertThat(US_ASCII.decode(bb).toString(), is(equalTo("TEST")));

	}

	@Test
	void testReadingChannelThrows() throws IOException {
		when(fc.read(any(ByteBuffer.class), anyLong())).thenThrow(IOException.class);

		assertThrows(HdfException.class, () -> hdfFc.readBufferFromAddress(3, 4));
	}

	@Test
	void testMap() throws IOException {
		MappedByteBuffer mockMappedByteBuffer = mock(MappedByteBuffer.class);
		when(fc.map(any(MapMode.class), anyLong(), anyLong())).thenReturn(mockMappedByteBuffer);

		// Force to always map
		System.setProperty("io.jhdf.storage.memoryMapMinSizeBytes", "0");
		HdfFileChannel hdfFcAlwaysMap = new HdfFileChannel(fc, sb);

		assertThat(hdfFcAlwaysMap.map(20, 10), is(sameInstance(mockMappedByteBuffer)));

		System.clearProperty("io.jhdf.storage.memoryMapMinSizeBytes"); // Reset property
	}

	@Test
	void testNotMappedWhenSmallRead() throws IOException {
		MappedByteBuffer mockMappedByteBuffer = mock(MappedByteBuffer.class);
		when(fc.map(any(MapMode.class), anyLong(), anyLong())).thenReturn(mockMappedByteBuffer);
		when(fc.read(any(ByteBuffer.class), anyLong())).thenReturn(15);

		// Force to always map
		System.setProperty("io.jhdf.storage.memoryMapMinSizeBytes", "20");
		HdfFileChannel hdfFileChannel = new HdfFileChannel(fc, sb);

		// Read a size below the min map size
		hdfFileChannel.map(23, 15);
		verify(fc, times(1)).read(any(ByteBuffer.class), anyLong());
		verifyNoMoreInteractions(fc);

		// Now read a size larger than min map so should be mapped
		hdfFileChannel.map(23, 25);
		verify(fc, times(1)).map(any(MapMode.class), anyLong(), anyLong());
		verifyNoMoreInteractions(fc);

		System.clearProperty("io.jhdf.storage.memoryMapMinSizeBytes"); // Reset property
	}

	@Test
	void testReadingMultipleTimesWorks() throws IOException {
		// Only read 5 bytes each time
		when(fc.read(any(ByteBuffer.class), anyLong())).thenReturn(5);

		// Force to always map
		System.setProperty("io.jhdf.storage.memoryMapMinSizeBytes", "20");
		HdfFileChannel hdfFileChannel = new HdfFileChannel(fc, sb);

		// Read a size below the min map size
		hdfFileChannel.map(23, 15);
		// Should be called 3 times 3*5=15
		verify(fc, times(3)).read(any(ByteBuffer.class), anyLong());
		verifyNoMoreInteractions(fc);

		System.clearProperty("io.jhdf.storage.memoryMapMinSizeBytes"); // Reset property
	}

	@Test
	void testMapThrows() throws IOException {
		when(fc.map(any(MapMode.class), anyLong(), anyLong())).thenThrow(IOException.class);

		assertThrows(HdfException.class, () -> hdfFc.map(20, 10));
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
