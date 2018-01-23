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

import org.testng.AssertJUnit;

import com.evolveum.midpoint.client.api.Service;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * @author semancik
 *
 */
public class Asserts {

	public static void assertPoly(Service service, String message, String expected, PolyStringType actualPoly) {
		String actual = null;
		if (actualPoly != null) {
			actual = service.util().getOrig(actualPoly);
		}
		AssertJUnit.assertEquals(message, expected, actual);
	}

}
