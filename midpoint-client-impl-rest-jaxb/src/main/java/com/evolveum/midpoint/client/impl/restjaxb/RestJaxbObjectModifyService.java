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
import com.evolveum.midpoint.client.api.ServiceUtil;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import org.apache.cxf.resource.ClasspathResolver;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jakmor
 */
public class RestJaxbObjectModifyService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectModifyService<O>
{

    private List<ItemDeltaType> modifications;

    public RestJaxbObjectModifyService(RestJaxbService service, Class<O> type, String oid)
    {
        super(service, type, oid);
        modifications = new ArrayList<>();
    }

    @Override
    public RestJaxbObjectModifyService<O> add(String path, Object value){
        addModification(path, value, ModificationTypeType.ADD);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> add(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.ADD);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> replace(String path, Object value){
        addModification(path, value, ModificationTypeType.REPLACE);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> replace(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.REPLACE);
        return this;
    }


    @Override
    public RestJaxbObjectModifyService<O> delete(String path, Object value){
        addModification(path, value, ModificationTypeType.DELETE);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> delete(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.DELETE);
        return this;
    }

    private void addModification(String path, Object value, ModificationTypeType modificationType){
        modifications.add(RestUtil.buildItemDelta(modificationType, path, value));
    }

    private void addModifications(Map<String, Object> modifications,  ModificationTypeType modificationType){
        modifications.forEach((path, value) ->
        addModification(path, value, modificationType));
    }

    @Override
    public TaskFuture apost() throws AuthorizationException, ObjectNotFoundException, AuthenticationException
    {
        String oid = getOid();
        String restPath = RestUtil.subUrl(Types.findType(getType()).getRestPath(), oid);

        Response response = getService().getClient().replacePath(restPath).post(RestUtil.buildModifyObject(modifications));

        switch (response.getStatus()) {
            case 204:
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
}
