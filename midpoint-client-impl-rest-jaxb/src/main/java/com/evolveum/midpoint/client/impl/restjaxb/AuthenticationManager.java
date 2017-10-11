package com.evolveum.midpoint.client.impl.restjaxb;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.jaxrs.client.WebClient;

import com.evolveum.midpoint.client.api.exception.SchemaException;

public interface AuthenticationManager<T extends AuthenticationChallenge> {

	
	default void setAuthenticationChallenge(String authenticationChallenge) throws SchemaException {
		parseChallenge(authenticationChallenge);
	}
	
	public String getType();
	
	public void parseChallenge(String authenticationChallenge) throws SchemaException;
	
	public void createAuthorizationHeader(WebClient client);
	
	public T getChallenge();
	
}
