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
