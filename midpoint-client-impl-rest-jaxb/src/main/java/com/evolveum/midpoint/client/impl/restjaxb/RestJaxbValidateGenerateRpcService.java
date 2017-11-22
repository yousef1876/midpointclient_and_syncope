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

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import com.evolveum.midpoint.client.api.PolicyItemDefinitionBuilder;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.AuthorizationException;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;

/**
 * 
 * @author katkav
 *
 */
public class RestJaxbValidateGenerateRpcService implements ValidateGenerateRpcService {

	private RestJaxbService service;
	private List<PolicyItemDefinitionType> policyItemDefinitions;
	
	private String path;
	
	
	public RestJaxbValidateGenerateRpcService(RestJaxbService service, String path) {
		this.service = service;
		this.path = path;
	}
	
	public RestJaxbValidateGenerateRpcService(RestJaxbService service, String path, PolicyItemDefinitionType policyItemDefinition) {
		this.service = service;
		this.path = path;
		if (policyItemDefinitions == null) {
			policyItemDefinitions = new ArrayList<>();
		}
		
		policyItemDefinitions.add(policyItemDefinition);
		 
	}
	
	@Override
	public TaskFuture<PolicyItemsDefinitionType> apost() throws CommonException {
		PolicyItemsDefinitionType policyItemsDefinition = new PolicyItemsDefinitionType();
		policyItemsDefinition.getPolicyItemDefinition().addAll(policyItemDefinitions);

		Response response = service.getClient().replacePath(path).post(policyItemsDefinition);
		
		switch (response.getStatus()) {
        case 200:
            PolicyItemsDefinitionType itemsDefinitionType = response.readEntity(PolicyItemsDefinitionType.class);
            return new RestJaxbCompletedFuture<PolicyItemsDefinitionType>(itemsDefinitionType);
        case 400:
            throw new BadRequestException(response.getStatusInfo().getReasonPhrase());
        case 401:
            throw new AuthenticationException(response.getStatusInfo().getReasonPhrase());
        case 403:
            throw new AuthorizationException(response.getStatusInfo().getReasonPhrase());
            //TODO: Do we want to return a reference? Might be useful.
        case 404:
            throw new ObjectNotFoundException(response.getStatusInfo().getReasonPhrase());
        default:
            throw new UnsupportedOperationException("Implement other status codes, unsupported return status: " + response.getStatus());
    }
	}

	@Override
	public PolicyItemDefinitionBuilder item() {
		return new PolicyItemDefinitionBuilderImpl(service, path);
	}
}
