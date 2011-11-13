/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/22
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
import java.util.TimeZone;

import jp.sf.amateras.mirage.annotation.Column;
import jp.sf.amateras.mirage.annotation.PrimaryKey;
import jp.sf.amateras.mirage.annotation.PrimaryKey.GenerationType;
import jp.sf.amateras.mirage.annotation.Table;

import org.springframework.data.annotation.Id;

/**
 * TODO を表すエンティティクラス。
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
@Table(name = "samples")
@SuppressWarnings("serial")
public class Entity implements Identifiable, Serializable {
	
	@Id
	@Column(name = "id")
	@PrimaryKey(generationType = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "time_zone")
	private String timeZone;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param timeZone タイムゾーン
	 */
	public Entity(TimeZone timeZone) {
		this.timeZone = timeZone.getID();
	}
	
	Entity() {
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@SuppressWarnings("javadoc")
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone(timeZone);
	}
	
	@SuppressWarnings("javadoc")
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone.getID();
	}
	
	void setId(long id) {
		this.id = id;
	}
}
