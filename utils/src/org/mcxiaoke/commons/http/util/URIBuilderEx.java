/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.mcxiaoke.commons.http.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.message.BasicNameValuePair;

public class URIBuilderEx {

	private String scheme;
	private String encodedSchemeSpecificPart;
	private String encodedAuthority;
	private String userInfo;
	private String encodedUserInfo;
	private String host;
	private int port;
	private String path;
	private String encodedPath;
	private String encodedQuery;
	private List<NameValuePair> queryParams;
	private String fragment;
	private String encodedFragment;

	/**
	 * Constructs an empty instance.
	 */
	public URIBuilderEx() {
		super();
		this.port = -1;
	}

	/**
	 * Construct an instance from the string which must be a valid URI.
	 * 
	 * @param string
	 *            a valid URI in string form
	 * @throws URISyntaxException
	 *             if the input is not a valid URI
	 */
	public URIBuilderEx(final String string) throws URISyntaxException {
		super();
		digestURI(new URI(string));
	}

	/**
	 * Construct an instance from the provided URI.
	 * 
	 * @param uri
	 */
	public URIBuilderEx(final URI uri) {
		super();
		digestURI(uri);
	}

	private List<NameValuePair> parseQuery(final String query,
			final Charset charset) {
		if (query != null && query.length() > 0) {
			return URIUtilsEx.parse(query, charset);
		}
		return null;
	}

	/**
	 * Builds a {@link URI} instance.
	 */
	public URI build() throws URISyntaxException {
		return new URI(buildString());
	}

	private String buildString() {
		StringBuilder sb = new StringBuilder();
		if (this.scheme != null) {
			sb.append(this.scheme).append(':');
		}
		if (this.encodedSchemeSpecificPart != null) {
			sb.append(this.encodedSchemeSpecificPart);
		} else {
			if (this.encodedAuthority != null) {
				sb.append("//").append(this.encodedAuthority);
			} else if (this.host != null) {
				sb.append("//");
				if (this.encodedUserInfo != null) {
					sb.append(this.encodedUserInfo).append("@");
				} else if (this.userInfo != null) {
					sb.append(encodeUserInfo(this.userInfo)).append("@");
				}
				if (InetAddressUtils.isIPv6Address(this.host)) {
					sb.append("[").append(this.host).append("]");
				} else {
					sb.append(this.host);
				}
				if (this.port >= 0) {
					sb.append(":").append(this.port);
				}
			}
			if (this.encodedPath != null) {
				sb.append(normalizePath(this.encodedPath));
			} else if (this.path != null) {
				sb.append(encodePath(normalizePath(this.path)));
			}
			if (this.encodedQuery != null) {
				sb.append("?").append(this.encodedQuery);
			} else if (this.queryParams != null) {
				sb.append("?").append(encodeQuery(this.queryParams));
			}
		}
		if (this.encodedFragment != null) {
			sb.append("#").append(this.encodedFragment);
		} else if (this.fragment != null) {
			sb.append("#").append(encodeFragment(this.fragment));
		}
		return sb.toString();
	}

	private void digestURI(final URI uri) {
		this.scheme = uri.getScheme();
		this.encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
		this.encodedAuthority = uri.getRawAuthority();
		this.host = uri.getHost();
		this.port = uri.getPort();
		this.encodedUserInfo = uri.getRawUserInfo();
		this.userInfo = uri.getUserInfo();
		this.encodedPath = uri.getRawPath();
		this.path = uri.getPath();
		this.encodedQuery = uri.getRawQuery();
		this.queryParams = parseQuery(uri.getRawQuery(), URIUtilsEx.UTF_8);
		this.encodedFragment = uri.getRawFragment();
		this.fragment = uri.getFragment();
	}

	private String encodeUserInfo(final String userInfo) {
		return URIUtilsEx.encUserInfo(userInfo, URIUtilsEx.UTF_8);
	}

	private String encodePath(final String path) {
		return URIUtilsEx.encPath(path, URIUtilsEx.UTF_8);
	}

	private String encodeQuery(final List<NameValuePair> params) {
		return URIUtilsEx.format(params, URIUtilsEx.UTF_8);
	}

	private String encodeFragment(final String fragment) {
		return URIUtilsEx.encFragment(fragment, URIUtilsEx.UTF_8);
	}

	/**
	 * Sets URI scheme.
	 */
	public URIBuilderEx setScheme(final String scheme) {
		this.scheme = scheme;
		return this;
	}

	/**
	 * Sets URI user info. The value is expected to be unescaped and may contain
	 * non ASCII characters.
	 */
	public URIBuilderEx setUserInfo(final String userInfo) {
		this.userInfo = userInfo;
		this.encodedSchemeSpecificPart = null;
		this.encodedAuthority = null;
		this.encodedUserInfo = null;
		return this;
	}

