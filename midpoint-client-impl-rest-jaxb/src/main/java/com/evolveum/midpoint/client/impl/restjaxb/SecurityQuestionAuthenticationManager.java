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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.WebClient;

import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public class SecurityQuestionAuthenticationManager implements AuthenticationManager<SecurityQuestionChallenge> {

	private SecurityQuestionChallenge challenge;
	
	private AuthenticationType authenticationType;
	
	public SecurityQuestionAuthenticationManager(String username, List<SecurityQuestionAnswer> secQ) {
		this.authenticationType = AuthenticationType.SECQ;
		challenge = new SecurityQuestionChallenge();
		challenge.setUsername(username);
		challenge.setAnswer(secQ);
	}
	
	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}
	
	
	
	@Override
	public String getType() {
		return authenticationType.getType();
	}
	
	@Override
	public void parseChallenge(String authenticationChallenge) throws SchemaException {
		JsonFactory f = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(f);
		JsonNode node = null;
			try {
				node = mapper.readTree(authenticationChallenge);
			} catch (IOException e) {
				throw new SchemaException(e);
			}
			
			JsonNode userNameNode = node.findPath("user");
			if (userNameNode instanceof MissingNode) {
				return;
			}
			
//			String userName = userNameNode.asText();
//			challenge.setUsername(userName);
			JsonNode answerNode = node.findPath("answer");

			if (answerNode instanceof MissingNode) {
				return;
			}
			ArrayNode answers = (ArrayNode) answerNode;
			Iterator<JsonNode> answersList = answers.elements();
			List<SecurityQuestionAnswer> questionAnswers = new ArrayList<>();
			while (answersList.hasNext()) {
				SecurityQuestionAnswer questionAnswer = new SecurityQuestionAnswer();
				JsonNode answer = answersList.next();
				String questionId = answer.findPath("qid").asText();
				questionAnswer.setQid(questionId);
				String questionText = answer.findPath("qtxt").asText();
				questionAnswer.setQtxt(questionText);
				questionAnswers.add(questionAnswer);
			}
			
			challenge.setAnswer(questionAnswers);
			
			return;
	}

	@Override
	public void createAuthorizationHeader(WebClient client) {
		
		
//		String USER_CHALLENGE = "\"user\" : \"$username\"";
//		String USER_QUESTION_ANSWER_CHALLENGE = ", \"answer\" :";
//		String QUESTION = "{\"qid\" : \"$QID\", \"qans\" : \"$QANS\"}";
		String authorizationHeader = getType();
		
		if (challenge != null) {
			JsonFactory f = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(f);
			mapper.setSerializationInclusion(Include.NON_NULL);
//			ObjectNode node = mapper.createObjectNode();
//			String stringChallenge;
//			if (challenge.getUsername() == null) {
//				client.header("Authorization", authorizationHeader);
//				return;
//			}
//			if (challenge.getUsername() != null) {
//				stringChallenge = "{" + USER_CHALLENGE.replace("$username", challenge.getUsername()) + "}";
//			}
//			
//			if (!CollectionUtils.isEmpty(challenge.getQuestionAnswer())) {
//				stringChallenge += USER_QUESTION_ANSWER_CHALLENGE + "[";
//				challenge.getQuestionAnswer().forEach(qa -> stringChallenge += QUESTION.replace("$QID" , qa.getId()).replace("$QANS", qa.getAnswer()));
//			}
			JsonNode node = mapper.valueToTree(challenge);
			authorizationHeader += " " + Base64Utility.encode(node.toString().getBytes());
			
		}
		
		client.header("Authorization", authorizationHeader);
	}
	
	@Override
	public SecurityQuestionChallenge getChallenge() {
		return challenge;
	}
	
}
