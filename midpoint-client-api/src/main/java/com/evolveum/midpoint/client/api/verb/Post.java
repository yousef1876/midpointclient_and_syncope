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
package com.evolveum.midpoint.client.api.verb;

import java.util.concurrent.ExecutionException;

import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.exception.*;

/**
 * @author semancik
 *
 */
public interface Post<T> {

	/**
	 * Synchronous POST.
	 */
	default T post() throws OperationInProgressException, CommonException {
		
		TaskFuture<T> future = apost();
		
		if (!future.isDone()) {
			// TODO: better error message
			throw new OperationInProgressException("Operation in progress");
		}
		
		try {
			
			return future.get();
			
		} catch (InterruptedException e) {
			// We do not support interruptions or cancelations yet.
			// Therefore this should not happen.
			throw new SystemException("Unexpected internal error", e);
			
		} catch (ExecutionException e) {
			// Exception during execution
			Throwable cause = e.getCause();
			if (cause instanceof CommonException) {
				throw (CommonException)cause;				
			} else if (cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			} else {
				throw new SystemException("Unexpected internal error", e);
			}
		}
	}
	
	/**
	 * Potentially asynchronous POST.
	 * @throws AuthorizationException 
	 */
	TaskFuture<T> apost() throws CommonException;
	
}
