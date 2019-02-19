package io.jhdf.examples;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Node;

public class PrintTree {

	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		System.out.println(file.getName());

		try (HdfFile hdfFile = new HdfFile(file)) {
			recursivePrintGroup(hdfFile, 0);
		}
	}

	private static void recursivePrintGroup(Group group, int level) {
		level++;
		String indent = Collections.nCopies(level, "    ").stream().collect(joining());
		for (Node node : group) {
			System.out.println(indent + node.getName());
			if (node instanceof Group) {
				recursivePrintGroup((Group) node, level);
			}
		}
	}

}
