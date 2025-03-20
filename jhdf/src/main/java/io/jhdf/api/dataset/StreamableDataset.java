/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.api.dataset;

import io.jhdf.StreamableDatasetImpl;
import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;
import io.jhdf.object.message.DataSpace;


/**
 <H2>Interface for streamable datasets</H2>

 <p>This type allows for users to provide arbitrarily large datasets during Hdf5 node
 construction. The dataset is not provided as a whole as with
 {@link WritableGroup#putDataset(String, Object)}. Instead, a source of data chunks is provided
 in its place. The data source is only accessed when the dataset is written to storage.</p>

 <h2>Usage and Caveats</h2>

 <p>The structure of the chunks provided should be congruent to each other, divided along the
 major dimension as slices of an array. The other dimensions are not meant to be partitioned in
 any way. You will generally want to buffer large enough chunks of data to make bulk IO
 efficient, but small enough to avoid heap or mmap limitations.</p>

 <p>
 While it is not as fast as having the data in memory, you can still access some dataset
 properties by allowing them to be recomputed. This can be a wasteful operation, and is generally
 not desired during bulk write streaming. To enable it, you will have to call {@link #enableCompute()}
 first.</p>

 <p>
 You can set the dimensions of the data assuming you know what they should be, in which case you can
 also access {@link #getDimensions()}. Doing so without setting the dimensions will cause an
 error to be thrown. If the dimensions have been set, then they will be cross-checked at the time
 the dataset is written and an error will be thrown if they do not match. As with other
 properties, you can call {@link #enableCompute()} and then simply ask for the dimensions to be
 computed over the input stream.
 </p>

 @author Jonathan Shook */
public interface StreamableDataset extends WritableDataset {

  void enableCompute();

  /**
   Sets the dimensions of this dataset, partially or fully. For StreamableDatasets, this can be set
   before the data is buffered later, so that API calls can see the dimensions. However, if the
   dimensions do not match the dimensions of the data as calculated during writing, then an exception
   will be thrown. This means that for each chunk of data provided, All dimensions except dimension 0
   must match, and that after buffering data to storage, the primary dimension must match the total
   number of array elements of dimension 0 written. Dimensions are not required to be set, and
   will be calculated during stream processing otherwise.
   @param dimensions the dimensions to be applied to the current data shape
   */
  public void modifyDimensions(int[] dimensions);

  /**
   Creates a new StreamableDataset from the given chunk supplier. The chunk supplier must not be
   a one-shot iterable. In some cases, the data stream may be accessed more than once, such as
   when dynamic field widths are being calculated. This is only done when necessary.
   The first chunk is used to determine the type of the dataset and the dimensions of the dataset
   before the constructor returns. Thus, basic {@link io.jhdf.object.datatype.DataType} and basic
   {@link DataSpace} information is available. However, some elements, like total dimensions and
   actual field width padding will not be fully accurate until the dataset is written.
   @param chunkSupplier
   The source of data chunks to be written to the dataset.
   @param name
   The name of the dataset.
   @param parent
   The parent group to which the dataset will be added.
   @return The newly created StreamableDataset.
   */
  static StreamableDataset create(Iterable<?> chunkSupplier, String name, WritableGroup parent) {
    return new StreamableDatasetImpl(chunkSupplier, name, parent);
  }
}
