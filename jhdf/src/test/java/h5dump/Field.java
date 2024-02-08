
package h5dump;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;sequence>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}DataType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="FieldName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataType"
})
@XmlRootElement(name = "Field", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class Field {

    @XmlElement(name = "DataType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected DataType dataType;
    @XmlAttribute(name = "FieldName", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String fieldName;

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link DataType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setDataType(DataType value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the fieldName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the value of the fieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setFieldName(String value) {
        this.fieldName = value;
    }

}
