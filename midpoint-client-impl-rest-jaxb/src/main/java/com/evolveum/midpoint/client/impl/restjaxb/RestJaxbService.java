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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import com.evolveum.midpoint.client.api.PolicyCollectionService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValuePolicyType;
import com.oracle.jrockit.jfr.UseConstantPool;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.w3c.dom.Document;

import com.evolveum.midpoint.client.api.ObjectCollectionService;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.ServiceUtil;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author semancik
 * @author katkav
 *
 */
public class RestJaxbService implements Service {
	
	private static final String URL_PREFIX_USERS = "users";
	private static final String IMPERSONATE_HEADER = "Switch-To-Principal";

	
	private final ServiceUtil util;

	// TODO: jaxb context
	
	private WebClient client;
	private DomSerializer domSerializer;
	private JAXBContext jaxbContext;
	private AuthenticationManager<?> authenticationManager;
	private List<AuthenticationType> supportedAuthenticationsByServer;
	
	public WebClient getClient() {
		return client;
	}
	
	public DomSerializer getDomSerializer() {
		return domSerializer;
	}
	
	public JAXBContext getJaxbContext() {
		return jaxbContext;
	}
	
	public List<AuthenticationType> getSupportedAuthenticationsByServer() {
		if (supportedAuthenticationsByServer == null) {
			supportedAuthenticationsByServer = new ArrayList<>();
		}
		return supportedAuthenticationsByServer;
	}
	
	
	public <T extends AuthenticationChallenge> AuthenticationManager<T> getAuthenticationManager() {
		return (AuthenticationManager<T>) authenticationManager;
	}
	
	public RestJaxbService() {
		super();
		client = WebClient.create("");
		util = new RestJaxbServiceUtil();
	}
	
	RestJaxbService(String url, String username, String password, AuthenticationType authentication, List<SecurityQuestionAnswer> secQ) throws IOException {
		super();
		try {
			jaxbContext = createJaxbContext();
		} catch (JAXBException e) {
			throw new IOException(e);
		}
		
		if (AuthenticationType.SECQ == authentication) {
			authenticationManager = new SecurityQuestionAuthenticationManager(username, secQ);
		} else if (authentication != null ){
			authenticationManager = new BasicAuthenticationManager(username, password);
		}
		
		CustomAuthNProvider authNProvider = new CustomAuthNProvider(authenticationManager, this);
		client = WebClient.create(url, Arrays.asList(new JaxbXmlProvider<>(jaxbContext)));
		ClientConfiguration config = WebClient.getConfig(client);
		config.getInInterceptors().add(authNProvider);
		config.getInFaultInterceptors().add(authNProvider);
		client.accept(MediaType.APPLICATION_XML);
		client.type(MediaType.APPLICATION_XML);
		
		if (authenticationManager != null) {
			authenticationManager.createAuthorizationHeader(client);
		}
		
		
//		if (authentication != null) {
//			String authorizationHeader = authentication.getType();
//			if (StringUtils.isNotBlank(username)) {
//			 authorizationHeader += " " + org.apache.cxf.common.util.Base64Utility
//					.encode((username + ":" + (password == null ? "" : password)).getBytes());
//			}
//			client.header("Authorization", authorizationHeader);
//		}
		
		util = new RestJaxbServiceUtil();
		domSerializer = new DomSerializer(jaxbContext);
	}
	@Override
	public Service impersonate(String oid){
		client.header(IMPERSONATE_HEADER, oid);
		return this;
	}

	@Override
	public Service addHeader(String header, String value){
		client.header(header, value);
		return this;
	}

	@Override
	public ObjectCollectionService<UserType> users() {
		return new RestJaxbObjectCollectionService<>(this, URL_PREFIX_USERS, UserType.class);
	}

	@Override
	public PolicyCollectionService<ValuePolicyType> valuePolicies() {
		return new RestJaxbPolicyCollectionService<>(this, URL_PREFIX_USERS, ValuePolicyType.class);
	}
	@Override
	public ServiceUtil util() {
		return util;
	}
	
