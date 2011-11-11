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

import jp.sf.amateras.mirage.IterationCallback;
import jp.sf.amateras.mirage.SqlManager;
import jp.sf.amateras.mirage.exception.SQLRuntimeException;
import jp.sf.amateras.mirage.naming.NameConverter;
import jp.sf.amateras.mirage.util.MirageUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
	
	@Autowired
	NameConverter nameConverter;
	
	private static final String PATH_BASE = "META-INF/";
	
	
	/**
	 * SQLファイル名をクラスパス上におけるパス名に変換する。
	 * 
	 * @param filename SQLファイル名
	 * @return パス名
	 * @since 1.0
	 */
	protected static String pathOf(String filename) {
		return PATH_BASE + filename;
	}
	
	
	@Autowired
	SqlManager sqlManager;
	
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
		return sqlManager.getCount(pathOf("baseSelect.sql"), createParams());
	}
	
	@Override
	public void delete(ID id) {
		sqlManager.deleteEntity(findOne(id));
	}
	
	@Override
	public void delete(Iterable<? extends E> entities) {
		for (E entity : entities) {
			delete(entity);
		}
	}
	
	@Override
	public void delete(E entity) {
		sqlManager.deleteEntity(entity);
	}
	
	@Override
	public void deleteAll() {
		delete(findAll());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void deleteInBatch(Iterable<E> entities) {
		sqlManager.deleteBatch(entities);
	}
	
	@Override
	public boolean exists(ID id) {
		return sqlManager.getCount(pathOf("baseSelect.sql"), createParams(id)) > 0;
	}
	
	@Override
	public List<E> findAll() {
		return sqlManager.getResultList(entityClass, pathOf("baseSelect.sql"), createParams());
	}
	
	@Override
	public Page<E> findAll(Pageable pageable) {
		if (null == pageable) {
			return new PageImpl<E>(findAll());
		}
		
		Map<String, Object> params = createParams();
		addPageParam(params, pageable);
		List<E> result = sqlManager.getResultList(entityClass, pathOf("baseSelect.sql"), params);
		return new PageImpl<E>(result, pageable, count());
	}
	
	@Override
	public List<E> findAll(Sort sort) {
		return sqlManager.getResultList(entityClass, pathOf("baseSelect.sql"), createParams());
	}
	
	@Override
	public E findOne(ID id) {
		return sqlManager.getSingleResult(entityClass, pathOf("baseSelectWithDeleted.sql"), createParams(id));
	}
	
	@Override
	public List<E> save(Iterable<? extends E> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		List<E> list = new ArrayList<E>();
		Iterator<? extends E> iterator = entities.iterator();
		while (iterator.hasNext()) {
			E entity = iterator.next();
			if (entity != null) {
				list.add(entity);
			}
		}
		sqlManager.insertBatch(list);
		return list;
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
			throw new DataIntegrityViolationException("", e);
		}
		return entity;
	}
	
	@SuppressWarnings("javadoc")
	protected E call(Class<E> resultClass, String functionName) {
		return sqlManager.call(resultClass, functionName);
	}
	
	@SuppressWarnings("javadoc")
	protected E call(Class<E> resultClass, String functionName, Object param) {
		return sqlManager.call(resultClass, functionName, param);
	}
	
	@SuppressWarnings("javadoc")
	protected void call(String procedureName) {
		sqlManager.call(procedureName);
	}
	
	@SuppressWarnings("javadoc")
	protected void call(String procedureName, Object parameter) {
		sqlManager.call(procedureName, parameter);
	}
	
	@SuppressWarnings("javadoc")
	protected List<E> callForList(Class<E> resultClass, String functionName) {
		return sqlManager.callForList(resultClass, functionName);
	}
	
	@SuppressWarnings("javadoc")
	protected List<E> callForList(Class<E> resultClass, String functionName, Object param) {
		return sqlManager.callForList(resultClass, functionName, param);
	}
	
	@SuppressWarnings("javadoc")
	protected Map<String, Object> createParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", MirageUtil.getTableName(entityClass, nameConverter));
		params.put("id", null); // 何故これが要るのだろう。無いとコケる
		return params;
	}
	
	@SuppressWarnings("javadoc")
	protected Map<String, Object> createParams(ID id) {
		Map<String, Object> params = createParams();
		addIdParam(params, id);
		return params;
	}
	
	@SuppressWarnings("javadoc")
	protected Map<String, Object> createParams(Pageable pageable) {
		Map<String, Object> params = createParams();
		addPageParam(params, pageable);
		return params;
	}
	
	@SuppressWarnings("javadoc")
	protected Map<String, Object> createParams(Sort sort) {
		Map<String, Object> params = createParams();
		addSortParam(params, sort);
		return params;
	}
	
	@SuppressWarnings("javadoc")
	protected Map<String, Object> createParams(Sort sort, Pageable pageable) {
		Map<String, Object> params = createParams();
		addSortParam(params, sort);
		addPageParam(params, pageable);
		return params;
	}
	
	@SuppressWarnings("javadoc")
	protected int deleteBatch(List<E> entities) {
		return sqlManager.deleteBatch(entities);
	}
	
	@SuppressWarnings("javadoc")
	protected int deleteBatch(E... entities) {
		return sqlManager.deleteBatch(entities);
	}
	
	@SuppressWarnings("javadoc")
	protected int deleteEntity(Object entity) {
		return sqlManager.deleteEntity(entity);
	}
	
	@SuppressWarnings("javadoc")
	protected int executeUpdate(String sqlPath) {
		return sqlManager.executeUpdate(sqlPath);
	}
	
	@SuppressWarnings("javadoc")
	protected int executeUpdate(String sqlPath, Object param) {
		return sqlManager.executeUpdate(sqlPath, param);
	}
	
	@SuppressWarnings("javadoc")
	protected int executeUpdateBySql(String sql) {
		return sqlManager.executeUpdateBySql(sql);
	}
	
	@SuppressWarnings("javadoc")
	protected int executeUpdateBySql(String sql, Object... params) {
		return sqlManager.executeUpdateBySql(sql, params);
	}
	
	@SuppressWarnings("javadoc")
	protected E findEntity(Object... id) {
		return sqlManager.findEntity(entityClass, id);
	}
	
	@SuppressWarnings("javadoc")
	protected int getCount(String sqlPath) {
		return sqlManager.getCount(sqlPath);
	}
	
	@SuppressWarnings("javadoc")
	protected int getCount(String sqlPath, Object param) {
		return sqlManager.getCount(sqlPath, param);
	}
	
	@SuppressWarnings("javadoc")
	protected List<E> getResultList(String sqlPath) {
		return sqlManager.getResultList(entityClass, sqlPath);
	}
	
	@SuppressWarnings("javadoc")
	protected List<E> getResultList(String sqlPath, Object param) {
		return sqlManager.getResultList(entityClass, sqlPath, param);
	}
	
	@SuppressWarnings("javadoc")
	protected List<E> getResultListBySql(String sql) {
		return sqlManager.getResultListBySql(entityClass, sql);
	}
	
	@SuppressWarnings("javadoc")
	protected List<E> getResultListBySql(String sql, Object... params) {
		return sqlManager.getResultListBySql(entityClass, sql, params);
	}
	
	@SuppressWarnings("javadoc")
	protected E getSingleResult(String sqlPath) {
		return sqlManager.getSingleResult(entityClass, sqlPath);
	}
	
	@SuppressWarnings("javadoc")
	protected E getSingleResult(String sqlPath, Object param) {
		return sqlManager.getSingleResult(entityClass, sqlPath, param);
	}
	
	@SuppressWarnings("javadoc")
	protected E getSingleResultBySql(String sql) {
		return sqlManager.getSingleResultBySql(entityClass, sql);
	}
	
	@SuppressWarnings("javadoc")
	protected E getSingleResultBySql(String sql, Object... params) {
		return sqlManager.getSingleResultBySql(entityClass, sql, params);
	}
	
	@SuppressWarnings("javadoc")
	protected SqlManager getSqlManager() {
		return sqlManager;
	}
	
	@SuppressWarnings("javadoc")
	protected int insertBatch(List<E> entities) {
		return sqlManager.insertBatch(entities);
	}
	
	@SuppressWarnings("javadoc")
	protected int insertBatch(E... entities) {
		return sqlManager.insertBatch(entities);
	}
	
	@SuppressWarnings("javadoc")
	protected int insertEntity(Object entity) {
		return sqlManager.insertEntity(entity);
	}
	
	@SuppressWarnings("javadoc")
	protected <R>R iterate(IterationCallback<E, R> callback, String sqlPath) {
		return sqlManager.iterate(entityClass, callback, sqlPath);
	}
	
	@SuppressWarnings("javadoc")
	protected <R>R iterate(IterationCallback<E, R> callback, String sqlPath, Object param) {
		return sqlManager.iterate(entityClass, callback, sqlPath, param);
	}
	
	@SuppressWarnings("javadoc")
	protected <R>R iterateBySql(IterationCallback<E, R> callback, String sql) {
		return sqlManager.iterateBySql(entityClass, callback, sql);
	}
	
	@SuppressWarnings("javadoc")
	protected <R>R iterateBySql(IterationCallback<E, R> callback, String sql, Object... params) {
		return sqlManager.iterateBySql(entityClass, callback, sql, params);
	}
	
	@SuppressWarnings("javadoc")
	protected int updateBatch(List<E> entities) {
		return sqlManager.updateBatch(entities);
	}
	
	@SuppressWarnings("javadoc")
	protected int updateBatch(E... entities) {
		return sqlManager.updateBatch(entities);
	}
	
	@SuppressWarnings("javadoc")
	protected int updateEntity(Object entity) {
		return sqlManager.updateEntity(entity);
	}
	
	private void addIdParam(Map<String, Object> params, ID id) {
		params.put("id", id);
	}
	
	private void addPageParam(Map<String, Object> params, Pageable pageable) {
		params.put("offset", pageable.getOffset());
		params.put("size", pageable.getPageSize());
	}
	
	private void addSortParam(Map<String, Object> params, Sort sort) {
		ArrayList<String> list = new ArrayList<String>();
		for (Order order : sort) {
			String orderDefinition = String.format("%s %s", order.getProperty(), order.getDirection()).trim();
			if (orderDefinition.isEmpty() == false) {
				list.add(orderDefinition);
			}
		}
		if (list.isEmpty() == false) {
			params.put("order", StringUtils.join(list.toArray(new String[list.size()]), ", "));
		}
	}
}
