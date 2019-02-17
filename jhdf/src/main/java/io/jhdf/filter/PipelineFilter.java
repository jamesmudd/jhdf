package io.jhdf.filter;

import java.io.IOException;
import java.io.InputStream;

public interface PipelineFilter {

	InputStream getPipelineFilter(InputStream inputStream) throws IOException;

}
