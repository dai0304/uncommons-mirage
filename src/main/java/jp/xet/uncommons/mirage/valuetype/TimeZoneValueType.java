/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2012/01/16
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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

import jp.sf.amateras.mirage.type.AbstractValueType;
import jp.sf.amateras.mirage.type.ValueType;

/**
 * {@link TimeZone}用{@link ValueType}実装クラス。
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class TimeZoneValueType extends AbstractValueType<TimeZone> {
	
	/**
	 * インスタンスを生成する。
	 */
	public TimeZoneValueType() {
		super(TimeZone.class);
	}
	
	@Override
	public TimeZone get(Class<? extends TimeZone> type, CallableStatement cs, int index) throws SQLException {
		String id = cs.getString(index);
		return id == null ? null : TimeZone.getTimeZone(id);
	}
	
	@Override
	public TimeZone get(Class<? extends TimeZone> type, CallableStatement cs, String parameterName) throws SQLException {
		String id = cs.getString(parameterName);
		return id == null ? null : TimeZone.getTimeZone(id);
	}
	
	@Override
	public TimeZone get(Class<? extends TimeZone> type, ResultSet rs, int index) throws SQLException {
		String id = rs.getString(index);
		return id == null ? null : TimeZone.getTimeZone(id);
	}
	
	@Override
	public TimeZone get(Class<? extends TimeZone> type, ResultSet rs, String columnName) throws SQLException {
		String id = rs.getString(columnName);
		return id == null ? null : TimeZone.getTimeZone(id);
	}
	
	@Override
	public boolean isSupport(Class<?> type) {
		return TimeZone.class.isAssignableFrom(type);
	}
	
	@Override
	public void set(Class<? extends TimeZone> type, PreparedStatement stmt, TimeZone value, int index)
			throws SQLException {
		if (value == null) {
			setNull(type, stmt, index);
		} else {
			stmt.setString(index, value.getID());
		}
	}
}
