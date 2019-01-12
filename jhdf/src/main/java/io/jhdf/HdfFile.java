package io.jhdf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.Superblock.SuperblockV0V1;
import io.jhdf.Superblock.SuperblockV2V3;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.AttributeMessage;

/**
 * The HDF file class this object represents a HDF5 file on disk and provides
 * methods to access it.
 * 
 * @author James Mudd
 */
public class HdfFile implements Group, AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(HdfFile.class);

	private final File file;
	private final RandomAccessFile raf;
	private final FileChannel fc;

	private final long userHeaderSize;

	private final Superblock superblock;

	private final Group rootGroup;

	private final Set<HdfFile> openExternalFiles = new HashSet<>();

	public HdfFile(File hdfFile) {
		logger.info("Opening HDF5 file '{}'...", hdfFile.getAbsolutePath());
		try {
			this.file = hdfFile;
			raf = new RandomAccessFile(hdfFile, "r");
			fc = raf.getChannel();

			// Find out if the file is a HDF5 file
			boolean validSignature = false;
			long offset = 0;
			for (offset = 0; offset < raf.length(); offset = nextOffset(offset)) {
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
			superblock = Superblock.readSuperblock(fc, offset);
			userHeaderSize = offset;

			if (superblock.getVersionOfSuperblock() == 0 || superblock.getVersionOfSuperblock() == 1) {
				SuperblockV0V1 sb = (SuperblockV0V1) superblock;
				SymbolTableEntry ste = new SymbolTableEntry(fc, sb.getRootGroupSymbolTableAddress(), sb);
				rootGroup = GroupImpl.createRootGroup(fc, sb, ste.getObjectHeaderAddress(), this);
			} else if (superblock.getVersionOfSuperblock() == 2 || superblock.getVersionOfSuperblock() == 3) {
				SuperblockV2V3 sb = (SuperblockV2V3) superblock;
				rootGroup = GroupImpl.createRootGroup(fc, sb, sb.getRootGroupObjectHeaderAddress(), this);
			} else {
				throw new HdfException("Unreconized superblock version = " + superblock.getVersionOfSuperblock());
			}

		} catch (IOException e) {
			throw new HdfException("Failed to open file. Is it a HDF5 file?", e);
		}
		logger.info("Opened HDF5 file '{}'", hdfFile.getAbsolutePath());
	}

	private long nextOffset(long offset) {
		if (offset == 0) {
			return 512L;
		}
		return offset * 2;
	}

	public long getUserHeaderSize() {
		return userHeaderSize;
	}

	public ByteBuffer getUserHeader() throws IOException {
		return raf.getChannel().map(MapMode.READ_ONLY, 0, userHeaderSize);
	}

	/**
	 * @throws IOException
	 * @see java.io.RandomAccessFile#close()
	 */
	@Override
	public void close() throws IOException {
		for (HdfFile externalhdfFile : openExternalFiles) {
			externalhdfFile.close();
			logger.info("Closed external file '{}'", externalhdfFile.getFile().getAbsolutePath());
		}

		fc.close();
		raf.close();
		logger.info("Closed HDF file '{}'", getFile().getAbsolutePath());
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.RandomAccessFile#length()
	 */
	public long length() throws IOException {
		return raf.length();
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
	public Map<String, AttributeMessage> getAttributes() {
		return rootGroup.getAttributes();
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
		path = StringUtils.stripStart(path, Constants.PATH_SEPERATOR);
		return rootGroup.getByPath(path);
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
}
