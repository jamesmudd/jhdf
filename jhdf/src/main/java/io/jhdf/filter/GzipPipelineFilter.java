package io.jhdf.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipPipelineFilter implements PipelineFilter {

	public InputStream getPipelineFilter(InputStream inputStream) throws IOException {
		return new GZIPInputStream(inputStream);
	}

}
