/**
 * FacialRecogn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.grupodigital.facerec;

@SuppressWarnings({"rawtypes", "unused"})
public class FacialRecogn  implements java.io.Serializable {
    private int faceListId;

    private java.lang.String imageFileName;

    private int facePositionXc;

    private int facePositionYc;

    private int facePositionW;

    private double facePositionAngle;

    private int eye1X;

    private int eye1Y;

    private int eye2X;

    private int eye2Y;

    private byte[] template;

    private byte[] image;

    private byte[] faceImage;

    private byte[] imageRecogn;

    public FacialRecogn() {
    }

    public FacialRecogn(
           int faceListId,
           java.lang.String imageFileName,
           int facePositionXc,
           int facePositionYc,
           int facePositionW,
           double facePositionAngle,
           int eye1X,
           int eye1Y,
           int eye2X,
           int eye2Y,
           byte[] template,
           byte[] image,
           byte[] faceImage,
           byte[] imageRecogn) {
           this.faceListId = faceListId;
           this.imageFileName = imageFileName;
           this.facePositionXc = facePositionXc;
           this.facePositionYc = facePositionYc;
           this.facePositionW = facePositionW;
           this.facePositionAngle = facePositionAngle;
           this.eye1X = eye1X;
           this.eye1Y = eye1Y;
           this.eye2X = eye2X;
           this.eye2Y = eye2Y;
           this.template = template;
           this.image = image;
           this.faceImage = faceImage;
           this.imageRecogn = imageRecogn;
    }


    /**
     * Gets the faceListId value for this FacialRecogn.
     * 
     * @return faceListId
     */
    public int getFaceListId() {
        return faceListId;
    }


    /**
     * Sets the faceListId value for this FacialRecogn.
     * 
     * @param faceListId
     */
    public void setFaceListId(int faceListId) {
        this.faceListId = faceListId;
    }


    /**
     * Gets the imageFileName value for this FacialRecogn.
     * 
     * @return imageFileName
     */
    public java.lang.String getImageFileName() {
        return imageFileName;
    }


    /**
     * Sets the imageFileName value for this FacialRecogn.
     * 
     * @param imageFileName
     */
    public void setImageFileName(java.lang.String imageFileName) {
        this.imageFileName = imageFileName;
    }


    /**
     * Gets the facePositionXc value for this FacialRecogn.
     * 
     * @return facePositionXc
     */
    public int getFacePositionXc() {
        return facePositionXc;
    }


    /**
     * Sets the facePositionXc value for this FacialRecogn.
     * 
     * @param facePositionXc
     */
    public void setFacePositionXc(int facePositionXc) {
        this.facePositionXc = facePositionXc;
    }


    /**
     * Gets the facePositionYc value for this FacialRecogn.
     * 
     * @return facePositionYc
     */
    public int getFacePositionYc() {
        return facePositionYc;
    }


    /**
     * Sets the facePositionYc value for this FacialRecogn.
     * 
     * @param facePositionYc
     */
    public void setFacePositionYc(int facePositionYc) {
        this.facePositionYc = facePositionYc;
    }


    /**
     * Gets the facePositionW value for this FacialRecogn.
     * 
     * @return facePositionW
     */
    public int getFacePositionW() {
        return facePositionW;
    }


    /**
     * Sets the facePositionW value for this FacialRecogn.
     * 
     * @param facePositionW
     */
    public void setFacePositionW(int facePositionW) {
        this.facePositionW = facePositionW;
    }


    /**
     * Gets the facePositionAngle value for this FacialRecogn.
     * 
     * @return facePositionAngle
     */
    public double getFacePositionAngle() {
        return facePositionAngle;
    }


    /**
     * Sets the facePositionAngle value for this FacialRecogn.
     * 
     * @param facePositionAngle
     */
    public void setFacePositionAngle(double facePositionAngle) {
        this.facePositionAngle = facePositionAngle;
    }


    /**
     * Gets the eye1X value for this FacialRecogn.
     * 
     * @return eye1X
     */
    public int getEye1X() {
        return eye1X;
    }


    /**
     * Sets the eye1X value for this FacialRecogn.
     * 
     * @param eye1X
     */
    public void setEye1X(int eye1X) {
        this.eye1X = eye1X;
    }


    /**
     * Gets the eye1Y value for this FacialRecogn.
     * 
     * @return eye1Y
     */
    public int getEye1Y() {
        return eye1Y;
    }


    /**
     * Sets the eye1Y value for this FacialRecogn.
     * 
     * @param eye1Y
     */
    public void setEye1Y(int eye1Y) {
        this.eye1Y = eye1Y;
    }


    /**
     * Gets the eye2X value for this FacialRecogn.
     * 
     * @return eye2X
     */
    public int getEye2X() {
        return eye2X;
    }


    /**
     * Sets the eye2X value for this FacialRecogn.
     * 
     * @param eye2X
     */
    public void setEye2X(int eye2X) {
        this.eye2X = eye2X;
    }


    /**
     * Gets the eye2Y value for this FacialRecogn.
     * 
     * @return eye2Y
     */
    public int getEye2Y() {
        return eye2Y;
    }


    /**
     * Sets the eye2Y value for this FacialRecogn.
     * 
     * @param eye2Y
     */
    public void setEye2Y(int eye2Y) {
        this.eye2Y = eye2Y;
    }


    /**
     * Gets the template value for this FacialRecogn.
     * 
     * @return template
     */
    public byte[] getTemplate() {
        return template;
    }


    /**
     * Sets the template value for this FacialRecogn.
     * 
     * @param template
     */
    public void setTemplate(byte[] template) {
        this.template = template;
    }


    /**
     * Gets the image value for this FacialRecogn.
     * 
     * @return image
     */
    public byte[] getImage() {
        return image;
    }


    /**
     * Sets the image value for this FacialRecogn.
     * 
     * @param image
     */
    public void setImage(byte[] image) {
        this.image = image;
    }


    /**
     * Gets the faceImage value for this FacialRecogn.
     * 
     * @return faceImage
     */
    public byte[] getFaceImage() {
        return faceImage;
    }


    /**
     * Sets the faceImage value for this FacialRecogn.
     * 
     * @param faceImage
     */
    public void setFaceImage(byte[] faceImage) {
        this.faceImage = faceImage;
    }


    /**
     * Gets the imageRecogn value for this FacialRecogn.
     * 
     * @return imageRecogn
     */
    public byte[] getImageRecogn() {
        return imageRecogn;
    }


    /**
     * Sets the imageRecogn value for this FacialRecogn.
     * 
     * @param imageRecogn
     */
    public void setImageRecogn(byte[] imageRecogn) {
        this.imageRecogn = imageRecogn;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FacialRecogn)) return false;
        FacialRecogn other = (FacialRecogn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.faceListId == other.getFaceListId() &&
            ((this.imageFileName==null && other.getImageFileName()==null) || 
             (this.imageFileName!=null &&
              this.imageFileName.equals(other.getImageFileName()))) &&
            this.facePositionXc == other.getFacePositionXc() &&
            this.facePositionYc == other.getFacePositionYc() &&
            this.facePositionW == other.getFacePositionW() &&
            this.facePositionAngle == other.getFacePositionAngle() &&
            this.eye1X == other.getEye1X() &&
            this.eye1Y == other.getEye1Y() &&
            this.eye2X == other.getEye2X() &&
            this.eye2Y == other.getEye2Y() &&
            ((this.template==null && other.getTemplate()==null) || 
             (this.template!=null &&
              java.util.Arrays.equals(this.template, other.getTemplate()))) &&
            ((this.image==null && other.getImage()==null) || 
             (this.image!=null &&
              java.util.Arrays.equals(this.image, other.getImage()))) &&
            ((this.faceImage==null && other.getFaceImage()==null) || 
             (this.faceImage!=null &&
              java.util.Arrays.equals(this.faceImage, other.getFaceImage()))) &&
            ((this.imageRecogn==null && other.getImageRecogn()==null) || 
             (this.imageRecogn!=null &&
              java.util.Arrays.equals(this.imageRecogn, other.getImageRecogn())));
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
        if (getImageFileName() != null) {
            _hashCode += getImageFileName().hashCode();
        }
        _hashCode += getFacePositionXc();
        _hashCode += getFacePositionYc();
        _hashCode += getFacePositionW();
        _hashCode += new Double(getFacePositionAngle()).hashCode();
        _hashCode += getEye1X();
        _hashCode += getEye1Y();
        _hashCode += getEye2X();
        _hashCode += getEye2Y();
        if (getTemplate() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTemplate());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTemplate(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getImage() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getImage());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getImage(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFaceImage() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFaceImage());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFaceImage(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getImageRecogn() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getImageRecogn());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getImageRecogn(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FacialRecogn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "FacialRecogn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("faceListId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FaceListId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("imageFileName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ImageFileName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("facePositionXc");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FacePositionXc"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("facePositionYc");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FacePositionYc"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("facePositionW");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FacePositionW"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("facePositionAngle");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FacePositionAngle"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eye1X");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Eye1X"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eye1Y");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Eye1Y"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eye2X");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Eye2X"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eye2Y");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Eye2Y"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("template");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Template"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("image");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Image"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("faceImage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FaceImage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("imageRecogn");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ImageRecogn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
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
