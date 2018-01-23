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

import java.io.IOException;
import java.util.List;

/**
 * 
 * @author katkav
 *
 */
public class RestJaxbServiceBuilder {
	
	private String url;
	private AuthenticationType authentication;
	private String username;
	private String password;
	private List<SecurityQuestionAnswer> questionAnswer;
	
	
	public RestJaxbServiceBuilder url(String url) {
		this.url = url;
		return this;
	}
	
	public RestJaxbServiceBuilder authentication(AuthenticationType authentication) {
		this.authentication = authentication;
		return this;
	}
	
	public RestJaxbServiceBuilder username(String username) {
		this.username = username;
		return this;
	}
	
	public RestJaxbServiceBuilder password(String password) {
		this.password = password;
		return this;
	}
	
	public RestJaxbServiceBuilder authenticationChallenge(List<SecurityQuestionAnswer> questionAnswer) {
		this.questionAnswer = questionAnswer;
		return this;
	}
	
	public RestJaxbService build() throws IOException {
		return new RestJaxbService(url, username, password, authentication, questionAnswer);
	}
	
	
}
