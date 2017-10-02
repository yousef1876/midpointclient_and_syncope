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
import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.w3c.dom.Document;

import com.evolveum.midpoint.client.api.ObjectCollectionService;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.ServiceUtil;
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
	
	private final ServiceUtil util;

	// TODO: jaxb context
	
	private WebClient client;
	private DomSerializer domSerializer;
	private JAXBContext jaxbContext;
	
	public WebClient getClient() {
		return client;
	}
	
	public DomSerializer getDomSerializer() {
		return domSerializer;
	}
	
	public JAXBContext getJaxbContext() {
		return jaxbContext;
	}
	
	public RestJaxbService() {
		super();
		client = WebClient.create("");
		util = new RestJaxbServiceUtil();
	}
	
	RestJaxbService(String url, String username, String password, AuthenticationType authentication) throws IOException {
		super();
		try {
			jaxbContext = createJaxbContext();
		} catch (JAXBException e) {
			throw new IOException(e);
		}
		client = WebClient.create(url, Arrays.asList(new JaxbXmlProvider<>(jaxbContext)));
		client.accept(MediaType.APPLICATION_XML);
		client.type(MediaType.APPLICATION_XML);
		
		if (username != null) {
			String authorizationHeader = "Basic " + org.apache.cxf.common.util.Base64Utility
					.encode((username + ":" + (password == null ? "" : password)).getBytes());
			client.header("Authorization", authorizationHeader);
		}
		
		util = new RestJaxbServiceUtil();
		domSerializer = new DomSerializer(jaxbContext);
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
	 * @throws ObjectNotFoundException 
	 */
	<O extends ObjectType> O getObject(final Class<O> type, final String oid) throws ObjectNotFoundException {
		// TODO
		String urlPrefix = RestUtil.subUrl(Types.findType(type).getRestPath(), oid);
		Response response = client.replacePath(urlPrefix).get();
		
		if (Status.OK.getStatusCode() == response.getStatus() ) {
			return response.readEntity(type);
		}
		
		if (Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
			throw new ObjectNotFoundException("Cannot get object with oid" + oid + ". Object doesn't exist");
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
