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

import java.util.Map;

import jp.sf.amateras.mirage.exception.SQLRuntimeException;

import org.apache.commons.lang.Validate;

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
	
	static final SqlResource BASE_LOGICAL_DELETE = new SimpleSqlResource(SimpleMirageRepository.class,
			"baseLogicalDelete.sql");
	
	
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
		Validate.notNull(entity);
		delete(entity.getId());
	}
	
	@Override
	public void delete(Long id) {
		if (id > 0) {
			try {
				executeUpdate(BASE_LOGICAL_DELETE, createParams(id));
			} catch (SQLRuntimeException e) {
				throw getExceptionTranslator().translate("delete", null, e.getCause());
			}
		}
	}
	
	@Override
	public void deleteInBatch(Iterable<E> entities) {
		// THINK これでいいのか…？
		try {
			delete(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("deleteInBatch", null, e.getCause());
		}
	}
	
	@Override
	public boolean exists(Long id) {
		try {
			Map<String, Object> params = createParams(id);
			params.put("include_logical_deleted", true);
			return getCount(getBaseSelectSqlResource(), params) > 0;
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("exists", null, e.getCause());
		}
	}
	
	@Override
	public E findOneIncludeLogicalDeleted(Long id) {
		Map<String, Object> params = createParams(id);
		params.remove("id");
		params.put("absid", id);
		params.put("include_logical_deleted", true);
		try {
			return getSingleResult(getBaseSelectSqlResource(), params);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("findOne", null, e.getCause());
		}
	}
	
	@Override
	public void physicalDelete(E entity) {
		try {
			sqlManager.deleteEntity(entity);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("physicalDelete", null, e.getCause());
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void physicalDelete(Iterable<? extends E> entities) {
		try {
			sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("physicalDelete", null, e.getCause());
		}
	}
	
	@Override
	public void physicalDelete(Long id) {
		try {
			sqlManager.deleteEntity(findOne(id));
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("physicalDelete", null, e.getCause());
		}
	}
	
	@Override
	public void physicalDeleteAll() {
		try {
			sqlManager.deleteBatch(findAll());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("physicalDeleteAll", null, e.getCause());
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void physicalDeleteInBatch(Iterable<E> entities) {
		try {
			sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("physicalDeleteInBatch", null, e.getCause());
		}
	}
	
	@Override
	public void revert(Long id) {
		if (id < 0) {
			try {
				executeUpdate(BASE_LOGICAL_DELETE, createParams(id));
			} catch (SQLRuntimeException e) {
				throw getExceptionTranslator().translate("revert", null, e.getCause());
			}
		}
	}
	
	@Override
	public E save(E entity) {
		if (entity == null) {
			return null;
		}
		try {
			long id = entity.getId();
			E found = findOneIncludeLogicalDeleted(id);
			if (found == null) {
				sqlManager.insertEntity(entity);
			} else if (found.getId() > 0) {
				sqlManager.updateEntity(entity);
			} else {
				throw new EntityDeletedException(id);
			}
//			if (exists(id)) {
//				sqlManager.updateEntity(entity);
//			} else if (exists(id * -1)) {
//				throw new EntityDeletedException(id);
//			} else {
//				sqlManager.insertEntity(entity);
//			}
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("save", null, e.getCause());
		}
		return entity;
	}
}
