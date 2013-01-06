/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/24
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

/**
 * エンティティ（IDを持つオブジェクト）を表すインターフェイス。
 * 
 * @since 1.0.0
 * @version $Id$
 * @author daisuke
 */
@Deprecated
public interface Identifiable {
	
	/**
	 * IDを返す。
	 * 
	 * @return ID
	 * @since 1.0.0
	 */
	long getId();
}
