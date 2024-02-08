
package h5dump;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *       &lt;attribute name="OBJ-XID" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="H5Path" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "DatasetPtr", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class DatasetPtr {

    @XmlAttribute(name = "OBJ-XID", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected Object objxid;
    @XmlAttribute(name = "H5Path", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String h5Path;

    /**
     * Gets the value of the objxid property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public Object getOBJXID() {
        return objxid;
    }

    /**
     * Sets the value of the objxid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setOBJXID(Object value) {
        this.objxid = value;
    }

    /**
     * Gets the value of the h5Path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getH5Path() {
        return h5Path;
    }

    /**
     * Sets the value of the h5Path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setH5Path(String value) {
        this.h5Path = value;
    }

}
