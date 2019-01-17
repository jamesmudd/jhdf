package io.jhdf;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfInvalidPathException;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.links.ExternalLink;
import io.jhdf.links.SoftLink;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.SymbolTableMessage;

public class GroupImpl extends AbstractNode implements Group {
	private final class ChildrenLazyInitializer extends LazyInitializer<Map<String, Node>> {
		private final FileChannel fc;
		private final Superblock sb;
		private final Group parent;

		private ChildrenLazyInitializer(FileChannel fc, Superblock sb, Group parent) {
			this.fc = fc;
			this.sb = sb;
			this.parent = parent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.lang3.concurrent.LazyInitializer#initialize()
		 */
		@Override
		protected Map<String, Node> initialize() throws ConcurrentException {
			logger.info("Lazy loading children of '{}'", getPath());

			// Load the object header
			final ObjectHeader oh = objectHeader.get();

			if (oh.hasMessageOfType(SymbolTableMessage.class)) {
				// Its an old style Group
				final SymbolTableMessage stm = oh.getMessageOfType(SymbolTableMessage.class);
				final BTreeNode rootbTreeNode = BTreeNode.createBTreeNode(fc, sb, stm.getbTreeAddress());
				final LocalHeap rootNameHeap = new LocalHeap(fc, stm.getLocalHeapAddress(), sb);
				final ByteBuffer nameBuffer = rootNameHeap.getDataBuffer();

				List<Long> childAddresses = new ArrayList<>();
				getAllChildAddresses(rootbTreeNode, childAddresses);

				final Map<String, Node> lazyChildren = new LinkedHashMap<>(childAddresses.size());

				for (long child : childAddresses) {
					GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(fc, child, sb);
					for (SymbolTableEntry ste : groupSTE.getSymbolTableEntries()) {
						String childName = readName(nameBuffer, ste.getLinkNameOffset());
						final Node node;
						if (ste.getCacheType() == 1) { // Its a group
							node = createGroup(fc, sb, ste.getObjectHeaderAddress(), childName, parent);
						} else { // Dataset
							node = new DatasetImpl(fc, sb, ste.getObjectHeaderAddress(), childName, parent);
						}
						lazyChildren.put(childName, node);
					}
				}
				return lazyChildren;

			} else {
				// Its a new style group
				final List<LinkMessage> links = oh.getMessagesOfType(LinkMessage.class);
				final LinkInfoMessage linkInfoMessage = oh.getMessageOfType(LinkInfoMessage.class);

				if (linkInfoMessage.getbTreeNameIndexAddress() == Constants.UNDEFINED_ADDRESS) {
					final Map<String, Node> lazyChildren = new LinkedHashMap<>(links.size());
					for (LinkMessage link : links) {
						switch (link.getLinkType()) {
						case HARD:
							ObjectHeader linkHeader = ObjectHeader.readObjectHeader(fc, sb, link.getHardLinkAddress());
							final Node node;
							if (linkHeader.hasMessageOfType(DataSpaceMessage.class)) {
								// Its a a Dataset
								node = new DatasetImpl(fc, sb, link.getHardLinkAddress(), link.getLinkName(), parent);
							} else {
								// Its a group
								node = createGroup(fc, sb, link.getHardLinkAddress(), link.getLinkName(), parent);
							}
							lazyChildren.put(link.getLinkName(), node);
							break;
						case SOFT:
							lazyChildren.put(link.getLinkName(),
									new SoftLink(link.getSoftLink(), link.getLinkName(), parent));
							break;
						case EXTERNAL:
							lazyChildren.put(link.getLinkName(), new ExternalLink(link.getExternalFile(),
									link.getExternalPath(), link.getLinkName(), parent));
							break;
						}

					}
					return lazyChildren;
				} else {
					// Links are not stored compactly
					final long bTreeNameIndexAddress = linkInfoMessage.getbTreeNameIndexAddress();
					BTreeNode bTreeNode = BTreeNode.createBTreeNode(fc, sb, bTreeNameIndexAddress);
					throw new UnsupportedHdfException("Only compact link storage is supported");
				}
			}
		}

		private void getAllChildAddresses(BTreeNode rootbTreeNode, List<Long> childAddresses) {
			if (rootbTreeNode.getNodeLevel() > 0) {
				for (long child : rootbTreeNode.getChildAddresses()) {
					BTreeNode bTreeNode = BTreeNode.createBTreeNode(fc, sb, child);
					getAllChildAddresses(bTreeNode, childAddresses);
				}
			} else {
				childAddresses.addAll(Longs.asList(rootbTreeNode.getChildAddresses()));
			}
		}

		private String readName(ByteBuffer bb, int linkNameOffset) {
			bb.position(linkNameOffset);
			return Utils.readUntilNull(bb);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(GroupImpl.class);

	private final LazyInitializer<ObjectHeader> objectHeader;
	private final LazyInitializer<Map<String, Node>> children;
	private final LazyInitializer<Map<String, AttributeMessage>> attributes;

	private GroupImpl(FileChannel fc, Superblock sb, long address, String name, Group parent) {
		super(address, name, parent);
		logger.trace("Creating group '{}'...", name);

		this.objectHeader = ObjectHeader.lazyReadObjectHeader(fc, sb, address);

		children = new ChildrenLazyInitializer(fc, sb, this);

		// Add attributes
		attributes = new AttributesLazyInitializer(objectHeader);

		logger.debug("Created group '{}'", getPath());
	}

	/**
	 * This is a special case constructor for the root group.
	 * 
	 * @param fc                  The file channel for reading the file
	 * @param sb                  The HDF superblock for this file
	 * @param objectHeaderAddress The offset into the file of the object header for
	 *                            this group
	 * @param parent              For the root group the parent is the file itself.
	 */
	private GroupImpl(FileChannel fc, Superblock sb, long objectHeaderAddress, HdfFile parent) {
		super(objectHeaderAddress, "", parent); // No name special case for root group no name
		logger.trace("Creating root group...");

		this.objectHeader = ObjectHeader.lazyReadObjectHeader(fc, sb, objectHeaderAddress);

		// Special case for root group pass parent instead of this
		children = new ChildrenLazyInitializer(fc, sb, parent);

		// Add attributes
		attributes = new AttributesLazyInitializer(objectHeader);

		logger.debug("Created root group of file '{}'", parent.getName());
	}

	/**
	 * Creates a group for the specified object header with the given name by
	 * reading from the file channel.
	 * 
	 * @param fc                  The file channel for reading the file
	 * @param sb                  The HDF superblock for this file
	 * @param objectHeaderAddress The offset into the file of the object header for
	 *                            this group
	 * @param name                The name of this group
	 * @param parent              For the root group the parent is the file itself.
	 * @return The newly read group
	 */
	/* package */ static Group createGroup(FileChannel fc, Superblock sb, long objectHeaderAddress, String name,
			Group parent) {
		return new GroupImpl(fc, sb, objectHeaderAddress, name, parent);
	}

	/* package */ static Group createRootGroup(FileChannel fc, Superblock sb, long objectHeaderAddress, HdfFile file) {
		// Call the special root group constructor
		return new GroupImpl(fc, sb, objectHeaderAddress, file);
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
	public String toString() {
		return "Group [name=" + name + ", path=" + getPath() + ", address=" + Utils.toHex(getAddress()) + "]";
	}

	@Override
	public String getPath() {
		return super.getPath() + "/";
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
	public NodeType getType() {
		return NodeType.GROUP;
	}

	@Override
	public Iterator<Node> iterator() {
		return getChildren().values().iterator();
	}

	@Override
	public Node getChild(String name) {
		try {
			return children.get().get(name);
		} catch (ConcurrentException e) {
			throw new HdfException(
					"Failed to load childen of group '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
	}

	@Override
	public Node getByPath(String path) {
		// Try splitting into 2 sections the child of this group and the remaining path
		// to pass down.
		final String[] pathElements = path.split(Constants.PATH_SEPERATOR, 2);
		final Node child = getChild(pathElements[0]);
		if (pathElements.length == 1) {
			// There is no remaing path to resolve so we have the result
			return child;
		} else if (child instanceof Group) {
			// The next level is also a group so try to keep resolving the remaining path
			return ((Group) child).getByPath(pathElements[1]);
		} else {
			// Path can't be resolved
			throw new HdfInvalidPathException(getPath() + path, getFile());
		}

	}

	@Override
	public Dataset getDatasetByPath(String path) {
		Node node = getByPath(path);
		if (node instanceof Dataset) {
			return (Dataset) node;
		} else {
			throw new HdfInvalidPathException(getPath() + path, getFile());

		}
	}

}
