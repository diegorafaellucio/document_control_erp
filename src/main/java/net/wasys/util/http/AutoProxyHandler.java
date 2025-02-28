/*
 * @(#)DummyAutoProxyHandler.java	1.6 10/03/24
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package net.wasys.util.http;

import java.net.URI;
import java.net.URL;
import java.util.StringTokenizer;

import com.sun.deploy.net.proxy.AbstractAutoProxyHandler;
import com.sun.deploy.net.proxy.ProxyInfo;
import com.sun.deploy.net.proxy.ProxyUnavailableException;
import net.wasys.util.LogLevel;

import static net.wasys.util.DummyUtils.systraceThread;

/**
 * Proxy handler for dummy auto proxy configuration. 
 *
 * This proxy handler basically parses the pac/js file manually and extract
 * the first PROXY/SOCKS/DIRECT string, so the proxy info may not be 
 * completely accurate.
 */
public final class AutoProxyHandler extends AbstractAutoProxyHandler
{
	/**
	 * Return true if auto proxy is handled by IE.
	 */
	protected boolean isIExplorer()
	{
		return true;
	}

	/**
	 * Returns proxy info for a given URL
	 *
	 * @param uri URL
	 * @return proxy info for a given URL
	 */
	public ProxyInfo[] getProxyInfo(URI uri){

		try {
			String result = null;

			if (jsPacScript != null) {		    
				StringTokenizer st = new StringTokenizer(jsPacScript, ";", false);

				while (st.hasMoreTokens()) {

					String pattern = st.nextToken();

					int x = pattern.indexOf("DIRECT");
					int y = pattern.indexOf("PROXY");
					int z = pattern.indexOf("SOCKS");

					int i = positiveMin(x, positiveMin(y, z));
					int j = pattern.lastIndexOf("\"");

					// Continue the loop if no proxy info is found.
					if (i == -1)
						continue;

					if (j <= i)
						result = pattern.substring(i);  // Case "DIRECT;..
					else 
						result = pattern.substring(i, j); // Case "DIRECT";

					break;
				}
			}

			return extractAutoProxySetting(result);
		}
		catch (Throwable e)
		{
			systraceThread("DummyAutoProxyHandler.getProxyInfo() - net.proxy.auto.result.error", LogLevel.ERROR);
			e.printStackTrace();
			return new ProxyInfo[] {new ProxyInfo(null)};
		}
	}

	/**
	 * Return positive minimum between two values. 
	 */
	private int positiveMin(int x, int y)
	{
		if (x < 0)
			return y;

		if (y < 0)
			return x;

		if (x > y)
			return y;
		else
			return x;
	}

	protected String getBrowserSpecificAutoProxy() {
		return null;
	}

	public ProxyInfo[] getProxyInfo(URL arg0) throws ProxyUnavailableException {
		return null;
	}

}