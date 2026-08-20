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

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.ChunkProvider;
import io.jhdf.api.DatasetCreationOptions;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.api.StreamingDataset;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritableDataset;
import io.jhdf.exceptions.HdfWritingException;
import io.jhdf.storage.HdfFileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class WritableHdfFile implements WritableGroup, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(WritableHdfFile.class);

	public static final long ROOT_GROUP_ADDRESS = 64;

	private final Path path;
	private final FileChannel fileChannel;
	private final Superblock.SuperblockV2V3 superblock;
	private final HdfFileChannel hdfFileChannel;
	private final WritableGroup rootGroup;

	/**
	 * The next address in the file not yet spoken for. Nothing moves it until a dataset streams data before the
	 * tree is written, so a file without one is laid out exactly as it always was.
	 */
	private long nextFreeAddress = ROOT_GROUP_ADDRESS;

	private final FileSpace fileSpace = new FileSpace() {
		@Override
		public long nextAddress() {
			return nextFreeAddress;
		}

		@Override
		public long reserve(long bytes) {
			final long address = nextFreeAddress;
			nextFreeAddress += bytes;
			return address;
		}
	};

	WritableHdfFile(Path path) {
		logger.warn("Writing files is in alpha. Check files carefully!");
		logger.info("Writing HDF5 file to [{}]", path.toAbsolutePath());
		this.path = path;
		try {
			this.fileChannel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new HdfWritingException("Failed to open file: " + path.toAbsolutePath(), e);
		}
		this.superblock = new Superblock.SuperblockV2V3();
		this.hdfFileChannel = new HdfFileChannel(this.fileChannel, this.superblock);

		final WritableGroupImpl root = new WritableGroupImpl(null, "/");
		root.setStreamingContext(this.hdfFileChannel, this.fileSpace);
		this.rootGroup = root;
		this.rootGroup.putAttribute("_jHDF", getJHdfInfo());
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public void close() {
		try {
			flush();
			fileChannel.close();
		} catch (IOException e) {
			throw new HdfWritingException("Failed to close file", e);
		}
	}

	private void flush() {
		logger.info("Flushing to disk [{}]...", path.toAbsolutePath());
		try {
			final long rootGroupAddress = nextFreeAddress;
			rootGroup.write(hdfFileChannel, rootGroupAddress);
			hdfFileChannel.write(getJHdfInfoBuffer());
			long endOfFile = hdfFileChannel.getFileChannel().size();
			hdfFileChannel.write(superblock.toBuffer(endOfFile, rootGroupAddress), 0L);
			logger.info("Flushed to disk [{}] file is [{}] bytes", path.toAbsolutePath(), endOfFile);
		} catch (IOException e) {
			throw new HdfWritingException("Error getting file size", e);
		}
	}

	private ByteBuffer getJHdfInfoBuffer() {
		final String info = getJHdfInfo();
		return ByteBuffer.wrap(info.getBytes(StandardCharsets.UTF_8));
	}

	private static String getJHdfInfo() {
		return "jHDF - " + JhdfInfo.VERSION + " - " + JhdfInfo.OS + " - " + JhdfInfo.ARCH + " - " + JhdfInfo.BYTE_ORDER;
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public WritableDataset putDataset(String name, Object data) {
		return rootGroup.putDataset(name, data);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public WritableDataset putDataset(String name, Object data, DatasetCreationOptions options) {
		return rootGroup.putDataset(name, data, options);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public WritableDataset putDataset(String name, Class<?> javaType, int[] dimensions,
									  DatasetCreationOptions options, ChunkProvider chunkProvider) {
		return rootGroup.putDataset(name, javaType, dimensions, options, chunkProvider);
	}

	/**
	 * Creates a chunked dataset written a chunk at a time, as the data becomes available, rather than from a
	 * dataset already in memory. Chunks are written to the file as they are handed over, so the memory needed is
	 * that of a single chunk.
	 * <p>
	 * The returned dataset must be closed, with every chunk written, before this file is closed.
	 *
	 * @param name the dataset name, in the root group
	 * @param javaType the dataset's element type e.g. {@code double.class}
	 * @param dimensions the dataset's dimensions
	 * @param options options controlling how the dataset is stored, must specify chunk dimensions
	 * @return the dataset to write chunks to
	 * @since v0.14.0
	 */
	@Override
	public StreamingDataset newStreamingDataset(String name, Class<?> javaType, int[] dimensions,
												DatasetCreationOptions options) {
		return rootGroup.newStreamingDataset(name, javaType, dimensions, options);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public WritableGroup putGroup(String name) {
		return rootGroup.putGroup(name);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Map<String, Node> getChildren() {
		return rootGroup.getChildren();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		return rootGroup.getChild(name);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Node getByPath(String path) {
		return rootGroup.getByPath(path);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Dataset getDatasetByPath(String path) {
		return rootGroup.getDatasetByPath(path);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public boolean isLinkCreationOrderTracked() {
		return rootGroup.isLinkCreationOrderTracked();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Group getParent() {
		return rootGroup.getParent();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public String getName() {
		return rootGroup.getName();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return rootGroup.getPath();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Map<String, Attribute> getAttributes() {
		return rootGroup.getAttributes();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Attribute getAttribute(String name) {
		return rootGroup.getAttribute(name);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Attribute putAttribute(String name, Object data) {
		return rootGroup.putAttribute(name, data);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Attribute putAttribute(String name, Object data, boolean unsigned) {
		return rootGroup.putAttribute(name, data, unsigned);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Attribute removeAttribute(String name) {
		return rootGroup.removeAttribute(name);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public NodeType getType() {
		return rootGroup.getType();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public boolean isGroup() {
		return rootGroup.isGroup();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public File getFile() {
		return  path.toFile();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Path getFileAsPath() {
		return path;
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public HdfFile getHdfFile() {
		return rootGroup.getHdfFile();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public long getAddress() {
		return rootGroup.getAddress();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public boolean isLink() {
		return rootGroup.isLink();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public boolean isAttributeCreationOrderTracked() {
		return rootGroup.isAttributeCreationOrderTracked();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Iterator<Node> iterator() {
		return rootGroup.iterator();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public void forEach(Consumer<? super Node> action) {
		rootGroup.forEach(action);
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public Spliterator<Node> spliterator() {
		return rootGroup.spliterator();
	}

	/**
	 {@inheritDoc}
	 */
	@Override
	public long write(HdfFileChannel hdfFileChannel, long position) {
		// TODO restructure interfaces to remove this method
		throw new UnsupportedOperationException();
	}
}
