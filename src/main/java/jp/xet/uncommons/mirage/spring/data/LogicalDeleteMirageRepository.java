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
package jp.xet.uncommons.mirage.spring.data;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public abstract class LogicalDeleteMirageRepository<T> extends SimpleMirageRepository<T, Long> implements
		LogicalDeleteJdbcRepository<T> {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param entityClass エンティティの型
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public LogicalDeleteMirageRepository(Class<T> entityClass) {
		super(entityClass);
	}
	
	@Override
	public void delete(Long id) {
		if (id > 0) {
			sqlManager.executeUpdate(pathOf("baseLogicalDelete.sql"), createParams(id));
		} else {
			// TODO
		}
	}
	
	@Override
	public void delete(T entity) {
		sqlManager.executeUpdate(pathOf("baseLogicalDelete.sql"), createParams(getId(entity)));
	}
	
	@Override
	public void deleteInBatch(Iterable<T> entities) {
		// TODO
		super.deleteInBatch(entities);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void physicalDelete(Iterable<? extends T> entities) {
		sqlManager.deleteBatch(entities);
	}
	
	@Override
	public void physicalDelete(Long id) {
		sqlManager.deleteEntity(findOne(id));
	}
	
	@Override
	public void physicalDelete(T entity) {
		sqlManager.deleteEntity(entity);
	}
	
	@Override
	public void physicalDeleteAll() {
		sqlManager.deleteBatch(findAll());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void physicalDeleteInBatch(Iterable<T> entities) {
		sqlManager.deleteBatch(entities);
	}
	
	@Override
	public void revert(Long id) {
		if (id > 0) {
			// TODO
		} else {
			sqlManager.executeUpdate(pathOf("baseLogicalDelete.sql"), createParams(id));
		}
	}
}
