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
		String pathname = "src/test/resources/com/jamesmudd/jhdf/test_file.hdf5";

		File file = new File(pathname);
		System.out.println(file.getName());

		RandomAccessFile raf = new RandomAccessFile(file, "r");
		FileChannel fc = raf.getChannel();
		Superblock sb = new Superblock(fc, 0);

		SymbolTableEntry rootSTE = new SymbolTableEntry(raf, sb.getRootGroupSymbolTableAddress(), sb);

		printGroup(raf, sb, rootSTE, 0);
	}

	private static void printGroup(RandomAccessFile raf, Superblock sb, SymbolTableEntry ste, int level) {

		level++;

		BTreeNode rootbTreeNode = new BTreeNode(raf, ste.getBTreeAddress(), sb);
		LocalHeap rootNameHeap = new LocalHeap(raf, ste.getNameHeapAddress(), sb);
		if (rootbTreeNode.getNodeType() == 0) { // groups
			for (long child : rootbTreeNode.getChildAddresses()) {
				GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(raf, child, sb);
				for (SymbolTableEntry e : groupSTE.getSymbolTableEntries()) {
					printName(rootNameHeap, e.getLinkNameOffset(), level);
					if (e.getCacheType() == 1) { // Its a group
						printGroup(raf, sb, e, level);
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
