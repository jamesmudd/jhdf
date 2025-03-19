/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * https://jhdf.io
 *
 * Copyright (c) 2025 James Mudd
 *
 * MIT License see 'LICENSE' file
 */

package io.jhdf;

import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.NodeType;
import io.jhdf.api.dataset.StreamableDataset;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfWritingException;
import io.jhdf.filter.PipelineFilterWithData;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.message.*;
import io.jhdf.object.message.DataLayoutMessage.ContiguousDataLayoutMessage;
import io.jhdf.storage.HdfFileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static io.jhdf.Utils.stripLeadingIndex;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

/**
 TODO: This won't work well for variable sized and padded types like Strings */
public class StreamableDatasetImpl extends AbstractWritableNode implements StreamableDataset {

  private static final Logger logger = LoggerFactory.getLogger(StreamableDatasetImpl.class);

  //	private final Object data;
  //  private final DataType dataTypePrototype;
  private final DataSpace dataSpacePrototype;
  //
  private DataSpace userDimensionedSpace;
  private final Iterable<?> iterable;
  // will double-buffer, this is only the hand-off, not the pre-read
  private final LinkedBlockingQueue<Object> chunks;
  private boolean computeEnabled;

  private DataSpace computedDataSpace;
  private DataType computedDataType;

  public enum Buffering {
    Single(1),
    Double(2),
    Triple(3);
    public final int buffers;

    Buffering(int i) {
      this.buffers = i;
    }
  }

  public void enableCompute() {
    this.computeEnabled = true;
  }

  /**
   Create a {@link StreamableDataset} with default double buffering.
   @param chunkSupplier
   The source of data chunks to be written to the dataset
   @param name
   The name of the dataset
   @param parent
   The parent group of the dataset
   */
  public StreamableDatasetImpl(Iterable<?> chunkSupplier, String name, Group parent) {
    this(chunkSupplier, name, parent, Buffering.Double);
  }

  /**
   Create a {@link StreamableDataset}
   @param chunkSupplier
   The source of data chunks to be written to the dataset
   @param name
   The name of the dataset
   @param parent
   The parent group of the dataset
   @param buffering
   The number of buffers to use for buffering the data. A separate thread will
   read-ahead to fill the buffers from the chunk supplier.
   */
  public StreamableDatasetImpl(
      Iterable<?> chunkSupplier,
      String name,
      Group parent,
      Buffering buffering
  )
  {
    super(parent, name);
    this.iterable = chunkSupplier;
    this.chunks = new LinkedBlockingQueue<>(buffering.buffers);

    Object example = chunkSupplier.iterator().next();

    if (example == null) {
      throw new HdfWritingException("Example (first chunk) of array for '" + name + "' was null");
    }
    this.dataSpacePrototype = DataSpace.fromObject(example);
    this.computedDataType = DataType.fromObject(example);
  }

  @Override
  public long getSize() {
    ensureComputeEnabled("getSize()");
    return getDataSpace().getTotalLength();
  }

  public DataSpace getDataSpace() {
    ensureComputeEnabled("getDataSpace()");
    if (this.computedDataSpace == null) {
      Iterator<?> scanner = this.iterable.iterator();
      DataSpace accumulator = null;
      while (scanner.hasNext()) {
        Object chunk = scanner.next();
        DataSpace chunkSpace = DataSpace.fromObject(chunk);
        accumulator = accumulator == null ? chunkSpace : accumulator.combineDim0(chunkSpace);
      }
      this.computedDataSpace = accumulator;
    }
    return this.computedDataSpace;
  }

  private void ensureComputeEnabled(String method, String... additional) {
    if (!computeEnabled) {
      throw new HdfException("For streaming datasets, some properties are not available without "
                             + "(possibly intenstive) compute over the input stream. If you want "
                             + "to use the " + method + " method, then you can call "
                             + ".enableCompute() first. Note that such methods will not be "
                             + "efficient for large datasets. " + String.join(",", additional));
    }
  }

  @Override
  public long getSizeInBytes() {
    ensureComputeEnabled("getSizeInBytes()");
    return getSize() * getDataType().getSize();

  }

  @Override
  public long getStorageInBytes() {
    ensureComputeEnabled("getStorageInBytes()");
    // As there is no compression this is correct ATM
    return getSizeInBytes();
  }

  // Todo dims are incorrect until after the data is flushed
  @Override
  public int[] getDimensions() {
    if (userDimensionedSpace != null) {
      return userDimensionedSpace.getDimensions();
    } else {
      ensureComputeEnabled(
          "getDimensions()",
          "Alternately, for dimensions, you may set them with"
          + " modifyDimensions first to assume and then "
          + "confirm dimensions during streaming to storage."
      );
      return getDataSpace().getDimensions();
    }
  }

