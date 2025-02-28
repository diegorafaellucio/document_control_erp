/**
 * ColetaSimultaneaTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package net.wasys.util.correiows;

@SuppressWarnings({"rawtypes", "unused"})
public class ColetaSimultaneaTO  extends net.wasys.util.correiows.ColetaTO  implements java.io.Serializable {
    private java.lang.String obj;

    private java.lang.String obs;

    public ColetaSimultaneaTO() {
    }

    public ColetaSimultaneaTO(
           java.lang.String cklist,
           java.lang.String descricao,
           java.lang.String id_cliente,
           net.wasys.util.correiows.ProdutoTO[] produto,
           net.wasys.util.correiows.RemetenteTO remetente,
           java.lang.String tipo,
           java.lang.String valor_declarado,
           java.lang.String obj,
           java.lang.String obs) {
        super(
            cklist,
            descricao,
            id_cliente,
            produto,
            remetente,
            tipo,
            valor_declarado);
        this.obj = obj;
        this.obs = obs;
    }


    /**
     * Gets the obj value for this ColetaSimultaneaTO.
     * 
     * @return obj
     */
    public java.lang.String getObj() {
        return obj;
    }


    /**
     * Sets the obj value for this ColetaSimultaneaTO.
     * 
     * @param obj
     */
    public void setObj(java.lang.String obj) {
        this.obj = obj;
    }


    /**
     * Gets the obs value for this ColetaSimultaneaTO.
     * 
     * @return obs
     */
    public java.lang.String getObs() {
        return obs;
    }


    /**
     * Sets the obs value for this ColetaSimultaneaTO.
     * 
     * @param obs
     */
    public void setObs(java.lang.String obs) {
        this.obs = obs;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ColetaSimultaneaTO)) return false;
        ColetaSimultaneaTO other = (ColetaSimultaneaTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.obj==null && other.getObj()==null) || 
             (this.obj!=null &&
              this.obj.equals(other.getObj()))) &&
            ((this.obs==null && other.getObs()==null) || 
             (this.obs!=null &&
              this.obs.equals(other.getObs())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getObj() != null) {
            _hashCode += getObj().hashCode();
        }
        if (getObs() != null) {
            _hashCode += getObs().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ColetaSimultaneaTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "coletaSimultaneaTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("obj");
        elemField.setXmlName(new javax.xml.namespace.QName("", "obj"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("obs");
        elemField.setXmlName(new javax.xml.namespace.QName("", "obs"));
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
