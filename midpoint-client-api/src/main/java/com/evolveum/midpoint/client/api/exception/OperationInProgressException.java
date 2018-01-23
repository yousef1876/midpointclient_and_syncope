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
package com.evolveum.midpoint.client.api.exception;

/**
 * Thrown when client invoked synchronous operation, but the
 * operation is executed as asynchronous on the server.
 * 
 * @author semancik
 *
 */
public class OperationInProgressException extends CommonException {
	private static final long serialVersionUID = 1L;

	public OperationInProgressException() {
		super();
	}

	public OperationInProgressException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OperationInProgressException(String message, Throwable cause) {
		super(message, cause);
	}

	public OperationInProgressException(String message) {
		super(message);
	}

	public OperationInProgressException(Throwable cause) {
		super(cause);
	}

}
