/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright (c) 2023 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;

/**
 * An example of recursively parsing a HDF5 file tree and printing it to the
 * console.
 *
 * Pass file path as argument
 *
 * @author James Mudd
 */
public class PrintTree {

	/**
	 * @param args ["path/to/file.hdf5""]
	 */
	public static void main(String[] args) {
		try (HdfFile hdfFile = new HdfFile(Paths.get(args[0]))) {
			System.out.println(hdfFile.getFile().getName()); //NOSONAR - sout in example
			recursivePrintGroup(hdfFile, 0);
		}
	}

	private static void recursivePrintGroup(Group group, int level) {
		level++;
		String indent = StringUtils.repeat("    ", level);
		for (Node node : group) {
			System.out.println(indent + node.getName()); //NOSONAR - sout in example
			if (node instanceof Group) {
				recursivePrintGroup((Group) node, level);
			}
		}
	}

}
