package com.jamesmudd.jhdf;

import java.util.Map;

public interface Node {

	boolean isGroup();

	Map<String, Node> getChildren();

	String getName();

}
