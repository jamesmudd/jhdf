/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2024 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf;

import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.OrderedDataType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.util.stream.Stream;

import static io.jhdf.TestUtils.loadTestHdfFile;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class CommittedDatatypeTest {

	private static final String HDF5_TEST_FILE_NAME = "committed_datatypes.hdf5";
	private static HdfFile hdfFile;

	@BeforeAll
	static void beforeAll() throws Exception {
		hdfFile = loadTestHdfFile(HDF5_TEST_FILE_NAME);
	}

	@ParameterizedTest
	@MethodSource
	void readCommittedDatatype(String path, Class<?> javaType, ByteOrder byteOrder, int bytes) {
		Node node = hdfFile.getByPath(path);
		assertThat(node.getType(), is(NodeType.COMMITTED_DATATYPE));
		DataType dataType = ((CommittedDatatype) node).getDataType();
		assertThat(dataType.getJavaType(), is(javaType));
		assertThat(dataType.getSize(), is(bytes));
		assertThat(((OrderedDataType) dataType).getByteOrder(), is(byteOrder));
	}

	static Stream<Arguments> readCommittedDatatype() {
		return Stream.of(
			Arguments.of("float32_LE", float.class, LITTLE_ENDIAN, 4),
			Arguments.of("float64_BE", double.class, LITTLE_ENDIAN, 8),
			Arguments.of("int32_LE", int.class, LITTLE_ENDIAN, 4),
			Arguments.of("int32_BE", int.class, LITTLE_ENDIAN, 4)
		);
	}

	@Test
	void testGettingCommittedDatatypeByAddress() {
		Node node = hdfFile.getNodeByAddress(1208L);
		assertThat(node, isA(CommittedDatatype.class));
		assertThat(node.getType(), is(NodeType.COMMITTED_DATATYPE));
	}
}
