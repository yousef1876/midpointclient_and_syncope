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

import com.evolveum.midpoint.client.api.ObjectAddService;
import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * @author semancik
 *
 */
public class RestJaxbObjectAddService<O extends ObjectType> extends AbstractObjectTypeWebResource<O> implements ObjectAddService<O> {

	private final O object;
	
	public RestJaxbObjectAddService(final RestJaxbService service, final String urlPrefix, final Class<O> type, final O object) {
		super(service, urlPrefix, type);
		this.object = object;
	}

	@Override
	public ObjectReference<O> post() {
		// TODO: add object
		String oid = null;
		return new RestJaxbObjectReference<>(getService(), getUrlPrefix(), getType(), oid);
	}
	
	
}
