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

import com.evolveum.midpoint.client.api.RpcService;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.CommonException;

/**
 * 
 * @author katkav
 *
 */
public class RestJaxbRpcService<T> implements RpcService<T>{

	private static final String GENERATE_PATH = "/rpc/generate";
	private static final String VALIDATE_PATH = "/rpc/validate";
	
	private RestJaxbService service;
	
	public RestJaxbRpcService(RestJaxbService service) {
		this.service = service;
	}
	
	public RestJaxbService getService() {
		return service;
	}

	@Override
	public ValidateGenerateRpcService validate() {
		return new RestJaxbValidateGenerateRpcService(getService(), VALIDATE_PATH);
	}
	
	
	@Override
	public void compare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeScript() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TaskFuture<T> apost() throws CommonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValidateGenerateRpcService generate() {
		return new RestJaxbValidateGenerateRpcService(getService(), GENERATE_PATH);
		
	}

	
}
