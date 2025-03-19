/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.writing;

import io.jhdf.HdfFile;
import io.jhdf.StreamableDatasetImpl;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.dataset.StreamableDataset;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

class StreamableDatasetImplTest {

  private static final Logger log = LoggerFactory.getLogger(StreamableDatasetImplTest.class);

  @Test
  void testLargeDataset() {

    System.out.println("memory: " + Runtime.getRuntime().maxMemory());
    Path hdf5Out;
    try {
      hdf5Out = Files.createTempFile(
          Path.of("."), // defaulting to /tmp isn't great for large files testing
          this.getClass().getSimpleName() + "_BiggerThan2GbB_dataset",
          ".hdf5"
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    List<Integer> chunkIdx = List.of(0, 1, 2, 3);
    int chunkRows = (1024 * 1024) / Long.BYTES;
    int rowsize = 1024;

    IterableSource<Integer, long[][]> sf =
        new IterableSource<>(chunkIdx, i -> getArrayData(i, rowsize, chunkRows));

    try (WritableHdfFile out = HdfFile.write(hdf5Out)) {
      StreamableDataset sd = new StreamableDatasetImpl(sf, "", out);
      sd.modifyDimensions(new int[]{chunkRows * chunkIdx.size(), rowsize});
      out.putWritableDataset("testname", sd);
    }
  }

  private long[][] getArrayData(long offset, int rowsize, int rows) {
    long[][] data = new long[rows][rowsize];
    for (int i = 0; i < data.length; i++) {
      long[] row = new long[rowsize];
      Arrays.fill(row, offset + i);
      data[i] = row;
    }
    return data;
  }

  public static final class IterableSource<I, O> implements Iterable<O> {

    private final List<I> list;
    private final Iterator<I> iter;
    private final Function<I, O> f;

    IterableSource(List<I> in, Function<I, O> f) {
      this.list = in;
      this.iter = list.iterator();
      this.f = f;
    }

    @Override
    public Iterator<O> iterator() {
      return new Iter(this.list.iterator(), this.f);
    }

    public final static class Iter<I, O> implements Iterator<O> {
      private final Function<I, O> f;
      private final Iterator<I> iter;

      public Iter(Iterator<I> iterI, Function<I, O> f) {
        this.f = f;
        this.iter = iterI;
      }

      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public O next() {
        return f.apply(iter.next());
      }
    }
  }
}
