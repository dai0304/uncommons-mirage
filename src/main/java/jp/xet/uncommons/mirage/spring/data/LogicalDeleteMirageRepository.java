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
package jp.xet.uncommons.mirage.spring.data;

import org.springframework.data.repository.NoRepositoryBean;

/**
 * TODO for daisuke
 * 
 * @param <T> the domain type the repository manages
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
@NoRepositoryBean
public interface LogicalDeleteMirageRepository<T> extends MirageRepository<T, Long> {
	
	void physicalDelete(Iterable<? extends T> entities);
	
	void physicalDelete(Long id);
	
	void physicalDelete(T entity);
	
	void physicalDeleteAll();
	
	void physicalDeleteInBatch(Iterable<T> entities);
	
	void revert(Long id);
	
}
