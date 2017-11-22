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

/**
 * @author katkav
 */
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public class OperationResultUtil {

	public static boolean isPartialError(OperationResultType result) {
		return OperationResultStatusType.PARTIAL_ERROR == result.getStatus();
	}
	
	public static boolean isFatalError(OperationResultType result) {
		return OperationResultStatusType.FATAL_ERROR == result.getStatus();
	}
	
	public static boolean isHandledError(OperationResultType result) {
		return OperationResultStatusType.HANDLED_ERROR == result.getStatus();
	}
	
	public static boolean isInProgress(OperationResultType result) {
		return OperationResultStatusType.IN_PROGRESS == result.getStatus();
	}
	
	public static boolean isNotApplicable(OperationResultType result) {
		return OperationResultStatusType.NOT_APPLICABLE == result.getStatus();
	}
	
	public static boolean isSuccess(OperationResultType result) {
		return OperationResultStatusType.SUCCESS == result.getStatus();
	}
	
	public static boolean isUnknown(OperationResultType result) {
		return result.getStatus() == null || OperationResultStatusType.UNKNOWN == result.getStatus();
	}
	
	public static boolean isWarning(OperationResultType result) {
		return OperationResultStatusType.WARNING == result.getStatus();
	}
	
	public static boolean isAcceptable(OperationResultType result) {
		return (result.getStatus() != OperationResultStatusType.FATAL_ERROR);
	}
	
	public static void computeStatusIfUnknown(OperationResultType result) {
		if (isUnknown(result)) {
			computeStatus(result);
		}
	}
	
	
	public static void computeStatus(OperationResultType result) {
		OperationResultStatusType status = result.getStatus();
		if (result.getPartialResults().isEmpty()) {
			if (status == null || status == OperationResultStatusType.UNKNOWN) {
				status = OperationResultStatusType.SUCCESS;
				result.setStatus(status);
			}
			return;
		}
        if (status == OperationResultStatusType.FATAL_ERROR) {
            return;
        }
		OperationResultStatusType newStatus = OperationResultStatusType.UNKNOWN;
		boolean allSuccess = true;
		boolean allNotApplicable = true;
		String newMessage = null;
		String message = result.getMessage();
		for (OperationResultType sub : result.getPartialResults()) {
			if (sub.getStatus() != OperationResultStatusType.NOT_APPLICABLE) {
				allNotApplicable = false;
			}
			if (sub.getStatus() == OperationResultStatusType.FATAL_ERROR) {
				status = OperationResultStatusType.FATAL_ERROR;
				if (message == null) {
					message = sub.getMessage();
				} else {
					message = message + ": " + sub.getMessage();
				}
				return;
			}
			if (sub.getStatus() == OperationResultStatusType.IN_PROGRESS) {
				status = OperationResultStatusType.IN_PROGRESS;
				if (message == null) {
					message = sub.getMessage();
				} else {
					message = message + ": " + sub.getMessage();
				}
				return;
			}
			if (sub.getStatus() == OperationResultStatusType.PARTIAL_ERROR) {
				newStatus = OperationResultStatusType.PARTIAL_ERROR;
				newMessage = sub.getMessage();
			}
			if (newStatus != OperationResultStatusType.PARTIAL_ERROR){
			if (sub.getStatus() == OperationResultStatusType.HANDLED_ERROR) {
				newStatus = OperationResultStatusType.HANDLED_ERROR;
				newMessage = sub.getMessage();
			}
			}
			if (sub.getStatus() != OperationResultStatusType.SUCCESS
					&& sub.getStatus() != OperationResultStatusType.NOT_APPLICABLE) {
				allSuccess = false;
			}
			if (newStatus != OperationResultStatusType.HANDLED_ERROR) {
				if (sub.getStatus() == OperationResultStatusType.WARNING) {
					newStatus = OperationResultStatusType.WARNING;
					newMessage = sub.getMessage();
				}
			}
		}

		if (allNotApplicable && !result.getPartialResults().isEmpty()) {
			status = OperationResultStatusType.NOT_APPLICABLE;
		}
		if (allSuccess && !result.getPartialResults().isEmpty()) {
			status = OperationResultStatusType.SUCCESS;
		} else {
			status = newStatus;
			if (message == null) {
				message = newMessage;
			} else {
				message = message + ": " + newMessage;
			}
		}
		result.setStatus(status);
	}

}
