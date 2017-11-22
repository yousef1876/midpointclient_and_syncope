package com.evolveum.midpoint.client.api;

public interface PolicyItemDefinitionEntryBuilder {

	PolicyItemDefinitionExitBuilder policy(String oid);
	PolicyItemDefinitionExitBuilder execute();
	
	ValidateGenerateRpcService build();
}
