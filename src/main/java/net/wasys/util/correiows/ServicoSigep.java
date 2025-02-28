/**
 * ServicoSigep.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package net.wasys.util.correiows;

@SuppressWarnings({"rawtypes", "unused"})
public class ServicoSigep  implements java.io.Serializable {
    private net.wasys.util.correiows.CategoriaServico categoriaServico;

    private net.wasys.util.correiows.ChancelaMaster chancela;

    private java.lang.Boolean exigeDimensoes;

    private java.lang.Boolean exigeValorCobrar;

    private long imitm;

    private long servico;

    private net.wasys.util.correiows.ServicoERP servicoERP;

    private java.lang.String ssiCoCodigoPostal;

    public ServicoSigep() {
    }

    public ServicoSigep(
           net.wasys.util.correiows.CategoriaServico categoriaServico,
           net.wasys.util.correiows.ChancelaMaster chancela,
           java.lang.Boolean exigeDimensoes,
           java.lang.Boolean exigeValorCobrar,
           long imitm,
           long servico,
           net.wasys.util.correiows.ServicoERP servicoERP,
           java.lang.String ssiCoCodigoPostal) {
           this.categoriaServico = categoriaServico;
           this.chancela = chancela;
           this.exigeDimensoes = exigeDimensoes;
           this.exigeValorCobrar = exigeValorCobrar;
           this.imitm = imitm;
           this.servico = servico;
           this.servicoERP = servicoERP;
           this.ssiCoCodigoPostal = ssiCoCodigoPostal;
    }


    /**
     * Gets the categoriaServico value for this ServicoSigep.
     * 
     * @return categoriaServico
     */
    public net.wasys.util.correiows.CategoriaServico getCategoriaServico() {
        return categoriaServico;
    }


    /**
     * Sets the categoriaServico value for this ServicoSigep.
     * 
     * @param categoriaServico
     */
    public void setCategoriaServico(net.wasys.util.correiows.CategoriaServico categoriaServico) {
        this.categoriaServico = categoriaServico;
    }


    /**
     * Gets the chancela value for this ServicoSigep.
     * 
     * @return chancela
     */
    public net.wasys.util.correiows.ChancelaMaster getChancela() {
        return chancela;
    }


    /**
     * Sets the chancela value for this ServicoSigep.
     * 
     * @param chancela
     */
    public void setChancela(net.wasys.util.correiows.ChancelaMaster chancela) {
        this.chancela = chancela;
    }


    /**
     * Gets the exigeDimensoes value for this ServicoSigep.
     * 
     * @return exigeDimensoes
     */
    public java.lang.Boolean getExigeDimensoes() {
        return exigeDimensoes;
    }


    /**
     * Sets the exigeDimensoes value for this ServicoSigep.
     * 
     * @param exigeDimensoes
     */
    public void setExigeDimensoes(java.lang.Boolean exigeDimensoes) {
        this.exigeDimensoes = exigeDimensoes;
    }


    /**
     * Gets the exigeValorCobrar value for this ServicoSigep.
     * 
     * @return exigeValorCobrar
     */
    public java.lang.Boolean getExigeValorCobrar() {
        return exigeValorCobrar;
    }


    /**
     * Sets the exigeValorCobrar value for this ServicoSigep.
     * 
     * @param exigeValorCobrar
     */
    public void setExigeValorCobrar(java.lang.Boolean exigeValorCobrar) {
        this.exigeValorCobrar = exigeValorCobrar;
    }


    /**
     * Gets the imitm value for this ServicoSigep.
     * 
     * @return imitm
     */
    public long getImitm() {
        return imitm;
    }


    /**
     * Sets the imitm value for this ServicoSigep.
     * 
     * @param imitm
     */
    public void setImitm(long imitm) {
        this.imitm = imitm;
    }


    /**
     * Gets the servico value for this ServicoSigep.
     * 
     * @return servico
     */
    public long getServico() {
        return servico;
    }


    /**
     * Sets the servico value for this ServicoSigep.
     * 
     * @param servico
     */
    public void setServico(long servico) {
        this.servico = servico;
    }


    /**
     * Gets the servicoERP value for this ServicoSigep.
     * 
     * @return servicoERP
     */
    public net.wasys.util.correiows.ServicoERP getServicoERP() {
        return servicoERP;
    }


    /**
     * Sets the servicoERP value for this ServicoSigep.
     * 
     * @param servicoERP
     */
    public void setServicoERP(net.wasys.util.correiows.ServicoERP servicoERP) {
        this.servicoERP = servicoERP;
    }


    /**
     * Gets the ssiCoCodigoPostal value for this ServicoSigep.
     * 
     * @return ssiCoCodigoPostal
     */
    public java.lang.String getSsiCoCodigoPostal() {
        return ssiCoCodigoPostal;
    }


    /**
     * Sets the ssiCoCodigoPostal value for this ServicoSigep.
     * 
     * @param ssiCoCodigoPostal
     */
    public void setSsiCoCodigoPostal(java.lang.String ssiCoCodigoPostal) {
        this.ssiCoCodigoPostal = ssiCoCodigoPostal;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ServicoSigep)) return false;
        ServicoSigep other = (ServicoSigep) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.categoriaServico==null && other.getCategoriaServico()==null) || 
             (this.categoriaServico!=null &&
              this.categoriaServico.equals(other.getCategoriaServico()))) &&
            ((this.chancela==null && other.getChancela()==null) || 
             (this.chancela!=null &&
              this.chancela.equals(other.getChancela()))) &&
            ((this.exigeDimensoes==null && other.getExigeDimensoes()==null) || 
             (this.exigeDimensoes!=null &&
              this.exigeDimensoes.equals(other.getExigeDimensoes()))) &&
            ((this.exigeValorCobrar==null && other.getExigeValorCobrar()==null) || 
             (this.exigeValorCobrar!=null &&
              this.exigeValorCobrar.equals(other.getExigeValorCobrar()))) &&
            this.imitm == other.getImitm() &&
            this.servico == other.getServico() &&
            ((this.servicoERP==null && other.getServicoERP()==null) || 
             (this.servicoERP!=null &&
              this.servicoERP.equals(other.getServicoERP()))) &&
            ((this.ssiCoCodigoPostal==null && other.getSsiCoCodigoPostal()==null) || 
             (this.ssiCoCodigoPostal!=null &&
              this.ssiCoCodigoPostal.equals(other.getSsiCoCodigoPostal())));
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
        if (getCategoriaServico() != null) {
            _hashCode += getCategoriaServico().hashCode();
        }
        if (getChancela() != null) {
            _hashCode += getChancela().hashCode();
        }
        if (getExigeDimensoes() != null) {
            _hashCode += getExigeDimensoes().hashCode();
        }
        if (getExigeValorCobrar() != null) {
            _hashCode += getExigeValorCobrar().hashCode();
        }
        _hashCode += new Long(getImitm()).hashCode();
        _hashCode += new Long(getServico()).hashCode();
        if (getServicoERP() != null) {
            _hashCode += getServicoERP().hashCode();
        }
        if (getSsiCoCodigoPostal() != null) {
            _hashCode += getSsiCoCodigoPostal().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ServicoSigep.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "servicoSigep"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categoriaServico");
        elemField.setXmlName(new javax.xml.namespace.QName("", "categoriaServico"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "categoriaServico"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("chancela");
        elemField.setXmlName(new javax.xml.namespace.QName("", "chancela"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "chancelaMaster"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exigeDimensoes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "exigeDimensoes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("exigeValorCobrar");
        elemField.setXmlName(new javax.xml.namespace.QName("", "exigeValorCobrar"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("imitm");
        elemField.setXmlName(new javax.xml.namespace.QName("", "imitm"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("servico");
        elemField.setXmlName(new javax.xml.namespace.QName("", "servico"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("servicoERP");
        elemField.setXmlName(new javax.xml.namespace.QName("", "servicoERP"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "servicoERP"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ssiCoCodigoPostal");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ssiCoCodigoPostal"));
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
