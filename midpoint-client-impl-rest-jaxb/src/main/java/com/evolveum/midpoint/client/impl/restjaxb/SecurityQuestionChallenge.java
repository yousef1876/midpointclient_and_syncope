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

import java.util.ArrayList;
import java.util.List;

public class SecurityQuestionChallenge implements AuthenticationChallenge {

	private String user;
	private List<SecurityQuestionAnswer> answer;
	
	public String getUser() {
		return user;
	}
	
	public void setUsername(String username) {
		this.user = username;
	}
	
	public List<SecurityQuestionAnswer> getAnswer() {
		return answer;
	}
	
	public void setAnswer(List<SecurityQuestionAnswer> answer) {
		this.answer = answer;
	}
	
}
