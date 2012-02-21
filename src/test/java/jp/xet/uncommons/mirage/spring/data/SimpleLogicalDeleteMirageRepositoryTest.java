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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import jp.xet.uncommons.mirage.spring.data.example.Entity;
import jp.xet.uncommons.mirage.spring.data.example.EntityRepositoryImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-context.xml")
@Transactional
@SuppressWarnings("javadoc")
public class SimpleLogicalDeleteMirageRepositoryTest {
	
	private static Logger logger = LoggerFactory.getLogger(SimpleLogicalDeleteMirageRepositoryTest.class);
	
	@Autowired
	EntityRepositoryImpl repos;
	
	
	@Test
	public void test_crud() {
		Entity entity = new Entity("foo");
		assertThat(repos.count(), is(0L));
		
		Entity saved = repos.save(entity); // insert
		assertThat(saved.getId(), is(not(0L)));
		assertThat(repos.count(), is(1L));
		
		saved.setStr("bar");
		repos.save(saved); // update
		assertThat(repos.count(), is(1L));
		
		Entity found = repos.findOne(saved.getId()); // found
		assertThat(found, is(notNullValue()));
		assertThat(found.getStr(), is("bar"));
		
		repos.delete(found);
		assertThat(repos.count(), is(0L));
		
		logger.info("id={}", found.getId());
		assertThat(repos.findOne(found.getId()), is(nullValue())); // not found
		assertThat(repos.findOne(found.getId() * -1), is(notNullValue())); // found deleted
		
		repos.revert(found.getId() * -1);
		assertThat(repos.count(), is(1L));
		assertThat(repos.findOne(found.getId()), is(notNullValue())); // found
	}
}
