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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.ws.rs.core.Response;
import javax.rmi.CORBA.Util;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.evolveum.midpoint.client.api.ServiceUtil;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.org.apache.xerces.internal.dom.TextImpl;
import com.sun.org.apache.xpath.internal.functions.FuncSubstring;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.transport.local.LocalConduit;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.impl.restjaxb.service.AuthenticationProvider;
import com.evolveum.midpoint.client.impl.restjaxb.service.MidpointMockRestService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * @author semancik
 *
 */
public class TestBasic {
	
	private static Server server;
	//private static final String ENDPOINT_ADDRESS = "http://localhost:18080/rest";
	private static final String ENDPOINT_ADDRESS = "http://mpdev1.its.uwo.pri:8080/midpoint/ws/rest";
	private static final String ADMIN = "administrator";
	private static final String ADMIN_PASS = "5ecr3t";

	@BeforeClass
	public void init() throws IOException {
		//startServer();
	}
	
	@Test
	public void test001UserAdd() throws Exception {
		Service service = getService();
		
		UserType userBefore = new UserType();
		userBefore.setName(service.util().createPoly("foo"));
		userBefore.setOid("123");
		
		// WHEN
		ObjectReference<UserType> ref = service.users().add(userBefore).post();
		
		// THEN
		assertNotNull("Null oid", ref.getOid());
		
		UserType userAfter = ref.get();
		Asserts.assertPoly(service, "Wrong name", "foo", userAfter.getName());
		
		// TODO: get user, compare
		
	}
	
	@Test
	public void test002UserGet() throws Exception {
		Service service = getService();
		
		// WHEN
		UserType userType = service.users().oid("123").get();
		
		// THEN
		assertNotNull("null user", userType);
	}
	
	@Test
	public void test003UserGetNotExist() throws Exception {
		Service service = getService();
		
		// WHEN
		try {
			service.users().oid("999").get();
			fail("Unexpected user found");
		} catch (ObjectNotFoundException e) {
			// nothing to do. this is expected
		}
	}

	@Test
	public void test004UserDeleteNotExist() throws Exception{
		Service service = getService();

		// WHEN
		try{
			service.users().oid("999").delete();
			fail("Unexpected user deleted");
		}catch(ObjectNotFoundException e){
			// nothing to do. this is expected
		}
	}

	@Test
	public void test005UserModify() throws Exception{
		Service service = getService();
		ServiceUtil util = service.util();

		Map<String, Object> modifications = new HashMap<>();
		modifications.put("description", "test description");

		ObjectReference<UserType> ref = null;

		try{
			ref	= service.users().oid("123")
					.modify()
					.replace(modifications)
					.add("givenName", util.createPoly("Charlie"))
					.post();
		}catch(ObjectNotFoundException e){
			fail("Cannot modify user, user not found");
		}

		UserType user = ref.get();
		assertEquals(user.getDescription(), "test description");
		assertEquals(util.getOrig(user.getGivenName()), "Charlie");
		ref	= service.users().oid("123").modify().delete("givenName", util.createPoly("Charlie")).post();

		assertEquals(ref.get().getGivenName(), null);
	}

//	@Test
//	public void test900UserDelete() throws Exception{
//		// SETUP
//		Service service = getService();
//
//		// WHEN
//		try{
//			service.users().oid("123").delete();
//		}catch(ObjectNotFoundException e){
//			fail("Cannot delete user, user not found");
//		}
//	}
	
	@Test
	public void test010UserSearch() throws Exception {
		Service service = getService();
		
		// WHEN
		ItemPathType itemPath = new ItemPathType();
		itemPath.setValue("name");
//		AssignmentType cal = new AssignmentType();
//		cal.setDescription("asdasda");
//		ObjectReferenceType ort = new ObjectReferenceType();
//		ort.setOid("12312313");
//		cal.setTargetRef(ort);
//		ActivationType activation = new ActivationType();
//		activation.setAdministrativeStatus(ActivationStatusType.ARCHIVED);
//		cal.setActivation(activation);
		SearchResult<UserType> result = service.users().search().queryFor(UserType.class).item(itemPath).eq("jack").finishQuery().get();
		
		// THEN
		assertEquals(result.size(), 0);
	}

	@Test
	public void test100challengeRepsonse() throws Exception {
		RestJaxbService service = (RestJaxbService) getService(ADMIN, "", AuthenticationType.SECQ);

		try { 
			service.users().oid("123").get();
			fail("unexpected success. should fail because of authentication");
		} catch (AuthenticationException ex) {
			//this is expected.. 
		}
		
		SecurityQuestionChallenge challenge = (SecurityQuestionChallenge) service.getAuthenticationManager().getChallenge();
		for (SecurityQuestionAnswer qa : challenge.getAnswer()) {
			if ("id1".equals(qa.getQid())) {
				qa.setQans("I'm pretty good, thanks for AsKinG");
			} else {
				qa.setQans("I do NOT have FAVORITE c0l0r!");
			}

		}
		
		service = (RestJaxbService) getService(ADMIN, challenge.getAnswer());
		
		try { 
			service.users().oid("123").get();
			
		} catch (AuthenticationException ex) {
			fail("should authenticate user successfully");
		}
		
		
	}
	
