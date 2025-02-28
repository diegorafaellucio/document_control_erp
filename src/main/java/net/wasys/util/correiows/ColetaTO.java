/**
 * ColetaTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package net.wasys.util.correiows;

@SuppressWarnings({"rawtypes", "unused"})
public class ColetaTO  implements java.io.Serializable {
    private java.lang.String cklist;

    private java.lang.String descricao;

    private java.lang.String id_cliente;

    private net.wasys.util.correiows.ProdutoTO[] produto;

    private net.wasys.util.correiows.RemetenteTO remetente;

    private java.lang.String tipo;

    private java.lang.String valor_declarado;

    public ColetaTO() {
    }

    public ColetaTO(
           java.lang.String cklist,
           java.lang.String descricao,
           java.lang.String id_cliente,
           net.wasys.util.correiows.ProdutoTO[] produto,
           net.wasys.util.correiows.RemetenteTO remetente,
           java.lang.String tipo,
           java.lang.String valor_declarado) {
           this.cklist = cklist;
           this.descricao = descricao;
           this.id_cliente = id_cliente;
           this.produto = produto;
           this.remetente = remetente;
           this.tipo = tipo;
           this.valor_declarado = valor_declarado;
    }


    /**
     * Gets the cklist value for this ColetaTO.
     * 
     * @return cklist
     */
    public java.lang.String getCklist() {
        return cklist;
    }


    /**
     * Sets the cklist value for this ColetaTO.
     * 
     * @param cklist
     */
    public void setCklist(java.lang.String cklist) {
        this.cklist = cklist;
    }


    /**
     * Gets the descricao value for this ColetaTO.
     * 
     * @return descricao
     */
    public java.lang.String getDescricao() {
        return descricao;
    }


    /**
     * Sets the descricao value for this ColetaTO.
     * 
     * @param descricao
     */
    public void setDescricao(java.lang.String descricao) {
        this.descricao = descricao;
    }


    /**
     * Gets the id_cliente value for this ColetaTO.
     * 
     * @return id_cliente
     */
    public java.lang.String getId_cliente() {
        return id_cliente;
    }


    /**
     * Sets the id_cliente value for this ColetaTO.
     * 
     * @param id_cliente
     */
    public void setId_cliente(java.lang.String id_cliente) {
        this.id_cliente = id_cliente;
    }


    /**
     * Gets the produto value for this ColetaTO.
     * 
     * @return produto
     */
    public net.wasys.util.correiows.ProdutoTO[] getProduto() {
        return produto;
    }


    /**
     * Sets the produto value for this ColetaTO.
     * 
     * @param produto
     */
    public void setProduto(net.wasys.util.correiows.ProdutoTO[] produto) {
        this.produto = produto;
    }

    public net.wasys.util.correiows.ProdutoTO getProduto(int i) {
        return this.produto[i];
    }

    public void setProduto(int i, net.wasys.util.correiows.ProdutoTO _value) {
        this.produto[i] = _value;
    }


    /**
     * Gets the remetente value for this ColetaTO.
     * 
     * @return remetente
     */
    public net.wasys.util.correiows.RemetenteTO getRemetente() {
        return remetente;
    }


    /**
     * Sets the remetente value for this ColetaTO.
     * 
     * @param remetente
     */
    public void setRemetente(net.wasys.util.correiows.RemetenteTO remetente) {
        this.remetente = remetente;
    }


    /**
     * Gets the tipo value for this ColetaTO.
     * 
     * @return tipo
     */
    public java.lang.String getTipo() {
        return tipo;
    }


    /**
     * Sets the tipo value for this ColetaTO.
     * 
     * @param tipo
     */
    public void setTipo(java.lang.String tipo) {
        this.tipo = tipo;
    }


    /**
     * Gets the valor_declarado value for this ColetaTO.
     * 
     * @return valor_declarado
     */
    public java.lang.String getValor_declarado() {
        return valor_declarado;
    }


    /**
     * Sets the valor_declarado value for this ColetaTO.
     * 
     * @param valor_declarado
     */
    public void setValor_declarado(java.lang.String valor_declarado) {
        this.valor_declarado = valor_declarado;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ColetaTO)) return false;
        ColetaTO other = (ColetaTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.cklist==null && other.getCklist()==null) || 
             (this.cklist!=null &&
              this.cklist.equals(other.getCklist()))) &&
            ((this.descricao==null && other.getDescricao()==null) || 
             (this.descricao!=null &&
              this.descricao.equals(other.getDescricao()))) &&
            ((this.id_cliente==null && other.getId_cliente()==null) || 
             (this.id_cliente!=null &&
              this.id_cliente.equals(other.getId_cliente()))) &&
            ((this.produto==null && other.getProduto()==null) || 
             (this.produto!=null &&
              java.util.Arrays.equals(this.produto, other.getProduto()))) &&
            ((this.remetente==null && other.getRemetente()==null) || 
             (this.remetente!=null &&
              this.remetente.equals(other.getRemetente()))) &&
            ((this.tipo==null && other.getTipo()==null) || 
             (this.tipo!=null &&
              this.tipo.equals(other.getTipo()))) &&
            ((this.valor_declarado==null && other.getValor_declarado()==null) || 
             (this.valor_declarado!=null &&
              this.valor_declarado.equals(other.getValor_declarado())));
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
        if (getCklist() != null) {
            _hashCode += getCklist().hashCode();
        }
        if (getDescricao() != null) {
            _hashCode += getDescricao().hashCode();
        }
        if (getId_cliente() != null) {
            _hashCode += getId_cliente().hashCode();
        }
        if (getProduto() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getProduto());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getProduto(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRemetente() != null) {
            _hashCode += getRemetente().hashCode();
        }
        if (getTipo() != null) {
            _hashCode += getTipo().hashCode();
        }
        if (getValor_declarado() != null) {
            _hashCode += getValor_declarado().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ColetaTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "coletaTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cklist");
        elemField.setXmlName(new javax.xml.namespace.QName("", "cklist"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descricao");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descricao"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id_cliente");
        elemField.setXmlName(new javax.xml.namespace.QName("", "id_cliente"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("produto");
        elemField.setXmlName(new javax.xml.namespace.QName("", "produto"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "produtoTO"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remetente");
        elemField.setXmlName(new javax.xml.namespace.QName("", "remetente"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://cliente.bean.master.sigep.bsb.correios.com.br/", "remetenteTO"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tipo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "tipo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valor_declarado");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valor_declarado"));
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
