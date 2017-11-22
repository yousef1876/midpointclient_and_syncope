package com.evolveum.midpoint.client.api;

public interface PolicyItemDefinitionBuilder {

	PolicyItemDefinitionBuilder policy(String oid);
	PolicyItemDefinitionBuilder execute();
	ValidateGenerateRpcService value(Object value);
	ValidateGenerateRpcService path(String ItemPath);
}
