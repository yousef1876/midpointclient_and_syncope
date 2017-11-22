package com.evolveum.midpoint.client.api;

public interface PolicyItemDefinitionExitBuilder {

	
	PolicyItemDefinitionBuilder value(Object value);
	PolicyItemDefinitionBuilder path(String ItemPath);
	
	ValidateGenerateRpcService build();
}
