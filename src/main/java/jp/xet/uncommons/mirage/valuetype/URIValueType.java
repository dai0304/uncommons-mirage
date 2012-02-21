/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2012/01/27
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package jp.xet.uncommons.mirage.valuetype;

import java.net.URI;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.sf.amateras.mirage.type.AbstractValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class URIValueType extends AbstractValueType<URI> {
	
	private static Logger logger = LoggerFactory.getLogger(URIValueType.class);
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 */
	public URIValueType() {
		super(URI.class);
	}
	
	@Override
	public URI get(Class<? extends URI> type, CallableStatement cs, int index) throws SQLException {
		String str = cs.getString(index);
		if (str == null) {
			return null;
		}
		URI result = null;
		try {
			result = URI.create(str);
		} catch (IllegalArgumentException e) {
			logger.warn(str, e);
		}
		return result;
	}
	
	@Override
	public URI get(Class<? extends URI> type, CallableStatement cs, String parameterName) throws SQLException {
		String str = cs.getString(parameterName);
		if (str == null) {
			return null;
		}
		URI result = null;
		try {
			result = URI.create(str);
		} catch (IllegalArgumentException e) {
			logger.warn(str, e);
		}
		return result;
	}
	
	@Override
	public URI get(Class<? extends URI> type, ResultSet rs, int index) throws SQLException {
		String str = rs.getString(index);
		if (str == null) {
			return null;
		}
		URI result = null;
		try {
			result = URI.create(str);
		} catch (IllegalArgumentException e) {
			logger.warn(str, e);
		}
		return result;
	}
	
	@Override
	public URI get(Class<? extends URI> type, ResultSet rs, String columnName) throws SQLException {
		String str = rs.getString(columnName);
		if (str == null) {
			return null;
		}
		URI result = null;
		try {
			result = URI.create(str);
		} catch (IllegalArgumentException e) {
			logger.warn(str, e);
		}
		return result;
	}
	
	@Override
	public void set(Class<? extends URI> type, PreparedStatement stmt, URI value, int index) throws SQLException {
		if (value == null) {
			setNull(type, stmt, index);
		} else {
			stmt.setString(index, value.toASCIIString());
		}
	}
}
