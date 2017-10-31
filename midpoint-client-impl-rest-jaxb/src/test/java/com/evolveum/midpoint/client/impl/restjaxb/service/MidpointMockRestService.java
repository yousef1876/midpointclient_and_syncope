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
package com.evolveum.midpoint.client.impl.restjaxb.service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.client.api.ServiceUtil;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbServiceUtil;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;

import com.evolveum.midpoint.client.impl.restjaxb.SchemaConstants;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;

/**
 * 
 * @author katkav
 *
 */
@Produces({"application/xml"})
public class MidpointMockRestService {
	
		private  Map<String, Map<String, ObjectType>> objectMap = new HashMap<>();
	
		private Map<String, ObjectType> userMap = new HashMap<>();
		
		public MidpointMockRestService() {
			objectMap.put("users", userMap);
		}
	
	@POST
	@Path("/{type}")
	@Consumes({MediaType.APPLICATION_XML})
	public <T extends ObjectType> Response addObject(@PathParam("type") String type, T object,
													 @QueryParam("options") List<String> options,
			@Context UriInfo uriInfo, @Context MessageContext mc) {
			
		String oid = object.getOid();
		if (object.getOid() == null) {
			oid = RandomStringUtils.random(5);
			object.setOid(oid);
		}
		objectMap.get(type).put(oid, object);
		
		URI location = uriInfo.getAbsolutePathBuilder().path(oid).build(oid);
		
		return location == null ? Response.status(Status.ACCEPTED).build() : Response.status(Status.ACCEPTED).location(location).build();
	}
	
	@GET
	@Path("/{type}/{id}")
	@Produces({MediaType.APPLICATION_XML})
	public <T extends ObjectType> Response getObject(@PathParam("type") String type, @PathParam("id") String id,
			@QueryParam("options") List<String> options,
			@QueryParam("include") List<String> include,
			@QueryParam("exclude") List<String> exclude,
			@Context MessageContext mc){
		
		OperationResultType result = new OperationResultType();
		result.setOperation("Get object");
		T objectType = (T) objectMap.get(type).get(id);
		
		if (objectType == null) {
			result.setStatus(OperationResultStatusType.FATAL_ERROR);
			result.setMessage("User with oid " + id + " not found");
			return RestMockServiceUtil.createResponse(Status.NOT_FOUND, result);
		}
		
		return Response.status(Status.OK).header("Content-Type", MediaType.APPLICATION_XML).entity(objectType).build();
		
	}

	@POST
	@Path("/{type}/{id}")
	@Produces({MediaType.APPLICATION_XML})
	public <T extends ObjectType> Response modifyObjectPost(@PathParam("type") String type, @PathParam("id") String id, ObjectModificationType object,
	                                                        @QueryParam("options") List<String> options,
	                                                        @Context UriInfo uriInfo, @Context MessageContext mc) {

		//TODO: Should we make this generic or does this satisfy our needs for the test case?

		RestJaxbServiceUtil util = new RestJaxbServiceUtil();
		OperationResultType result = new OperationResultType();
		result.setOperation("Modify object");

		UserType objectType = (UserType) objectMap.get(type).get(id);

		if (objectType == null) {
			result.setStatus(OperationResultStatusType.FATAL_ERROR);
			result.setMessage("User with oid " + id + " not found");
			return RestMockServiceUtil.createResponse(Status.NOT_FOUND, result);
		}

		//Grab changes from the ObjectModificationType
		List<ItemDeltaType> deltaTypeList = object.getItemDelta();

		for(ItemDeltaType delta : deltaTypeList){
			if(delta.getModificationType() == ModificationTypeType.ADD){
				objectType.setGivenName((PolyStringType)delta.getValue().get(0));
			}
			else if(delta.getModificationType() == ModificationTypeType.REPLACE){

				objectType.setDescription(delta.getValue().get(0).toString());
			}
			else{ //ModificationTypeType.DELETE
				objectType.setGivenName(null);
			}
		}

		return Response.status(Status.NO_CONTENT).header("Content-Type", MediaType.APPLICATION_XML).entity(objectType).build();

	}

	@DELETE
	@Path("/{type}/{id}")
	@Produces({MediaType.APPLICATION_XML})
	public <T extends ObjectType> Response deleteObject(@PathParam("type") String type, @PathParam("id") String id,
                                                        @QueryParam("options") List<String> options,
                                                        @QueryParam("include") List<String> include,
                                                        @QueryParam("exclude") List<String> exclude,
                                                        @Context MessageContext mc){

		OperationResultType result = new OperationResultType();
		result.setOperation("Delete object");

        if (!objectMap.get(type).containsKey(id)) {
            result.setStatus(OperationResultStatusType.FATAL_ERROR);
            result.setMessage("Object with oid " + id + " was not found.");
            return RestMockServiceUtil.createResponse(Status.NOT_FOUND, result);
        }

		objectMap.get(type).remove(id);

		return Response.status(Status.OK).header("Content-Type", MediaType.APPLICATION_XML).build();
	}
	
	@POST
	@Path("/{type}/search")
	@Produces({MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_XML})
	public Response searchObjects(@PathParam("type") String type, QueryType queryType,
			@QueryParam("options") List<String> options,
			@QueryParam("include") List<String> include,
			@QueryParam("exclude") List<String> exclude,
			@Context MessageContext mc){
		OperationResultType result = new OperationResultType();
		result.setOperation("Search objects");
		
		ObjectListType resultList = new ObjectListType();
		
		JAXBContext jaxbCtx;
		try {
			jaxbCtx = createJaxbContext();
		
		Marshaller marshaller = jaxbCtx.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(new JAXBElement<QueryType>(new QName(SchemaConstants.NS_QUERY, "query"), QueryType.class, queryType), writer);
		
		System.out.println("Query received on the service: " + writer);
		} catch (IOException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return RestMockServiceUtil.createResponse(Status.OK, resultList, result);
	}

	@GET
	@Path("/self")
	@Produces({MediaType.APPLICATION_XML})
	public Response searchObjects(@Context MessageContext mc){
		OperationResultType result = new OperationResultType();
		result.setOperation("Self");

		UserType userType = new UserType();
		RestJaxbServiceUtil util = new RestJaxbServiceUtil();
		userType.setName(util.createPoly("administrator"));

		return RestMockServiceUtil.createResponse(Status.OK, userType, result);
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
