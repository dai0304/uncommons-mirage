/*
 * Copyright 2011 Daisuke Miyamoto.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jp.sf.amateras.mirage.IterationCallback;
import jp.sf.amateras.mirage.SqlManager;
import jp.sf.amateras.mirage.exception.SQLRuntimeException;
import jp.sf.amateras.mirage.naming.NameConverter;
import jp.sf.amateras.mirage.util.MirageUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.util.Assert;

/**
 * Mirageフレームワークを利用した {@link JdbcRepository} の実装クラス。
 * 
 * @param <E> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @since 1.0
 * @version $Id: SimpleMirageRepository.java 161 2011-10-21 10:08:21Z daisuke $
 * @author daisuke
 */
public abstract class SimpleMirageRepository<E, ID extends Serializable> implements JdbcRepository<E, ID> {
	
	static final SqlResource BASE_SELECT_SQL = new SimpleSqlResource(SimpleMirageRepository.class, "baseSelect.sql");
	
	
	/**
	 * 新しい {@link SqlResource} を生成する。
	 * 
	 * @param scope クラスパス上のSQLの位置を表すクラス。無名パッケージの場合は{@code null}
	 * @param filename クラスパス上のSQLファイル名
	 * @return {@link SqlResource}
	 * @throws IllegalArgumentException 引数{@code filename}に{@code null}を与えた場合
	 * @since 1.0
	 */
	public static SqlResource newSqlResource(Class<?> scope, String filename) {
		Validate.notNull(filename);
		return new SimpleSqlResource(scope, filename);
	}
	
	/**
	 * SQLファイル名をクラスパス上におけるパス名に変換する。
	 * 
	 * @param filename SQLファイル名
	 * @return パス名
	 * @since 1.0
	 */
	@Deprecated
	public static SqlResource pathOf(final String filename) {
		return new SqlResource() {
			
			@Override
			public String getAbsolutePath() {
				return "META-INF/" + filename;
			}
		};
	}
	
	
	private SQLExceptionTranslator exceptionTranslator;
	
	@Autowired
	SqlManager sqlManager;
	
	@Autowired
	NameConverter nameConverter;
	
	@Autowired(required = false)
	DataSource dataSource;
	
	private final Class<E> entityClass;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param entityClass エンティティの型
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public SimpleMirageRepository(Class<E> entityClass) {
		Assert.notNull(entityClass);
		this.entityClass = entityClass;
	}
	
	@Override
	public long count() {
		return getCount(getBaseSelectSqlResource(), createParams());
	}
	
	@Override
	public void delete(E entity) {
		Validate.notNull(entity);
		try {
			sqlManager.deleteEntity(entity);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("delete", null, e.getCause());
		}
	}
	
	@Override
	public void delete(ID id) {
		E found = findOne(id);
		if (found != null) {
			try {
				sqlManager.deleteEntity(found);
			} catch (SQLRuntimeException e) {
				throw getExceptionTranslator().translate("delete", null, e.getCause());
			}
		}
	}
	
	@Override
	public void delete(Iterable<? extends E> entities) {
		Validate.notNull(entities);
		for (E entity : entities) {
			try {
				delete(entity);
			} catch (SQLRuntimeException e) {
				throw getExceptionTranslator().translate("delete", null, e.getCause());
			}
		}
	}
	
