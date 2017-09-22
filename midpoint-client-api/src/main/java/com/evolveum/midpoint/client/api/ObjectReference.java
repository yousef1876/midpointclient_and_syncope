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
package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.client.api.verb.Get;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * Reference to an object. It contains object OID and type as
 * a very minimum. But the reference may be resolved and then
 * it contains full object.
 * 
 * The reference is used in places where the method can return
 * either object identifiers (OID, type) or they may return complete
 * object.
 * 
 * @author semancik
 *
 */
public interface ObjectReference<O extends ObjectType> extends Get<O> {

	String getOid();
	
	Class<O> getType();
	
	O getObject();
	
	boolean containsObject();
}
