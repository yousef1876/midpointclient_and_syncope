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
package com.evolveum.midpoint.client.impl.restjaxb;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.evolveum.midpoint.client.api.QueryBuilder;
import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;

/**
 * @author semancik
 * @author katkav
 *
 */
public class RestJaxbSearchService<O extends ObjectType> extends AbstractObjectTypeWebResource<O> implements SearchService<O> {

	private QueryType query;
	
	public RestJaxbSearchService(final RestJaxbService service, final Class<O> type) {
		this(service, type, null);
	}
	
	public RestJaxbSearchService(final RestJaxbService service, final Class<O> type, final QueryType query) {
		super(service, type);
		this.query = query;
	}
	
		@Override
	public SearchResult<O> get() throws ObjectNotFoundException {
		Response response = getService().getClient().replacePath("/" + Types.findType(getType()).getRestPath() + "/search").post(query);
		
		if (Status.OK.getStatusCode() == response.getStatus()) {
			ObjectListType resultList = response.readEntity(ObjectListType.class);
			return new JaxbSearchResult(resultList.getObject());
		}
		
		if (Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
			throw new ObjectNotFoundException("Cannot search objects. No such service");
		}
		return null;
		
	}
		
	@Override
	public QueryBuilder<O> queryFor(Class<O> type) {
		return new FilterBuilder<O>(getService(), getType());
	}

	
}
