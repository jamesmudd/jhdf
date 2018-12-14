package io.jhdf.examples;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

import io.jhdf.Group;
import io.jhdf.HdfFile;
import io.jhdf.Node;

public class ApiTest {

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		File file = new File(args[0]);
		System.out.println(file.getName());

		try (HdfFile hdfFile = new HdfFile(file)) {
			recursivePrintGroup(hdfFile.getRootGroup(), 0);
		}

		System.out.println("Took " + (System.currentTimeMillis() - start) + " ms");
	}

	private static void recursivePrintGroup(Group group, int level) {
		level++;
		String indent = Collections.nCopies(level, "    ").stream().collect(Collectors.joining(""));
		for (Node node : group.getChildren().values()) {
			System.out.println(indent + node.getName());
			System.out.println(indent + node.getAttributes().keySet());
			if (node instanceof Group) {
				recursivePrintGroup((Group) node, level);
			}
		}
		level--;
	}

}
