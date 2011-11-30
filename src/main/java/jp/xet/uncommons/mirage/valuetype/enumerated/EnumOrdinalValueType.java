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

import org.springframework.core.annotation.AnnotationUtils;

/**
 * {@link Enum}型をordinalの {@code int}型としてDBに保存するための {@link ValueType}実装クラス。
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class EnumOrdinalValueType implements ValueType<Object> {
	
	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>>T toEnum(Class<?> type, int ordinal) {
		return (T) type.getEnumConstants()[ordinal];
	}
	
	@Override
	public Integer get(Class<?> type, CallableStatement cs, int index) throws SQLException {
		int value = cs.getInt(index);
		return value;
	}
	
	@Override
	public Integer get(Class<?> type, CallableStatement cs, String parameterName) throws SQLException {
		int value = cs.getInt(parameterName);
		return value;
	}
	
	@Override
	public Enum<?> get(Class<?> type, ResultSet rs, int columnIndex) throws SQLException {
		int ordinal = rs.getInt(columnIndex);
		return toEnum(type, ordinal);
	}
	
	@Override
	public Enum<?> get(Class<?> type, ResultSet rs, String columnName) throws SQLException {
		int ordinal = rs.getInt(columnName);
		return toEnum(type, ordinal);
	}
	
	@Override
	public Class<?> getJavaType(int sqlType) {
		return Integer.class;
	}
	
	@Override
	public boolean isSupport(Class<?> type) {
		if (Enum.class.isAssignableFrom(type) == false) {
			return false;
		}
		Enumerated enumerated = AnnotationUtils.findAnnotation(type, Enumerated.class);
		if (enumerated == null) {
			return false;
		}
		return enumerated.value() == EnumType.ORDINAL;
	}
	
	@Override
	public void registerOutParameter(Class<?> type, CallableStatement cs, int index) throws SQLException {
		cs.registerOutParameter(index, Types.INTEGER);
	}
	
	@Override
	public void registerOutParameter(Class<?> type, CallableStatement cs, String parameterName) throws SQLException {
		cs.registerOutParameter(parameterName, Types.INTEGER);
	}
	
	@Override
	public void set(Class<?> type, PreparedStatement stmt, Object value, int index) throws SQLException {
		if (value == null) {
			stmt.setNull(index, Types.INTEGER);
		} else {
			stmt.setInt(index, ((Enum<?>) value).ordinal());
		}
	}
}
