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

import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.btree.BTreeV2;
import io.jhdf.btree.record.AttributeNameForIndexedAttributesRecord;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.message.AttributeInfoMessage;
import io.jhdf.object.message.AttributeMessage;
import io.jhdf.object.message.Message;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public abstract class AbstractNode implements Node {
	private static final Logger logger = LoggerFactory.getLogger(AbstractNode.class);

	protected final class AttributesLazyInitializer extends LazyInitializer<Map<String, Attribute>> {
		private final LazyInitializer<ObjectHeader> lazyObjectHeader;

		public AttributesLazyInitializer(LazyInitializer<ObjectHeader> lazyObjectHeader) {
			this.lazyObjectHeader = lazyObjectHeader;
		}

		@Override
		protected Map<String, Attribute> initialize() throws ConcurrentException {
			logger.debug("Lazy initializing attributes for '{}'", getPath());
			final ObjectHeader oh = lazyObjectHeader.get();

			List<AttributeMessage> attributeMessages = new ArrayList<>();

			if (oh.hasMessageOfType(AttributeInfoMessage.class)) {
				// Attributes stored in b-tree
				AttributeInfoMessage attributeInfoMessage = oh.getMessageOfType(AttributeInfoMessage.class);

				if (attributeInfoMessage.getFractalHeapAddress() != Constants.UNDEFINED_ADDRESS) {
					// Create the heap and btree
					FractalHeap fractalHeap = new FractalHeap(hdfFc, attributeInfoMessage.getFractalHeapAddress());
					BTreeV2<AttributeNameForIndexedAttributesRecord> btree = new BTreeV2<>(hdfFc,
							attributeInfoMessage.getAttributeNameBTreeAddress());

					// Read the attribute messages from the btree+heap
					for (AttributeNameForIndexedAttributesRecord attributeRecord : btree.getRecords()) {
						ByteBuffer bb = fractalHeap.getId(attributeRecord.getHeapId());
						AttributeMessage attributeMessage = new AttributeMessage(bb, hdfFc.getSuperblock(),
								attributeRecord.getFlags());
						logger.trace("Read attribute message '{}'", attributeMessage);
						attributeMessages.add(attributeMessage);
					}
				}
			}

			// Add the messages stored directly in the header
			attributeMessages.addAll(oh.getMessagesOfType(AttributeMessage.class));

			return attributeMessages.stream()
					.collect(
							toMap(AttributeMessage::getName,
									message -> new AttributeImpl(hdfFc, AbstractNode.this, message)));
		}
	}

	private final HdfFileChannel hdfFc;
	protected final long address;
	protected final String name;
	protected final Group parent;
	protected final LazyInitializer<ObjectHeader> header;
	protected final AttributesLazyInitializer attributes;

	public AbstractNode(HdfFileChannel hdfFc, long address, String name, Group parent) {
		this.hdfFc = hdfFc;
		this.address = address;
		this.name = name;
		this.parent = parent;

		try {
			header = ObjectHeader.lazyReadObjectHeader(hdfFc, address);

			// Attributes
			attributes = new AttributesLazyInitializer(header);
		} catch (Exception e) {
			throw new HdfException("Error reading node '" + getPath() + "' at address " + address, e);
		}
	}

	@Override
	public boolean isGroup() {
		return getType() == NodeType.GROUP;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return parent.getPath() + name;
	}

	@Override
	public Group getParent() {
		return parent;
	}

	@Override
	public long getAddress() {
		return address;
	}

	@Override
	public File getFile() {
		// Recurse back up to the file
		return getParent().getFile();
	}

	@Override
	public HdfFile getHdfFile() {
		return getParent().getHdfFile();
	}

	@Override
	public boolean isLink() {
		return false;
	}

	protected <T extends Message> T getHeaderMessage(Class<T> clazz) {
		return getHeader().getMessageOfType(clazz);
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		try {
			return attributes.get();
		} catch (Exception e) {
			throw new HdfException(
					"Failed to load attributes for '" + getPath() + "' at address '" + getAddress() + "'", e);
		}
	}

	@Override
	public Attribute getAttribute(String name) {
		return getAttributes().get(name);
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return getHeader().isAttributeCreationOrderTracked();
	}

	public ObjectHeader getHeader() {
		try {
			return header.get();
		} catch (Exception e) {
			throw new HdfException("Failed reading header for '" + getPath() + "' at address '" + getAddress() + "'",
					e);
		}
	}
}
