package io.jhdf.api.dataset;

import io.jhdf.StreamableDatasetImpl;
import io.jhdf.api.WritableDataset;
import io.jhdf.api.WritableGroup;
import io.jhdf.object.message.DataSpace;


/**
 Interface for streamable datasets
 <hr/>

 <p>This type allows for users to provide arbitrarily large datasets during Hdf5 node
 construction. The dataset is not provided as a whole as with
 {@link WritableGroup#putDataset(String, Object)}. Instead, a source of data chunks is provided
 in its place. The data source is only accessed when the dataset is written to storage.</p>

 <h2>Usage and Caveats</h2>

 <p>The structure of the chunks provided should be congruent to each other, divided among the
 major dimension as slices of an array. The other dimensions are not meant to be partitioned in
 any way.</p>

 <p>
 Since the dataset content is not known at the tie it is added to the group, it is not
 possible to access the contents of the dataset as you would normally do with
 {@link WritableDataset#getData()} or {@link WritableDataset#getDataFlat()} methods. However, you
 can set the dimensions of the data assuming you know what they should be, in which case you can
 also access {@link #getDimensions()}. Doing so without setting the dimensions will cause an
 error to be thrown. If the dimensions have been set, then they will be cross-checked at the time
 the dataset is written and an error will be thrown if they do not match.
 </p>
 @author Jonathan Shook */
public interface StreamableDataset extends WritableDataset {

  /**
   Sets the dimensions of this dataset, partially or fully. For StreamableDatasets, this can be set
   before the data is buffered later, so that API calls can see the dimensions. However, if the
   dimensions do not match the dimensions of the data as calculated during writing, then an exception
   will be thrown. This means that for each chunk of data provided, All dimensions except dimension 0
   must match, and that after buffering data to storage, the primary dimension must match the total
   number of array elements of dimension 0 written.
   */
  void modifyDimensions(int[] dimensions);

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
