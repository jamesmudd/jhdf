package com.jamesmudd.jhdf;

import java.util.Map;

import com.jamesmudd.jhdf.object.message.AttributeMessage;

public interface Node {

	boolean isGroup();

	Map<String, Node> getChildren();

	String getName();

	String getPath();

	Map<String, AttributeMessage> getAttributes();

}
