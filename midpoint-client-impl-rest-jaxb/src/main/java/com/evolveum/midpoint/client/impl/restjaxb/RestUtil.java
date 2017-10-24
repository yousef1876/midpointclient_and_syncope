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

import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemTargetType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author semancik
 *
 */
public class RestUtil {

	private final String NS_COMMON = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";
	
	public static String subUrl(final String urlPrefix, final String pathSegment) {
		// TODO: better code (e.g. escaping)
		return "/" + urlPrefix + "/" + pathSegment;
	}

	//TODO: If these work, item to interface
	public static ObjectModificationType buildModifyObject(String path, Object value, ModificationTypeType modificationType)
	{
		ObjectModificationType objectModificationType = new ObjectModificationType();
		objectModificationType.getItemDelta().add(buildItemDelta(modificationType, path, value));
		return objectModificationType;
	}

	public static ObjectModificationType buildModifyObject(Map<String, Object> pathValueMap, ModificationTypeType modificationType)
	{
		ObjectModificationType objectModificationType = new ObjectModificationType();
		pathValueMap.forEach((path, value) ->
				objectModificationType.getItemDelta().add(buildItemDelta(modificationType, path, value)));

		return objectModificationType;
	}

	public static ItemDeltaType buildItemDelta(ModificationTypeType modificationType, String path, Object value)
	{
		//Create ItemDelta
		ItemDeltaType itemDeltaType = new ItemDeltaType();
		itemDeltaType.setModificationType(modificationType);

		//Set Path
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue(path);
		itemDeltaType.setPath(itemPathType);

		//Set Value
		//TODO: Refactor. This really sucks. Passing an Object is very open-ended here.
		//This is done because currently when a string value is marshalled to xml it gets a type attribute
		//on it. That type attribute can cause conflicts depending on the attribute being updated.
		//For example, if updating GivenName, an error would be thrown as midpoint is expecting a polyStringType.
		//This is probably a namespacing issue. The current marshalling process is not robust enough to handle it and
		//still be generic.
		itemDeltaType.getValue().add(value);

		return itemDeltaType;
	}

	private PolicyItemsDefinitionType buildGenerateObject(String policyOid, String targetPath, Boolean execute)
	{
		PolicyItemsDefinitionType policyItemsDefinitionType = new PolicyItemsDefinitionType();
		PolicyItemDefinitionType policyItemDefinitionType = new PolicyItemDefinitionType();

		//Set target path
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue(targetPath);
		PolicyItemTargetType targetType = new PolicyItemTargetType();
		targetType.setPath(itemPathType);
		policyItemDefinitionType.setTarget(targetType);

		//Set valuePolicyRef
		policyItemDefinitionType.setValuePolicyRef(buildValuePolicyRef(policyOid));

		//Set Execute
		policyItemDefinitionType.setExecute(execute);

		policyItemsDefinitionType.getPolicyItemDefinition().add(policyItemDefinitionType);
		return policyItemsDefinitionType;
	}

	private ObjectReferenceType buildValuePolicyRef(String policyOid)
	{
		ObjectReferenceType objectReferenceType = new ObjectReferenceType();
		objectReferenceType.setOid(policyOid);
		QName qname = new QName(NS_COMMON, "ValuePolicyType");
		objectReferenceType.setType(qname);
		return objectReferenceType;
	}


	

}
