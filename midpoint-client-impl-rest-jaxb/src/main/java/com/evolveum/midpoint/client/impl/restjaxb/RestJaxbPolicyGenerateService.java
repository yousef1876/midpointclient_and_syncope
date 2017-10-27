package com.evolveum.midpoint.client.impl.restjaxb;

import com.evolveum.midpoint.client.api.ObjectGenerateService;
import com.evolveum.midpoint.client.api.PolicyGenerateService;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.AuthorizationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

/**
 * @author jakmor
 */
public class RestJaxbPolicyGenerateService<O extends ObjectType> extends AbstractObjectWebResource<O> implements PolicyGenerateService
{

    private String path;

    public RestJaxbPolicyGenerateService(RestJaxbService service, Class<O> type, String oid)
    {
        super(service, type, oid);
        this.path = "description";
    }


    @Override
    public TaskFuture apost() throws AuthorizationException, ObjectNotFoundException, AuthenticationException
    {
        String oid = getOid();
        String restPath = RestUtil.subUrl(Types.findType(getType()).getRestPath(), oid);
        restPath += "/generate";
        Response response = getService().getClient().replacePath(restPath).post(RestUtil.buildGenerateObject(getOid(),this.path, false));

        switch (response.getStatus()) {
            case 200:
                PolicyItemsDefinitionType itemsDefinitionType = response.readEntity(PolicyItemsDefinitionType.class);
                return new RestJaxbCompletedFuture<>(RestUtil.getPolicyItemsDefValue(itemsDefinitionType));
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
