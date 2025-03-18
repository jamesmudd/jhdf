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

import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Link;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.btree.BTreeV1;
import io.jhdf.btree.BTreeV2;
import io.jhdf.btree.record.LinkNameForIndexedGroupRecord;
import io.jhdf.dataset.DatasetLoader;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfInvalidPathException;
import io.jhdf.links.ExternalLink;
import io.jhdf.links.SoftLink;
import io.jhdf.object.message.DataSpaceMessage;
import io.jhdf.object.message.DataTypeMessage;
import io.jhdf.object.message.LinkInfoMessage;
import io.jhdf.object.message.LinkMessage;
import io.jhdf.object.message.SymbolTableMessage;
import io.jhdf.storage.HdfBackingStorage;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupImpl extends AbstractNode implements Group {
	private final class ChildrenLazyInitializer extends LazyInitializer<Map<String, Node>> {
		private final HdfBackingStorage hdfBackingStorage;
		private final Group parent;

		private ChildrenLazyInitializer(HdfBackingStorage hdfBackingStorage, Group parent) {
			this.hdfBackingStorage = hdfBackingStorage;
			this.parent = parent;
		}

		@Override
		protected Map<String, Node> initialize() throws ConcurrentException {
			logger.info("Lazy loading children of '{}'", getPath());

			if (header.get().hasMessageOfType(SymbolTableMessage.class)) {
				// Its an old style Group
				return createOldStyleGroup(header.get());
			} else {
				return createNewStyleGroup(header.get());
			}
		}

		private Map<String, Node> createNewStyleGroup(final ObjectHeader oh) {
			logger.debug("Loading 'new' style group");
			// Need to get a list of LinkMessages
			final List<LinkMessage> links;

			final LinkInfoMessage linkInfoMessage = oh.getMessageOfType(LinkInfoMessage.class);
			if (linkInfoMessage.getBTreeNameIndexAddress() == Constants.UNDEFINED_ADDRESS) {
				// Links stored compactly i.e in the object header, so get directly
				links = oh.getMessagesOfType(LinkMessage.class);
				logger.debug("Loaded group links from object header");
			} else {
				// Links are not stored compactly i.e in the fractal heap
				final BTreeV2<LinkNameForIndexedGroupRecord> bTreeNode = new BTreeV2<>(hdfBackingStorage,
					linkInfoMessage.getBTreeNameIndexAddress());
				final FractalHeap fractalHeap = new FractalHeap(hdfBackingStorage, linkInfoMessage.getFractalHeapAddress());

				List<LinkNameForIndexedGroupRecord> records = bTreeNode.getRecords();
				links = new ArrayList<>(records.size());
				for (LinkNameForIndexedGroupRecord linkName : records) {
					ByteBuffer id = linkName.getId();
					// Get the name data from the fractal heap
					ByteBuffer bb = fractalHeap.getId(id);
					links.add(LinkMessage.fromBuffer(bb, hdfBackingStorage.getSuperblock()));
				}
				logger.debug("Loaded group links from fractal heap");
			}

			final Map<String, Node> lazyChildren = new LinkedHashMap<>(links.size());
			for (LinkMessage link : links) {
				String linkName = link.getLinkName();
				switch (link.getLinkType()) {
					case HARD:
						long hardLinkAddress = link.getHardLinkAddress();
						final Node node = createNode(linkName, hardLinkAddress);
						lazyChildren.put(linkName, node);
						break;
					case SOFT:
						lazyChildren.put(linkName, new SoftLink(link.getSoftLink(), linkName, parent));
						break;
					case EXTERNAL:
						lazyChildren.put(linkName,
							new ExternalLink(link.getExternalFile(), link.getExternalPath(), linkName, parent));
						break;
				}
			}

			return lazyChildren;
		}

		private Map<String, Node> createOldStyleGroup(final ObjectHeader oh) {
			logger.debug("Loading 'old' style group");
			final SymbolTableMessage stm = oh.getMessageOfType(SymbolTableMessage.class);
			final BTreeV1 rootBTreeNode = BTreeV1.createGroupBTree(hdfBackingStorage, stm.getBTreeAddress());
			final LocalHeap rootNameHeap = new LocalHeap(hdfBackingStorage, stm.getLocalHeapAddress());
			final ByteBuffer nameBuffer = rootNameHeap.getDataBuffer();

			final List<Long> childAddresses = rootBTreeNode.getChildAddresses();
			final Map<String, Node> lazyChildren = new LinkedHashMap<>(childAddresses.size());

			for (long child : childAddresses) {
				GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(hdfBackingStorage, child);
				for (SymbolTableEntry ste : groupSTE.getSymbolTableEntries()) {
					String childName = readName(nameBuffer, ste.getLinkNameOffset());
					final Node node;
					switch (ste.getCacheType()) {
						case 0: // No cache
							node = createNode(childName, ste.getObjectHeaderAddress());
							break;
						case 1: // Cached group
							logger.trace("Creating group '{}'", childName);
							node = createGroup(hdfBackingStorage, ste.getObjectHeaderAddress(), childName, parent);
							break;
						case 2: // Soft Link
							logger.trace("Creating soft link '{}'", childName);
							String target = readName(nameBuffer, ste.getLinkValueOffset());
							node = new SoftLink(target, childName, parent);
							break;
						default:
							throw new HdfException(
								"Unrecognized symbol table entry cache type. Type was: " + ste.getCacheType());
					}
					lazyChildren.put(childName, node);
				}
			}
			return lazyChildren;
		}

		private Node createNode(String name, long address) {
			final ObjectHeader linkHeader = ObjectHeader.readObjectHeader(hdfBackingStorage, address);
			final Node node;
			if (linkHeader.hasMessageOfType(DataSpaceMessage.class)) {
				// Its a a Dataset
				logger.trace("Creating dataset [{}]", name);
				node = DatasetLoader.createDataset(hdfBackingStorage, linkHeader, name, parent);
			} else if (linkHeader.hasMessageOfType(DataTypeMessage.class)) {
				// Has a datatype but no dataspace so its a committed datatype
				logger.trace("Creating committed data type [{}]", name);
				node = new CommittedDatatype(hdfBackingStorage, address, name, parent);
			} else {
				// Its a group
				logger.trace("Creating group [{}]", name);
				node = createGroup(hdfBackingStorage, address, name, parent);
			}
			return node;
		}

		private String readName(ByteBuffer bb, int linkNameOffset) {
			bb.position(linkNameOffset);
			return Utils.readUntilNull(bb);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(GroupImpl.class);

	private final LazyInitializer<Map<String, Node>> children;

	private GroupImpl(HdfBackingStorage hdfBackingStorage, long address, String name, Group parent) {
		super(hdfBackingStorage, address, name, parent);
		logger.trace("Creating group '{}'...", name);

		children = new ChildrenLazyInitializer(hdfBackingStorage, this);

		logger.debug("Created group '{}'", getPath());
	}

	/**
	 * This is a special case constructor for the root group.
	 *
	 * @param hdfBackingStorage   The file channel for reading the file
	 * @param objectHeaderAddress The offset into the file of the object header for
	 *                            this group
	 * @param parent              For the root group the parent is the file itself.
	 */
	private GroupImpl(HdfBackingStorage hdfBackingStorage, long objectHeaderAddress, HdfFile parent) {
		super(hdfBackingStorage, objectHeaderAddress, "", parent); // No name special case for root group no name
		logger.trace("Creating root group...");

		// Special case for root group pass parent instead of this
		children = new ChildrenLazyInitializer(hdfBackingStorage, parent);

		logger.debug("Created root group of file '{}'", parent.getName());
	}

	/**
	 * Creates a group for the specified object header with the given name by
	 * reading from the file channel.
	 *
	 * @param hdfBackingStorage   The file channel for reading the file
	 * @param objectHeaderAddress The offset into the file of the object header for
	 *                            this group
	 * @param name                The name of this group
	 * @param parent              For the root group the parent is the file itself.
	 * @return The newly read group
	 */
	/* package */
	static Group createGroup(HdfBackingStorage hdfBackingStorage, long objectHeaderAddress, String name,
							 Group parent) {
		return new GroupImpl(hdfBackingStorage, objectHeaderAddress, name, parent);
	}

	/* package */
	static Group createRootGroup(HdfBackingStorage hdfBackingStorage, long objectHeaderAddress, HdfFile file) {
		// Call the special root group constructor
		return new GroupImpl(hdfBackingStorage, objectHeaderAddress, file);
	}

	@Override
	public Map<String, Node> getChildren() {
		try {
			return children.get();
		} catch (Exception e) {
			throw new HdfException(
				"Failed to load children for group '" + getPath() + "' at address '" + getAddress() + "'", e);
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
		} catch (Exception e) {
			throw new HdfException(
				"Failed to load children of group '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
	}

	@Override
	public Node getByPath(String path) {
		// Special case when path is dot
		if(path.equals(".")) {
			return this;
		}
		// Try splitting into 2 sections the child of this group and the remaining path
		// to pass down.
		final String[] pathElements = path.split(Constants.PATH_SEPARATOR, 2);
		Node child = getChild(pathElements[0]);
		// If we have a link try to resolve it
		if (child instanceof Link) {
			child = ((Link) child).getTarget();
		}
		if (pathElements.length == 1 && child != null) {
			// There is no remaining path to resolve so we have the result
			return child;
		} else if (child instanceof Group) {
			// The next level is also a group so try to keep resolving the remaining path
			return ((Group) child).getByPath(pathElements[1]);
		} else {
			// Path can't be resolved
			throw new HdfInvalidPathException(getPath() + path, getFileAsPath());
		}

	}

	@Override
	public Dataset getDatasetByPath(String path) {
		Node node = getByPath(path);
		if (node instanceof Link) {
			node = ((Link) node).getTarget();
		}
		if (node instanceof Dataset) {
			return (Dataset) node;
		} else {
			throw new HdfInvalidPathException(getPath() + path, getFileAsPath());
		}
	}

	@Override
	public boolean isLinkCreationOrderTracked() {
		ObjectHeader oh = getHeader();
		if (oh.hasMessageOfType(LinkInfoMessage.class)) {
			// New style, supports link creation tracking but might not be enabled
			return oh.getMessageOfType(LinkInfoMessage.class).isLinkCreationOrderTracked();
		} else {
			// Old style no support for link tracking
			return false;
		}
	}

}
