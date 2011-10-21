/*
 * Copyright 2011 datemplatecopy.
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

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import jp.sf.amateras.mirage.bean.PropertyExtractor;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id: FieldPropertyExtractor.java 160 2011-10-21 09:49:56Z daisuke $
 * @author daisuke
 */
public class FieldPropertyExtractor implements PropertyExtractor {
	
	@Override
	public Map<String, PropertyInfo> extractProperties(Class<?> clazz) {
		Map<String, PropertyInfo> map = new LinkedHashMap<String, PropertyInfo>();
		extractProperties0(clazz, map);
		return map;
	}
	
	private void extractProperties0(Class<?> clazz, Map<String, PropertyInfo> map) {
		if (clazz == null) {
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (!map.containsKey(field.getName())) {
				PropertyInfo info = new PropertyInfo();
				info.name = field.getName();
				info.field = field;
				info.type = field.getType();
				map.put(field.getName(), info);
			}
		}
		extractProperties0(clazz.getSuperclass(), map);
	}
}
