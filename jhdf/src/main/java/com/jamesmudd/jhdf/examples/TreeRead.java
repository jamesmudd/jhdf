package com.jamesmudd.jhdf.examples;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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
    Superblock sb = new Superblock(raf, 0);
    int sizeOfOffsets = sb.getSizeOfOffsets();
    int sizeOfLengths = sb.getSizeOfLengths();
    int leafK = sb.getGroupLeafNodeK();
    int internalK = sb.getGroupInternalNodeK();

    long time = System.currentTimeMillis();

    SymbolTableEntry rootSTE =
        new SymbolTableEntry(raf, sb.getRootGroupSymbolTableAddress(), sizeOfOffsets);

    printGroup(raf, sizeOfOffsets, sizeOfLengths, leafK, internalK, rootSTE, 0);

  }

  private static void printGroup(RandomAccessFile raf, int sizeOfOffsets, int sizeOfLengths,
      int leafK, int internalK, SymbolTableEntry ste, int level) {

    level++;

    BTreeNode rootbTreeNode =
        new BTreeNode(raf, ste.getBTreeAddress(), sizeOfOffsets, sizeOfLengths, leafK, internalK);
    LocalHeap rootNameHeap =
        new LocalHeap(raf, ste.getNameHeapAddress(), sizeOfOffsets, sizeOfLengths);
    if (rootbTreeNode.getNodeType() == 0) { // groups
      for (long child : rootbTreeNode.getChildAddresses()) {
        GroupSymbolTableNode groupSTE = new GroupSymbolTableNode(raf, child, sizeOfOffsets);
        for (SymbolTableEntry e : groupSTE.getSymbolTableEntries()) {
          printName(rootNameHeap, e.getLinkNameOffset(), level);
          if (e.getCacheType() == 1) { // Its a group
            printGroup(raf, sizeOfOffsets, sizeOfLengths, leafK, internalK, e, level);
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
