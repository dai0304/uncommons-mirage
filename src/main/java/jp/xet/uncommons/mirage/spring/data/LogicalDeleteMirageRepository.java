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
 * Mirageフレームワークを利用した {@link LogicalDeleteJdbcRepository} の実装クラス。
 * 
 * @param <E> the domain type the repository manages
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public abstract class LogicalDeleteMirageRepository<E extends Identifiable> extends IdentifiableMirageRepository<E>
		implements LogicalDeleteJdbcRepository<E> {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param entityClass エンティティの型
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public LogicalDeleteMirageRepository(Class<E> entityClass) {
		super(entityClass);
	}
	
	@Override
	public void delete(E entity) {
		sqlManager.executeUpdate(pathOf("baseLogicalDelete.sql"), createParams(getId(entity)));
	}
	
	@Override
	public void delete(Long id) {
		if (id > 0) {
			sqlManager.executeUpdate(pathOf("baseLogicalDelete.sql"), createParams(id));
		}
	}
	
	@Override
	public void deleteInBatch(Iterable<E> entities) {
		// THINK これでいいのか…？
		delete(entities);
	}
	
	@Override
	public void physicalDelete(E entity) {
		sqlManager.deleteEntity(entity);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void physicalDelete(Iterable<? extends E> entities) {
		sqlManager.deleteBatch(entities);
	}
	
	@Override
	public void physicalDelete(Long id) {
		sqlManager.deleteEntity(findOne(id));
	}
	
	@Override
	public void physicalDeleteAll() {
		sqlManager.deleteBatch(findAll());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void physicalDeleteInBatch(Iterable<E> entities) {
		sqlManager.deleteBatch(entities);
	}
	
	@Override
	public void revert(Long id) {
		if (id < 0) {
			sqlManager.executeUpdate(pathOf("baseLogicalDelete.sql"), createParams(id));
		}
	}
}
