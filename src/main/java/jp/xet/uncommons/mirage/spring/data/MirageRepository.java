/*
 * Copyright 2011 datemplatecopy.
 * Created on 2011/10/20
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

import java.io.Serializable;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * TODO for daisuke
 * 
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @since 1.0
 * @version $Id: MirageRepository.java 161 2011-10-21 10:08:21Z daisuke $
 * @author daisuke
 */
@NoRepositoryBean
public interface MirageRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 */
	@Override
	void delete(ID id);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 */
	@Override
	void delete(Iterable<? extends T> entities);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 */
	@Override
	void delete(T entity);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 */
	@Override
	void deleteAll();
	
	/**
	 * TODO
	 * 
	 * @param entities 
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 */
	void deleteInBatch(Iterable<T> entities);
	
	@Override
	List<T> findAll();
	
	@Override
	List<T> findAll(Sort sort);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 */
	@Override
	List<T> save(Iterable<? extends T> entities);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 */
	@Override
	T save(T entity);
}
