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
