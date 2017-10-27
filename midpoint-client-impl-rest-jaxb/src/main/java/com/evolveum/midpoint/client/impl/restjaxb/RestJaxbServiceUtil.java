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

import java.util.Arrays;
import java.util.Map;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.client.api.ServiceUtil;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * @author semancik
 *
 */
public class RestJaxbServiceUtil implements ServiceUtil {
	
	@Override
	public PolyStringType createPoly(String orig) {
		PolyStringType poly = new PolyStringType();
		poly.getContent().add(orig);
		return poly;
	}

	@Override
	public String getOrig(PolyStringType poly) {
		if (poly == null) {
			return null;
		}
		for (Object content: poly.getContent()) {
			if (content instanceof String) {
				return (String)content;
			}
			// TODO: DOM elements and JAXB elements
		}
		return null;
	}
	
	@Override
	public ItemPathType createItemPathType(QName... qname) {
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue("");
		Arrays.asList(qname).forEach(name -> itemPathType.setValue(itemPathType + "/" + name.getLocalPart()));
		return itemPathType;
	}


}
