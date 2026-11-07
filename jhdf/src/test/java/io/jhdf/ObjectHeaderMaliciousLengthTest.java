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

import io.jhdf.exceptions.HdfException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for object header length fields which are read from the file and used to size allocations.
 * <p>
 * A corrupt or malicious file which declares a huge length must cause a clean {@link HdfException} on first access,
 * not an {@link OutOfMemoryError} escaping the public API. The object header is read lazily so the failure only
 * appears when the file is first accessed, for example by {@link HdfFile#getChildren()}.
 * <p>
 * All object header versions share the same storage layer check so only the version 1 header is exercised here, at
 * both length fields which reach an allocation, the "size of the messages" field and the object header continuation
 * message length.
 *
 * @see io.jhdf.storage.HdfFileChannel
 */
class ObjectHeaderMaliciousLengthTest {

	private static final String CONTROL_FILE = "space_padding_problem.hdf5";

	// Layout of the version 0 superblock and version 1 root object header in the control file
	private static final int ROOT_OBJECT_HEADER_ADDRESS = 96;
	private static final int NUMBER_OF_MESSAGES_OFFSET = 98;
	private static final int HEADER_SIZE_OFFSET = 104;
	private static final int FIRST_MESSAGE_OFFSET = 112;
	private static final int CONTINUATION_OFFSET_OFFSET = 120;
	private static final int CONTINUATION_LENGTH_OFFSET = 128;

	private static final int ORIGINAL_HEADER_SIZE = 24;
	private static final int CONTINUATION_MESSAGE_TYPE = 16;
	private static final int ORIGINAL_CONTINUATION_OFFSET = 800;
	private static final int ORIGINAL_CONTINUATION_LENGTH = 96;

	/**
	 * Just under 2GB, positive so passes the unsigned int field validation
	 */
	private static final int HUGE_LENGTH = 0x7F00_0000;

	/**
	 * The message appended by the bounds check in {@link io.jhdf.storage.HdfFileChannel}
	 */
	private static final String BOUNDS_FAILURE_MESSAGE = "but the file is only";

	@TempDir
	Path tempDir;

	@Test
	void controlFileOpensCleanly() throws IOException {
		Path controlFile = writeControlFile();

		try (HdfFile hdfFile = new HdfFile(controlFile)) {
			assertThat(hdfFile.getChildren().keySet(), is(empty()));
		}
	}

	@Test
	void hugeHeaderSizeIsRejectedFromPath() throws IOException {
		Path file = writePatchedFile(HEADER_SIZE_OFFSET, int32(HUGE_LENGTH));

		assertCleanHdfException(() -> {
			try (HdfFile hdfFile = new HdfFile(file)) {
				hdfFile.getChildren();
			}
		});
	}

	@Test
	void hugeHeaderSizeIsRejectedFromFile() throws IOException {
		Path file = writePatchedFile(HEADER_SIZE_OFFSET, int32(HUGE_LENGTH));

		assertCleanHdfException(() -> {
			try (HdfFile hdfFile = new HdfFile(file.toFile())) {
				hdfFile.getChildren();
			}
		});
	}

	@Test
	void hugeHeaderSizeIsRejectedFromUri() throws IOException {
		Path file = writePatchedFile(HEADER_SIZE_OFFSET, int32(HUGE_LENGTH));

		assertCleanHdfException(() -> {
			try (HdfFile hdfFile = new HdfFile(file.toUri())) {
				hdfFile.getChildren();
			}
		});
	}

	@Test
	void hugeHeaderSizeIsRejectedFromChannel() throws IOException {
		Path file = writePatchedFile(HEADER_SIZE_OFFSET, int32(HUGE_LENGTH));

		assertCleanHdfException(() -> {
			try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ);
				 HdfFile hdfFile = new HdfFile(channel, file.toUri())) {
				hdfFile.getChildren();
			}
		});
	}

	@Test
	void hugeHeaderSizeIsRejectedFromInputStream() throws IOException {
		Path file = writePatchedFile(HEADER_SIZE_OFFSET, int32(HUGE_LENGTH));

		assertCleanHdfException(() -> {
			try (InputStream inputStream = Files.newInputStream(file);
				 HdfFile hdfFile = HdfFile.fromInputStream(inputStream)) {
				hdfFile.getChildren();
			}
		});
	}

	@Test
	void hugeContinuationLengthIsRejected() throws IOException {
		Path file = writePatchedFile(CONTINUATION_LENGTH_OFFSET, int64(HUGE_LENGTH));

		assertCleanHdfException(() -> {
			try (HdfFile hdfFile = new HdfFile(file)) {
				hdfFile.getChildren();
			}
		});
	}

	@Test
	void hugeHeaderSizeIsRejectedInMemoryWithoutAllocating() throws IOException {
		byte[] bytes = patchedBytes(HEADER_SIZE_OFFSET, int32(HUGE_LENGTH));

		// The in-memory backing storage never allocates so has always failed cleanly, keep it that way
		assertThrows(HdfException.class, () -> {
			try (HdfFile hdfFile = HdfFile.fromBytes(bytes)) {
				hdfFile.getChildren();
			}
		});
	}

	/**
	 * Asserts the failure is a clean {@link HdfException} from the file bounds check and not an {@link Error}, or
	 * some other unrelated failure.
	 */
	private static void assertCleanHdfException(org.junit.jupiter.api.function.Executable executable) {
		HdfException e = assertThrows(HdfException.class, executable);
		assertTrue(hasCauseContaining(e, BOUNDS_FAILURE_MESSAGE),
			"Expected the file bounds check to reject the read but got: " + describe(e));
	}

	private static boolean hasCauseContaining(Throwable throwable, String text) {
		for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
			if (cause.getMessage() != null && cause.getMessage().contains(text)) {
				return true;
			}
			if (cause.getCause() == cause) {
				break; // Self referencing cause
			}
		}
		return false;
	}

	private static String describe(Throwable throwable) {
		StringBuilder sb = new StringBuilder();
		for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
			sb.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append(" -> ");
			if (cause.getCause() == cause) {
				break;
			}
		}
		return sb.toString();
	}

	private Path writeControlFile() throws IOException {
		return writeFile(controlBytes());
	}

	private Path writePatchedFile(int offset, byte[] replacement) throws IOException {
		return writeFile(patchedBytes(offset, replacement));
	}

	private Path writeFile(byte[] bytes) throws IOException {
		Path file = tempDir.resolve("malicious_length.hdf5");
		Files.write(file, bytes);
		return file;
	}

	private static byte[] controlBytes() throws IOException {
		byte[] bytes = Files.readAllBytes(TestUtils.getTestPath(CONTROL_FILE));

		// Self check the layout being patched is what this test expects
		assertEquals(896, bytes.length, "Unexpected control file size");
		assertEquals(1, bytes[ROOT_OBJECT_HEADER_ADDRESS], "Unexpected object header version");
		assertEquals(3, int16At(bytes, NUMBER_OF_MESSAGES_OFFSET));
		assertEquals(ORIGINAL_HEADER_SIZE, int32At(bytes, HEADER_SIZE_OFFSET));
		assertEquals(CONTINUATION_MESSAGE_TYPE, int16At(bytes, FIRST_MESSAGE_OFFSET));
		assertEquals(ORIGINAL_CONTINUATION_OFFSET, int64At(bytes, CONTINUATION_OFFSET_OFFSET));
		assertEquals(ORIGINAL_CONTINUATION_LENGTH, int64At(bytes, CONTINUATION_LENGTH_OFFSET));

		return bytes;
	}

	private static byte[] patchedBytes(int offset, byte[] replacement) throws IOException {
		byte[] bytes = controlBytes();
		System.arraycopy(replacement, 0, bytes, offset, replacement.length);
		return bytes;
	}

	private static int int16At(byte[] bytes, int offset) {
		return ByteBuffer.wrap(bytes, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
	}

	private static long int32At(byte[] bytes, int offset) {
		return ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
	}

	private static long int64At(byte[] bytes, int offset) {
		return ByteBuffer.wrap(bytes, offset, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	private static byte[] int32(int value) {
		return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
	}

	private static byte[] int64(long value) {
		return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
	}
}
