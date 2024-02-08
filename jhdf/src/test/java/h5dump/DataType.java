
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
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}AtomicType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}CompoundType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}VLType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ArrayType"/>
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
    "atomicType",
    "compoundType",
    "vlType",
    "arrayType"
})
@XmlRootElement(name = "DataType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class DataType {

    @XmlElement(name = "AtomicType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected AtomicType atomicType;
    @XmlElement(name = "CompoundType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected CompoundType compoundType;
    @XmlElement(name = "VLType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected VLType vlType;
    @XmlElement(name = "ArrayType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ArrayType arrayType;

    /**
     * Gets the value of the atomicType property.
     * 
     * @return
     *     possible object is
     *     {@link AtomicType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public AtomicType getAtomicType() {
        return atomicType;
    }

    /**
     * Sets the value of the atomicType property.
     * 
     * @param value
     *     allowed object is
     *     {@link AtomicType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setAtomicType(AtomicType value) {
        this.atomicType = value;
    }

    /**
     * Gets the value of the compoundType property.
     * 
     * @return
     *     possible object is
     *     {@link CompoundType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public CompoundType getCompoundType() {
        return compoundType;
    }

    /**
     * Sets the value of the compoundType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompoundType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setCompoundType(CompoundType value) {
        this.compoundType = value;
    }

    /**
     * Gets the value of the vlType property.
     * 
     * @return
     *     possible object is
     *     {@link VLType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public VLType getVLType() {
        return vlType;
    }

    /**
     * Sets the value of the vlType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VLType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setVLType(VLType value) {
        this.vlType = value;
    }

    /**
     * Gets the value of the arrayType property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ArrayType getArrayType() {
        return arrayType;
    }

    /**
     * Sets the value of the arrayType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setArrayType(ArrayType value) {
        this.arrayType = value;
    }

}
