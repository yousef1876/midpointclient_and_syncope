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
package com.evolveum.midpoint.client.api;

import java.util.concurrent.Future;

import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;

/**
 * @author semancik
 *
 */
public interface TaskFuture<T> extends Future<T> {

	/**
	 * Returns reference to the task that is executing the operation.
	 * Returns null if the operation was synchronous.
	 */
	ObjectReference<TaskType> getTaskRef();
	
	default boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException("Future cancel is not supported");
	}
	
	default boolean isCancelled() {
		return false;
	}
}
