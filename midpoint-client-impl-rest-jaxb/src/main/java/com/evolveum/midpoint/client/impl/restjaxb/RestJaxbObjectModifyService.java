package com.evolveum.midpoint.client.impl.restjaxb;

import com.evolveum.midpoint.client.api.ObjectModifyService;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.AuthorizationException;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.client.api.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.client.api.exception.OperationInProgressException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Description
 * @author jakmor
 */
public class RestJaxbObjectModifyService<O extends ObjectType> extends AbstractObjectTypeWebResource<O> implements ObjectModifyService
{

    private Map<String, Object> modifications;

    public RestJaxbObjectModifyService(RestJaxbService service, Class<O> type)
    {
        super(service, type);
    }

    @Override
    public TaskFuture apost() throws AuthorizationException, ObjectAlreadyExistsException
    {
        // if object created (sync):
        String oid = null;
        String restPath = Types.findType(getType()).getRestPath();
        Response response = getService().getClient().replacePath("/" + restPath).post();

        switch (response.getStatus()) {
            case 400:
                throw new BadRequestException(response.getStatusInfo().getReasonPhrase());
            case 401:
            case 403:
                throw new AuthorizationException(response.getStatusInfo().getReasonPhrase());
            case 409:
                throw new ObjectAlreadyExistsException(response.getStatusInfo().getReasonPhrase());
            case 201:
            case 202:
                String location = response.getLocation().toString();
                String[] locationSegments = location.split(restPath + "/");
                oid = locationSegments[1];
                RestJaxbObjectReference<O> ref = new RestJaxbObjectReference<>(getService(), getType(), oid);
                return new RestJaxbCompletedFuture<>(ref);
            default:
                throw new UnsupportedOperationException("Implement other status codes, unsupported return status: " + response.getStatus());
        }
    }
}
