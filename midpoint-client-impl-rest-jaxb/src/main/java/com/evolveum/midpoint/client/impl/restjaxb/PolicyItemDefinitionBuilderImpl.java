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

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.client.api.PolicyItemDefinitionBuilder;
import com.evolveum.midpoint.client.api.PolicyItemDefinitionEntryBuilder;
import com.evolveum.midpoint.client.api.PolicyItemDefinitionEntryOrExitBuilder;
import com.evolveum.midpoint.client.api.PolicyItemDefinitionExitBuilder;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemTargetType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * 
 * @author katkav
 *
 */
public class PolicyItemDefinitionBuilderImpl implements PolicyItemDefinitionEntryOrExitBuilder, PolicyItemDefinitionBuilder, PolicyItemDefinitionEntryBuilder, PolicyItemDefinitionExitBuilder {
	
	private PolicyItemDefinitionType policyItemDefinition;
	private RestJaxbService service;
	private String restPath;
	
	private List<PolicyItemDefinitionType> allItemDefinitions;
	

	public PolicyItemDefinitionBuilderImpl(RestJaxbService service, String restPath) {
		this.service  = service;
		this.restPath = restPath;
		this.allItemDefinitions = new ArrayList<>();

	}
	
	public PolicyItemDefinitionExitBuilder policy(String oid) {
		ObjectReferenceType ref = new ObjectReferenceType();
		ref.setOid(oid);
		ref.setType(Types.VALUE_POLICIES.getTypeName());
		policyItemDefinition.setValuePolicyRef(ref);
		return this;
	}

	public PolicyItemDefinitionExitBuilder execute() {
		policyItemDefinition.setExecute(Boolean.TRUE);
		return this;
	}
	
	public PolicyItemDefinitionBuilder value(Object value) {
		policyItemDefinition.setValue(value);
		return this;
	}

	public PolicyItemDefinitionBuilder path(String itemPath) {
		PolicyItemTargetType policyItemTargetType = new PolicyItemTargetType();
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue(itemPath);
		policyItemTargetType.setPath(itemPathType);
		policyItemDefinition.setTarget(policyItemTargetType);
		return this;
	}


	public ValidateGenerateRpcService build() {
		PolicyItemsDefinitionType policyItemsDefinition = new PolicyItemsDefinitionType();
		policyItemsDefinition.getPolicyItemDefinition().addAll(allItemDefinitions);
		return new RestJaxbValidateGenerateRpcService(service, restPath, policyItemsDefinition);
	}

	@Override
	public PolicyItemDefinitionEntryOrExitBuilder item() {
		policyItemDefinition = new PolicyItemDefinitionType();
		allItemDefinitions.add(policyItemDefinition);
		return this;
	}

	
	
	
	
}