	@Test
	public void test200fullChallengeRepsonse() throws Exception {
		RestJaxbService service = (RestJaxbService) getService(null, null, null);

		try {
			service.users().oid("123").get();
			fail("unexpected success. should fail because of authentication");
		} catch (AuthenticationException ex) {
			//this is expected..
		}

		List<AuthenticationType> supportedAuthentication = service.getSupportedAuthenticationsByServer();
		assertNotNull("no supported authentication. something wen wrong", supportedAuthentication);
		AuthenticationType basicAtuh = supportedAuthentication.iterator().next();
		assertEquals(basicAtuh.getType(), AuthenticationType.BASIC.getType(), "expected basic authentication, but got" + basicAtuh);


		service = (RestJaxbService) getService(ADMIN, ADMIN_PASS, basicAtuh);

		try {
			service.users().oid("123").get();

		} catch (AuthenticationException ex) {
			fail("should authenticate user successfully");
		}


	}

	@Test
	public void test201modifyGenerate() throws Exception
	{
		Service service = getService();
		ObjectReference<UserType> userRef = service.users().oid("123").modifyGenerate("givenName").post();
		UserType user = userRef.get();
		assertNotNull(service.util().getOrig(user.getGivenName()));
	}

	@Test
	public void test202policyGenerate() throws Exception
	{
		Service service = getService();
		String generatedPassword = service.valuePolicies().oid("00000000-0000-0000-0000-000000000003").generate().post();
		assertNotNull(generatedPassword);
	}
	
	public void test012Self() throws Exception {
		Service service = getService();

		UserType loggedInUser = null;

		try {
			loggedInUser = service.self();

		} catch (AuthenticationException ex) {
			fail("should authenticate user successfully");
		}

		assertEquals(service.util().getOrig(loggedInUser.getName()), ADMIN);
	}

	@Test
	public void test013SelfImpersonate() throws Exception {
		Service service = getService();

		UserType loggedInUser = null;

		try {
			loggedInUser = service.impersonate("44af349b-5a0c-4f3a-9fe9-2f64d9390ed3").self();

		} catch (AuthenticationException ex) {
			fail("should authenticate user successfully");
		}

		assertEquals(service.util().getOrig(loggedInUser.getName()), "impersonate");
	}


	@Test
	public void test203UserDelete() throws Exception{
		// SETUP
		Service service = getService();

		// WHEN
		try{
			service.users().oid("123").delete();
		}catch(ObjectNotFoundException e){
			fail("Cannot delete user, user not found");
		}
	}



	private Service getService() throws IOException {		
		return getService(ADMIN, ADMIN_PASS, AuthenticationType.BASIC);
		
	}
	
	private Service getService(String username, List<SecurityQuestionAnswer> answer) throws IOException {
	
	RestJaxbServiceBuilder serviceBuilder = new RestJaxbServiceBuilder();
	serviceBuilder.authentication(AuthenticationType.SECQ).username(username).authenticationChallenge(answer).url(ENDPOINT_ADDRESS);
	RestJaxbService service = serviceBuilder.build();
	WebClient client = service.getClient();
	WebClient.getConfig(client).getRequestContext().put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
	
	return service;
	
}

	private Service getService(String username, String password, AuthenticationType authenticationType) throws IOException {
		
		RestJaxbServiceBuilder serviceBuilder = new RestJaxbServiceBuilder();
		serviceBuilder.authentication(authenticationType).username(username).password(password).url(ENDPOINT_ADDRESS);
		RestJaxbService service = serviceBuilder.build();
		WebClient client = service.getClient();
		WebClient.getConfig(client).getRequestContext().put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
		
		return service;
		
	}
	
	private void startServer() throws IOException {
		JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
	     sf.setResourceClasses(MidpointMockRestService.class);
	     
	     sf.setProviders(Arrays.asList(new JaxbXmlProvider<>(createJaxbContext()), new AuthenticationProvider()));
	         
	     sf.setResourceProvider(MidpointMockRestService.class,
	                            new SingletonResourceProvider(new MidpointMockRestService(), true));
	     sf.setAddress(ENDPOINT_ADDRESS);
	 
	     
	     server = sf.create();
	}
	
	private JAXBContext createJaxbContext() throws IOException {
		try {
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
		} catch (JAXBException e) {
			throw new IOException(e);
		}
		
	}
	

}
