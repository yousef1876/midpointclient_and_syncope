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
package com.evolveum.midpoint.client.impl.restjaxb.service;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;

/**
 * 
 * @author katkav
 *
 */
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationProvider implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestCtx) throws IOException {
		Message m = JAXRSUtils.getCurrentMessage();

		AuthorizationPolicy policy = (AuthorizationPolicy) m.get(AuthorizationPolicy.class);
		if (policy == null){
    		RestMockServiceUtil.createAbortMessage(requestCtx);
        	return;
        }


        String enteredUsername = policy.getUserName();

        if (enteredUsername == null){
        	RestMockServiceUtil.createAbortMessage(requestCtx);
        	return;
        }

        //TODO: better impelemtnation:
        
        if ("administrator".equals(enteredUsername) && "5ecr3t".equals(policy.getPassword())) {
        	//successfull authn
        } else {
        	requestCtx.abortWith(Response.status(Status.FORBIDDEN).build());
        }
		
	}

	
	
}
