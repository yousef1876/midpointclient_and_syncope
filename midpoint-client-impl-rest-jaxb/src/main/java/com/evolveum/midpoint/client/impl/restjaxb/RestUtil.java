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
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.org.apache.xerces.internal.dom.TextImpl;
import javax.ws.rs.core.AbstractMultivaluedMap;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * @author semancik
 *
 */
public class RestUtil {

	private static final String NS_COMMON = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";
	
	public static String subUrl(final String urlPrefix, final String pathSegment) {
		// TODO: better code (e.g. escaping)
		return "/" + urlPrefix + "/" + pathSegment;
	}


	public static ObjectModificationType buildModifyObject(List<ItemDeltaType> itemDeltas)
	{
		ObjectModificationType objectModificationType = new ObjectModificationType();
		itemDeltas.forEach((itemDelta) ->
				objectModificationType.getItemDelta().add(itemDelta));

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

		itemDeltaType.getValue().add(value);

		return itemDeltaType;
	}
	public static PolicyItemsDefinitionType buildGenerateObject(String targetPath, Boolean execute){
		return buildGenerateObject("", targetPath,execute);
	}

	public static PolicyItemsDefinitionType buildGenerateObject(String policyOid, String targetPath, Boolean execute)
	{
		PolicyItemsDefinitionType policyItemsDefinitionType = new PolicyItemsDefinitionType();
		PolicyItemDefinitionType policyItemDefinitionType = new PolicyItemDefinitionType();

		//Set target path
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue(targetPath);
		PolicyItemTargetType targetType = new PolicyItemTargetType();
		targetType.setPath(itemPathType);
		policyItemDefinitionType.setTarget(targetType);

		if(!"".equals(policyOid))
		{
			//Set valuePolicyRef
			policyItemDefinitionType.setValuePolicyRef(buildValuePolicyRef(policyOid));
		}
		//Set Execute
		policyItemDefinitionType.setExecute(execute);

		policyItemsDefinitionType.getPolicyItemDefinition().add(policyItemDefinitionType);
		return policyItemsDefinitionType;
	}

	private static ObjectReferenceType buildValuePolicyRef(String policyOid)
	{
		ObjectReferenceType objectReferenceType = new ObjectReferenceType();
		objectReferenceType.setOid(policyOid);
		QName qname = new QName(NS_COMMON, "ValuePolicyType");
		objectReferenceType.setType(qname);
		return objectReferenceType;
	}

	public static String getPolicyItemsDefValue(PolicyItemsDefinitionType policyItems){
		List<PolicyItemDefinitionType> resultList = policyItems.getPolicyItemDefinition();
		PolicyItemDefinitionType policyItemDefinitionType = resultList.get(0);
		//Why cant getValue just return the string value? :(
		ElementNSImpl elementNSImpl = (ElementNSImpl) policyItemDefinitionType.getValue();
		TextImpl textImpl = (TextImpl) elementNSImpl.getFirstChild();
		return textImpl.getData();
	}
	

}
