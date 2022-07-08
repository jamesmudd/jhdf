package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.filter.PipelineFilterWithData;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReadFilters {

	public static void main(String[] args) {
		try (HdfFile hdfFile = new HdfFile(Paths.get(args[0]))) {
			Dataset dataset = hdfFile.getDatasetByPath(args[1]);
			List<PipelineFilterWithData> filters = dataset.getFilters();
			System.out.println(filters.stream().map(Objects::toString).collect(Collectors.joining(" -> "))); //NOSONAR - sout in example
		}
	}
}
