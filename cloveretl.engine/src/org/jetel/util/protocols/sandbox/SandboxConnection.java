/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.util.protocols.sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jetel.graph.ContextProvider;
import org.jetel.graph.runtime.IAuthorityProxy;

public class SandboxConnection extends URLConnection {
	
	/**
	 * SFTP constructor.
	 * @param graph 
	 * 
	 * @param url
	 */
	protected SandboxConnection(URL url) {
		super(url);
	}

	/*
	 * (non-Javadoc)
	 * @see java.net.URLConnection#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		String storageCode = url.getHost();
		String path = url.getPath();
		IAuthorityProxy authorityProxy = IAuthorityProxy.getAuthorityProxy(ContextProvider.getGraph());
		try {
			return authorityProxy.getSandboxResourceInput(ContextProvider.getComponentId(), storageCode, path);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.net.URLConnection#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		String storageCode = url.getHost();
		String path = url.getPath();
		IAuthorityProxy authorityProxy = IAuthorityProxy.getAuthorityProxy(ContextProvider.getGraph());
		try {
			return authorityProxy.getSandboxResourceOutput(ContextProvider.getComponentId(), storageCode, path, false);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.net.URLConnection#connect()
	 */
	@Override
	public void connect() throws IOException {
	}

}
