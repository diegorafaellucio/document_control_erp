/**
 * MatchResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.grupodigital.facerec;

@SuppressWarnings({"rawtypes", "unused"})
public class MatchResult  implements java.io.Serializable {
    private int faceListId;

    private float similiarity;

    private java.lang.String imageSimiliarity;

    public MatchResult() {
    }

    public MatchResult(
           int faceListId,
           float similiarity,
           java.lang.String imageSimiliarity) {
           this.faceListId = faceListId;
           this.similiarity = similiarity;
           this.imageSimiliarity = imageSimiliarity;
    }


    /**
     * Gets the faceListId value for this MatchResult.
     * 
     * @return faceListId
     */
    public int getFaceListId() {
        return faceListId;
    }


    /**
     * Sets the faceListId value for this MatchResult.
     * 
     * @param faceListId
     */
    public void setFaceListId(int faceListId) {
        this.faceListId = faceListId;
    }


    /**
     * Gets the similiarity value for this MatchResult.
     * 
     * @return similiarity
     */
    public float getSimiliarity() {
        return similiarity;
    }


    /**
     * Sets the similiarity value for this MatchResult.
     * 
     * @param similiarity
     */
    public void setSimiliarity(float similiarity) {
        this.similiarity = similiarity;
    }


    /**
     * Gets the imageSimiliarity value for this MatchResult.
     * 
     * @return imageSimiliarity
     */
    public java.lang.String getImageSimiliarity() {
        return imageSimiliarity;
    }


    /**
     * Sets the imageSimiliarity value for this MatchResult.
     * 
     * @param imageSimiliarity
     */
    public void setImageSimiliarity(java.lang.String imageSimiliarity) {
        this.imageSimiliarity = imageSimiliarity;
    }

    private java.lang.Object __equalsCalc = null;
	public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MatchResult)) return false;
        MatchResult other = (MatchResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.faceListId == other.getFaceListId() &&
            this.similiarity == other.getSimiliarity() &&
            ((this.imageSimiliarity==null && other.getImageSimiliarity()==null) || 
             (this.imageSimiliarity!=null &&
              this.imageSimiliarity.equals(other.getImageSimiliarity())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getFaceListId();
        _hashCode += new Float(getSimiliarity()).hashCode();
        if (getImageSimiliarity() != null) {
            _hashCode += getImageSimiliarity().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MatchResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "MatchResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("faceListId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FaceListId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("similiarity");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Similiarity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("imageSimiliarity");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ImageSimiliarity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
