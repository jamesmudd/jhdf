/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf.benchmarks;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks reading slices from a large real-world chunked dataset. The data
 * file is not committed to git, download it by running
 * {@code src/jmh/resources/download_benchmark_data.py} (see
 * {@code src/jmh/resources/README.md}).
 * <p>
 * The dataset is {@code Experiments/__unnamed__/data} from the Zenodo record
 * <a href="https://doi.org/10.5281/zenodo.19882183">10.5281/zenodo.19882183</a>:
 * <ul>
 *     <li>shape (255, 255, 257, 257) uint8 (~4 GiB uncompressed)</li>
 *     <li>chunk shape (2, 4, 257, 257) (~516 KiB per chunk uncompressed)</li>
 *     <li>gzip compressed</li>
 * </ul>
 * Reading the whole dataset at once is deliberately not benchmarked, it
 * decompresses to ~4 GiB of small Java arrays which dominates the measurement
 * with allocation/GC noise. Instead slices of increasing size and alignment
 * are measured.
 * <p>
 * The file location can be overridden with the system property
 * {@code jmh.benchmark.data} or the environment variable
 * {@code JMH_BENCHMARK_DATA}.
 */
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
public class ChunkedDatasetReadBenchmark {

	private static final String DATASET_PATH = "Experiments/__unnamed__/data";
	private static final String DEFAULT_FILE = "src/jmh/resources/stem_data_binned2.hdf5";

	private HdfFile hdfFile;
	private Dataset dataset;

	@Setup(Level.Trial)
	public void setup() {
		Path path = resolveDataFile();
		hdfFile = new HdfFile(path);
		dataset = hdfFile.getDatasetByPath(DATASET_PATH);
	}

	@TearDown(Level.Trial)
	public void tearDown() {
		hdfFile.close();
	}

	/**
	 * Reads exactly one chunk (2, 4, 257, 257) = ~516 KiB decompressed.
	 */
	@Benchmark
	public void readSingleChunk(Blackhole blackhole) {
		blackhole.consume(dataset.getData(new long[]{120, 120, 0, 0}, new int[]{2, 4, 257, 257}));
	}

	/**
	 * Reads a single detector frame (1, 1, 257, 257) = ~64 KiB decompressed.
	 * A partial chunk read, the containing chunk must be fetched and decoded.
	 */
	@Benchmark
	public void readSingleFrame(Blackhole blackhole) {
		blackhole.consume(dataset.getData(new long[]{100, 100, 0, 0}, new int[]{1, 1, 257, 257}));
	}

	/**
	 * Reads one full row (1, 255, 257, 257) = ~16 MiB decompressed, spanning
	 * all 64 chunk columns of the dataset.
	 */
	@Benchmark
	public void readRowAcrossChunks(Blackhole blackhole) {
		blackhole.consume(dataset.getData(new long[]{7, 0, 0, 0}, new int[]{1, 255, 257, 257}));
	}

	/**
	 * Reads a chunk grid aligned slab (16, 255, 257, 257) = ~256 MiB
	 * decompressed, 512 chunks.
	 */
	@Benchmark
	public void readAlignedSlab(Blackhole blackhole) {
		blackhole.consume(dataset.getData(new long[]{32, 0, 0, 0}, new int[]{16, 255, 257, 257}));
	}

	/**
	 * Reads the same size slab as {@link #readAlignedSlab} but offset by 1 in
	 * the first 2 dimensions so every chunk contributes only partially,
	 * exercising the slice extraction path.
	 */
	@Benchmark
	public void readUnalignedSlab(Blackhole blackhole) {
		blackhole.consume(dataset.getData(new long[]{31, 1, 0, 0}, new int[]{16, 254, 257, 257}));
	}

	private static Path resolveDataFile() {
		String fromProperty = System.getProperty("jmh.benchmark.data");
		if (fromProperty != null) {
			return Paths.get(fromProperty);
		}
		String fromEnv = System.getenv("JMH_BENCHMARK_DATA");
		if (fromEnv != null) {
			return Paths.get(fromEnv);
		}
		Path path = Paths.get(DEFAULT_FILE).toAbsolutePath();
		if (!Files.exists(path)) {
			throw new IllegalStateException(
				"Benchmark data file not found: " + path + System.lineSeparator() +
					"Download it by running: python3 src/jmh/resources/download_benchmark_data.py" + System.lineSeparator() +
					"Or set -Djmh.benchmark.data=/path/to/stem_data_binned2.hdf5");
		}
		return path;
	}

}
