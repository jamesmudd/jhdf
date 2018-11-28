package com.jamesmudd.jhdf.examples;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.stream.Collectors;

import com.jamesmudd.jhdf.BTreeNode;
import com.jamesmudd.jhdf.GroupSymbolTableNode;
import com.jamesmudd.jhdf.LocalHeap;
import com.jamesmudd.jhdf.Superblock;
import com.jamesmudd.jhdf.SymbolTableEntry;
import com.jamesmudd.jhdf.Utils;

public class TreeRead {

	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		System.out.println(file.getName());

		try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fc = raf.getChannel()) {

			Superblock sb = new Superblock(fc, 0);

			SymbolTableEntry rootSTE = new SymbolTableEntry(fc, sb.getRootGroupSymbolTableAddress(), sb);

			printGroup(fc, sb, rootSTE, 0);
		}
	}

	private static void printGroup(FileChannel fc, Superblock sb, SymbolTableEntry ste, int level) {

		level++;

		BTreeNode rootbTreeNode = new BTreeNode(fc, ste.getBTreeAddress(), sb);
		LocalHeap rootNameHeap = new LocalHeap(fc, ste.getNameHeapAddress(), sb);
		if (rootbTreeNode.getNodeType() == 0) { // groups
			for (long child : rootbTreeNode.getChildAddresses()) {
				GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(fc, child, sb);
				for (SymbolTableEntry e : groupSTE.getSymbolTableEntries()) {
					printName(rootNameHeap, e.getLinkNameOffset(), level);
					if (e.getCacheType() == 1) { // Its a group
						printGroup(fc, sb, e, level);
					}
				}
			}
		}
	}

	private static void printName(LocalHeap rootNameHeap, long linkNameOffset, int level) {
		ByteBuffer bb = rootNameHeap.getDataBuffer();
		bb.position((int) linkNameOffset);
		String indent = Collections.nCopies(level, "    ").stream().collect(Collectors.joining(""));
		System.out.println(indent + Utils.readUntilNull(bb));
	}

}
