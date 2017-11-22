package com.evolveum.midpoint.client.impl.restjaxb;

import com.evolveum.midpoint.client.api.PolicyItemDefinitionBuilder;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemTargetType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

public class PolicyItemDefinitionBuilderImpl implements PolicyItemDefinitionBuilder {
	
	private PolicyItemDefinitionType policyItemDefinition;
	private RestJaxbService service;
	private String restPath;

	public PolicyItemDefinitionBuilderImpl(RestJaxbService service, String restPath) {
		this.service  = service;
		this.restPath = restPath;
		this.policyItemDefinition = new PolicyItemDefinitionType();
	}
	
	@Override
	public PolicyItemDefinitionBuilder policy(String oid) {
		ObjectReferenceType ref = new ObjectReferenceType();
		ref.setOid(oid);
		ref.setType(Types.VALUE_POLICIES.getTypeName());
		policyItemDefinition.setValuePolicyRef(ref);
		return this;
	}

	@Override
	public PolicyItemDefinitionBuilder execute() {
		policyItemDefinition.setExecute(Boolean.TRUE);
		return this;
	}
	
	@Override
	public ValidateGenerateRpcService value(Object value) {
		policyItemDefinition.setValue(value);
		return new RestJaxbValidateGenerateRpcService(service, restPath, policyItemDefinition);
	}

	@Override
	public ValidateGenerateRpcService path(String itemPath) {
		PolicyItemTargetType policyItemTargetType = new PolicyItemTargetType();
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue(itemPath);
		policyItemTargetType.setPath(itemPathType);
		policyItemDefinition.setTarget(policyItemTargetType);
		return new RestJaxbValidateGenerateRpcService(service, restPath, policyItemDefinition);
	}

	
	
}
