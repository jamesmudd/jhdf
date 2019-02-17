package io.jhdf.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class GzipPipelineFilter implements PipelineFilter {

	@Override
	public InputStream getPipelineFilter(InputStream inputStream) throws IOException {
		return new InflaterInputStream(inputStream, new Inflater());
	}

}
