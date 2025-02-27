/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.nio;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests the {@link FileChannelFromSeekableByteChannel} implementation. In all tests, read operations are performed
 * on the {@link FileChannel} of {@link #testFileToRead}, whereas write operations are performed on the
 * {@code FileChannel} of {@link #testFileToWrite}. The initial bytes of {@code testFileToRead} are given by
 * {@link #TEST_BYTES_TO_READ}, whereas the initial bytes of {@code testFileToWrite} are given by
 * {@link #INITIAL_CONTENT_FILE_TO_WRITE}. Whenever we write bytes, they are either from {@code testFileToRead} or,
 * when we explicitly specify bytes, they are given by {@link #TEST_BYTES_TO_WRITE}.
 */
public class FileChannelFromSeekableByteChannelTest
{
	private static final String	FILE_NAME_TO_READ				= "read_test";
	private static final String	FILE_NAME_TO_WRITE				= "write_test";
	private static final byte[] TEST_BYTES_TO_READ				= {9, 4, 8, 5, 0, 4, 7, 4, 6, 4};
	private static final byte[] TEST_BYTES_TO_WRITE				= {0, 8, 3, 3, 2, 6, 0, 4, 5, 7};
	private static final byte[] INITIAL_CONTENT_FILE_TO_WRITE	= {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

	private static final NamedInt		NO_TRANSFER_LIMIT			= new NamedInt("unlimited byte transfer", 0);
	private static final NamedInt		TWO_BYTE_TRANSFER_LIMIT		= new NamedInt("transfer limited to 2 bytes", 2);
	@SuppressWarnings("unused")
	private static final List<NamedInt>	TRANSFER_LIMITS				= Arrays.asList(NO_TRANSFER_LIMIT, TWO_BYTE_TRANSFER_LIMIT);

	private FileSystem	testFileSystem;
	private Path		testFileToRead;
	private Path		testFileToWrite;

	@BeforeEach
	void setUpFileSystem() throws IOException {
		Configuration configurationWithoutFileChannel = Configuration.unix()
			.toBuilder()
			.setSupportedFeatures()
			.build();
		testFileSystem = Jimfs.newFileSystem("FS", configurationWithoutFileChannel);
		testFileToRead = testFileSystem.getPath(FILE_NAME_TO_READ);
		testFileToWrite = testFileSystem.getPath(FILE_NAME_TO_WRITE);
		Files.write(testFileToRead, TEST_BYTES_TO_READ);
		Files.write(testFileToWrite, INITIAL_CONTENT_FILE_TO_WRITE);
	}

	@AfterEach
	void shutDownFileSystem() throws IOException {
		testFileSystem.close();
		testFileToRead = null;
		testFileToWrite = null;
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testRead(NamedInt maxBytesToTransfer) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(3);
		try (FileChannel channel = openReadableChannel(maxBytesToTransfer)) {
			long expectedPos = 0;
			while (expectedPos < TEST_BYTES_TO_READ.length) {
				assertThat(channel.position(), is(expectedPos));

				int maxBytesToRead = buffer.remaining();
				int bytesRead = channel.read(buffer);
				assertThat(bytesRead, lessThanOrEqualTo(maxBytesToRead));
				assertThat("Unexpected end of channel", bytesRead, greaterThan(-1));

				buffer.flip();

				assertThat(toBytes(buffer), is(subBytes(TEST_BYTES_TO_READ, expectedPos, expectedPos + bytesRead)));
				expectedPos += bytesRead;
			}
			assertThat(channel.position(), is(expectedPos));

			int bytesRead = channel.read(buffer);
			assertThat("The channel end should have been reached", bytesRead, is(-1));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testReadAtPosition(NamedInt maxBytesToTransfer) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(3);
		try (FileChannel channel = openReadableChannel(maxBytesToTransfer)) {
			long expectedPos = 0;
			long startPos = 2;
			while (startPos < TEST_BYTES_TO_READ.length) {
				int maxBytesToRead = buffer.remaining();
				int bytesRead = channel.read(buffer, startPos);
				assertThat(bytesRead, lessThanOrEqualTo(maxBytesToRead));
				assertThat("Unexpected end of channel", bytesRead, greaterThan(-1));

				buffer.flip();

				assertThat(toBytes(buffer), is(subBytes(TEST_BYTES_TO_READ, startPos, startPos + bytesRead)));
				assertThat(channel.position(), is(expectedPos));

				channel.position(startPos);
				expectedPos = startPos;

				startPos += bytesRead;
			}
			int bytesRead = channel.read(buffer, startPos);
			assertThat("The channel end should have been reached", bytesRead, is(-1));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testReadIntoMultipleBuffersLessThanAvailable(NamedInt maxBytesToTransfer) throws IOException {
		final long maxAvailableBytes = TEST_BYTES_TO_READ.length;

		int[] bufferCapacities = {1, 2, 4, 3};
		ByteBuffer[] buffers = Arrays.stream(bufferCapacities)
				.mapToObj(ByteBuffer::allocate)
				.toArray(ByteBuffer[]::new);
		try (FileChannel channel = openReadableChannel(maxBytesToTransfer)) {
			long startPos = 2;
			channel.position(startPos);

			assertThat("Invalid test setup", startPos + bufferCapacities[1] + bufferCapacities[2], lessThanOrEqualTo(maxAvailableBytes));

			// fill buffer[1] and buffer[2], starting at position startPos
			long bytesRead = readFully(channel, buffers, 1, 2);
			Arrays.stream(buffers).forEach(ByteBuffer::flip);

			long expectedPos1 = startPos + buffers[1].capacity();
			long expectedPos2 = expectedPos1 + buffers[2].capacity();

			assertThat(bytesRead, is((long) bufferCapacities[1] + (long) bufferCapacities[2]));
			assertThat(channel.position(), is(expectedPos2));

			assertThat(buffers[0].remaining(), is(0));	// buffer 0 not filled
			assertThat(toBytes(buffers[1]), is(subBytes(TEST_BYTES_TO_READ, startPos, expectedPos1)));
			assertThat(toBytes(buffers[2]), is(subBytes(TEST_BYTES_TO_READ, expectedPos1, expectedPos2)));
			assertThat(buffers[3].remaining(), is(0));	// buffer 3 not filled
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testReadIntoMultipleBuffersAllAvailable(NamedInt maxBytesToTransfer) throws IOException {
		final long maxAvailableBytes = TEST_BYTES_TO_READ.length;

		int[] bufferCapacities = {1, 3, 2, 4, 5};
		ByteBuffer[] buffers = Arrays.stream(bufferCapacities)
				.mapToObj(ByteBuffer::allocate)
				.toArray(ByteBuffer[]::new);
		try (FileChannel channel = openReadableChannel(maxBytesToTransfer)) {
			long startPos = 1;
			channel.position(startPos);

			assertThat("Invalid test setup", startPos + bufferCapacities[2] + bufferCapacities[3], lessThanOrEqualTo(maxAvailableBytes));
			assertThat("Invalid test setup", startPos + bufferCapacities[2] + bufferCapacities[3] + bufferCapacities[4], greaterThanOrEqualTo(maxAvailableBytes));

			// fill buffer[2], buffer[3], and buffer[4], starting at position startPos
			long bytesRead = readFully(channel, buffers, 2, 3);
			Arrays.stream(buffers).forEach(ByteBuffer::flip);

			long expectedPos1 = startPos + buffers[2].capacity();
			long expectedPos2 = expectedPos1 + buffers[3].capacity();

			assertThat(bytesRead, is(maxAvailableBytes - startPos));
			assertThat(channel.position(), is(maxAvailableBytes));

			assertThat(buffers[0].remaining(), is(0));	// buffer 0 not filled
			assertThat(buffers[1].remaining(), is(0));	// buffer 1 not filled
			assertThat(toBytes(buffers[2]), is(subBytes(TEST_BYTES_TO_READ, startPos, expectedPos1)));
			assertThat(toBytes(buffers[3]), is(subBytes(TEST_BYTES_TO_READ, expectedPos1, expectedPos2)));
			assertThat(toBytes(buffers[4]), is(subBytes(TEST_BYTES_TO_READ, expectedPos2, maxAvailableBytes)));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testReadIntoMultipleBuffersMoreThanAvailable(NamedInt maxBytesToTransfer) throws IOException {
		final long maxAvailableBytes = TEST_BYTES_TO_READ.length;

		int[] bufferCapacities = {1, 2, 4, 5, 3};
		ByteBuffer[] buffers = Arrays.stream(bufferCapacities)
				.mapToObj(ByteBuffer::allocate)
				.toArray(ByteBuffer[]::new);
		try (FileChannel channel = openReadableChannel(maxBytesToTransfer)) {
			long startPos = 3;
			channel.position(startPos);

			assertThat("Invalid test setup", startPos + bufferCapacities[1] + bufferCapacities[2], lessThanOrEqualTo(maxAvailableBytes));
			assertThat("Invalid test setup", startPos + bufferCapacities[1] + bufferCapacities[2] + bufferCapacities[3], greaterThanOrEqualTo(maxAvailableBytes));

			// fill buffer[1], buffer[2], buffer[3], and buffer[4] starting at position startPos
			long bytesRead = readFully(channel, buffers, 1, 4);
			Arrays.stream(buffers).forEach(ByteBuffer::flip);

			long expectedPos1 = startPos + buffers[1].capacity();
			long expectedPos2 = expectedPos1 + buffers[2].capacity();

			assertThat(bytesRead, is(maxAvailableBytes - startPos));
			assertThat(channel.position(), is(maxAvailableBytes));

			assertThat(buffers[0].remaining(), is(0));	// buffer 0 not filled
			assertThat(toBytes(buffers[1]), is(subBytes(TEST_BYTES_TO_READ, startPos, expectedPos1)));
			assertThat(toBytes(buffers[2]), is(subBytes(TEST_BYTES_TO_READ, expectedPos1, expectedPos2)));
			assertThat(toBytes(buffers[3]), is(subBytes(TEST_BYTES_TO_READ, expectedPos2, maxAvailableBytes)));
			assertThat(buffers[4].remaining(), is(0));	// not enough bytes available for buffer[4]
		}
	}

	/**
	 * The method {@link FileChannel#read(ByteBuffer[], int, int)} does not guarantee that all buffers are filled
	 * even if the file channel contains enough bytes. This makes it difficult to write a unit test against this
	 * method directly. Therefore, we write the unit tests against this method.
	 */
	private long readFully(FileChannel channel, ByteBuffer[] dsts, int offset, int length) throws IOException {
		long maxBytesToRead = IntStream.range(offset, offset + length)
			.map(i -> dsts[i].remaining())
			.sum();
		long totalBytesRead = 0;
		long expectedPos = channel.position();
		while (totalBytesRead < maxBytesToRead) {
			long bytesRead = channel.read(dsts, offset, length);
			if (bytesRead == -1) {
				assertThat(channel.position(), is(channel.size()));
				break;
			}
			totalBytesRead += bytesRead;
			expectedPos += bytesRead;
			assertThat(channel.position(), is(expectedPos));
		}
		return totalBytesRead;
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testWrite(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel channel = openWritableChannel(maxBytesToTransfer)) {
			long startPos = 2;

			assertThat(channel.position(), is(0L));
			long expectedPos = startPos;
			channel.position(expectedPos);

			int totalBytesWritten = 0;
			while (totalBytesWritten < TEST_BYTES_TO_WRITE.length) {
				assertThat(channel.position(), is(expectedPos));

				byte[] bytesToWrite = subBytes(TEST_BYTES_TO_WRITE, totalBytesWritten, Math.min(totalBytesWritten + 3, TEST_BYTES_TO_WRITE.length));
				ByteBuffer buffer = ByteBuffer.wrap(bytesToWrite);

				int maxBytesToWrite = buffer.remaining();
				int bytesWritten = channel.write(buffer);
				assertThat(bytesWritten, lessThanOrEqualTo(maxBytesToWrite));

				buffer.flip();

				expectedPos += bytesWritten;
				totalBytesWritten += bytesWritten;
			}
			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(allBytes.length, is((int) startPos + TEST_BYTES_TO_WRITE.length));
			assertThat(subBytes(allBytes, 0, startPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, startPos)));
			assertThat(subBytes(allBytes, startPos, allBytes.length), is(TEST_BYTES_TO_WRITE));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testWriteAtPosition(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel channel = openWritableChannel(maxBytesToTransfer)) {
			long expectedPos = 0;
			long firstStartPos = 2;
			long startPos = firstStartPos;

			int totalBytesWritten = 0;
			while (totalBytesWritten < TEST_BYTES_TO_WRITE.length) {
				byte[] bytesToWrite = subBytes(TEST_BYTES_TO_WRITE, totalBytesWritten, Math.min(totalBytesWritten + 3, TEST_BYTES_TO_WRITE.length));
				ByteBuffer buffer = ByteBuffer.wrap(bytesToWrite);

				int maxBytesToWrite = buffer.remaining();
				int bytesWritten = channel.write(buffer, startPos);
				assertThat(bytesWritten, lessThanOrEqualTo(maxBytesToWrite));

				buffer.flip();

				assertThat(channel.position(), is(expectedPos));

				channel.position(startPos);
				expectedPos = startPos;

				startPos += bytesWritten;
				totalBytesWritten += bytesWritten;
			}
			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(allBytes.length, is((int) firstStartPos + TEST_BYTES_TO_WRITE.length));
			assertThat(subBytes(allBytes, 0, firstStartPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, firstStartPos)));
			assertThat(subBytes(allBytes, firstStartPos, allBytes.length), is(TEST_BYTES_TO_WRITE));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testWriteMultipleBuffers(NamedInt maxBytesToTransfer) throws IOException {
		assertThat("Invalid test setup", TEST_BYTES_TO_WRITE.length, greaterThanOrEqualTo(6));

		ByteBuffer[] buffers = {
			ByteBuffer.wrap(new byte[]{0, 1, 2}),	// dummy buffer
			ByteBuffer.wrap(new byte[]{4, 3, 2}),	// dummy buffer
			ByteBuffer.wrap(subBytes(TEST_BYTES_TO_WRITE, 0, 3)),
			ByteBuffer.wrap(subBytes(TEST_BYTES_TO_WRITE, 3, 6)),
			ByteBuffer.wrap(subBytes(TEST_BYTES_TO_WRITE, 6, TEST_BYTES_TO_WRITE.length)),
			ByteBuffer.wrap(new byte[]{7, 7, 7})	// dummy buffer
		};
		try (FileChannel channel = openWritableChannel(maxBytesToTransfer)) {
			long startPos = 4;

			assertThat(channel.position(), is(0L));
			long expectedPos = startPos;

			channel.position(expectedPos);
			assertThat(channel.position(), is(expectedPos));

			long bytesWritten = writeFully(channel, buffers, 2, 3);
			assertThat(bytesWritten, is((long) TEST_BYTES_TO_WRITE.length));

			expectedPos += bytesWritten;
			assertThat(expectedPos, is(startPos + TEST_BYTES_TO_WRITE.length));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(allBytes.length, is((int) startPos + TEST_BYTES_TO_WRITE.length));
			assertThat(subBytes(allBytes, 0, startPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, startPos)));
			assertThat(subBytes(allBytes, startPos, allBytes.length), is(TEST_BYTES_TO_WRITE));
		}
	}

	/**
	 * The method {@link FileChannel#write(ByteBuffer[], int, int)} does not guarantee that all buffers are written.
	 * This makes it difficult to write a unit test against this method directly. Therefore, we write the unit tests
	 * against this method.
	 */
	private long writeFully(FileChannel channel, ByteBuffer[] srcs, int offset, int length) throws IOException {
		long maxBytesToWrite = IntStream.range(offset, offset + length)
			.map(i -> srcs[i].remaining())
			.sum();
		long totalBytesWritten = 0;
		long expectedPos = channel.position();
		while (totalBytesWritten < maxBytesToWrite) {
			long bytesWritten = channel.write(srcs, offset, length);
			totalBytesWritten += bytesWritten;
			expectedPos += bytesWritten;
			assertThat(channel.position(), is(expectedPos));
		}
		return totalBytesWritten;
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testSize(NamedInt maxBytesToTransfer) throws IOException {
		long startPos = 6;
		long truncatedSize = 8;

		assertThat("Invalid test setup", startPos, lessThan(truncatedSize));
		assertThat("Invalid test setup", truncatedSize, lessThan((long) TEST_BYTES_TO_READ.length));

		try (FileChannel channel = openWritableChannel(maxBytesToTransfer)) {
			assertThat(channel.size(), is((long) TEST_BYTES_TO_READ.length));

			channel.position(startPos);
			assertThat(channel.size(), is((long) TEST_BYTES_TO_READ.length));

			ByteBuffer[] buffers = {ByteBuffer.wrap(TEST_BYTES_TO_WRITE)};
			long bytesWritten = writeFully(channel, buffers, 0, 1);
			assertThat(bytesWritten, is((long) TEST_BYTES_TO_WRITE.length));

			assertThat(channel.size(), is(startPos + TEST_BYTES_TO_WRITE.length));
			assertThat(channel.position(), is(channel.size()));

			FileChannel otherChannel = channel.truncate(truncatedSize);

			assertThat(otherChannel, is(channel));
			assertThat(channel.size(), is(truncatedSize));
			assertThat(channel.position(), is(channel.size()));
		}
		// The truncation does not only limit the file channel's size, but the actual file's size
		assertThat(Files.size(testFileToWrite), is(truncatedSize));
		byte[] allBytes = Files.readAllBytes(testFileToWrite);
		assertThat(allBytes.length, is((int) truncatedSize));
		assertThat(subBytes(allBytes, 0, startPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, startPos)));
		assertThat(subBytes(allBytes, startPos, truncatedSize), is(subBytes(TEST_BYTES_TO_WRITE, 0, truncatedSize - startPos)));
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferToSourceAndTargetPositionZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long startPos = 2;
			long count = 5;

			long bytesTransferred = src.transferTo(startPos, count, target);
			assertThat(bytesTransferred, lessThanOrEqualTo(count));

			assertThat(src.position(), is(0L));
			assertThat(target.position(), is(bytesTransferred));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(subBytes(allBytes, 0, bytesTransferred), is(subBytes(TEST_BYTES_TO_READ, startPos, startPos + bytesTransferred)));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferToSourcePositionNotZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long srcPos = 7;
			src.position(srcPos);
			long startPos = 2;
			long count = 5;

			long bytesTransferred = src.transferTo(startPos, count, target);
			assertThat(bytesTransferred, lessThanOrEqualTo(count));

			assertThat(src.position(), is(srcPos));
			assertThat(target.position(), is(bytesTransferred));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(subBytes(allBytes, 0, bytesTransferred), is(subBytes(TEST_BYTES_TO_READ, startPos, startPos + bytesTransferred)));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferToSourceAndTargetPositionNotZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long srcPos = 3;
			src.position(srcPos);
			long targetPos = 5;
			target.position(targetPos);
			long startPos = 6;
			long count = 8;

			long maximumExpectedBytesTransferred = TEST_BYTES_TO_READ.length - startPos;
			assertThat("Invalid test setup", maximumExpectedBytesTransferred, lessThan(count));

			long bytesTransferred = src.transferTo(startPos, count, target);
			assertThat(bytesTransferred, lessThanOrEqualTo(maximumExpectedBytesTransferred));

			assertThat(src.position(), is(srcPos));
			assertThat(target.position(), is(targetPos + bytesTransferred));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(subBytes(allBytes, 0, targetPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, targetPos)));
			assertThat(subBytes(allBytes, targetPos, targetPos + bytesTransferred), is(subBytes(TEST_BYTES_TO_READ, startPos, startPos + bytesTransferred)));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferToStartPositionLargerThanSize(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long startPos = TEST_BYTES_TO_READ.length + 1;
			long count = 5;

			long bytesTransferred = src.transferTo(startPos, count, target);
			assertThat(bytesTransferred, is(0L));

			assertThat(src.position(), is(0L));
			assertThat(target.position(), is(0L));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(allBytes, is(INITIAL_CONTENT_FILE_TO_WRITE));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferToCountZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long count = 0;

			long bytesTransferred = src.transferTo(2, count, target);
			assertThat(bytesTransferred, is(0L));

			assertThat(src.position(), is(0L));
			assertThat(target.position(), is(0L));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(allBytes, is(INITIAL_CONTENT_FILE_TO_WRITE));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferFromSourceAndTargetPositionZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long startPos = 2;
			long count = 5;

			long bytesTransferred = target.transferFrom(src, startPos, count);
			assertThat(bytesTransferred, lessThanOrEqualTo(count));

			assertThat(src.position(), is(bytesTransferred));
			assertThat(target.position(), is(0L));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(subBytes(allBytes, 0, startPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, startPos)));
			assertThat(subBytes(allBytes, startPos, startPos + bytesTransferred), is(subBytes(TEST_BYTES_TO_READ, 0, bytesTransferred)));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferFromTargetPositionNotZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long targetPos = 7;
			target.position(targetPos);
			long startPos = 2;
			long count = 5;

			long bytesTransferred = target.transferFrom(src, startPos, count);
			assertThat(bytesTransferred, lessThanOrEqualTo(count));

			assertThat(src.position(), is(bytesTransferred));
			assertThat(target.position(), is(targetPos));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(subBytes(allBytes, 0, startPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, startPos)));
			assertThat(subBytes(allBytes, startPos, startPos + bytesTransferred), is(subBytes(TEST_BYTES_TO_READ, 0, bytesTransferred)));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferFromSourceAndTargetPositionNotZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long srcPos = 5;
			src.position(srcPos);
			long targetPos = 3;
			target.position(targetPos);
			long startPos = 6;
			long count = 8;

			long maximumExpectedBytesTransferred = TEST_BYTES_TO_READ.length - srcPos;
			assertThat("Invalid test setup", maximumExpectedBytesTransferred, lessThan(count));

			long bytesTransferred = target.transferFrom(src, startPos, count);
			assertThat(bytesTransferred, lessThanOrEqualTo(maximumExpectedBytesTransferred));

			assertThat(src.position(), is(srcPos + bytesTransferred));
			assertThat(target.position(), is(targetPos));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(subBytes(allBytes, 0, startPos), is(subBytes(INITIAL_CONTENT_FILE_TO_WRITE, 0, startPos)));
			assertThat(subBytes(allBytes, startPos, startPos + bytesTransferred), is(subBytes(TEST_BYTES_TO_READ, srcPos, srcPos + bytesTransferred)));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferFromStartPositionLargerThanSize(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long startPos = INITIAL_CONTENT_FILE_TO_WRITE.length + 1;
			long count = 5;

			long bytesTransferred = target.transferFrom(src, startPos, count);
			assertThat(bytesTransferred, is(0L));

			assertThat(src.position(), is(0L));
			assertThat(target.position(), is(0L));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(allBytes, is(INITIAL_CONTENT_FILE_TO_WRITE));
		}
	}

	@ParameterizedTest
	@FieldSource("TRANSFER_LIMITS")
	void testTransferFromCountZero(NamedInt maxBytesToTransfer) throws IOException {
		try (FileChannel src = openReadableChannel(NO_TRANSFER_LIMIT);
			 FileChannel target = openWritableChannel(maxBytesToTransfer)) {
			long count = 0;

			long bytesTransferred = target.transferFrom(src, 2, count);
			assertThat(bytesTransferred, is(0L));

			assertThat(src.position(), is(0L));
			assertThat(target.position(), is(0L));

			byte[] allBytes = Files.readAllBytes(testFileToWrite);
			assertThat(allBytes, is(INITIAL_CONTENT_FILE_TO_WRITE));
		}
	}

	private FileChannel openReadableChannel(NamedInt maxBytesToTransfer) throws IOException {
		SeekableByteChannel seekableByteChannel = Files.newByteChannel(testFileToRead, StandardOpenOption.READ);
		if (maxBytesToTransfer != NO_TRANSFER_LIMIT) {
			seekableByteChannel = new LimitedSeekableByteChannel(seekableByteChannel, maxBytesToTransfer.getValue());
		}
		return new FileChannelFromSeekableByteChannel(seekableByteChannel);
	}

	/**
	 * Wraps a {@link SeekableByteChannel} from Jimfs (which is no {@link FileChannel}) into a
	 * {@link FileChannelFromSeekableByteChannel}. If {@code maxBytesToTransfer} is not
	 * {@link #NO_TRANSFER_LIMIT}, then the {@code SeekableByteChannel} is wrapped in a
	 * {@link LimitedSeekableByteChannel} with limited transfer capacity before. This allows
	 * testing scenarios in which buffers are not fully written or read, which is valid, but
	 * happens rarely in reality.<br>
	 * <br>
	 * We test against both, limited and unlimited {@code SeekableByteChannel}s, to exclude the
	 * possibility that the unit tests only pass due to a potential bug in the implementation
	 * of {@code LimitedSeekableByteChannel}.
	 */
	private FileChannel openWritableChannel(NamedInt maxBytesToTransfer) throws IOException {
		SeekableByteChannel seekableByteChannel = Files.newByteChannel(testFileToWrite, StandardOpenOption.WRITE);
		if (maxBytesToTransfer != NO_TRANSFER_LIMIT) {
			seekableByteChannel = new LimitedSeekableByteChannel(seekableByteChannel, maxBytesToTransfer.getValue());
		}
		return new FileChannelFromSeekableByteChannel(seekableByteChannel);
	}

	private static byte[] toBytes(ByteBuffer buffer) {
		byte[] bytes = buffer.array();
		return subBytes(bytes, buffer.position(), buffer.limit());
	}

	private static byte[] subBytes(byte[] array, long start, long end) {
		return Arrays.copyOfRange(array, (int) start, (int) end);
	}

	/**
	 * Wrapper class a round a {@link SeekableByteChannel} that essentially delegates
	 * all calls to the wrapped channel. The only behavioral difference is that this
	 * channel ensures that not more than the specified {@link #maxBytesToTransfer}
	 * are read into a buffer of written from a buffer.<br>
	 * <br>
	 * The reason for the existence of this class is that in almost all cases channels
	 * write all bytes of a buffer or read until the buffer is full (or no more bytes
	 * exist). Since this is, however, not guaranteed, we also want to test against
	 * a channel with a valid yet uncommon behavior.
	 */
	private static class LimitedSeekableByteChannel implements SeekableByteChannel
	{
		private final SeekableByteChannel	delegate;
		private final int					maxBytesToTransfer;

		LimitedSeekableByteChannel(SeekableByteChannel delegate, int maxBytesToTransfer) {
			if (maxBytesToTransfer <= 0) {
				throw new IllegalArgumentException("The maximum number of bytes to transfer must be positive, but is " + maxBytesToTransfer);
			}
			this.delegate = delegate;
			this.maxBytesToTransfer = maxBytesToTransfer;
		}

		@Override
		public int read(ByteBuffer dst) throws IOException {
			int originalLimit = dst.limit();
			int temporaryLimit = Math.min(originalLimit, dst.position() + maxBytesToTransfer);
			try {
				dst.limit(temporaryLimit);
				return delegate.read(dst);
			} finally {
				dst.limit(originalLimit);
			}
		}

		@Override
		public int write(ByteBuffer src) throws IOException {
			int originalLimit = src.limit();
			int temporaryLimit = Math.min(originalLimit, src.position() + maxBytesToTransfer);
			try {
				src.limit(temporaryLimit);
				return delegate.write(src);
			} finally {
				src.limit(originalLimit);
			}
		}

		@Override
		public long position() throws IOException {
			return delegate.position();
		}

		@Override
		public SeekableByteChannel position(long newPosition) throws IOException {
			delegate.position(newPosition);
			return this;
		}

		@Override
		public long size() throws IOException {
			return delegate.size();
		}

		@Override
		public SeekableByteChannel truncate(long size) throws IOException {
			delegate.truncate(size);
			return this;
		}

		@Override
		public boolean isOpen() {
			return delegate.isOpen();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}

	private static class NamedInt
	{
		private final String	name;
		private final int		value;

		NamedInt(String name, int value) {
			this.name = name;
			this.value = value;
		}

		int getValue() {
			return value;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
