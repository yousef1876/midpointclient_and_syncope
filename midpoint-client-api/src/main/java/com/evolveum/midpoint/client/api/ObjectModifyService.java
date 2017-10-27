package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.Map;

/**
 * @author jakmor
 */
public interface ObjectModifyService <O extends ObjectType> extends Post<ObjectReference<O>>
{

    ObjectModifyService<O> item(String path, Object value);

    ObjectGenerateService<O> generate(String path) throws ObjectNotFoundException, AuthenticationException;
}