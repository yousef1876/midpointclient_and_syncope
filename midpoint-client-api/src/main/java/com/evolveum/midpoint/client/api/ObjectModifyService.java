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
    ObjectModifyService<O> add(String path, Object value);
    ObjectModifyService<O> add(Map<String, Object> modifications);
    ObjectModifyService<O> replace(String path, Object value);
    ObjectModifyService<O> replace(Map<String, Object> modifications);
    ObjectModifyService<O> delete(String path, Object value);
    ObjectModifyService<O> delete(Map<String, Object> modifications);
}
