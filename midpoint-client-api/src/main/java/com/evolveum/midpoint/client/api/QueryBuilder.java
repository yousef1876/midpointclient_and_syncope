/**
 * Copyright (c) 2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.client.api;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.verb.Get;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * 
 * 
 * @author semancik
 */
public interface QueryBuilder<O extends ObjectType> extends Get<SearchResult<O>> {
	
	// TODO: do we need separate QueryBuilder and QueryBuilderService?
	
	// TODO: item(), and(), or(), ...
	
	/**
	 * Returns search service with the query set.
	 */
	SearchService<O> build();
	
	public ConditionEntryBuilder<O> item(ItemPathType itemPath); 
	public ConditionEntryBuilder<O> item(QName... qnames);
	
	
	
	/**
	 * Shortcut.
	 * From: r.query().item(x).eq(y).build().get();
	 * To:   r.query().item(x).eq(y).get();
	 * @throws AuthenticationException 
	 */
	default SearchResult<O> get() throws ObjectNotFoundException, AuthenticationException {
		return build().get();
	}
	
	
}

