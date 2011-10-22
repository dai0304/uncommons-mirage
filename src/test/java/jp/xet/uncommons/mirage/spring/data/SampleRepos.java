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

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@SuppressWarnings("javadoc")
public class SampleRepos extends LogicalDeleteMirageRepository<Sample> {
	
	public SampleRepos() {
		super(Sample.class);
	}
	
	@Override
	public Long getId(Sample entity) {
		Assert.notNull(entity);
		return entity.getId();
	}
}
