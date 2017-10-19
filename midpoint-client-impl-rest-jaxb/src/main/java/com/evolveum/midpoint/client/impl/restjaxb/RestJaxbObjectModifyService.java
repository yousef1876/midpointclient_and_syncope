package com.evolveum.midpoint.client.impl.restjaxb;

import com.evolveum.midpoint.client.api.ObjectModifyService;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jakmor
 */
public class RestJaxbObjectModifyService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectModifyService<O>
{

    private Map<String, Object> modifications;

    public RestJaxbObjectModifyService(RestJaxbService service, Class<O> type, String oid)
    {

        this(service, type, oid, new HashMap<String, Object>());
    }


    public RestJaxbObjectModifyService(RestJaxbService service, Class<O> type, String oid, Map<String, Object> modifications)
    {
        super(service, type, oid);
        this.modifications = modifications;
    }

    public RestJaxbObjectModifyService<O> add(String path, Object value){
        modifications.put(path, value);
        return this;
    }

    @Override
    public TaskFuture apost() throws AuthorizationException, ObjectNotFoundException
    {
        String oid = getOid();
        String restPath = RestUtil.subUrl(Types.findType(getType()).getRestPath(), oid);

        Response response = getService().getClient().replacePath(restPath).post(RestUtil.buildModifyObject(modifications, ModificationTypeType.ADD));

        switch (response.getStatus()) {
            case 400:
                throw new BadRequestException(response.getStatusInfo().getReasonPhrase());
            case 401:
            case 403:
                throw new AuthorizationException(response.getStatusInfo().getReasonPhrase());
                //TODO: Do we want to return a reference? Might be useful.
            case 404:
                throw new ObjectNotFoundException(response.getStatusInfo().getReasonPhrase());
            case 204:
                RestJaxbObjectReference<O> ref = new RestJaxbObjectReference<>(getService(), getType(), oid);
                return new RestJaxbCompletedFuture<>(ref);
            default:
                throw new UnsupportedOperationException("Implement other status codes, unsupported return status: " + response.getStatus());
        }
    }
}
