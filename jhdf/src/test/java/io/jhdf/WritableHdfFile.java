package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritiableDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class WritableHdfFile implements WritableGroup, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(WritableHdfFile.class);
	private final Path path;

	@Override
	public Map<String, Node> getChildren() {
		return rootGroup.getChildren();
	}

	@Override
	public Node getChild(String name) {
		return rootGroup.getChild(name);
	}

	@Override
	public Node getByPath(String path) {
		return rootGroup.getByPath(path);
	}

	@Override
	public Dataset getDatasetByPath(String path) {
		return rootGroup.getDatasetByPath(path);
	}

	@Override
	public boolean isLinkCreationOrderTracked() {
		return rootGroup.isLinkCreationOrderTracked();
	}

	@Override
	public Group getParent() {
		return rootGroup.getParent();
	}

	@Override
	public String getName() {
		return rootGroup.getName();
	}

	@Override
	public String getPath() {
		return rootGroup.getPath();
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return rootGroup.getAttributes();
	}

	@Override
	public Attribute getAttribute(String name) {
		return rootGroup.getAttribute(name);
	}

	@Override
	public NodeType getType() {
		return rootGroup.getType();
	}

	@Override
	public boolean isGroup() {
		return rootGroup.isGroup();
	}

	@Override
	public File getFile() {
		return path.toFile();
	}

	@Override
	public Path getFileAsPath() {
		return rootGroup.getFileAsPath();
	}

	@Override
	public HdfFile getHdfFile() {
		return rootGroup.getHdfFile();
	}

	@Override
	public long getAddress() {
		return rootGroup.getAddress();
	}

	@Override
	public boolean isLink() {
		return rootGroup.isLink();
	}

	@Override
	public boolean isAttributeCreationOrderTracked() {
		return rootGroup.isAttributeCreationOrderTracked();
	}

	@Override
	public WritiableDataset putDataset(String name, Object data) {
		return rootGroup.putDataset(name, data);
	}

	@Override
	public WritableGroup putGroup(String name) {
		return rootGroup.putGroup(name);
	}

	@Override
	public Iterator<Node> iterator() {
		return rootGroup.iterator();
	}

	@Override
	public void forEach(Consumer<? super Node> action) {
		rootGroup.forEach(action);
	}

	@Override
	public Spliterator<Node> spliterator() {
		return rootGroup.spliterator();
	}

	final WritableGroup rootGroup;

	public WritableHdfFile(Path path) {
		this.path = path;
		rootGroup = new WritableGroupImpl(null, "/");
		logger.info("Created writable HDF5 file [{}]", path.toAbsolutePath());
	}

	@Override
	public void close() throws Exception {
		// TODO impl
		logger.info("Closing file [{}]", path.toAbsolutePath());
	}
}
