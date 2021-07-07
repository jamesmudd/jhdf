package io.jhdf;

import io.jhdf.api.WritableGroup;
import io.jhdf.exceptions.HdfWritingExcpetion;
import io.jhdf.object.message.GroupInfoMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.Message;
import io.jhdf.object.message.NilMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WritableHdfFile implements WritableGroup, AutoCloseable {

	private static final long ROOT_GROUP_ADDRESS = 48;

	private final FileChannel fileChannel;
	private final Superblock.SuperblockV2V3 superblock;
	private ObjectHeader.ObjectHeaderV2 rootGroupObjectHeader;


	public WritableHdfFile(FileChannel fileChannel) {
		this.fileChannel = fileChannel;
		this.superblock = new Superblock.SuperblockV2V3();
		createRootGroup();
		try {
			fileChannel.write(superblock.toBuffer());
			fileChannel.write(rootGroupObjectHeader.toBuffer(), ROOT_GROUP_ADDRESS);
			fileChannel.write(ByteBuffer.wrap(new byte[]{1}), 250);
		} catch (IOException e) {
			throw new HdfWritingExcpetion("Failed to write superblock", e);
		}
	}

	private void createRootGroup() {
		List<Message> messages = new ArrayList<>();
		messages.add(LinkInfoMessage.createBasic());
		messages.add(GroupInfoMessage.createBasic());
		messages.add(NilMessage.create());
		this.rootGroupObjectHeader = new ObjectHeader.ObjectHeaderV2(ROOT_GROUP_ADDRESS, messages);
	}

	@Override
	public void close() {
		try {
			fileChannel.close();
		} catch (IOException e) {
			throw new HdfWritingExcpetion("Failed to close file", e);
		}
	}

	@Override
	public WritableGroup newGroup(String name) {
		return null;
	}
}