	@Override
	public void deleteAll() {
		try {
			delete(findAll());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("deleteAll", null, e.getCause());
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void deleteInBatch(Iterable<E> entities) {
		try {
			sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("deleteInBatch", null, e.getCause());
		}
	}
	
	@Override
	public boolean exists(ID id) {
		try {
			return getCount(getBaseSelectSqlResource(), createParams(id)) > 0;
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("exists", null, e.getCause());
		}
	}
	
	@Override
	public List<E> findAll() {
		try {
			return getResultList(getBaseSelectSqlResource(), createParams());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("findAll", null, e.getCause());
		}
	}
	
	@Override
	public Page<E> findAll(Pageable pageable) {
		if (null == pageable) {
			return new PageImpl<E>(findAll());
		}
		
		Map<String, Object> params = createParams();
		addPageParam(params, pageable);
		
		try {
			List<E> result = getResultList(getBaseSelectSqlResource(), params);
			return new PageImpl<E>(result, pageable, count());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("findAll", null, e.getCause());
		}
	}
	
	@Override
	public List<E> findAll(Sort sort) {
		try {
			return getResultList(getBaseSelectSqlResource(), createParams(sort));
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("findAll", null, e.getCause());
		}
	}
	
	@Override
	public E findOne(ID id) {
		Map<String, Object> params = createParams(id);
		params.put("include_logical_deleted", true);
		try {
			return getSingleResult(getBaseSelectSqlResource(), params);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("findOne", null, e.getCause());
		}
	}
	
	@Override
	public E save(E entity) {
		if (entity == null) {
			return null;
		}
		try {
			if (exists(getId(entity))) {
				sqlManager.updateEntity(entity);
			} else {
				sqlManager.insertEntity(entity);
			}
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("save", null, e.getCause());
		}
		return entity;
	}
	
	@Override
	public List<E> save(Iterable<? extends E> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		List<E> toUpdate = new ArrayList<E>();
		List<E> toInsert = new ArrayList<E>();
		Iterator<? extends E> iterator = entities.iterator();
		try {
			while (iterator.hasNext()) {
				E entity = iterator.next();
				if (entity != null) {
					if (exists(getId(entity))) {
						toUpdate.add(entity);
					} else {
						toInsert.add(entity);
					}
				}
			}
			sqlManager.updateBatch(toUpdate);
			sqlManager.insertBatch(toInsert);
			return Lists.newArrayList(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("save", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#call(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected E call(Class<E> resultClass, String functionName) {
		try {
			return sqlManager.call(resultClass, functionName);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("call", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#call(Class, String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected E call(Class<E> resultClass, String functionName, Object param) {
		try {
			return sqlManager.call(resultClass, functionName, param);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("call", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#call(String)
	 */
	@SuppressWarnings("javadoc")
	protected void call(String procedureName) {
		try {
			sqlManager.call(procedureName);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("call", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#call(String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected void call(String procedureName, Object parameter) {
		try {
			sqlManager.call(procedureName, parameter);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("call", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#callForList(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> callForList(Class<E> resultClass, String functionName) {
		try {
			return sqlManager.callForList(resultClass, functionName);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("callForList", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#callForList(Class, String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> callForList(Class<E> resultClass, String functionName, Object param) {
		try {
			return sqlManager.callForList(resultClass, functionName, param);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("callForList", null, e.getCause());
		}
	}
	
	protected Map<String, Object> createParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", MirageUtil.getTableName(entityClass, nameConverter));
		params.put("id", null); // 何故これが要るのだろう。無いとコケる
		return params;
	}
	
	protected Map<String, Object> createParams(ID id) {
		Map<String, Object> params = createParams();
		addIdParam(params, id);
		return params;
	}
	
	protected Map<String, Object> createParams(Pageable pageable) {
		Map<String, Object> params = createParams();
		addPageParam(params, pageable);
		return params;
	}
	
	protected Map<String, Object> createParams(Sort sort) {
		Map<String, Object> params = createParams();
		addSortParam(params, sort);
		return params;
	}
	
	protected Map<String, Object> createParams(Sort sort, Pageable pageable) {
		Map<String, Object> params = createParams();
		addSortParam(params, sort);
		addPageParam(params, pageable);
		return params;
	}
	
	/**
	 * @see SqlManager#deleteBatch(Object...)
	 */
	@SuppressWarnings("javadoc")
	protected int deleteBatch(E... entities) {
		try {
			return sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("deleteBatch", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#deleteBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int deleteBatch(List<E> entities) {
		try {
			return sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("deleteBatch", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#deleteEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int deleteEntity(Object entity) {
		try {
			return sqlManager.deleteEntity(entity);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("deleteEntity", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#executeUpdate(String)
	 */
	@SuppressWarnings("javadoc")
	protected int executeUpdate(SqlResource resource) {
		Assert.notNull(resource);
		try {
			return sqlManager.executeUpdate(resource.getAbsolutePath());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("executeUpdate", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#executeUpdate(String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected int executeUpdate(SqlResource resource, Object param) {
		Assert.notNull(resource);
		try {
			return sqlManager.executeUpdate(resource.getAbsolutePath(), param);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("executeUpdate", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#executeUpdateBySql(String)
	 */
	@SuppressWarnings("javadoc")
	protected int executeUpdateBySql(String sql) {
		try {
			return sqlManager.executeUpdateBySql(sql);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("executeUpdateBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#executeUpdateBySql(String, Object...)
	 */
	@SuppressWarnings("javadoc")
	protected int executeUpdateBySql(String sql, Object... params) {
		try {
			return sqlManager.executeUpdateBySql(sql, params);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("executeUpdateBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#findEntity(Class, Object...)
	 */
	@SuppressWarnings("javadoc")
	protected E findEntity(Object... id) {
		try {
			return sqlManager.findEntity(entityClass, id);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("findEntity", null, e.getCause());
		}
	}
	
	protected SqlResource getBaseSelectSqlResource() {
		return BASE_SELECT_SQL;
	}
	
	/**
	 * @see SqlManager#getCount(String)
	 */
	@SuppressWarnings("javadoc")
	protected int getCount(SqlResource resource) {
		Assert.notNull(resource);
		try {
			return sqlManager.getCount(resource.getAbsolutePath());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getCount", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getCount(String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected int getCount(SqlResource resource, Object param) {
		Assert.notNull(resource);
		try {
			return sqlManager.getCount(resource.getAbsolutePath(), param);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getCount", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getCountBySql(String, Object...)
	 */
	@SuppressWarnings("javadoc")
	protected int getCountBySql(String sql) {
		Assert.notNull(sql);
		try {
			return sqlManager.getCountBySql(sql);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getCountBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getCountBySql(String, Object...)
	 */
	@SuppressWarnings("javadoc")
	protected int getCountBySql(String sql, Object... params) {
		Assert.notNull(sql);
		try {
			return sqlManager.getCountBySql(sql, params);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getCountBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getResultList(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> getResultList(SqlResource resource) {
		Assert.notNull(resource);
		try {
			return sqlManager.getResultList(entityClass, resource.getAbsolutePath());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getResultList", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getResultList(Class, String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> getResultList(SqlResource resource, Object param) {
		Assert.notNull(resource);
		try {
			return sqlManager.getResultList(entityClass, resource.getAbsolutePath(), param);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getResultList", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getResultListBySql(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> getResultListBySql(String sql) {
		try {
			return sqlManager.getResultListBySql(entityClass, sql);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getResultListBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getResultListBySql(Class, String, Object...)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> getResultListBySql(String sql, Object... params) {
		try {
			return sqlManager.getResultListBySql(entityClass, sql, params);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getResultListBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getSingleResult(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected E getSingleResult(SqlResource resource) {
		Assert.notNull(resource);
		try {
			return sqlManager.getSingleResult(entityClass, resource.getAbsolutePath());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getSingleResult", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getSingleResult(Class, String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected E getSingleResult(SqlResource resource, Object param) {
		Assert.notNull(resource);
		try {
			return sqlManager.getSingleResult(entityClass, resource.getAbsolutePath(), param);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getSingleResult", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getSingleResultBySql(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected E getSingleResultBySql(String sql) {
		try {
			return sqlManager.getSingleResultBySql(entityClass, sql);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getSingleResultBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#getSingleResultBySql(Class, String, Object...)
	 */
	@SuppressWarnings("javadoc")
	protected E getSingleResultBySql(String sql, Object... params) {
		try {
			return sqlManager.getSingleResultBySql(entityClass, sql, params);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("getSingleResultBySql", sql, e.getCause());
		}
	}
	
	protected SqlManager getSqlManager() {
		return sqlManager;
	}
	
	/**
	 * @see SqlManager#insertBatch(Object...)
	 */
	@SuppressWarnings("javadoc")
	protected int insertBatch(E... entities) {
		try {
			return sqlManager.insertBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("insertBatch", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#insertBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int insertBatch(List<E> entities) {
		try {
			return sqlManager.insertBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("insertBatch", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#insertEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int insertEntity(Object entity) {
		try {
			return sqlManager.insertEntity(entity);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("insertEntity", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#iterate(Class, IterationCallback, String)
	 */
	@SuppressWarnings("javadoc")
	protected <R>R iterate(IterationCallback<E, R> callback, SqlResource resource) {
		Assert.notNull(resource);
		try {
			return sqlManager.iterate(entityClass, callback, resource.getAbsolutePath());
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("iterate", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#iterate(Class, IterationCallback, String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected <R>R iterate(IterationCallback<E, R> callback, SqlResource resource, Object param) {
		Assert.notNull(resource);
		try {
			return sqlManager.iterate(entityClass, callback, resource.getAbsolutePath(), param);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("iterate", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#iterateBySql(Class, IterationCallback, String)
	 */
	@SuppressWarnings("javadoc")
	protected <R>R iterateBySql(IterationCallback<E, R> callback, String sql) {
		try {
			return sqlManager.iterateBySql(entityClass, callback, sql);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("iterateBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#iterateBySql(Class, IterationCallback, String, Object...)
	 */
	@SuppressWarnings("javadoc")
	protected <R>R iterateBySql(IterationCallback<E, R> callback, String sql, Object... params) {
		try {
			return sqlManager.iterateBySql(entityClass, callback, sql, params);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("iterateBySql", sql, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#updateBatch(Object...)
	 */
	@SuppressWarnings("javadoc")
	protected int updateBatch(E... entities) {
		try {
			return sqlManager.updateBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("updateBatch", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#updateBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int updateBatch(List<E> entities) {
		try {
			return sqlManager.updateBatch(entities);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("updateBatch", null, e.getCause());
		}
	}
	
	/**
	 * @see SqlManager#updateEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int updateEntity(Object entity) {
		try {
			return sqlManager.updateEntity(entity);
		} catch (SQLRuntimeException e) {
			throw getExceptionTranslator().translate("updateEntity", null, e.getCause());
		}
	}
	
	private void addIdParam(Map<String, Object> params, ID id) {
		params.put("id", id);
	}
	
	private void addPageParam(Map<String, Object> params, Pageable pageable) {
		params.put("offset", pageable == null ? null : pageable.getOffset());
		params.put("size", pageable == null ? null : pageable.getPageSize());
		if (pageable != null && pageable.getSort() != null) {
			List<String> orders = Lists.newArrayList();
			Sort sort = pageable.getSort();
			for (Order order : sort) {
				orders.add(String.format("%s %s", order.getProperty(), order.getDirection().name()));
			}
			if (orders.size() != 0) {
				params.put("orders", Joiner.on(", ").join(orders));
			}
		}
	}
	
	private void addSortParam(Map<String, Object> params, Sort sort) {
		params.put("orders", null);
		if (sort == null) {
			return;
		}
		List<String> list = new ArrayList<String>();
		for (Order order : sort) {
			String orderDefinition = String.format("%s %s", order.getProperty(), order.getDirection()).trim();
			if (orderDefinition.isEmpty() == false) {
				list.add(orderDefinition);
			}
		}
		if (list.isEmpty() == false) {
			params.put("orders", Joiner.on(", ").join(list));
		}
	}
	
	private synchronized SQLExceptionTranslator getExceptionTranslator() {
		if (this.exceptionTranslator == null) {
			if (dataSource != null) {
				this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			} else {
				this.exceptionTranslator = new SQLStateSQLExceptionTranslator();
			}
		}
		return this.exceptionTranslator;
	}
}
