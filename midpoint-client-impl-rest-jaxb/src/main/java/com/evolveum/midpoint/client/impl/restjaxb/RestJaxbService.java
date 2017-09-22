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

import com.evolveum.midpoint.client.api.ObjectCollectionService;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.ServiceUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author semancik
 *
 */
public class RestJaxbService implements Service {
	
	private static final String URL_PREFIX_USERS = "users";
	
	private final ServiceUtil util;

	// TODO: jaxb context
	
	public RestJaxbService() {
		super();
		util = new RestJaxbServiceUtil();
	}	

	@Override
	public ObjectCollectionService<UserType> users() {
		return new RestJaxbObjectCollectionService<>(this, URL_PREFIX_USERS, UserType.class);
	}

	@Override
	public ServiceUtil util() {
		return util;
	}
	
	/**
	 * Used frequently at several places. Therefore unified here.
	 */
	<O extends ObjectType> O getObject(final String collectionUrlPrefix, final Class<O> type, final String oid) {
		// TODO
		String urlPrefix = RestUtil.subUrl(collectionUrlPrefix, oid);
		// TODO
		return null;
	}

}
