/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.Superblock.SuperblockV0V1;
import io.jhdf.Superblock.SuperblockV2V3;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The HDF file class this object represents a HDF5 file on disk and provides
 * methods to access it.
 *
 * @author James Mudd
 */
public class HdfFile implements Group, AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(HdfFile.class);

	static {
		final String versionStr =  HdfFile.class.getPackage().getImplementationVersion();
		if( versionStr != null) {
			logger.info("jHDF version: {}", HdfFile.class.getPackage().getImplementationVersion());
		} else {
			logger.warn("Using development version of jHDF");
		}
	}

	private final File file;
	private final HdfFileChannel hdfFc;

    private final Group rootGroup;

	private final Set<HdfFile> openExternalFiles = new HashSet<>();

	public HdfFile(File hdfFile) {
		logger.info("Opening HDF5 file '{}'...", hdfFile.getAbsolutePath());
		this.file = hdfFile;

		try {
			// Sonar would like this closed but we are implementing a file object which
			// needs this channel for operation it is closed when this HdfFile is closed
			FileChannel fc = FileChannel.open(hdfFile.toPath(), StandardOpenOption.READ); // NOSONAR

			// Find out if the file is a HDF5 file
			boolean validSignature = false;
			long offset;
			for (offset = 0; offset < fc.size(); offset = nextOffset(offset)) {
				logger.trace("Checking for signature at offset = {}", offset);
				validSignature = Superblock.verifySignature(fc, offset);
				if (validSignature) {
					logger.debug("Found valid signature at offset = {}", offset);
					break;
				}
			}
			if (!validSignature) {
				throw new HdfException("No valid HDF5 signature found");
			}

			// We have a valid HDF5 file so read the full superblock
            final Superblock superblock = Superblock.readSuperblock(fc, offset);

			// Validate the superblock
			if (superblock.getBaseAddressByte() != offset) {
				throw new HdfException("Invalid superblock base address detected");
			}

			hdfFc = new HdfFileChannel(fc, superblock);

			if (superblock instanceof SuperblockV0V1) {
				SuperblockV0V1 sb = (SuperblockV0V1) superblock;
				SymbolTableEntry ste = new SymbolTableEntry(hdfFc,
						sb.getRootGroupSymbolTableAddress() - sb.getBaseAddressByte());
				rootGroup = GroupImpl.createRootGroup(hdfFc, ste.getObjectHeaderAddress(), this);
			} else if (superblock instanceof SuperblockV2V3) {
				SuperblockV2V3 sb = (SuperblockV2V3) superblock;
				rootGroup = GroupImpl.createRootGroup(hdfFc, sb.getRootGroupObjectHeaderAddress(), this);
			} else {
				throw new HdfException("Unrecognized superblock version = " + superblock.getVersionOfSuperblock());
			}

		} catch (IOException e) {
			throw new HdfException("Failed to open file '" + file.getAbsolutePath() + "' . Is it a HDF5 file?", e);
		}
		logger.info("Opened HDF5 file '{}'", hdfFile.getAbsolutePath());
	}

	private long nextOffset(long offset) {
		if (offset == 0) {
			return 512L;
		}
		return offset * 2;
	}

	/**
	 * Gets the size of the user block of this file.
	 *
	 * @return the size of the user block
	 */
	public long getUserBlockSize() {
		return hdfFc.getUserBlockSize();
	}

	/**
	 * Gets the buffer containing the user block data.
	 *
	 * @return the buffer containing the user block data
	 */
	public ByteBuffer getUserBlockBuffer() {
		return hdfFc.mapNoOffset(0, hdfFc.getUserBlockSize());
	}

	/**
	 * @throws HdfException if closing the file fails
	 * @see java.io.RandomAccessFile#close()
	 */
	@Override
	public void close() {
		for (HdfFile externalHdfFile : openExternalFiles) {
			externalHdfFile.close();
			logger.info("Closed external file '{}'", externalHdfFile.getFile().getAbsolutePath());
		}

		hdfFc.close();
		logger.info("Closed HDF file '{}'", getFile().getAbsolutePath());
	}

	/**
	 * Returns the size of this HDF5 file.
	 *
	 * @return the size of this file in bytes
	 */
	public long size() {
		return hdfFc.size();
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public Map<String, Node> getChildren() {
		return rootGroup.getChildren();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getPath() {
		return "/";
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
	public String toString() {
		return "HdfFile [file=" + file.getName() + "]";
	}

	@Override
	public NodeType getType() {
		return NodeType.FILE;
	}

	@Override
	public Group getParent() {
		// The file has no parent so return null
		return null;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public long getAddress() {
		return rootGroup.getAddress();
	}

	@Override
	public Iterator<Node> iterator() {
		return rootGroup.iterator();
	}

	@Override
	public Node getChild(String name) {
		return rootGroup.getChild(name);
	}

	@Override
	public Node getByPath(String path) {
		// As its the file its ok to have a leading slash but strip it here to be
		// consistent with other groups
		path = StringUtils.stripStart(path, Constants.PATH_SEPARATOR);
		return rootGroup.getByPath(path);
	}

	@Override
	public Dataset getDatasetByPath(String path) {
		// As its the file its ok to have a leading slash but strip it here to be
		// consistent with other groups
		path = StringUtils.stripStart(path, Constants.PATH_SEPARATOR);
		return rootGroup.getDatasetByPath(path);
	}

	@Override
	public HdfFile getHdfFile() {
		return this;
	}

	/**
	 * Add an external file to this HDF file. it needed so that external files open
	 * via links from this file can also be closed when this file is closed.
	 *
	 * @param hdfFile an open external file linked from this file
	 */
	public void addExternalFile(HdfFile hdfFile) {
		openExternalFiles.add(hdfFile);
	}

	@Override
	public boolean isLink() {
		return false;
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return rootGroup.isAttributeCreationOrderTracked();
	}

	@Override
	public boolean isLinkCreationOrderTracked() {
		return rootGroup.isLinkCreationOrderTracked();
	}

	/**
	 * @return the underlying {@link HdfFileChannel}
	 */
	public HdfFileChannel getHdfChannel() {
		return hdfFc;
	}

}
