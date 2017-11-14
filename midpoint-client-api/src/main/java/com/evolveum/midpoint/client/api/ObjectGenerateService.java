package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author jakmor
 */
public interface ObjectGenerateService<O extends ObjectType> extends Post<ObjectReference<O>>{
}
