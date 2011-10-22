/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/21
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

import jp.sf.amateras.mirage.naming.DefaultNameConverter;
import jp.sf.amateras.mirage.naming.NameConverter;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id: RailsLikeNameConverter.java 161 2011-10-21 10:08:21Z daisuke $
 * @author daisuke
 */
public class RailsLikeNameConverter implements NameConverter {
	
	private static final NameConverter DNC = new DefaultNameConverter();
	
	
	@Override
	public String columnToProperty(String columnName) {
		return DNC.columnToProperty(columnName);
	}
	
	@Override
	public String entityToTable(String entityName) {
		return DNC.entityToTable(entityName) + "S";
	}
	
	@Override
	public String propertyToColumn(String propertyName) {
		return DNC.propertyToColumn(propertyName);
	}
}
