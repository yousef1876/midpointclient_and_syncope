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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.evolveum.midpoint.client.api.exception.TunnelException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * 
 * @author katkav
 *
 */
public class DomSerializer {
	
	private static final String FILTER_EQUAL = "equal";
	private static final String FILTER_REF = "ref";
	private static final String FILTER_REF_OID = "oid";
	private static final String FILTER_REF_TYPE = "type";
	
	private static final String FILTER_GREATER = "greater";
	private static final String FILTER_LESS = "less";
	
	private static final String FILTER_NOT = "not";
	private static final String FILTER_AND = "and";
	private static final String FILTER_OR = "or";
	
	private static final String FILTER_PATH = "path";
	private static final String FILTER_VALUE = "value";
	
	private Document document;
	private DocumentBuilder documentBuilder;
	private JAXBContext jaxbContext;
	
	public DomSerializer(JAXBContext jaxbContext) throws IOException{
		this.jaxbContext = jaxbContext;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
	}

	
	public Element createEqualFilter(ItemPathType itemPath, List<Object> values) {
//		Document document = documentBuilder.newDocument();
		Element equal = createEqual(itemPath, document);
		
		List<Element> valueElements = createValueElements(values, document);
		if (values == null) {
			//TODO throw exception?
			return equal;
		}
		
		valueElements.forEach(v -> equal.appendChild(v));

		return equal;
	}
	
	public Element createEqualPolyFilter(ItemPathType itemPath, String orig, String norm) {
		Document document = documentBuilder.newDocument();
		Element equal = createEqual(itemPath, document);
		
		Element value = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_VALUE);
		if (!StringUtils.isBlank(orig)) {
			Element origElement = document.createElementNS(SchemaConstants.NS_TYPES, "orig");
			origElement.setTextContent(orig);
			value.appendChild(origElement);
		}
		
		if (!StringUtils.isBlank(norm)) {
			Element normElement = document.createElementNS(SchemaConstants.NS_TYPES, "norm");
			normElement.setTextContent(norm);
			value.appendChild(normElement);
		}
		
		return equal;
	}
	
	private Element createEqual(ItemPathType itemPath, Document document){
		
		Element equal = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_EQUAL);
		Element path = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_PATH);
		path.setTextContent(itemPath.getValue());
		equal.appendChild(path);
		return equal;
	}
	
	private List<Element> createValueElements(List<Object> values, Document value) {
		if (CollectionUtils.isEmpty(values)) {
			return null;
		}
		List<Element> valueElements = new ArrayList<>();
		
			values.forEach(v -> {
				Marshaller marshaller;
				try {
					marshaller = jaxbContext.createMarshaller();
					marshaller.marshal(new JAXBElement<>(new QName(SchemaConstants.NS_QUERY, "value"), Object.class, v),
							value);
					valueElements.add(value.getDocumentElement());
				} catch (JAXBException e) {
					throw new TunnelException(e);
				}
			});
		return valueElements;
		
	}
	
	public Element createRefFilter(ItemPathType itemPath, List<ObjectReferenceType> values) {
		Document document = documentBuilder.newDocument();
		Element ref = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_REF);
		Element path = document.createElement(FILTER_PATH);
		path.setTextContent(itemPath.getValue());
		ref.appendChild(path);
		
		Element value = document.createElement(FILTER_VALUE);
		if (!CollectionUtils.isEmpty(values)) {
			values.forEach(v -> {
				if (StringUtils.isNotBlank(v.getOid())) {
					Element refOid = document.createElement(FILTER_REF_OID);
					refOid.setTextContent(v.getOid());
					value.appendChild(refOid);
				}
				if (v.getType() != null) {
					Element refType = document.createElement(FILTER_REF_TYPE);
					refType.setTextContent(v.getType().getLocalPart());
					value.appendChild(refType);
				}
				//TODO relation...
			});
		}
		ref.appendChild(value);
		return ref;
	}
	
	public Element createGreaterFilter(ItemPathType itemPath, Object realValue) {
		Document document = documentBuilder.newDocument();
		Element greater = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_GREATER);
		Element path = document.createElement(FILTER_PATH);
		path.setTextContent(itemPath.getValue());
		greater.appendChild(path);
		
		Element value = document.createElement(FILTER_VALUE);
		Marshaller marshaller;
		try {
			marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(new JAXBElement(new QName(SchemaConstants.NS_QUERY, "value"), realValue.getClass(), realValue),
					value);
			
		} catch (JAXBException e) {
			//throw new SchemaException(e);
		}
		
		greater.appendChild(value);
		return greater;
	}
	
	public Element createNotFilter(Element filter) {
		Document document = documentBuilder.newDocument();
		Element not = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_NOT);
		not.appendChild(filter);
		return not;
	}
	
	public Element createAndFilter(Element filter) {
		Document document = null;
		if (filter != null) {
			document = filter.getOwnerDocument();
		} else {
			return null;
		}
		Element and = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_AND);
		and.appendChild(filter);
		return and;
	}
	
	public Element addCondition(Element andFilter, Element subFilter) {
		andFilter.appendChild(subFilter);
		return andFilter;
	}
	
	public Element createOrFilter(List<Element> children) {
		if (children == null) {
			return null;
		}
		
		if (children.isEmpty()) {
			return null;
		}
		Document document = children.iterator().next().getOwnerDocument();
		Element or = document.createElementNS(SchemaConstants.NS_QUERY, FILTER_OR);
		children.forEach(child -> or.appendChild(child));
		return or;
	}
	
	
}
