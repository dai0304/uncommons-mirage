/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/11/11
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
package jp.xet.uncommons.mirage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.sf.amateras.mirage.EntityCreationFailedException;
import jp.sf.amateras.mirage.ResultEntityCreator;
import jp.sf.amateras.mirage.bean.BeanDesc;
import jp.sf.amateras.mirage.bean.PropertyDesc;
import jp.sf.amateras.mirage.dialect.Dialect;
import jp.sf.amateras.mirage.naming.NameConverter;
import jp.sf.amateras.mirage.type.ValueType;
import jp.sf.amateras.mirage.util.MirageUtil;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class AccessibleEntityCreator implements ResultEntityCreator {
	
	/**
	 * Creates and returns one entity instance from the ResultSet.
	 *
	 * @param <T> the type parameter of entity class
	 * @param clazz the entity class
	 * @param rs the ResultSet
	 * @param meta the ResultSetMetaData
	 * @param columnCount the column count
	 * @param beanDesc the BeanDesc of the entity class
	 * @param dialect the Dialect
	 * @param valueTypes the list of ValueTypes
	 * @param nameConverter the NameConverter
	 * @return the instance of entity class or Map
	 * @throws EntityCreationFailedException if {@link ResultEntityCreator} failed to create a result entity
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T>T createEntity(Class<T> clazz, ResultSet rs, ResultSetMetaData meta, int columnCount, BeanDesc beanDesc,
			Dialect dialect, List<ValueType> valueTypes, NameConverter nameConverter) {
		
		try {
			if (dialect.getValueType() != null) {
				ValueType valueType = dialect.getValueType();
				if (valueType.isSupport(clazz)) {
					return (T) valueType.get(clazz, rs, 1);
				}
			}
			
			for (ValueType valueType : valueTypes) {
				if (valueType.isSupport(clazz)) {
					return (T) valueType.get(clazz, rs, 1);
				}
			}
			
			T entity = null;
			
			if (clazz == Map.class) {
				entity = (T) new HashMap<String, Object>();
			} else {
				Constructor<T> constructor = clazz.getDeclaredConstructor(new Class<?>[0]);
				constructor.setAccessible(true);
				entity = constructor.newInstance();
			}
			
			for (int i = 0; i < columnCount; i++) {
				String columnName = meta.getColumnName(i + 1);
				String propertyName = nameConverter.columnToProperty(columnName);
				
				PropertyDesc pd = beanDesc.getPropertyDesc(propertyName);
				
				if (pd != null) {
					Class<?> fieldType = pd.getPropertyType();
					ValueType valueType = MirageUtil.getValueType(fieldType, dialect, valueTypes);
					if (valueType != null) {
						pd.setValue(entity, valueType.get(fieldType, rs, columnName));
					}
				}
			}
			
			return entity;
		} catch (SQLException e) {
			throw new EntityCreationFailedException(e);
			
		} catch (SecurityException e) {
			throw new EntityCreationFailedException(e);
			
		} catch (NoSuchMethodException e) {
			throw new EntityCreationFailedException(e);
			
		} catch (IllegalArgumentException e) {
			throw new EntityCreationFailedException(e);
			
		} catch (InstantiationException e) {
			throw new EntityCreationFailedException(e);
			
		} catch (IllegalAccessException e) {
			throw new EntityCreationFailedException(e);
			
		} catch (InvocationTargetException e) {
			throw new EntityCreationFailedException(e);
			
		}
	}
	
}
