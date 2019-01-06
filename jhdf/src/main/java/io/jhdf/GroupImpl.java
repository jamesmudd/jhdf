package io.jhdf;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.SymbolTableMessage;

public class GroupImpl implements Group {
	private static final Logger logger = LoggerFactory.getLogger(GroupImpl.class);

	private final String name;
	private final long address;
	private final Group parent;
	private final LazyInitializer<Map<String, Node>> children;
	private final LazyInitializer<Map<String, AttributeMessage>> attributes;

	private GroupImpl(FileChannel fc, Superblock sb, long bTreeAddress, long nameHeapAddress, long ojbectHeaderAddress,
			String name, Group parent) {
		this.name = name;
		this.address = ojbectHeaderAddress;
		this.parent = parent;

		children = new LazyInitializer<Map<String, Node>>() {

			@Override
			protected Map<String, Node> initialize() throws ConcurrentException {
				logger.info("Loading children of '{}'", getPath());

				final BTreeNode rootbTreeNode = new BTreeNode(fc, bTreeAddress, sb);
				final LocalHeap rootNameHeap = new LocalHeap(fc, nameHeapAddress, sb);
				final ByteBuffer nameBuffer = rootNameHeap.getDataBuffer();

				final Map<String, Node> lazyChildren = new LinkedHashMap<>(rootbTreeNode.getEntriesUsed());

				for (long child : rootbTreeNode.getChildAddresses()) {
					GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(fc, child, sb);
					for (SymbolTableEntry ste : groupSTE.getSymbolTableEntries()) {
						String childName = readName(nameBuffer, ste.getLinkNameOffset());
						if (ste.getCacheType() == 1) { // Its a group
							Group group = createGroup(fc, sb, ste.getObjectHeaderAddress(), childName, GroupImpl.this);
							lazyChildren.put(childName, group);
						} else { // Dataset
							Dataset dataset = new Dataset(fc, sb, ste.getObjectHeaderAddress(), childName,
									GroupImpl.this);
							lazyChildren.put(childName, dataset);
						}
					}
				}
				return lazyChildren;
			}
		};

		// Add attributes
		attributes = createAttributes(fc, sb, ojbectHeaderAddress);

		logger.debug("Created group '{}'", getPath());
	}

	private LazyInitializer<Map<String, AttributeMessage>> createAttributes(FileChannel fc, Superblock sb,
			long ojbectHeaderAddress) {
		return new LazyInitializer<Map<String, AttributeMessage>>() {
			@Override
			protected Map<String, AttributeMessage> initialize() throws ConcurrentException {
				final ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, ojbectHeaderAddress);
				return oh.getMessagesOfType(AttributeMessage.class).stream()
						.collect(toMap(AttributeMessage::getName, identity()));
			}
		};
	}

	private String readName(ByteBuffer bb, int linkNameOffset) {
		bb.position(linkNameOffset);
		return Utils.readUntilNull(bb);
	}

	private GroupImpl(FileChannel fc, Superblock sb, ObjectHeader oh, String name, Group parent) {
		this.name = name;
		this.address = oh.getAddress();
		this.parent = parent;

		List<LinkMessage> links = oh.getMessagesOfType(LinkMessage.class);
		children = new LazyInitializer<Map<String, Node>>() {

			@Override
			protected Map<String, Node> initialize() throws ConcurrentException {
				logger.info("Loading children of '{}'", getPath());

				LinkInfoMessage linkInfoMessage = oh.getMessagesOfType(LinkInfoMessage.class).get(0);

				if (linkInfoMessage.getbTreeNameIndexAddress() == Constants.UNDEFINED_ADDRESS) {

					final Map<String, Node> lazyChildren = new LinkedHashMap<>(links.size());
					for (LinkMessage link : links) {
						ObjectHeader linkHeader = ObjectHeader.readObjectHeader(fc, sb, link.getHardLinkAddress());
						if (!linkHeader.getMessagesOfType(DataSpaceMessage.class).isEmpty()) {
							// Its a a Dataset
							Dataset dataset = new Dataset(fc, sb, link.getHardLinkAddress(), link.getLinkName(),
									GroupImpl.this);
							lazyChildren.put(link.getLinkName(), dataset);
						} else {
							// Its a group
							GroupImpl group = createGroup(fc, sb, link.getHardLinkAddress(), link.getLinkName(),
									GroupImpl.this);
							lazyChildren.put(link.getLinkName(), group);
						}

					}
					return lazyChildren;
				} else {
					throw new UnsupportedHdfException("Only compact link storage is supported");
				}
			}
		};

		// Add attributes
		attributes = createAttributes(fc, sb, oh.getAddress());

		logger.debug("Created group '{}'", getPath());
	}

	/* package */ static GroupImpl createGroup(FileChannel fc, Superblock sb, long objectHeaderAddress, String name,
			Group parent) {
		ObjectHeader oh = ObjectHeader.readObjectHeader(fc, sb, objectHeaderAddress);

		if (oh.hasMessageOfType(SymbolTableMessage.class)) {
			// Its an old style Group
			SymbolTableMessage stm = oh.getMessageOfType(SymbolTableMessage.class);
			return new GroupImpl(fc, sb, stm.getbTreeAddress(), stm.getLocalHeapAddress(), objectHeaderAddress, name,
					parent);
		} else {
			// Its a new style group
			return new GroupImpl(fc, sb, oh, name, parent);
		}
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public Map<String, Node> getChildren() {
		try {
			return children.get();
		} catch (ConcurrentException e) {
			throw new HdfException(
					"Failed to load chirdren for group '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Group [name=" + name + ", path=" + getPath() + ", address=" + Utils.toHex(getAddress()) + "]";
	}

	@Override
	public String getPath() {
		return parent.getPath() + name + "/";
	}

	@Override
	public Map<String, AttributeMessage> getAttributes() {
		try {
			return attributes.get();
		} catch (ConcurrentException e) {
			throw new HdfException(
					"Failed to load attributes for group '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
	}

	@Override
	public String getType() {
		return "Group";
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public long getAddress() {
		return address;
	}

}
