package io.jhdf;

import java.util.Map;

import io.jhdf.object.message.AttributeMessage;

public interface Node {

	boolean isGroup();

	Map<String, Node> getChildren();

	String getName();

	String getPath();

	Map<String, AttributeMessage> getAttributes();

	String getType();

}
