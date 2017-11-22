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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.naming.CommunicationException;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.text.StrBuilder;

import com.evolveum.midpoint.client.api.exception.ConfigurationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.evolveum.midpoint.client.api.exception.SecurityViolationException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CharacterClassType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LimitationsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ParamsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordLifeTimeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.StringLimitType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.StringPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValuePolicyType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

public class ValuePolicyProcessor {
	
	private static final Random RAND = new Random(System.currentTimeMillis());
	private static final String ASCII7_CHARS = " !\"#$%&'()*+,-.01234567890:;<=>?"
			+ "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_" + "`abcdefghijklmnopqrstuvwxyz{|}~";
	
	public boolean validate(ValuePolicyType pp, String newValue,
			OperationResultType parentResult) throws SchemaException, ObjectNotFoundException, CommunicationException, ConfigurationException, SecurityViolationException {

		Validate.notNull(pp, "Value policy must not be null.");

		StringBuilder message = new StringBuilder();
		
		OperationResultType result = new OperationResultType();
		result.setOperation("string policy validation");
		result.getPartialResults().add(result);	//	parentResult.createSubresult(OPERATION_STRING_POLICY_VALIDATION);
		ParamsType params = new ParamsType();
		EntryType entry = new EntryType();
		entry.setKey("policyName");
		entry.setEntryValue(new JAXBElement<PolyStringType>(new QName("value"), PolyStringType.class, pp.getName()));
		params.getEntry().add(entry);
		result.setParams(params);
		normalize(pp);

		if (newValue == null && 
				(pp.getMinOccurs() == null)) {
			// No password is allowed
			result.setStatus(OperationResultStatusType.SUCCESS);
			return true;
		}

		if (newValue == null) {
			newValue = "";
		}

		LimitationsType lims = pp.getStringPolicy().getLimitations();

		testMinimalLength(newValue, lims, result, message);
		testMaximalLength(newValue, lims, result, message);

		testMinimalUniqueCharacters(newValue, lims, result, message);
		
		if (lims.getLimit() == null || lims.getLimit().isEmpty()) {
			if (message.toString() == null || message.toString().isEmpty()) {
				OperationResultUtil.computeStatus(result);
			} else {
				result.setMessage(message.toString());
				OperationResultUtil.computeStatus(result);

			}

			return OperationResultUtil.isAcceptable(result);
		}

		// check limitation
		HashSet<String> validChars = null;
		HashSet<String> allValidChars = new HashSet<>();
		List<String> passwd = stringTokenizer(newValue);
		for (StringLimitType stringLimitationType : lims.getLimit()) {
			OperationResultType limitResult = new OperationResultType();
			limitResult.setOperation(
					"Tested limitation: " + stringLimitationType.getDescription());

			validChars = getValidCharacters(stringLimitationType.getCharacterClass(), pp);
			int count = countValidCharacters(validChars, passwd);
			allValidChars.addAll(validChars);
			testMinimalOccurence(stringLimitationType, count, limitResult, message);
			testMaximalOccurence(stringLimitationType, count, limitResult, message);
			testMustBeFirst(stringLimitationType, count, limitResult, message, newValue, validChars);

			OperationResultUtil.computeStatus(limitResult);
			result.getPartialResults().add(limitResult);
			
		}
		testInvalidCharacters(passwd, allValidChars, result, message);
		
		if (message.toString() == null || message.toString().isEmpty()) {
			OperationResultUtil.computeStatus(result);
		} else {
			result.setMessage(message.toString());
			OperationResultUtil.computeStatus(result);

		}

		return OperationResultUtil.isAcceptable(result);
	}
	
	private HashSet<String> getValidCharacters(CharacterClassType characterClassType,
			ValuePolicyType passwordPolicy) {
		if (null != characterClassType.getValue()) {
			return new HashSet<String>(stringTokenizer(characterClassType.getValue()));
		} else {
			return new HashSet<String>(stringTokenizer(collectCharacterClass(passwordPolicy.getStringPolicy().getCharacterClass(),
							characterClassType.getRef())));
		}
	}

