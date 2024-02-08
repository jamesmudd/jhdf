
package h5dump;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}IntegerType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}FloatType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}StringType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}TimeType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}BitfieldType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}OpaqueType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ReferenceType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}EnumType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "integerType",
    "floatType",
    "stringType",
    "timeType",
    "bitfieldType",
    "opaqueType",
    "referenceType",
    "enumType"
})
@XmlRootElement(name = "AtomicType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class AtomicType {

    @XmlElement(name = "IntegerType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected IntegerType integerType;
    @XmlElement(name = "FloatType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected FloatType floatType;
    @XmlElement(name = "StringType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected StringType stringType;
    @XmlElement(name = "TimeType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected TimeType timeType;
    @XmlElement(name = "BitfieldType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BitfieldType bitfieldType;
    @XmlElement(name = "OpaqueType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected OpaqueType opaqueType;
    @XmlElement(name = "ReferenceType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ReferenceType referenceType;
    @XmlElement(name = "EnumType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected EnumType enumType;

    /**
     * Gets the value of the integerType property.
     * 
     * @return
     *     possible object is
     *     {@link IntegerType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public IntegerType getIntegerType() {
        return integerType;
    }

    /**
     * Sets the value of the integerType property.
     * 
     * @param value
     *     allowed object is
     *     {@link IntegerType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setIntegerType(IntegerType value) {
        this.integerType = value;
    }

    /**
     * Gets the value of the floatType property.
     * 
     * @return
     *     possible object is
     *     {@link FloatType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public FloatType getFloatType() {
        return floatType;
    }

    /**
     * Sets the value of the floatType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FloatType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setFloatType(FloatType value) {
        this.floatType = value;
    }

    /**
     * Gets the value of the stringType property.
     * 
     * @return
     *     possible object is
     *     {@link StringType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public StringType getStringType() {
        return stringType;
    }

    /**
     * Sets the value of the stringType property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setStringType(StringType value) {
        this.stringType = value;
    }

    /**
     * Gets the value of the timeType property.
     * 
     * @return
     *     possible object is
     *     {@link TimeType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public TimeType getTimeType() {
        return timeType;
    }

    /**
     * Sets the value of the timeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setTimeType(TimeType value) {
        this.timeType = value;
    }

    /**
     * Gets the value of the bitfieldType property.
     * 
     * @return
     *     possible object is
     *     {@link BitfieldType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BitfieldType getBitfieldType() {
        return bitfieldType;
    }

    /**
     * Sets the value of the bitfieldType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BitfieldType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setBitfieldType(BitfieldType value) {
        this.bitfieldType = value;
    }

    /**
     * Gets the value of the opaqueType property.
     * 
     * @return
     *     possible object is
     *     {@link OpaqueType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public OpaqueType getOpaqueType() {
        return opaqueType;
    }

    /**
     * Sets the value of the opaqueType property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpaqueType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setOpaqueType(OpaqueType value) {
        this.opaqueType = value;
    }

    /**
     * Gets the value of the referenceType property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ReferenceType getReferenceType() {
        return referenceType;
    }

    /**
     * Sets the value of the referenceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setReferenceType(ReferenceType value) {
        this.referenceType = value;
    }

    /**
     * Gets the value of the enumType property.
     * 
     * @return
     *     possible object is
     *     {@link EnumType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public EnumType getEnumType() {
        return enumType;
    }

    /**
     * Sets the value of the enumType property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnumType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setEnumType(EnumType value) {
        this.enumType = value;
    }

}
