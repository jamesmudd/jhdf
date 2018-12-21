package io.jhdf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.Superblock.SuperblockV0V1;
import io.jhdf.Superblock.SuperblockV2V3;
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

	private final RandomAccessFile file;
	private final FileChannel fc;

	private final long userHeaderSize;

	private final Superblock superblock;

	private final Group rootGroup;

	public HdfFile(File hdfFile) {
		logger.info("Opening HDF5 file '{}'", hdfFile.getAbsolutePath());
		try {
			file = new RandomAccessFile(hdfFile, "r");
			fc = file.getChannel();

			// Find out if the file is a HDF5 file
			boolean validSignature = false;
			long offset = 0;
			for (offset = 0; offset < file.length(); offset = nextOffset(offset)) {
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
				rootGroup = GroupImpl.createGroup(fc, sb, ste.getObjectHeaderAddress(), "/", this);
			} else if (superblock.getVersionOfSuperblock() == 2 || superblock.getVersionOfSuperblock() == 3) {
				SuperblockV2V3 sb = (SuperblockV2V3) superblock;
				rootGroup = GroupImpl.createGroup(fc, sb, sb.getRootGroupObjectHeaderAddress(), "/", this);
			} else {
				throw new HdfException("Unreconized superblock version = " + superblock.getVersionOfSuperblock());
			}

		} catch (IOException e) {
			throw new HdfException("Failed to open file. Is it a HDF5 file?", e);
		}
		logger.info("Opend HDF5 file '{}'", hdfFile.getAbsolutePath());
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
		return file.getChannel().map(MapMode.READ_ONLY, 0, userHeaderSize);
	}

	/**
	 * @throws IOException
	 * @see java.io.RandomAccessFile#close()
	 */
	@Override
	public void close() throws IOException {
		fc.close();
		file.close();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.RandomAccessFile#length()
	 */
	public long length() throws IOException {
		return file.length();
	}

	@Override
	public boolean isGroup() {
		return rootGroup.isGroup();
	}

	@Override
	public Map<String, Node> getChildren() {
		return rootGroup.getChildren();
	}

	@Override
	public String getName() {
		return "/";
	}

	@Override
	public String getPath() {
		return "/";
	}

	@Override
	public Map<String, AttributeMessage> getAttributes() {
		return rootGroup.getAttributes();
	}

}