	private int countValidCharacters(Set<String> validChars, List<String> password) {
		int count = 0;
		for (String s : password) {
			if (validChars.contains(s)) {
				count++;
			}
		}
		return count;
	}
	
	private void normalize(ValuePolicyType pp) {
		if (null == pp) {
			throw new IllegalArgumentException("Password policy cannot be null");
		}

		if (null == pp.getStringPolicy()) {
			StringPolicyType sp = new StringPolicyType();
			pp.setStringPolicy(normalize(sp));
		} else {
			pp.setStringPolicy(normalize(pp.getStringPolicy()));
		}

		if (null == pp.getLifetime()) {
			PasswordLifeTimeType lt = new PasswordLifeTimeType();
			lt.setExpiration(-1);
			lt.setWarnBeforeExpiration(0);
			lt.setLockAfterExpiration(0);
			lt.setMinPasswordAge(0);
			lt.setPasswordHistoryLength(0);
		}
		return;
	}
	
	
	private void testMinimalLength(String password, LimitationsType limitations,
			OperationResultType result, StringBuilder message) {
		// Test minimal length
		if (limitations.getMinLength() == null) {
			limitations.setMinLength(0);
		}
		if (limitations.getMinLength() > password.length()) {
			String msg = "Required minimal size (" + limitations.getMinLength()
					+ ") is not met (actual length: " + password.length() + ")";
			OperationResultType subResult = new OperationResultType();
			subResult.setOperation("Check global minimal length");
			subResult.setStatus(OperationResultStatusType.FATAL_ERROR);
			subResult.setMessage(msg);
			result.getPartialResults().add(subResult);
			message.append(msg);
			message.append("\n");
		}
	}

	private void testMaximalLength(String password, LimitationsType limitations,
			OperationResultType result, StringBuilder message) {
		// Test maximal length
		if (limitations.getMaxLength() != null) {
			if (limitations.getMaxLength() < password.length()) {
				String msg = "Required maximal size (" + limitations.getMaxLength()
						+ ") was exceeded (actual length: " + password.length() + ").";
				OperationResultType subResult = new OperationResultType();
				subResult.setOperation("Check global maximal length");
				subResult.setStatus(OperationResultStatusType.FATAL_ERROR);
				subResult.setMessage(msg);
				result.getPartialResults().add(subResult);
				message.append(msg);
				message.append("\n");
			}
		}
	}
	
	private void testMinimalUniqueCharacters(String password, LimitationsType limitations,
			OperationResultType result, StringBuilder message) {
		// Test uniqueness criteria
		HashSet<String> tmp = new HashSet<String>(stringTokenizer(password));
		if (limitations.getMinUniqueChars() != null) {
			if (limitations.getMinUniqueChars() > tmp.size()) {
				String msg = "Required minimal count of unique characters (" + limitations.getMinUniqueChars()
						+ ") in password are not met (unique characters in password " + tmp.size() + ")";
				OperationResultType subResult = new OperationResultType();
				subResult.setOperation("Check minimal count of unique chars");
				subResult.setStatus(OperationResultStatusType.FATAL_ERROR);
				subResult.setMessage(msg);
				result.getPartialResults().add(subResult);
				message.append(msg);
				message.append("\n");
			}

		}
	}
	
	private void testMaximalOccurence(StringLimitType stringLimitationType, int count,
			OperationResultType limitResult, StringBuilder message) {
		// Test maximal occurrence
		if (stringLimitationType.getMaxOccurs() != null) {

			if (stringLimitationType.getMaxOccurs() < count) {
				String msg = "Required maximal occurrence (" + stringLimitationType.getMaxOccurs()
						+ ") of characters (" + stringLimitationType.getDescription()
						+ ") in password was exceeded (occurrence of characters in password " + count + ").";
				
				OperationResultType subResult = new OperationResultType();
				subResult.setOperation("Check maximal occurrence of characters");
				subResult.setStatus(OperationResultStatusType.FATAL_ERROR);
				subResult.setMessage(msg);
				limitResult.getPartialResults().add(subResult);
				
				message.append(msg);
				message.append("\n");
			}
			// else {
			// limitResult.addSubresult(new OperationResult(
			// "Check maximal occurrence of characters in password OK.",
			// OperationResultStatus.SUCCESS,
			// "PASSED"));
			// }
		}

	}

