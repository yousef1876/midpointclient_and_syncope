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
import com.evolveum.midpoint.client.api.PolicyItemDefinitionBuilder;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.AuthorizationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import com.sun.org.apache.xpath.internal.functions.FuncSubstring;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jakmor
 */
public class RestJaxbObjectGenerateService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectGenerateService<O>
{


    private String path;


    public RestJaxbObjectGenerateService(RestJaxbService service, Class<O> type, String oid, String path)
    {
        super(service, type, oid);
        this.path = path;
    }


    @Override
    public TaskFuture apost() throws AuthorizationException, ObjectNotFoundException, AuthenticationException
    {
        String oid = getOid();
        String restPath = RestUtil.subUrl(Types.findType(getType()).getRestPath(), oid);
        restPath += "/generate";
        Response response = getService().getClient().replacePath(restPath).post(RestUtil.buildGenerateObject(this.path, true));

        switch (response.getStatus()) {
            case 200:
                RestJaxbObjectReference<O> ref = new RestJaxbObjectReference<>(getService(), getType(), oid);
                return new RestJaxbCompletedFuture<>(ref);
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
		// TODO Auto-generated method stub
		return null;
	}

}
