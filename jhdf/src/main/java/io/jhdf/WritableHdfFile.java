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

	@Override
	public WritableDataset putDataset(String name, Object data) {
		return rootGroup.putDataset(name, data);
	}

	@Override
	public WritableGroup putGroup(String name) {
		return rootGroup.putGroup(name);
	}

	/**
	 This is a way to put a dataset into a group when you are constructing the dataset directly.
	 *
	 * @param name the name of the dataset
	 * @param dataset the dataset
	 * @return the dataset
	 */
	@Override
	public WritableDataset putWritableDataset(
			String name,
			WritableDataset dataset
	)
	{
		return rootGroup.putWritableDataset(name, dataset);
	}

	@Override
	public Map<String, Node> getChildren() {
		return rootGroup.getChildren();
	}

	@Override
	public Node getChild(String name) {
		return rootGroup.getChild(name);
	}

	@Override
	public Node getByPath(String path) {
		return rootGroup.getByPath(path);
	}

	@Override
	public Dataset getDatasetByPath(String path) {
		return rootGroup.getDatasetByPath(path);
	}

	@Override
	public boolean isLinkCreationOrderTracked() {
		return rootGroup.isLinkCreationOrderTracked();
	}

	@Override
	public Group getParent() {
		return rootGroup.getParent();
	}

	@Override
	public String getName() {
		return rootGroup.getName();
	}

	@Override
	public String getPath() {
		return rootGroup.getPath();
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return rootGroup.getAttributes();
	}

	@Override
	public Attribute getAttribute(String name) {
		return rootGroup.getAttribute(name);
	}

	@Override
	public Attribute putAttribute(String name, Object data) {
		return rootGroup.putAttribute(name, data);
	}

	@Override
	public Attribute removeAttribute(String name) {
		return rootGroup.removeAttribute(name);
	}

	@Override
	public NodeType getType() {
		return rootGroup.getType();
	}

	@Override
	public boolean isGroup() {
		return rootGroup.isGroup();
	}

	@Override
	public File getFile() {
		return  path.toFile();
	}

	@Override
	public Path getFileAsPath() {
		return path;
	}

	@Override
	public HdfFile getHdfFile() {
		return rootGroup.getHdfFile();
	}

	@Override
	public long getAddress() {
		return rootGroup.getAddress();
	}

	@Override
	public boolean isLink() {
		return rootGroup.isLink();
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return rootGroup.isAttributeCreationOrderTracked();
	}

	@Override
	public Iterator<Node> iterator() {
		return rootGroup.iterator();
	}

	@Override
	public void forEach(Consumer<? super Node> action) {
		rootGroup.forEach(action);
	}

	@Override
	public Spliterator<Node> spliterator() {
		return rootGroup.spliterator();
	}

	@Override
	public long write(HdfFileChannel hdfFileChannel, long position) {
		// TODO restructure interfaces to remove this method
		throw new UnsupportedOperationException();
	}
}
