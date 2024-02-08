
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
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ScalarDataspace"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}SimpleDataspace"/>
 *         &lt;element ref="{http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File}ComplexDataspace"/>
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
    "scalarDataspace",
    "simpleDataspace",
    "complexDataspace"
})
@XmlRootElement(name = "Dataspace", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class Dataspace {

    @XmlElement(name = "ScalarDataspace", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ScalarDataspace scalarDataspace;
    @XmlElement(name = "SimpleDataspace", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected SimpleDataspace simpleDataspace;
    @XmlElement(name = "ComplexDataspace", namespace = "http://hdf.ncsa.uiuc.edu/DTDs/HDF5-File")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ComplexDataspace complexDataspace;

    /**
     * Gets the value of the scalarDataspace property.
     * 
     * @return
     *     possible object is
     *     {@link ScalarDataspace }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ScalarDataspace getScalarDataspace() {
        return scalarDataspace;
    }

    /**
     * Sets the value of the scalarDataspace property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarDataspace }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setScalarDataspace(ScalarDataspace value) {
        this.scalarDataspace = value;
    }

    /**
     * Gets the value of the simpleDataspace property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleDataspace }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public SimpleDataspace getSimpleDataspace() {
        return simpleDataspace;
    }

    /**
     * Sets the value of the simpleDataspace property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleDataspace }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setSimpleDataspace(SimpleDataspace value) {
        this.simpleDataspace = value;
    }

    /**
     * Gets the value of the complexDataspace property.
     * 
     * @return
     *     possible object is
     *     {@link ComplexDataspace }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ComplexDataspace getComplexDataspace() {
        return complexDataspace;
    }

    /**
     * Sets the value of the complexDataspace property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplexDataspace }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2024-02-08T09:27:25+00:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setComplexDataspace(ComplexDataspace value) {
        this.complexDataspace = value;
    }

}
