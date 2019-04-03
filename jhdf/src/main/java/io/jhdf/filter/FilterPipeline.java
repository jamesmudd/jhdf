package io.jhdf.filter;

import java.util.List;

import io.jhdf.exceptions.HdfFilterException;

/**
 * A collection of filters making up a ordered pipeline to decode chunks.
 * 
 * @author James Mudd
 */
public class FilterPipeline {

	private final List<PipelineFilter> filters;

	/* package */ FilterPipeline(List<PipelineFilter> filters) {
		this.filters = filters;
	}

	/**
	 * Applies this filter pipeline to decode data
	 * 
	 * @param encodedData the data to be decoded
	 * @return the decoded data
	 * @throws HdfFilterException if the decode operation fails
	 */
	public byte[] decode(byte[] encodedData) {

		byte[] data = encodedData;
		for (PipelineFilter b : filters) {
			data = b.decode(data);
		}

		return data;
	}

}
