/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpSeekableByteChannelTest {

	private static final URL TEST_URL;

	static {
		try {
			TEST_URL = new URL("https://raw.githubusercontent.com/jamesmudd/jhdf/refs/heads/master/README.md");
		} catch (Exception e) {
			throw new RuntimeException("Invalid test URL", e);
		}
	}

	@Test
	void testFetchRemoteSize() throws IOException {
		HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL);
		long size = channel.size();
		HttpURLConnection conn = (HttpURLConnection) TEST_URL.openConnection();
		conn.setRequestMethod("HEAD");
		conn.connect();
		long expectedSize = conn.getContentLengthLong();
		assertEquals(HttpURLConnection.HTTP_OK, conn.getResponseCode());
		conn.disconnect();
		assertEquals(expectedSize, size, "Size should match Content-Length header");
		assertTrue(size > 1e3 && size < 1e6, "Size of README about 1 KB to 1 MB");
		channel.close();
	}

	@Test
	void testReadInitialBytes() throws IOException {
		try (HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL)) {
			ByteBuffer buffer = ByteBuffer.allocate(10);
			int bytesRead = channel.read(buffer);
			assertEquals(10, bytesRead, "Should read 10 bytes initially");
			buffer.flip();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			String content = new String(bytes, StandardCharsets.UTF_8);
			assertTrue(content.startsWith("# jHDF"), "Content should start with '# jHDF'");
		}
	}

	@Test
	void testPositionAndRead() throws IOException {
		try (HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL)) {
			channel.position(3);
			ByteBuffer buffer = ByteBuffer.allocate(5);
			int bytesRead = channel.read(buffer);
			assertEquals(5, bytesRead, "Should read 5 bytes from offset 3");
			buffer.flip();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			String content = new String(bytes, StandardCharsets.UTF_8);
			assertEquals("HDF -", content, "Bytes 3-7 should spell 'HDF -'");
		}
	}

	@Test
	void testReadAcrossBlocks() throws IOException {
		int blockSize = 10;
		int maxCacheBlocks = 2;
		try (HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL, blockSize, maxCacheBlocks)) {
			channel.position(5);
			ByteBuffer buffer = ByteBuffer.allocate(10);
			int bytesRead = channel.read(buffer);
			assertEquals(10, bytesRead, "Should read 10 bytes across block boundary");
			buffer.flip();
			String content = new String(buffer.array(), StandardCharsets.UTF_8);
			assertEquals("F - Pure J", content, "Cross-block read should yield 'F - Pure J'");
		}
	}

	@Test
	void testReadPastEOF() throws IOException {
		try (HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL)) {
			channel.position(channel.size());
			ByteBuffer buffer = ByteBuffer.allocate(10);
			int bytesRead = channel.read(buffer);
			assertEquals(-1, bytesRead, "Reading at EOF should return -1");
		}
	}

	@Test
	void testPositionInvalid() throws IOException {
		try (HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL)) {
			assertThrows(IllegalArgumentException.class, () -> channel.position(-1), "Negative position should throw IllegalArgumentException");
			long fileSize = channel.size();
			assertThrows(IllegalArgumentException.class, () -> channel.position(fileSize + 1), "Position beyond size should throw IllegalArgumentException");
		}
	}

	@Test
	void testUnsupportedOperations() throws IOException {
		try (HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL)) {
			assertThrows(UnsupportedOperationException.class, () -> channel.truncate(10), "truncate() should throw UnsupportedOperationException");
			assertThrows(UnsupportedOperationException.class, () -> channel.write(ByteBuffer.allocate(1)), "write() should throw UnsupportedOperationException");
		}
	}

	@Test
	void testCloseAndIsOpen() throws IOException {
		HttpSeekableByteChannel channel = new HttpSeekableByteChannel(TEST_URL);
		assertTrue(channel.isOpen(), "Channel should be open initially");
		channel.close();
		assertFalse(channel.isOpen(), "Channel should be closed after close()");
		ByteBuffer buffer = ByteBuffer.allocate(1);
		assertThrows(IOException.class, () -> channel.read(buffer), "read() after close should throw IOException");
	}
}
