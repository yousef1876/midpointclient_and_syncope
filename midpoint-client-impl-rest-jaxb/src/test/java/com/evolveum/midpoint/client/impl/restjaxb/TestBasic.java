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

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.client.api.ObjectUtils;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author semancik
 *
 */
public class TestBasic {
	
	@Test
	public void testUserGet() {
		Service service = getService();
		
		// WHEN
		UserType userType = service.users().oid("123").get();
		
		// THEN
		assertNotNull("null user", userType);
	}
	
	@Test
	public void testUserAdd() {
		Service service = getService();
		
		UserType userBefore = new UserType();
		userBefore.setName(ObjectUtils.createPoly("foo"));
		
		// WHEN
		ObjectReference<UserType> ref = service.users().add(userBefore).post();
		
		// THEN
		assertNotNull("Null oid", ref.getOid());
		// TODO: get user, compare
		
	}
	
	private Service getService() {
		RestJaxbServiceFactory factory = new RestJaxbServiceFactory();
		return factory.create();
	}

}
