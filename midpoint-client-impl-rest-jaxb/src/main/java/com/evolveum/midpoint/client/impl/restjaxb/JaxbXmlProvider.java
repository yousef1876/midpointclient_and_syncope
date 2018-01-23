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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.provider.AbstractJAXBProvider;

/**
 * 
 * @author katkav
 *
 */
@Produces({"application/xml"})
@Consumes({"application/xml"})
@Provider
public class JaxbXmlProvider<T> extends AbstractJAXBProvider<T>{

	private JAXBContext jaxbContext;
	
	public JaxbXmlProvider(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}
	
	@Override
	public T readFrom(Class<T> clazz, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4,
			InputStream inputStream) throws IOException, WebApplicationException {
		
		if (inputStream == null || inputStream.available() == 0) {
			return null;
		}
			try {
				
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Object object = unmarshaller.unmarshal(inputStream);
			if (object instanceof JAXBElement) {
				return (T) ((JAXBElement) object).getValue();
			}
			return (T) object;
			} catch (JAXBException e) {
				throw new IOException(e);
			}
	}

	@Override
	public void writeTo(T jaxbElement, Class<?> clazz, Type arg2, Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream outputStream) throws IOException, WebApplicationException {
		try {
			
			
		Marshaller marshaller = jaxbContext.createMarshaller();
		JAXBElement<T> element = new JAXBElement(Types.findType(clazz).getTypeName(), clazz, jaxbElement);
		marshaller.marshal(element, outputStream);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}


	

}
