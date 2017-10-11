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
