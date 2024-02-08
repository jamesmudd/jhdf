
package h5dump;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
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
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}DataType"/>
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}EnumElement"/>
 *           &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}EnumValue"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="Nelems" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataTypeAndEnumElementAndEnumValue"
})
@XmlRootElement(name = "EnumType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class EnumType {

    @XmlElementRefs({
        @XmlElementRef(name = "DataType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", type = DataType.class),
        @XmlElementRef(name = "EnumValue", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", type = JAXBElement.class),
        @XmlElementRef(name = "EnumElement", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", type = JAXBElement.class)
    })
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<Object> dataTypeAndEnumElementAndEnumValue;
    @XmlAttribute(name = "Nelems", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String nelems;

    /**
     * Gets the value of the dataTypeAndEnumElementAndEnumValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataTypeAndEnumElementAndEnumValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataTypeAndEnumElementAndEnumValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataType }
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<Object> getDataTypeAndEnumElementAndEnumValue() {
        if (dataTypeAndEnumElementAndEnumValue == null) {
            dataTypeAndEnumElementAndEnumValue = new ArrayList<Object>();
        }
        return this.dataTypeAndEnumElementAndEnumValue;
    }

    /**
     * Gets the value of the nelems property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getNelems() {
        return nelems;
    }

    /**
     * Sets the value of the nelems property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setNelems(String value) {
        this.nelems = value;
    }

}
