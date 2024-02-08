
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
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ObjectReferenceType"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}RegionReferenceType"/>
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
    "objectReferenceType",
    "regionReferenceType"
})
@XmlRootElement(name = "ReferenceType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class ReferenceType {

    @XmlElement(name = "ObjectReferenceType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ObjectReferenceType objectReferenceType;
    @XmlElement(name = "RegionReferenceType", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected RegionReferenceType regionReferenceType;

    /**
     * Gets the value of the objectReferenceType property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectReferenceType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ObjectReferenceType getObjectReferenceType() {
        return objectReferenceType;
    }

    /**
     * Sets the value of the objectReferenceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectReferenceType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setObjectReferenceType(ObjectReferenceType value) {
        this.objectReferenceType = value;
    }

    /**
     * Gets the value of the regionReferenceType property.
     * 
     * @return
     *     possible object is
     *     {@link RegionReferenceType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public RegionReferenceType getRegionReferenceType() {
        return regionReferenceType;
    }

    /**
     * Sets the value of the regionReferenceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegionReferenceType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setRegionReferenceType(RegionReferenceType value) {
        this.regionReferenceType = value;
    }

}
