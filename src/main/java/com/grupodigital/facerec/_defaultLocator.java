/**
 * _defaultLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.grupodigital.facerec;

@SuppressWarnings({"rawtypes", "unchecked"})
public class _defaultLocator extends org.apache.axis.client.Service implements com.grupodigital.facerec._default {

    public _defaultLocator() {
    }


    public _defaultLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public _defaultLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DefaultSoap
    private java.lang.String DefaultSoap_address = "http://127.0.0.1/facepolws/default.asmx";

    public java.lang.String getDefaultSoapAddress() {
        return DefaultSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DefaultSoapWSDDServiceName = "DefaultSoap";

    public java.lang.String getDefaultSoapWSDDServiceName() {
        return DefaultSoapWSDDServiceName;
    }

    public void setDefaultSoapWSDDServiceName(java.lang.String name) {
        DefaultSoapWSDDServiceName = name;
    }

    public com.grupodigital.facerec.DefaultSoap getDefaultSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DefaultSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDefaultSoap(endpoint);
    }

    public com.grupodigital.facerec.DefaultSoap getDefaultSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.grupodigital.facerec.DefaultSoapStub _stub = new com.grupodigital.facerec.DefaultSoapStub(portAddress, this);
            _stub.setPortName(getDefaultSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDefaultSoapEndpointAddress(java.lang.String address) {
        DefaultSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.grupodigital.facerec.DefaultSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                com.grupodigital.facerec.DefaultSoapStub _stub = new com.grupodigital.facerec.DefaultSoapStub(new java.net.URL(DefaultSoap_address), this);
                _stub.setPortName(getDefaultSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("DefaultSoap".equals(inputPortName)) {
            return getDefaultSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "Default");
    }

    private java.util.HashSet ports = null;

	public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "DefaultSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DefaultSoap".equals(portName)) {
            setDefaultSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
