package com.evolveum.midpoint.client.impl.restjaxb;

import com.evolveum.midpoint.client.api.ObjectModifyService;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.AuthorizationException;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.client.api.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.client.api.exception.OperationInProgressException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @author jakmor
 */
public class RestJaxbObjectModifyService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectModifyService<O>
{

    private Map<String, Object> modifications;

    public RestJaxbObjectModifyService(RestJaxbService service, Class<O> type, String oid, Map<String, Object> modifications)
    {
        super(service, type, oid);
        this.modifications = modifications;
    }

    @Override
    public TaskFuture apost() throws AuthorizationException, ObjectAlreadyExistsException
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
            case 409:
                throw new ObjectAlreadyExistsException(response.getStatusInfo().getReasonPhrase());
                //TODO: Do we want to return a reference? Might be useful.
            case 204:
                RestJaxbObjectReference<O> ref = new RestJaxbObjectReference<>(getService(), getType(), oid);
                return new RestJaxbCompletedFuture<>(ref);
            default:
                throw new UnsupportedOperationException("Implement other status codes, unsupported return status: " + response.getStatus());
        }
    }
}
