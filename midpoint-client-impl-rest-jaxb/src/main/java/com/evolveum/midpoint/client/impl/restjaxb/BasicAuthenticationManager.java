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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import com.evolveum.midpoint.client.api.exception.SchemaException;

public class BasicAuthenticationManager implements AuthenticationManager<BasicChallenge>{

	private BasicChallenge authnCtx;
	private AuthenticationType type;
	
	public BasicAuthenticationManager(String username, String password) {
		this.type = AuthenticationType.BASIC;
		authnCtx = new BasicChallenge();
		authnCtx.setUsername(username);
		authnCtx.setPassword(password);
	}
	
	@Override
	public String getType() {
		return type.getType();
	}

	@Override
	public void parseChallenge(String authenticationChallenge) throws SchemaException {
		//nopthing to do
	}

	@Override
	public void createAuthorizationHeader(WebClient client) {
		String authorizationHeader = getType();
		
		if (StringUtils.isNotBlank(authnCtx.getUsername())) {
			authorizationHeader += " " + org.apache.cxf.common.util.Base64Utility.encode(
					(authnCtx.getUsername() + ":" + (authnCtx.getPassword() == null ? "" : authnCtx.getPassword()))
							.getBytes());
		}
		client.header("Authorization", authorizationHeader);

	}
	
	@Override
	public BasicChallenge getChallenge() {
		return authnCtx;
	}
}
