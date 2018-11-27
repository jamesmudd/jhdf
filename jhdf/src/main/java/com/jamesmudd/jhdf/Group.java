package com.jamesmudd.jhdf;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;

public class Group implements Node {

	private final String name;
	Map<String, Node> children = new LinkedHashMap<>();

	private final SymbolTableEntry symbolTableEntry;
	private final BTreeNode rootbTreeNode;
	private final LocalHeap rootNameHeap;

	private Group(FileChannel fc, Superblock sb, long steAddress, String name) {
		this.name = name;

		symbolTableEntry = new SymbolTableEntry(fc, steAddress, sb);
		rootbTreeNode = new BTreeNode(fc, symbolTableEntry.getBTreeAddress(), sb);
		rootNameHeap = new LocalHeap(fc, symbolTableEntry.getNameHeapAddress(), sb);

		final ByteBuffer bb = rootNameHeap.getDataBuffer();

		if (rootbTreeNode.getNodeType() == 0) { // groups
			for (long child : rootbTreeNode.getChildAddresses()) {
				GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(fc, child, sb);
				for (SymbolTableEntry e : groupSTE.getSymbolTableEntries()) {
					String childName = readName(bb, (int) e.getLinkNameOffset()); // TODO remove this cast
					if (e.getCacheType() == 1) { // Its a group
						Group group = createGroup(fc, sb, e.getAddress(), childName);
						children.put(childName, group);
					} else { // Dataset
						Dataset dataset = new Dataset(childName);
						children.put(childName, dataset);
					}
				}
			}
		}
	}

	private String readName(ByteBuffer bb, int linkNameOffset) {
		bb.position(linkNameOffset);
		return Utils.readUntilNull(bb);
	}

	@Override
	public boolean isGroup() {
		return true;
	}

	@Override
	public Map<String, Node> getChildren() {
		return children;
	}

	public static Group createGroup(FileChannel fc, Superblock sb, long steAddress, String name) {
		return new Group(fc, sb, steAddress, name);
	}

	@Override
	public String getName() {
		return name;
	}

}
