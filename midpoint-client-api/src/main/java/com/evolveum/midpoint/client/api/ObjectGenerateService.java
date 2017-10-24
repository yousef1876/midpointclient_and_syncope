package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * @author jakmor
 */
public interface ObjectGenerateService<O extends ObjectType> extends Post<ObjectReference<O>>{

    ObjectGenerateService<O> execute();
    ObjectGenerateService<O> path(String path);
    ObjectGenerateService<O> policy(String policyOid);
}