	private void testMinimalOccurence(StringLimitType stringLimitation, int count,
			OperationResultType result, StringBuilder message) {
		// Test minimal occurrence
		if (stringLimitation.getMinOccurs() == null) {
			stringLimitation.setMinOccurs(0);
		}
		if (stringLimitation.getMinOccurs() > count) {
			String msg = "Required minimal occurrence (" + stringLimitation.getMinOccurs()
					+ ") of characters (" + stringLimitation.getDescription()
					+ ") in password is not met (occurrence of characters in password " + count + ").";
			OperationResultType subResult = new OperationResultType();
			subResult.setOperation("Check minimal occurrence of characters");
			subResult.setStatus(OperationResultStatusType.FATAL_ERROR);
			subResult.setMessage(msg);
			result.getPartialResults().add(subResult);
			message.append(msg);
			message.append("\n");
		}
	}
	
	private void testMustBeFirst(StringLimitType stringLimitationType, int count,
			OperationResultType limitResult, StringBuilder message, String password, Set<String> validChars) {
		// test if first character is valid
		if (stringLimitationType.isMustBeFirst() == null) {
			stringLimitationType.setMustBeFirst(false);
		}
		// we check mustBeFirst only for non-empty passwords
		if (StringUtils.isNotEmpty(password) && stringLimitationType.isMustBeFirst()
				&& !validChars.contains(password.substring(0, 1))) {
			String msg = "First character is not from allowed set. Allowed set: " + validChars.toString();
			OperationResultType subResult = new OperationResultType();
			subResult.setOperation("Check valid first char");
			subResult.setStatus(OperationResultStatusType.FATAL_ERROR);
			subResult.setMessage(msg);
			limitResult.getPartialResults().add(subResult);
			message.append(msg);
			message.append("\n");
		}
		// else {
		// limitResult.addSubresult(new OperationResult("Check valid first char
		// in password OK.",
		// OperationResultStatus.SUCCESS, "PASSED"));
		// }

	}
	
	private void testInvalidCharacters(List<String> password, HashSet<String> validChars,
			OperationResultType result, StringBuilder message) {

		// Check if there is no invalid character
		StringBuilder invalidCharacters = new StringBuilder();
		for (String s : password) {
			if (!validChars.contains(s)) {
				// memorize all invalid characters
				invalidCharacters.append(s);
			}
		}
		if (invalidCharacters.length() > 0) {
			String msg = "Characters [ " + invalidCharacters + " ] are not allowed in value";
			
			OperationResultType subResult = new OperationResultType();
			subResult.setOperation("Check if value does not contain invalid characters");
			subResult.setStatus(OperationResultStatusType.FATAL_ERROR);
			subResult.setMessage(msg);
			result.getPartialResults().add(subResult);
			
			message.append(msg);
			message.append("\n");
		}
		// else {
		// ret.addSubresult(new OperationResult("Check if password does not
		// contain invalid characters OK.",
		// OperationResultStatus.SUCCESS, "PASSED"));
		// }

	}
	
