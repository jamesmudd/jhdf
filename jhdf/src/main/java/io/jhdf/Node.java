package io.jhdf;

import java.io.File;
import java.util.Map;

import io.jhdf.object.message.AttributeMessage;

public interface Node {

	boolean isGroup();

	Node getParent();

	Map<String, Node> getChildren();

	String getName();

	String getPath();

	Map<String, AttributeMessage> getAttributes();

	String getType();

	default File getFile() {
		// Recurse back up to the file
		if (getParent() != null) {
			return getParent().getFile();
		}
		return getFile();
	}

	/**
	 * This returns the byte offset into the file of the object header for this
	 * node.
	 * 
	 * @return The address of the object header for this node
	 */
	long getAddress();

}
