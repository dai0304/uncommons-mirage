/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/23
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
package jp.xet.uncommons.mirage.valuetype.enumerated;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import jp.sf.amateras.mirage.type.ValueType;
import jp.xet.uncommons.mirage.valuetype.enumerated.Enumerated.EnumType;

/**
 * {@link Enum}型を {@link String}型としてDBに保存するための {@link ValueType}実装クラス。
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class EnumStringValueType implements ValueType {
	
	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>>T toEnum(Class<?> type, String name) {
		try {
			return Enum.valueOf((Class<T>) type, name);
		} catch (IllegalArgumentException e) {
			// TODO 仕様をきちんと考える
			return (T) type.getEnumConstants()[0];
		}
	}
	
	@Override
	public Object get(Class<?> type, CallableStatement cs, int index) throws SQLException {
		String value = cs.getString(index);
		return value;
	}
	
	@Override
	public Object get(Class<?> type, CallableStatement cs, String parameterName) throws SQLException {
		String value = cs.getString(parameterName);
		return value;
	}
	
	@Override
	public Object get(Class<?> type, ResultSet rs, int columnIndex) throws SQLException {
		String name = rs.getString(columnIndex);
		return toEnum(type, name);
	}
	
	@Override
	public Object get(Class<?> type, ResultSet rs, String columnName) throws SQLException {
		String name = rs.getString(columnName);
		return toEnum(type, name);
	}
	
	@Override
	public Class<?> getJavaType(int sqlType) {
		return String.class;
	}
	
	@Override
	public boolean isSupport(Class<?> type) {
		if (Enum.class.isAssignableFrom(type) == false) {
			return false;
		}
		Enumerated enumerated = type.getAnnotation(Enumerated.class);
		if (enumerated == null) {
			return false;
		}
		return enumerated.value() == EnumType.STRING;
	}
	
	@Override
	public void registerOutParameter(Class<?> type, CallableStatement cs, int index) throws SQLException {
		cs.registerOutParameter(index, Types.VARCHAR);
	}
	
	@Override
	public void registerOutParameter(Class<?> type, CallableStatement cs, String parameterName) throws SQLException {
		cs.registerOutParameter(parameterName, Types.VARCHAR);
	}
	
	@Override
	public void set(Class<?> type, PreparedStatement stmt, Object value, int index) throws SQLException {
		if (value == null) {
			stmt.setNull(index, Types.VARCHAR);
		} else {
			stmt.setString(index, ((Enum<?>) value).name());
		}
	}
}
