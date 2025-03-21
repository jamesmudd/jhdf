/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
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

		this.rootGroup = new WritableGroupImpl(null, "/");
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
			rootGroup.write(hdfFileChannel, ROOT_GROUP_ADDRESS);
			hdfFileChannel.write(getJHdfInfoBuffer());
			long endOfFile = hdfFileChannel.getFileChannel().size();
			hdfFileChannel.write(superblock.toBuffer(endOfFile), 0L);
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
