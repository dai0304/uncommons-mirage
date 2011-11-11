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

import java.util.TimeZone;

import jp.sf.amateras.mirage.annotation.PrimaryKey;
import jp.sf.amateras.mirage.annotation.PrimaryKey.GenerationType;

@SuppressWarnings("javadoc")
public class Sample implements Identifiable {
	
	@PrimaryKey(generationType = GenerationType.IDENTITY)
	private long id;
	
	private String timeZone;
	
	
	public Sample() {
	}
	
	public Sample(TimeZone timeZone) {
		this.timeZone = timeZone.getID();
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone(timeZone);
	}
	
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone.getID();
	}
	
	void setId(long id) {
		this.id = id;
	}
}