  @Override
  public void modifyDimensions(int[] dimensions) {
    this.userDimensionedSpace = DataSpace.modifyDimensions(this.dataSpacePrototype, dimensions);
    logger.debug("user dimensions:" + userDimensionedSpace);
  }

  @Override
  public boolean isScalar() {
    ensureComputeEnabled("isScalar()");
    if (isEmpty()) {
      return false;
    }
    return getDimensions().length == 0;
  }

  @Override
  public boolean isEmpty() {
    return !iterable.iterator().hasNext();
  }

  @Override
  public boolean isCompound() {
    return false;
  }

  @Override
  public boolean isVariableLength() {
    return false;
  }

  @Override
  public long[] getMaxSize() {
    if (userDimensionedSpace != null) {
      return userDimensionedSpace.getMaxSizes();
    } else {
      ensureComputeEnabled("getMaxSize()");
      return getDataSpace().getMaxSizes();
    }
  }

  @Override
  public DataLayout getDataLayout() {
    // ATM we only support contiguous
    return DataLayout.CONTIGUOUS;
  }

  @Override
  public Object getData() {
    throw new HdfWritingException("Slicing a streamable dataset source not supported");
  }

  @Override
  public Object getDataFlat() {
    throw new HdfWritingException("Slicing a streamable dataset source not supported");
  }

  @Override
  public Object getData(long[] sliceOffset, int[] sliceDimensions) {
    throw new HdfWritingException("Slicing a writable dataset not supported");
  }

  @Override
  public Class<?> getJavaType() {
    ensureComputeEnabled("getJavaType()");
    final Class<?> type = getDataType().getJavaType();
    // For scalar datasets the returned type will be the wrapper class because
    // getData returns Object
    if (isScalar() && type.isPrimitive()) {
      return primitiveToWrapper(type);
    }
    return type;
  }

  @Override
  public DataType getDataType() {
    ensureComputeEnabled("getComputedDataType");
    // Even though this could be faster for some types, we leave it as a computed type for the
    // cases where it definitely would not be. Nobody wants their code to have to change because
    // someone else decided to use Strings on a new dataset.
    if (this.computedDataType == null) {
      this.computedDataType = computeDataType();
    }
    return this.computedDataType;
  }

  private DataType computeDataType() {
    if (this.computedDataType == null) {
      Iterator<?> scanner = this.iterable.iterator();
      DataType computed = null;
      if (scanner.hasNext()) {
        computed = DataType.fromObject(scanner.next());
      }
      if (computed.getJavaType().equals(String.class)) {
        DataType typeAccumulator = null;
        while (scanner.hasNext()) {
          Object chunk = scanner.next();
          DataType chunkType = DataType.fromObject(chunk);
          computed = computed.reduce(chunkType);
        }
      }
      this.computedDataType = computed;
    }
    return this.computedDataType;
  }

  @Override
  public Object getFillValue() {
    return null;
  }

  @Override
  public List<PipelineFilterWithData> getFilters() {
    // ATM no filters support
    return Collections.emptyList();
  }

  @Override
  public NodeType getType() {
    return NodeType.DATASET;
  }

  @Override
  public boolean isGroup() {
    return false;
  }

  @Override
  public File getFile() {
    return getParent().getFile();
  }

  @Override
  public Path getFileAsPath() {
    return getParent().getFileAsPath();
  }

  @Override
  public HdfFile getHdfFile() {
    return getParent().getHdfFile();
  }

  @Override
  public long getAddress() {
    throw new HdfWritingException("Address not known until written");
  }

  @Override
  public boolean isLink() {
    return false;
  }

  @Override
  public boolean isAttributeCreationOrderTracked() {
    return false;
  }

  @Override
  public long write(HdfFileChannel hdfFileChannel, long position) {
    logger.info("Writing dataset via chunked reads [{}] at position [{}]", getPath(), position);
    List<Message> messages = new ArrayList<>();
    messages.add(DataTypeMessage.create(computeDataType()));

    Message dataSpaceMessagePlaceholder = DataSpaceMessage.create(this.userDimensionedSpace);
    messages.add(dataSpaceMessagePlaceholder);
    messages.add(FillValueMessage.NO_FILL);
    // TODO will have know fixed size so don't really need these objects but for now...
    ContiguousDataLayoutMessage placeholder =
        ContiguousDataLayoutMessage.create(
            Constants.UNDEFINED_ADDRESS,
            Constants.UNDEFINED_ADDRESS
        );
    messages.add(placeholder);

    if (!getAttributes().isEmpty()) {
      AttributeInfoMessage attributeInfoMessage = AttributeInfoMessage.create();
      messages.add(attributeInfoMessage);
      for (Map.Entry<String, Attribute> attribute : getAttributes().entrySet()) {
        logger.info("Writing attribute [{}]", attribute.getKey());
        AttributeMessage attributeMessage =
            AttributeMessage.create(attribute.getKey(), attribute.getValue());
        messages.add(attributeMessage);
      }
    }

    ObjectHeader.ObjectHeaderV2 objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);
    int ohSize = objectHeader.toBuffer().limit();

