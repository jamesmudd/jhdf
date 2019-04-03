package io.jhdf.filter;

import java.util.ArrayList;
import java.util.List;

import io.jhdf.exceptions.HdfFilterException;

/**
 * A collection of filters making up a ordered pipeline to decode chunks.
 * 
 * @author James Mudd
 */
public class FilterPipeline {

	private class PipelineFilterWithData {

		final PipelineFilter filter;
		final int[] filterData;

		public PipelineFilterWithData(PipelineFilter filter, int[] filterData) {
			this.filter = filter;
			this.filterData = filterData;
		}

		byte[] decode(byte[] data) {
			return filter.decode(data, filterData);
		}
	}

	private final List<PipelineFilterWithData> filters = new ArrayList<>();

	/* package */ void addFilter(PipelineFilter filter, int[] data) {
		filters.add(new PipelineFilterWithData(filter, data));
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

		for (PipelineFilterWithData b : filters) {
			data = b.decode(data);
		}

		return data;
	}

}