	/**
	 * Sets URI user info as a combination of username and password. These
	 * values are expected to be unescaped and may contain non ASCII characters.
	 */
	public URIBuilderEx setUserInfo(final String username, final String password) {
		return setUserInfo(username + ':' + password);
	}

	/**
	 * Sets URI host.
	 */
	public URIBuilderEx setHost(final String host) {
		this.host = host;
		this.encodedSchemeSpecificPart = null;
		this.encodedAuthority = null;
		return this;
	}

	/**
	 * Sets URI port.
	 */
	public URIBuilderEx setPort(final int port) {
		this.port = port < 0 ? -1 : port;
		this.encodedSchemeSpecificPart = null;
		this.encodedAuthority = null;
		return this;
	}

	/**
	 * Sets URI path. The value is expected to be unescaped and may contain non
	 * ASCII characters.
	 */
	public URIBuilderEx setPath(final String path) {
		this.path = path;
		this.encodedSchemeSpecificPart = null;
		this.encodedPath = null;
		return this;
	}

	/**
	 * Removes URI query.
	 */
	public URIBuilderEx removeQuery() {
		this.queryParams = null;
		this.encodedQuery = null;
		this.encodedSchemeSpecificPart = null;
		return this;
	}

	/**
	 * Sets URI query.
	 * <p>
	 * The value is expected to be encoded form data.
	 */
	public URIBuilderEx setQuery(final String query) {
		this.queryParams = parseQuery(query, URIUtilsEx.UTF_8);
		this.encodedQuery = null;
		this.encodedSchemeSpecificPart = null;
		return this;
	}

	public URIBuilderEx addParameters(Map<String, String> params) {
		if (this.queryParams == null) {
			this.queryParams = new ArrayList<NameValuePair>();
		}
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				this.queryParams.add(new BasicNameValuePair(entry.getKey(),
						entry.getValue()));
			}
		}
		this.encodedQuery = null;
		this.encodedSchemeSpecificPart = null;
		return this;
	}

	public URIBuilderEx addParameters(Collection<? extends NameValuePair> params) {
		if (this.queryParams == null) {
			this.queryParams = new ArrayList<NameValuePair>();
		}
		this.queryParams.addAll(params);
		this.encodedQuery = null;
		this.encodedSchemeSpecificPart = null;
		return this;
	}

	/**
	 * Adds parameter to URI query. The parameter name and value are expected to
	 * be unescaped and may contain non ASCII characters.
	 */
	public URIBuilderEx addParameter(final String param, final String value) {
		if (this.queryParams == null) {
			this.queryParams = new ArrayList<NameValuePair>();
		}
		this.queryParams.add(new BasicNameValuePair(param, value));
		this.encodedQuery = null;
		this.encodedSchemeSpecificPart = null;
		return this;
	}

	/**
	 * Sets parameter of URI query overriding existing value if set. The
	 * parameter name and value are expected to be unescaped and may contain non
	 * ASCII characters.
	 */
	public URIBuilderEx setParameter(final String param, final String value) {
		if (this.queryParams == null) {
			this.queryParams = new ArrayList<NameValuePair>();
		}
		if (!this.queryParams.isEmpty()) {
			for (Iterator<NameValuePair> it = this.queryParams.iterator(); it
					.hasNext();) {
				NameValuePair nvp = it.next();
				if (nvp.getName().equals(param)) {
					it.remove();
				}
			}
		}
		this.queryParams.add(new BasicNameValuePair(param, value));
		this.encodedQuery = null;
		this.encodedSchemeSpecificPart = null;
		return this;
	}

	/**
	 * Sets URI fragment. The value is expected to be unescaped and may contain
	 * non ASCII characters.
	 */
	public URIBuilderEx setFragment(final String fragment) {
		this.fragment = fragment;
		this.encodedFragment = null;
		return this;
	}

	public String getScheme() {
		return this.scheme;
	}

	public String getUserInfo() {
		return this.userInfo;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getPath() {
		return this.path;
	}

	public List<NameValuePair> getQueryParams() {
		if (this.queryParams != null) {
			return new ArrayList<NameValuePair>(this.queryParams);
		} else {
			return new ArrayList<NameValuePair>();
		}
	}

	public String getFragment() {
		return this.fragment;
	}

	@Override
	public String toString() {
		return buildString();
	}

	private static String normalizePath(String path) {
		if (path == null) {
			return null;
		}
		int n = 0;
		for (; n < path.length(); n++) {
			if (path.charAt(n) != '/') {
				break;
			}
		}
		if (n > 1) {
			path = path.substring(n - 1);
		}
		return path;
	}

}
