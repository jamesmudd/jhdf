
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
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ContiguousLayout"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ChunkedLayout"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}CompactLayout"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ExternalLayout"/>
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
    "contiguousLayout",
    "chunkedLayout",
    "compactLayout",
    "externalLayout"
})
@XmlRootElement(name = "StorageLayout", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class StorageLayout {

    @XmlElement(name = "ContiguousLayout", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ContiguousLayout contiguousLayout;
    @XmlElement(name = "ChunkedLayout", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ChunkedLayout chunkedLayout;
    @XmlElement(name = "CompactLayout", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected CompactLayout compactLayout;
    @XmlElement(name = "ExternalLayout", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ExternalLayout externalLayout;

    /**
     * Gets the value of the contiguousLayout property.
     * 
     * @return
     *     possible object is
     *     {@link ContiguousLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ContiguousLayout getContiguousLayout() {
        return contiguousLayout;
    }

    /**
     * Sets the value of the contiguousLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContiguousLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setContiguousLayout(ContiguousLayout value) {
        this.contiguousLayout = value;
    }

    /**
     * Gets the value of the chunkedLayout property.
     * 
     * @return
     *     possible object is
     *     {@link ChunkedLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ChunkedLayout getChunkedLayout() {
        return chunkedLayout;
    }

    /**
     * Sets the value of the chunkedLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChunkedLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setChunkedLayout(ChunkedLayout value) {
        this.chunkedLayout = value;
    }

    /**
     * Gets the value of the compactLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CompactLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public CompactLayout getCompactLayout() {
        return compactLayout;
    }

    /**
     * Sets the value of the compactLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompactLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setCompactLayout(CompactLayout value) {
        this.compactLayout = value;
    }

    /**
     * Gets the value of the externalLayout property.
     * 
     * @return
     *     possible object is
     *     {@link ExternalLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ExternalLayout getExternalLayout() {
        return externalLayout;
    }

    /**
     * Sets the value of the externalLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExternalLayout }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setExternalLayout(ExternalLayout value) {
        this.externalLayout = value;
    }

}
