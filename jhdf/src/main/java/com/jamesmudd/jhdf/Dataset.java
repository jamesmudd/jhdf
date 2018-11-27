package com.jamesmudd.jhdf;

import java.util.Map;

public class Dataset implements Node {

	private final String name;

	public Dataset(String name) {
		this.name = name;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isGroup() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, Node> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

}
