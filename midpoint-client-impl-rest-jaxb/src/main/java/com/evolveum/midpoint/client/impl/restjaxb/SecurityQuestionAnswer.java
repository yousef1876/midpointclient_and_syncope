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

public class SecurityQuestionAnswer {

	
	private String qid;
	private String qtxt;
	private String qans;
	
	public String getQid() {
		return qid;
	}
	
	public void setQid(String id) {
		this.qid = id;
	}
	
	public String getQtxt() {
		return qtxt;
	}
	
	public void setQtxt(String question) {
		this.qtxt = question;
	}
	
	public String getQans() {
		return qans;
	}
	
	public void setQans(String answer) {
		this.qans = answer;
	}
}
