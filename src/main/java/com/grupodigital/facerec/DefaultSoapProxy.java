package com.grupodigital.facerec;

public class DefaultSoapProxy implements com.grupodigital.facerec.DefaultSoap {
  private String _endpoint = null;
  private com.grupodigital.facerec.DefaultSoap defaultSoap = null;
  
  public DefaultSoapProxy() {
    _initDefaultSoapProxy();
  }
  
  public DefaultSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initDefaultSoapProxy();
  }
  
  private void _initDefaultSoapProxy() {
    try {
      defaultSoap = (new com.grupodigital.facerec._defaultLocator()).getDefaultSoap();
      if (defaultSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)defaultSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)defaultSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (defaultSoap != null)
      ((javax.xml.rpc.Stub)defaultSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.grupodigital.facerec.DefaultSoap getDefaultSoap() {
    if (defaultSoap == null)
      _initDefaultSoapProxy();
    return defaultSoap;
  }
  
  public com.grupodigital.facerec.FacialRecogn getFaceImage(java.lang.String pathImage) throws java.rmi.RemoteException{
    if (defaultSoap == null)
      _initDefaultSoapProxy();
    return defaultSoap.getFaceImage(pathImage);
  }
  
  public com.grupodigital.facerec.MatchResult[] getMatch(com.grupodigital.facerec.FacialRecogn imagePrincipal, com.grupodigital.facerec.FacialRecogn[] compareList) throws java.rmi.RemoteException{
    if (defaultSoap == null)
      _initDefaultSoapProxy();
    return defaultSoap.getMatch(imagePrincipal, compareList);
  }
  
  
}