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

import com.evolveum.midpoint.client.api.ObjectGenerateService;
import com.evolveum.midpoint.client.api.ObjectModifyService;
import com.evolveum.midpoint.client.api.ObjectService;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.Map;

/**
 * @author semancik
 *
 */
public class RestJaxbObjectService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectService<O> {

	public RestJaxbObjectService(final RestJaxbService service, final Class<O> type, final String oid) {
		super(service, type, oid);
	}

	@Override
	public O get() throws ObjectNotFoundException, AuthenticationException {
		return getService().getObject(getType(), getOid());
	}

	@Override
	public void delete() throws ObjectNotFoundException, AuthenticationException
	{
		 getService().deleteObject(getType(), getOid());
	}

	@Override
	public ObjectModifyService<O> modify() throws ObjectNotFoundException, AuthenticationException
	{
		return new RestJaxbObjectModifyService<>(getService(), getType(), getOid());
	}

	@Override
	public ValidateGenerateRpcService generate() {
		String restPath = RestUtil.subUrl(Types.findType(getType()).getRestPath(), getOid());
        restPath += "/generate";
		return new RestJaxbValidateGenerateRpcService(getService(), restPath);
	}
//	@Override
//	public ObjectGenerateService<O> modifyGenerate(String path) throws ObjectNotFoundException, AuthenticationException{
//		return new RestJaxbObjectGenerateService<>(getService(), getType(), getOid(), path);
//	}
}
