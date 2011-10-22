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
package jp.xet.uncommons.mirage.spring;

import java.sql.Connection;
import java.sql.SQLException;

import jp.sf.amateras.mirage.integration.spring.SpringConnectionProvider;
import jp.sf.amateras.mirage.provider.ConnectionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * {@link SpringConnectionProvider}実装内で、{@link ConnectionHolder}が{@code null}で返って来て、NPE落ちすることがあるので
 * それを応急処置として回避するためのダメクラス。何がおかしいんじゃ…。
 * 
 * @since 1.0
 * @version $Id: MySpringConnectionProvider.java 160 2011-10-21 09:49:56Z daisuke $
 * @author daisuke
 */
public class MySpringConnectionProvider implements ConnectionProvider, InitializingBean {
	
	private static Logger logger = LoggerFactory.getLogger(MySpringConnectionProvider.class);
	
	private DataSourceTransactionManager transactionManager;
	
	
	@Override
	@SuppressWarnings("unused")
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(transactionManager);
	}
	
	@Override
	public Connection getConnection() {
		ConnectionHolder conHolder =
				(ConnectionHolder) TransactionSynchronizationManager.getResource(transactionManager.getDataSource());
		
		if (conHolder != null) {
			logger.warn("connection holder is null...");
			return conHolder.getConnection();
		} else {
			try {
				return transactionManager.getDataSource().getConnection();
			} catch (SQLException e) {
				logger.error("fail to connect", e);
			}
		}
		return null;
	}
	
	@SuppressWarnings("javadoc")
	public void setTransactionManager(DataSourceTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
}
