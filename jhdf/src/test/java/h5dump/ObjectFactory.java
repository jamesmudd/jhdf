
package h5dump;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the h5dump package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DataFromFile_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "DataFromFile");
    private final static QName _Fletcher32_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "Fletcher32");
    private final static QName _UserBlock_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "UserBlock");
    private final static QName _Compression_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "Compression");
    private final static QName _NoFill_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "NoFill");
    private final static QName _Shuffle_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "Shuffle");
    private final static QName _EnumValue_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "EnumValue");
    private final static QName _EnumElement_QNAME = new QName("http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", "EnumElement");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: h5dump
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ChunkDimension }
     * 
     */
    public ChunkDimension createChunkDimension() {
        return new ChunkDimension();
    }

    /**
     * Create an instance of {@link StringType }
     * 
     */
    public StringType createStringType() {
        return new StringType();
    }

    /**
     * Create an instance of {@link GroupPtr }
     * 
     */
    public GroupPtr createGroupPtr() {
        return new GroupPtr();
    }

    /**
     * Create an instance of {@link VLType }
     * 
     */
    public VLType createVLType() {
        return new VLType();
    }

    /**
     * Create an instance of {@link DataType }
     * 
     */
    public DataType createDataType() {
        return new DataType();
    }

    /**
     * Create an instance of {@link AtomicType }
     * 
     */
    public AtomicType createAtomicType() {
        return new AtomicType();
    }

    /**
     * Create an instance of {@link IntegerType }
     * 
     */
    public IntegerType createIntegerType() {
        return new IntegerType();
    }

    /**
     * Create an instance of {@link FloatType }
     * 
     */
    public FloatType createFloatType() {
        return new FloatType();
    }

    /**
     * Create an instance of {@link TimeType }
     * 
     */
    public TimeType createTimeType() {
        return new TimeType();
    }

    /**
     * Create an instance of {@link BitfieldType }
     * 
     */
    public BitfieldType createBitfieldType() {
        return new BitfieldType();
    }

    /**
     * Create an instance of {@link OpaqueType }
     * 
     */
    public OpaqueType createOpaqueType() {
        return new OpaqueType();
    }

    /**
     * Create an instance of {@link ReferenceType }
     * 
     */
    public ReferenceType createReferenceType() {
        return new ReferenceType();
    }

    /**
     * Create an instance of {@link ObjectReferenceType }
     * 
     */
    public ObjectReferenceType createObjectReferenceType() {
        return new ObjectReferenceType();
    }

    /**
     * Create an instance of {@link RegionReferenceType }
     * 
     */
    public RegionReferenceType createRegionReferenceType() {
        return new RegionReferenceType();
    }

    /**
     * Create an instance of {@link EnumType }
     * 
     */
    public EnumType createEnumType() {
        return new EnumType();
    }

    /**
     * Create an instance of {@link CompoundType }
     * 
     */
    public CompoundType createCompoundType() {
        return new CompoundType();
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link ArrayType }
     * 
     */
    public ArrayType createArrayType() {
        return new ArrayType();
    }

    /**
     * Create an instance of {@link ArrayDimension }
     * 
     */
    public ArrayDimension createArrayDimension() {
        return new ArrayDimension();
    }

    /**
     * Create an instance of {@link SoftLink }
     * 
     */
    public SoftLink createSoftLink() {
        return new SoftLink();
    }

    /**
     * Create an instance of {@link NamedDataTypePtr }
     * 
     */
    public NamedDataTypePtr createNamedDataTypePtr() {
        return new NamedDataTypePtr();
    }

    /**
     * Create an instance of {@link StorageLayout }
     * 
     */
    public StorageLayout createStorageLayout() {
        return new StorageLayout();
    }

    /**
     * Create an instance of {@link ContiguousLayout }
     * 
     */
    public ContiguousLayout createContiguousLayout() {
        return new ContiguousLayout();
    }

    /**
     * Create an instance of {@link ChunkedLayout }
     * 
     */
    public ChunkedLayout createChunkedLayout() {
        return new ChunkedLayout();
    }

    /**
     * Create an instance of {@link RequiredFilter }
     * 
     */
    public RequiredFilter createRequiredFilter() {
        return new RequiredFilter();
    }

    /**
     * Create an instance of {@link Deflate }
     * 
     */
    public Deflate createDeflate() {
        return new Deflate();
    }

    /**
     * Create an instance of {@link SZIP }
     * 
     */
    public SZIP createSZIP() {
        return new SZIP();
    }

    /**
     * Create an instance of {@link CompactLayout }
     * 
     */
    public CompactLayout createCompactLayout() {
        return new CompactLayout();
    }

    /**
     * Create an instance of {@link ExternalLayout }
     * 
     */
    public ExternalLayout createExternalLayout() {
        return new ExternalLayout();
    }

    /**
     * Create an instance of {@link ExternalFileInfo }
     * 
     */
    public ExternalFileInfo createExternalFileInfo() {
        return new ExternalFileInfo();
    }

    /**
     * Create an instance of {@link SimpleDataspace }
     * 
     */
    public SimpleDataspace createSimpleDataspace() {
        return new SimpleDataspace();
    }

    /**
     * Create an instance of {@link Dimension }
     * 
     */
    public Dimension createDimension() {
        return new Dimension();
    }

    /**
     * Create an instance of {@link Dataspace }
     * 
     */
    public Dataspace createDataspace() {
        return new Dataspace();
    }

    /**
     * Create an instance of {@link ScalarDataspace }
     * 
     */
    public ScalarDataspace createScalarDataspace() {
        return new ScalarDataspace();
    }

    /**
     * Create an instance of {@link ComplexDataspace }
     * 
     */
    public ComplexDataspace createComplexDataspace() {
        return new ComplexDataspace();
    }

    /**
     * Create an instance of {@link FillValueInfo }
     * 
     */
    public FillValueInfo createFillValueInfo() {
        return new FillValueInfo();
    }

    /**
     * Create an instance of {@link FillValue }
     * 
     */
    public FillValue createFillValue() {
        return new FillValue();
    }

    /**
     * Create an instance of {@link Data }
     * 
     */
    public Data createData() {
        return new Data();
    }

    /**
     * Create an instance of {@link NativeHDF5 }
     * 
     */
    public NativeHDF5 createNativeHDF5() {
        return new NativeHDF5();
    }

    /**
     * Create an instance of {@link NoData }
     * 
     */
    public NoData createNoData() {
        return new NoData();
    }

    /**
     * Create an instance of {@link SuperBlock }
     * 
     */
    public SuperBlock createSuperBlock() {
        return new SuperBlock();
    }

    /**
     * Create an instance of {@link DatasetPtr }
     * 
     */
    public DatasetPtr createDatasetPtr() {
        return new DatasetPtr();
    }

    /**
     * Create an instance of {@link Group }
     * 
     */
    public Group createGroup() {
        return new Group();
    }

    /**
     * Create an instance of {@link Attribute }
     * 
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link Dataset }
     * 
     */
    public Dataset createDataset() {
        return new Dataset();
    }

    /**
     * Create an instance of {@link NamedDataType }
     * 
     */
    public NamedDataType createNamedDataType() {
        return new NamedDataType();
    }

    /**
     * Create an instance of {@link RootGroup }
     * 
     */
    public RootGroup createRootGroup() {
        return new RootGroup();
    }

    /**
     * Create an instance of {@link HDF5File }
     * 
     */
    public HDF5File createHDF5File() {
        return new HDF5File();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "DataFromFile")
    public JAXBElement<String> createDataFromFile(String value) {
        return new JAXBElement<String>(_DataFromFile_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "Fletcher32")
    public JAXBElement<Object> createFletcher32(Object value) {
        return new JAXBElement<Object>(_Fletcher32_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "UserBlock")
    public JAXBElement<String> createUserBlock(String value) {
        return new JAXBElement<String>(_UserBlock_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "Compression")
    public JAXBElement<String> createCompression(String value) {
        return new JAXBElement<String>(_Compression_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "NoFill")
    public JAXBElement<Object> createNoFill(Object value) {
        return new JAXBElement<Object>(_NoFill_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "Shuffle")
    public JAXBElement<Object> createShuffle(Object value) {
        return new JAXBElement<Object>(_Shuffle_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "EnumValue")
    public JAXBElement<String> createEnumValue(String value) {
        return new JAXBElement<String>(_EnumValue_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", name = "EnumElement")
    public JAXBElement<String> createEnumElement(String value) {
        return new JAXBElement<String>(_EnumElement_QNAME, String.class, null, value);
    }

}