    // Now know where we will write the data
    long dataAddress = position + ohSize;
    DataSpace actualDataSpace = writeData(hdfFileChannel, dataAddress);

    // Now switch placeholder for real data layout message
    messages.add(ContiguousDataLayoutMessage.create(
        dataAddress,
        actualDataSpace.getTotalLength() * computedDataType.getSize()
    ));
    messages.remove(placeholder);

    if (userDimensionedSpace != null) {
      if (!actualDataSpace.equals(userDimensionedSpace)) {
        throw new HdfWritingException(
            "DataSpace mismatch:\n provided:" + userDimensionedSpace + "\n   actual:"
            + actualDataSpace);
      } else {
        logger.debug("DataSpace matches after buffering to storage: " + actualDataSpace);
      }
    }
    messages.add(DataSpaceMessage.create(actualDataSpace));
    messages.remove(dataSpaceMessagePlaceholder);

    objectHeader = new ObjectHeader.ObjectHeaderV2(position, messages);

    hdfFileChannel.write(objectHeader.toBuffer(), position);

    return dataAddress + actualDataSpace.getTotalLength() * computeDataType().getSize();
  }

  private DataSpace writeData(HdfFileChannel hdfFileChannel, long dataAddress) {
    logger.info("Writing data for dataset [{}] at position [{}]", getPath(), dataAddress);

    hdfFileChannel.position(dataAddress);

    BufferingWorker doubleBuffer =
        BufferingWorker.start(chunks, iterable.iterator(), "ds-streamer-" + getName());

    DataSpace dsbuffer = null;

    while (doubleBuffer.running || !chunks.isEmpty()) {
      Object chunk = null;
      try {
        chunk = chunks.take();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      DataSpace chunkSpace = DataSpace.fromObject(chunk);
      dsbuffer = dsbuffer != null ? dsbuffer.combineDim0(chunkSpace) : chunkSpace;

      logger.debug(
          "dimensions (chunk,space) = ({},{})",
          Arrays.toString(chunkSpace.getDimensions()),
          Arrays.toString(dsbuffer.getDimensions())
      );

      computedDataType.writeData(chunk, chunkSpace.getDimensions(), hdfFileChannel);
    }

    return dsbuffer;
  }


  private static void writeDoubleData(
      Object data,
      int[] dims,
      ByteBuffer buffer,
      HdfFileChannel hdfFileChannel
  )
  {
    if (dims.length > 1) {
      for (int i = 0; i < dims[0]; i++) {
        Object newArray = Array.get(data, i);
        writeDoubleData(newArray, stripLeadingIndex(dims), buffer, hdfFileChannel);
      }
    } else {
      buffer.asDoubleBuffer().put((double[]) data);
      hdfFileChannel.write(buffer);
      buffer.clear();
    }
  }

  private final static class BufferingWorker implements Runnable {

    private final LinkedBlockingQueue<Object> queue;
    private final Iterator<?> iter;
    public volatile boolean running = true;

    private BufferingWorker(LinkedBlockingQueue<Object> queue, Iterator<?> iter) {
      this.queue = queue;
      this.iter = iter;
    }

    public static BufferingWorker start(
        LinkedBlockingQueue<Object> queue,
        Iterator<?> iter,
        String name
    )
    {
      BufferingWorker db = new BufferingWorker(queue, iter);
      Thread thread = new Thread(db);
      thread.setDaemon(true);
      thread.setName("bufferer-" + name);
      thread.start();
      return db;
    }

    @Override
    public void run() {
      while (iter.hasNext()) {
        logger.debug("got object, supplying to queue");
        try {
          queue.put(iter.next());
        } catch (InterruptedException e) {
          throw new HdfWritingException("error enqueuing chunk: " + e.getMessage(), e);
        }
      }
      running = false;
    }
  }

  public static class TotalWritten {
    public long totalWritten;
    public int totalDim0;

    public TotalWritten(long totalWritten, int totalDim0) {
      this.totalWritten = totalWritten;
      this.totalDim0 = totalDim0;
    }
  }
}
