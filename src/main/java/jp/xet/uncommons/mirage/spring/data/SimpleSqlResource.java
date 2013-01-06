/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/11/13
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
package jp.xet.uncommons.mirage.spring.data;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * {@link SqlResource}のデフォルト実装クラス。
 * 
 * @since 1.0.0
 * @version $Id$
 * @author daisuke
 */
@Deprecated
public class SimpleSqlResource implements SqlResource {
	
	static String toAbsolutePath(final String packageName, final String relativePath) {
		// Is path already absolute?
		if (relativePath.startsWith("/")) {
			return relativePath;
		} else {
			// Break package into list of package names
			List<String> absolutePath = Lists.newArrayList(packageName.split("\\."));
			
			// Break path into folders
			final String[] folders = relativePath.split("[/\\\\]");
			
			// Iterate through folders
			for (String folder : folders) {
				// Up one?
				if ("..".equals(folder)) {
					// Pop off stack
					if (absolutePath.size() > 0) {
						absolutePath.remove(absolutePath.size() - 1);
					} else {
						throw new IllegalArgumentException("Invalid path " + relativePath);
					}
				} else {
					// Add to stack
					absolutePath.add(folder);
				}
			}
			
			// Return absolute path
			return StringUtils.join(absolutePath, "/");
		}
	}
	
	
	private final String absolutePath;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param scope 
	 * @param name 
	 */
	public SimpleSqlResource(Class<?> scope, final String name) {
		Validate.notEmpty(name);
		String packageName = scope != null ? scope.getPackage().getName() : "";
		absolutePath = toAbsolutePath(packageName, name);
	}
	
	@Override
	public String getAbsolutePath() {
		return absolutePath;
	}
}
