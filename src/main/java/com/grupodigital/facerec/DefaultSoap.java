/**
 * DefaultSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.grupodigital.facerec;

public interface DefaultSoap extends java.rmi.Remote {
    public com.grupodigital.facerec.FacialRecogn getFaceImage(java.lang.String pathImage) throws java.rmi.RemoteException;
    public com.grupodigital.facerec.MatchResult[] getMatch(com.grupodigital.facerec.FacialRecogn imagePrincipal, com.grupodigital.facerec.FacialRecogn[] compareList) throws java.rmi.RemoteException;
}
