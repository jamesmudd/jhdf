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

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritiableDataset;
import io.jhdf.exceptions.HdfWritingException;
import io.jhdf.storage.HdfFileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class WritableHdfFile implements WritableGroup, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(WritableHdfFile.class);

	private static final long ROOT_GROUP_ADDRESS = 48;

	private final Path path;
	private final FileChannel fileChannel;
	private final Superblock.SuperblockV2V3 superblock;
	private final HdfFileChannel hdfFileChannel;
	private ObjectHeader.ObjectHeaderV2 rootGroupObjectHeader;

	private final WritableGroup rootGroup;

	public WritableHdfFile(Path path) {
		logger.info("Writing HDF5 file to [{}]", path.toAbsolutePath());
		this.path = path;
		try {
			this.fileChannel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new HdfWritingException("Failed to ope file: " + path.toAbsolutePath(), e);
		}
		this.superblock = new Superblock.SuperblockV2V3();
		this.hdfFileChannel = new HdfFileChannel(this.fileChannel, this.superblock);

		this.rootGroup = new WritableGroupImpl(null, "/");
//		createRootGroup();
//		try {
//			hdfFileChannel.write(superblock.toBuffer(), 0L);
//			long endOfFile = rootGroup.write(hdfFileChannel, ROOT_GROUP_ADDRESS);
////			hdfFileChannel.write(rootGroup.(), ROOT_GROUP_ADDRESS);
//			hdfFileChannel.write(ByteBuffer.wrap(new byte[]{1}), 250);
//		} catch (IOException e) {
//			throw new HdfWritingExcpetion("Failed to write superblock", e);
//		}
	}

//	private void createRootGroup() {
//		List<Message> messages = new ArrayList<>();
//		messages.add(LinkInfoMessage.createBasic());
//		messages.add(GroupInfoMessage.createBasic());
//		messages.add(NilMessage.create());
//		this.rootGroupObjectHeader = new ObjectHeader.ObjectHeaderV2(ROOT_GROUP_ADDRESS, messages);
//	}

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
			long endOfFile = hdfFileChannel.getFileChannel().size();
			hdfFileChannel.write(superblock.toBuffer(endOfFile), 0L);
			logger.info("Flushed to disk [{}] file is [{}] bytes", path.toAbsolutePath(), endOfFile);
		} catch (IOException e) {
			throw new HdfWritingException("Error getting file size", e);
		}
	}

	@Override
	public WritiableDataset putDataset(String name, Object data) {
		return rootGroup.putDataset(name, data);
	}

	@Override
	public WritableGroup putGroup(String name) {
		return rootGroup.putGroup(name);
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
