/*
 * Copyright  2020 Balazs Kreith
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

package org.observertc.webrtc.observer.tasks;

public abstract class TaskAbstract<T> implements AutoCloseable {
	private volatile boolean executed = false;

	public T perform() {
		this.validate();
		try {
			return this.doPerform();
		} catch (Exception ex) {
			throw ex;
		} finally {
			this.executed = true;
		}
	}

	protected abstract T doPerform();


	@Override
	public void close() {

	}

	protected void validate() {
		if (this.executed) {
			throw new IllegalStateException("The task is alrerady executed");
		}
	}
}
