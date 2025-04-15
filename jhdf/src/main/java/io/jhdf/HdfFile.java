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

import io.jhdf.Superblock.SuperblockV0V1;
import io.jhdf.Superblock.SuperblockV2V3;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.dataset.DatasetLoader;
import io.jhdf.dataset.NoParent;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.InMemoryHdfException;
import io.jhdf.nio.FileChannelFromSeekableByteChannel;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.storage.HdfBackingStorage;
import io.jhdf.storage.HdfFileChannel;
import io.jhdf.storage.HdfInMemoryByteBuffer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.nio.file.spi.FileSystemProvider;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * The HDF file class this object represents a HDF5 file on disk and provides
 * methods to access it.
 *
 * @author James Mudd
 */
public class HdfFile implements Group, AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(HdfFile.class);

	static {
		if (JhdfInfo.VERSION != null) {
			logger.info("jHDF version: {}", JhdfInfo.VERSION);
		} else {
			logger.warn("Using development version of jHDF");
		}
	}

	private final Optional<Path> optionalFile;
	private final HdfBackingStorage hdfBackingStorage;

	private final Group rootGroup;

	private final Set<HdfFile> openExternalFiles = new HashSet<>();

	public HdfFile(File file) {
		this(file.toPath());
	}

	public HdfFile(URI uri) {
		this(Paths.get(uri));
	}

	/**
	 * Opens an HDF5 contained in an in-memory byte array.
	 *
	 * @param bytes The byte array containing an HDF5 file
	 * @return an open HdfFile object
	 * @see HdfFile#fromByteBuffer(ByteBuffer)
	 * @see HdfFile#fromInputStream(InputStream)
	 */
	public static HdfFile fromBytes(byte[] bytes) {
		logger.info("Reading HDF5 file from byte[]");
		return fromByteBuffer(ByteBuffer.wrap(bytes));
	}

	/**
	 * Opens an HDF5 contained in {@link ByteBuffer}
	 *
	 * @param byteBuffer The buffer containing an HDF5 file
	 * @return an open HdfFile object
	 * @see HdfFile#fromBytes(byte[])
	 * @see HdfFile#fromInputStream(InputStream)
	 */
	public static HdfFile fromByteBuffer(ByteBuffer byteBuffer) {
		logger.info("Reading HDF5 file from ByteBuffer");
		byteBuffer.order(LITTLE_ENDIAN); // HDF5 metadata is always stored LE
		// Find out if the buffer is a HDF5 file
		boolean validSignature = false;
		int offset;
		for (offset = 0; offset < byteBuffer.capacity(); offset = Math.toIntExact(nextOffset(offset))) {
			logger.trace("Checking for signature at offset = {}", offset);
			validSignature = Superblock.verifySignature(byteBuffer, offset);
			if (validSignature) {
				logger.debug("Found valid signature at offset = {}", offset);
				break;
			}
		}
		if (!validSignature) {
			throw new HdfException("No valid HDF5 signature found");
		}

		byteBuffer.position(offset);

		// We have a valid HDF5 file so read the full superblock
		final Superblock superblock = Superblock.readSuperblock(byteBuffer);

		// Validate the superblock
		if (superblock.getBaseAddressByte() != offset) {
			throw new HdfException("Invalid superblock base address detected");
		}

		HdfBackingStorage hdfBackingStorage = new HdfInMemoryByteBuffer(byteBuffer, superblock);

		return new HdfFile(hdfBackingStorage);
	}

	private HdfFile(HdfBackingStorage hdfBackingStorage) {
		this.hdfBackingStorage = hdfBackingStorage;
		this.optionalFile = Optional.empty(); // No file its in memory

		Superblock superblock = hdfBackingStorage.getSuperblock();
		if (superblock instanceof SuperblockV0V1) {
			SuperblockV0V1 sb = (SuperblockV0V1) superblock;
			SymbolTableEntry ste = new SymbolTableEntry(hdfBackingStorage,
				sb.getRootGroupSymbolTableAddress() - sb.getBaseAddressByte());
			this.rootGroup = GroupImpl.createRootGroup(hdfBackingStorage, ste.getObjectHeaderAddress(), this);
		} else if (superblock instanceof SuperblockV2V3) {
			SuperblockV2V3 sb = (SuperblockV2V3) superblock;
			this.rootGroup = GroupImpl.createRootGroup(hdfBackingStorage, sb.getRootGroupObjectHeaderAddress(), this);
		} else {
			throw new HdfException("Unrecognized superblock version = " + superblock.getVersionOfSuperblock());
		}

	}

	/**
	 * Opens an {@link HdfFile} from an {@link InputStream}. The stream will be read fully into a temporary file. The
	 * file will be deleted when the HdfFile is closed, or at application exit if it was never closed.
	 *
	 * @param inputStream the {@link InputStream} to read
	 * @return HdfFile instance
	 * @see HdfFile#fromBytes(byte[])
	 * @see HdfFile#fromByteBuffer(ByteBuffer)
	 */
	public static HdfFile fromInputStream(InputStream inputStream) {
		return TempHdfFile.fromInputStream(inputStream);
	}

	public HdfFile(Path hdfFile) {
		Path absoluteFile = hdfFile.toAbsolutePath();
		logger.info("Opening HDF5 file '{}'...", absoluteFile);
		this.optionalFile = Optional.of(hdfFile);

		try {
			// Sonar would like this closed but we are implementing a file object which
			// needs this channel for operation it is closed when this HdfFile is closed
			FileChannel fc;
			try {
				fc = FileChannel.open(hdfFile, StandardOpenOption.READ); // NOSONAR
			} catch (UnsupportedOperationException e) {
				// some FileSystemProviders don't support FileChannels
				FileSystemProvider provider = hdfFile.getFileSystem().provider();
				SeekableByteChannel byteChannel = provider.newByteChannel(hdfFile, Collections.singleton(StandardOpenOption.READ));
				fc = new FileChannelFromSeekableByteChannel(byteChannel);
			}

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

			hdfBackingStorage = new HdfFileChannel(fc, superblock);

			if (superblock instanceof SuperblockV0V1) {
				SuperblockV0V1 sb = (SuperblockV0V1) superblock;
				SymbolTableEntry ste = new SymbolTableEntry(hdfBackingStorage,
					sb.getRootGroupSymbolTableAddress() - sb.getBaseAddressByte());
				rootGroup = GroupImpl.createRootGroup(hdfBackingStorage, ste.getObjectHeaderAddress(), this);
			} else if (superblock instanceof SuperblockV2V3) {
				SuperblockV2V3 sb = (SuperblockV2V3) superblock;
				rootGroup = GroupImpl.createRootGroup(hdfBackingStorage, sb.getRootGroupObjectHeaderAddress(), this);
			} else {
				throw new HdfException("Unrecognized superblock version = " + superblock.getVersionOfSuperblock());
			}

		} catch (IOException e) {
			throw new HdfException("Failed to open file '" + absoluteFile + "' . Is it a HDF5 file?", e);
		}
		logger.info("Opened HDF5 file '{}'", absoluteFile);
	}

	private static long nextOffset(long offset) {
		if (offset == 0) {
			return 512L;
		}
		return offset * 2;
	}

	public HdfFile(SeekableByteChannel channel, URI sourceUri) {
		logger.info("Opening remote HDF5 file from URI: {}", sourceUri);
		this.optionalFile = Optional.of(Paths.get(sourceUri.getPath()));

		try {
			FileChannel fc = new FileChannelFromSeekableByteChannel(channel);

			// Validate HDF5 signature
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
				throw new HdfException("No valid HDF5 signature found in remote file");
			}

			Superblock superblock = Superblock.readSuperblock(fc, offset);
			if (superblock.getBaseAddressByte() != offset) {
				throw new HdfException("Invalid superblock base address detected in remote file");
			}

			hdfBackingStorage = new HdfFileChannel(fc, superblock);

			if (superblock instanceof SuperblockV0V1) {
				SuperblockV0V1 sb = (SuperblockV0V1) superblock;
				SymbolTableEntry ste = new SymbolTableEntry(hdfBackingStorage,
					sb.getRootGroupSymbolTableAddress() - sb.getBaseAddressByte());
				this.rootGroup = GroupImpl.createRootGroup(hdfBackingStorage, ste.getObjectHeaderAddress(), this);
			} else if (superblock instanceof SuperblockV2V3) {
				SuperblockV2V3 sb = (SuperblockV2V3) superblock;
				this.rootGroup = GroupImpl.createRootGroup(hdfBackingStorage, sb.getRootGroupObjectHeaderAddress(), this);
			} else {
				throw new HdfException("Unsupported superblock version: " + superblock.getVersionOfSuperblock());
			}

		} catch (IOException e) {
			throw new HdfException("Failed to open remote HDF5 file from URI: " + sourceUri, e);
		}
	}


	public static WritableHdfFile write(Path path) {
		return new WritableHdfFile(path);
	}

	/**
	 * Gets the size of the user block of this file.
	 *
	 * @return the size of the user block
	 */
	public long getUserBlockSize() {
		return hdfBackingStorage.getUserBlockSize();
	}

	/**
	 * Gets the buffer containing the user block data.
	 *
	 * @return the buffer containing the user block data
	 */
	public ByteBuffer getUserBlockBuffer() {
		return hdfBackingStorage.mapNoOffset(0, hdfBackingStorage.getUserBlockSize());
	}

	/**
	 * @throws HdfException if closing the file fails
	 * @see java.io.RandomAccessFile#close()
	 */
	@Override
	public void close() {
		if (!inMemory()) {
			for (HdfFile externalHdfFile : openExternalFiles) {
				externalHdfFile.close();
				logger.info("Closed external file '{}'", externalHdfFile.getFileAsPath().toAbsolutePath());
			}

			hdfBackingStorage.close();
			logger.info("Closed HDF file '{}'", getFileAsPath().toAbsolutePath());
		}
	}

	/**
	 * Returns the size of this HDF5 file.
	 *
	 * @return the size of this file in bytes
	 */
	public long size() {
		return hdfBackingStorage.size();
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
		return optionalFile.map(Path::getFileName).map(Path::toString).orElse("In-Memory no backing file");
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
		return "HdfFile [file=" + getName() + "]";
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
		Path fileAsPath = getFileAsPath();
		return fileAsPath.getFileSystem() == FileSystems.getDefault() ? fileAsPath.toFile() : null;
	}

	@Override
	public Path getFileAsPath() {
		return optionalFile.orElseThrow(() -> new HdfException("No backing file. In-memory"));
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
		if (inMemory()) {
			throw new InMemoryHdfException();
		}
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
	 * @return the underlying {@link HdfBackingStorage}
	 */
	public HdfBackingStorage getHdfBackingStorage() {
		return hdfBackingStorage;
	}

	/**
	 * @return true if the file is in memory
	 */
	public boolean inMemory() {
		return hdfBackingStorage.inMemory();
	}

	public Node getNodeByAddress(long address) {
		ObjectHeader objectHeader = ObjectHeader.readObjectHeader(hdfBackingStorage, address);
		final String name = "__ADDRESS__" + address;
		final Node node;
		if (objectHeader.hasMessageOfType(DataSpaceMessage.class)) {
			// Its a a Dataset
			logger.trace("Creating dataset [{}]", name);
			node = DatasetLoader.createDataset(hdfBackingStorage, objectHeader, name, NoParent.INSTANCE);
		} else if (objectHeader.hasMessageOfType(DataTypeMessage.class)) {
			// Has a datatype but no dataspace so its a committed datatype
			logger.trace("Creating committed data type [{}]", name);
			node = new CommittedDatatype(hdfBackingStorage, address, name, NoParent.INSTANCE);
		} else {
			// Its a group
			logger.trace("Creating group [{}]", name);
			node = GroupImpl.createGroup(hdfBackingStorage, address, name, NoParent.INSTANCE);
		}
		return node;
	}
}
