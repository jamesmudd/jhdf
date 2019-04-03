package io.jhdf.filter;

public class ByteShuffleFilter implements PipelineFilter {

	/** Bytes in each element e.g float32 = 4 bytes */
	private final int dataSize;

	public ByteShuffleFilter(int[] data) {
		dataSize = data[0];
	}

	@Override
	public String getName() {
		return "shuffle";
	}

	@Override
	public byte[] decode(byte[] data) {

		// A quick shortcut if no shuffling is needed
		if (dataSize == 1) {
			return data;
		}

		final int elements = data.length / dataSize;

		// shuffle doesn't change the size of the data it rearranges it
		final byte[] out = new byte[data.length];

		int pos = 0;
		for (int i = 0; i < dataSize; i++) {
			for (int j = 0; j < elements; j++) {
				out[j * dataSize + i] = data[pos];
				pos++; // step through the input array
			}
		}

		return out;
	}
}
