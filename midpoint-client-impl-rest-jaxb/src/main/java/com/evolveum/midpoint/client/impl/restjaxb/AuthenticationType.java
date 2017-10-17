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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;


import com.evolveum.midpoint.client.api.exception.SchemaException;

/**
 * 
 * @author katkav
 *
 */
public enum AuthenticationType {

	
	BASIC("Basic"),
	SECQ("SecQ");
	
	private String type;
	private Class<AuthenticationManager<? extends AuthenticationChallenge>> clazz; 
	
	private AuthenticationType(String type) {
		this.type = type;
//		this.clazz = clazz;
	}
	
	public static AuthenticationType getAuthenticationType(String type) throws SchemaException {
		
		if (StringUtils.isBlank(type)) {
			return null;
		}
		
		return Arrays.asList(values()).stream().filter(authnType -> type.equals(authnType.getType())).findAny().orElseThrow(() -> new SchemaException("Unsupported type: " + type));
		
	}
	
	public String getType() {
		return type;
	}
	
//	public <T extends AbstractAuthentication> Class<T> getClazz() {
//		return (Class<T>) clazz;
//	}
//	
}