	/**
	 * Used frequently at several places. Therefore unified here.
	 * @throws ObjectNotFoundException 
	 */
	<O extends ObjectType> O getObject(final Class<O> type, final String oid) throws ObjectNotFoundException, AuthenticationException {
		// TODO
		String urlPrefix = RestUtil.subUrl(Types.findType(type).getRestPath(), oid);
		Response response = client.replacePath(urlPrefix).get();
		
		if (Status.OK.getStatusCode() == response.getStatus() ) {
			return response.readEntity(type);
		}
		
		if (Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
			throw new ObjectNotFoundException("Cannot get object with oid" + oid + ". Object doesn't exist");
		}
		
		if (Status.UNAUTHORIZED.getStatusCode() == response.getStatus()) {
			throw new AuthenticationException(response.getStatusInfo().getReasonPhrase());
		}
		
		return null;
	}

	<O extends ObjectType> void deleteObject(final Class<O> type, final String oid) throws ObjectNotFoundException, AuthenticationException {
		String urlPrefix = RestUtil.subUrl(Types.findType(type).getRestPath(), oid);
		Response response = client.replacePath(urlPrefix).delete();

		//TODO: Looks like midPoint returns a 204 and not a 200 on success
		if (Status.OK.getStatusCode() == response.getStatus() ) {
			//TODO: Do we want to return anything on successful delete or just remove this if block?
		}

		if (Status.NO_CONTENT.getStatusCode() == response.getStatus() ) {
			//TODO: Do we want to return anything on successful delete or just remove this if block?
		}


		if (Status.BAD_REQUEST.getStatusCode() == response.getStatus()) {
			throw new BadRequestException("Bad request");
		}

		if (Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
			throw new ObjectNotFoundException("Cannot delete object with oid" + oid + ". Object doesn't exist");
		}

		if (Status.UNAUTHORIZED.getStatusCode() == response.getStatus()) {
			throw new AuthenticationException("Cannot authentication user");
		}
	}

	@Override
	public UserType self() throws AuthenticationException{
		String urlPrefix = "/self";
		Response response = client.replacePath(urlPrefix).get();


		if (Status.OK.getStatusCode() == response.getStatus() ) {
			return response.readEntity(UserType.class);
		}

		if (Status.BAD_REQUEST.getStatusCode() == response.getStatus()) {
			throw new BadRequestException("Bad request");
		}

		if (Status.UNAUTHORIZED.getStatusCode() == response.getStatus()) {
			throw new AuthenticationException("Cannot authentication user");
		}
		return null;
	}

	private JAXBContext createJaxbContext() throws JAXBException {
		JAXBContext jaxbCtx = JAXBContext.newInstance("com.evolveum.midpoint.xml.ns._public.common.api_types_3:"
				+ "com.evolveum.midpoint.xml.ns._public.common.audit_3:"
				+ "com.evolveum.midpoint.xml.ns._public.common.common_3:"
				+ "com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_extension_3:"
				+ "com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3:"
				+ "com.evolveum.midpoint.xml.ns._public.connector.icf_1.resource_schema_3:"
				+ "com.evolveum.midpoint.xml.ns._public.gui.admin_1:"
				+ "com.evolveum.midpoint.xml.ns._public.model.extension_3:"
				+ "com.evolveum.midpoint.xml.ns._public.model.scripting_3:"
				+ "com.evolveum.midpoint.xml.ns._public.model.scripting.extension_3:"
				+ "com.evolveum.midpoint.xml.ns._public.report.extension_3:"
				+ "com.evolveum.midpoint.xml.ns._public.resource.capabilities_3:"
				+ "com.evolveum.midpoint.xml.ns._public.task.extension_3:"
				+ "com.evolveum.midpoint.xml.ns._public.task.jdbc_ping.handler_3:"
				+ "com.evolveum.midpoint.xml.ns._public.task.noop.handler_3:"
				+ "com.evolveum.prism.xml.ns._public.annotation_3:"
				+ "com.evolveum.prism.xml.ns._public.query_3:"
				+ "com.evolveum.prism.xml.ns._public.types_3");
		return jaxbCtx;
	}
}