	public Object generate(ItemPathType path, ValuePolicyType policy, OperationResultType result) {
		StringPolicyType stringPolicy = policy.getStringPolicy();
		// if (policy.getLimitations() != null &&
		// policy.getLimitations().getMinLength() != null){
		// generateMinimalSize = true;
		// }
		// setup default values where missing
		// PasswordPolicyUtils.normalize(pp);

		// Optimize usage of limits ass hashmap of limitas and key is set of
		// valid chars for each limitation
		boolean generateMinimalSize = false;
		int defaultLength = 8;
		Map<StringLimitType, List<String>> lims = new HashMap<StringLimitType, List<String>>();
		int minLen = 8;
		int maxLen = 8;
		int unique = 8 / 2;
		if (stringPolicy != null) {
			for (StringLimitType l : stringPolicy.getLimitations().getLimit()) {
				if (null != l.getCharacterClass().getValue()) {
					lims.put(l, stringTokenizer(l.getCharacterClass().getValue()));
				} else {
					lims.put(l, stringTokenizer(collectCharacterClass(
							stringPolicy.getCharacterClass(), l.getCharacterClass().getRef())));
				}
			}

			// Get global limitations
			minLen = stringPolicy.getLimitations().getMinLength() == null ? 0
					: stringPolicy.getLimitations().getMinLength().intValue();
			if (minLen != 0 && minLen > defaultLength) {
				defaultLength = minLen;
			}
			maxLen = (stringPolicy.getLimitations().getMaxLength() == null ? 0
					: stringPolicy.getLimitations().getMaxLength().intValue());
			unique = stringPolicy.getLimitations().getMinUniqueChars() == null ? minLen
					: stringPolicy.getLimitations().getMinUniqueChars().intValue();

		} 
		// test correctness of definition
		if (unique > minLen) {
			minLen = unique;
			OperationResultType reportBug = new OperationResultType();
			reportBug.setOperation("Global limitation check");
			reportBug.setStatus(OperationResultStatusType.WARNING);
			reportBug.setMessage(
					"There is more required uniq characters then definied minimum. Raise minimum to number of required uniq chars.");
		}

		if (minLen == 0 && maxLen == 0) {
			minLen = defaultLength;
			maxLen = defaultLength;
			generateMinimalSize = true;
		}

		if (maxLen == 0) {
			if (minLen > defaultLength) {
				maxLen = minLen;
			} else {
				maxLen = defaultLength;
			}
		}

		// Initialize generator
		StringBuilder password = new StringBuilder();

		/*
		 * ********************************** Try to find best characters to be
		 * first in password
		 */
		Map<StringLimitType, List<String>> mustBeFirst = new HashMap<StringLimitType, List<String>>();
		for (StringLimitType l : lims.keySet()) {
			if (l.isMustBeFirst() != null && l.isMustBeFirst()) {
				mustBeFirst.put(l, lims.get(l));
			}
		}

		// If any limitation was found to be first
		if (!mustBeFirst.isEmpty()) {
			Map<Integer, List<String>> posibleFirstChars = cardinalityCounter(mustBeFirst, null, false, false,
					result);
			int intersectionCardinality = mustBeFirst.keySet().size();
			List<String> intersectionCharacters = posibleFirstChars.get(intersectionCardinality);
			// If no intersection was found then raise error
			if (null == intersectionCharacters || intersectionCharacters.size() == 0) {
				result.setStatus(OperationResultStatusType.FATAL_ERROR);
				result.setMessage(
						"No intersection for required first character sets in value policy:"
								+ stringPolicy.getDescription());
				// No more processing unrecoverable conflict
				return null; // EXIT
			} else {
				// Generate random char into password from intersection
				password.append(intersectionCharacters.get(RAND.nextInt(intersectionCharacters.size())));
			}
		}

		/*
		 * ************************************** Generate rest to fulfill
		 * minimal criteria
		 */

		boolean uniquenessReached = false;

		// Count cardinality of elements
		Map<Integer, List<String>> chars;
		for (int i = 0; i < minLen; i++) {

			// Check if still unique chars are needed
			if (password.length() >= unique) {
				uniquenessReached = true;
			}
			// Find all usable characters
			chars = cardinalityCounter(lims, stringTokenizer(password.toString()), false,
					uniquenessReached, result);
			// If something goes badly then go out
			if (null == chars) {
				return null;
			}

			if (chars.isEmpty()) {
				break;
			}
			// Find lowest possible cardinality and then generate char
			for (int card = 1; card < lims.keySet().size(); card++) {
				if (chars.containsKey(card)) {
					List<String> validChars = chars.get(card);
					password.append(validChars.get(RAND.nextInt(validChars.size())));
					break;
				}
			}
		}

		// test if maximum is not exceeded
		if (password.length() > maxLen) {
			result.setStatus(OperationResultStatusType.FATAL_ERROR);
			result.setMessage(
					"Unable to meet minimal criteria and not exceed maximxal size of " + path + ".");
			return null;
		}

		/*
		 * *************************************** Generate chars to not exceed
		 * maximal
		 */

		for (int i = 0; i < minLen; i++) {
			// test if max is reached
			if (password.length() == maxLen) {
				// no more characters maximal size is reached
				break;
			}

			if (password.length() >= minLen && generateMinimalSize) {
				// no more characters are needed
				break;
			}

			// Check if still unique chars are needed
			if (password.length() >= unique) {
				uniquenessReached = true;
			}
			// find all usable characters
			chars = cardinalityCounter(lims, stringTokenizer(password.toString()), true,
					uniquenessReached, result);

			// If something goes badly then go out
			if (null == chars) {
				// we hope this never happend.
				result.setStatus(OperationResultStatusType.FATAL_ERROR);
				result.setMessage(
						"No valid characters to generate, but no all limitation are reached");
				return null;
			}

			// if selection is empty then no more characters and we can close
			// our work
			if (chars.isEmpty()) {
				if (i == 0) {
					password.append(RandomStringUtils.randomAlphanumeric(minLen));

				}
				break;
				// if (!StringUtils.isBlank(password.toString()) &&
				// password.length() >= minLen) {
				// break;
				// }
				// check uf this is a firs cycle and if we need to user some
				// default (alphanum) character class.

			}

			// Find lowest possible cardinality and then generate char
			for (int card = 1; card <= lims.keySet().size(); card++) {
				if (chars.containsKey(card)) {
					List<String> validChars = chars.get(card);
					password.append(validChars.get(RAND.nextInt(validChars.size())));
					break;
				}
			}
		}

		if (password.length() < minLen) {
			result.setStatus(OperationResultStatusType.FATAL_ERROR);
			result.setMessage(
					"Unable to generate value for " + path + " and meet minimal size of " + path + ". Actual lenght: "
							+ password.length() + ", required: " + minLen);
			return null;
		}

		result.setStatus(OperationResultStatusType.SUCCESS);

		// Shuffle output to solve pattern like output
		StrBuilder sb = new StrBuilder(password.substring(0, 1));
		List<String> shuffleBuffer = stringTokenizer(password.substring(1));
		Collections.shuffle(shuffleBuffer);
		sb.appendAll(shuffleBuffer);

		return sb.toString();
	
}

private Map<Integer, List<String>> cardinalityCounter(Map<StringLimitType, List<String>> lims,
		List<String> password, Boolean skipMatchedLims, boolean uniquenessReached, OperationResultType op) {
	HashMap<String, Integer> counter = new HashMap<String, Integer>();

	for (StringLimitType l : lims.keySet()) {
		int counterKey = 1;
		List<String> chars = lims.get(l);
		int i = 0;
		if (null != password) {
			i = charIntersectionCounter(lims.get(l), password);
		}
		// If max is exceed then error unable to continue
		if (l.getMaxOccurs() != null && i > l.getMaxOccurs()) {
			OperationResultType o = new OperationResultType();
			o.setOperation("Limitation check :" + l.getDescription());
			o.setStatus(OperationResultStatusType.FATAL_ERROR);
			o.setMessage(
					"Exceeded maximal value for this limitation. " + i + ">" + l.getMaxOccurs());
			op.getPartialResults().add(o);
			return null;
			// if max is all ready reached or skip enabled for minimal skip
			// counting
		} else if (l.getMaxOccurs() != null && i == l.getMaxOccurs()) {
			continue;
			// other cases minimum is not reached
		} else if ((l.getMinOccurs() == null || i >= l.getMinOccurs()) && !skipMatchedLims) {
			continue;
		}
		for (String s : chars) {
			if (null == password || !password.contains(s) || uniquenessReached) {
				// if (null == counter.get(s)) {
				counter.put(s, counterKey);
				// } else {
				// counter.put(s, counter.get(s) + 1);
				// }
			}
		}
		counterKey++;

	}

	// If need to remove disabled chars (already reached limitations)
	if (null != password) {
		for (StringLimitType l : lims.keySet()) {
			int i = charIntersectionCounter(lims.get(l), password);
			if (l.getMaxOccurs() != null && i > l.getMaxOccurs()) {
				OperationResultType o = new OperationResultType();
				o.setOperation("Limitation check :" + l.getDescription());
				o.setStatus(OperationResultStatusType.FATAL_ERROR);
				o.setMessage(
						"Exceeded maximal value for this limitation. " + i + ">" + l.getMaxOccurs());
				op.getPartialResults().add(o);
				return null;
			} else if (l.getMaxOccurs() != null && i == l.getMaxOccurs()) {
				// limitation matched remove all used chars
				for (String charToRemove : lims.get(l)) {
					counter.remove(charToRemove);
				}
			}
		}
	}

	// Transpone to better format
	Map<Integer, List<String>> ret = new HashMap<Integer, List<String>>();
	for (String s : counter.keySet()) {
		// if not there initialize
		if (null == ret.get(counter.get(s))) {
			ret.put(counter.get(s), new ArrayList<String>());
		}
		ret.get(counter.get(s)).add(s);
	}
	return ret;
}

private static List<String> stringTokenizer(String in) {
List<String> l = new ArrayList<String>();
for (String a: in.split("")) {
	if (!a.isEmpty()) {
		l.add(a);
	}
}
return l;
}

public static String collectCharacterClass(CharacterClassType cc, QName ref) {
StrBuilder l = new StrBuilder();
if (null == cc) {
	throw new IllegalArgumentException("Character class cannot be null");
}

if (null != cc.getValue() && (null == ref || ref.equals(cc.getName()))) {
	l.append(cc.getValue());
} else if (null != cc.getCharacterClass() && !cc.getCharacterClass().isEmpty()) {
	// Process all sub lists
	for (CharacterClassType subClass : cc.getCharacterClass()) {
		// If we found requested name or no name defined
		if (null == ref || ref.equals(cc.getName())) {
			l.append(collectCharacterClass(subClass, null));
		} else {
			l.append(collectCharacterClass(subClass, ref));
		}
	}
}
// Remove duplicity in return;
HashSet<String> h = new HashSet<String>();
for (String s : l.toString().split("")) {
	h.add(s);
}
return new StrBuilder().appendAll(h).toString();
}

private int charIntersectionCounter(List<String> a, List<String> b) {
	int ret = 0;
	for (String s : b) {
		if (a.contains(s)) {
			ret++;
		}
	}
	return ret;
}

public static StringPolicyType normalize(StringPolicyType sp) {
	if (null == sp) {
		throw new IllegalArgumentException("Providide string policy cannot be null");
	}

	if (null == sp.getLimitations()) {
		LimitationsType sl = new LimitationsType();
		sl.setCheckAgainstDictionary(false);
		sl.setCheckPattern("");
		sl.setMaxLength(-1);
		sl.setMinLength(0);
		sl.setMinUniqueChars(0);
		sp.setLimitations(sl);
	}

	// Add default char class
	if (null == sp.getCharacterClass()) {
		CharacterClassType cct = new CharacterClassType();
		cct.setValue(ASCII7_CHARS);
		sp.setCharacterClass(cct);
	}

	return sp;
}

}
