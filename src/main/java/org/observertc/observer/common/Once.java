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

package org.observertc.observer.common;

public final class Once<T> {
	private volatile boolean set = false;
	private T value = null;

	public void set(final T value) {
		if (this.set)
			throw new IllegalStateException("Illegal attempt to set a Once value after it's value has already been set.");
		this.value = value;
		this.set = true;
	}

	public T get() {
		if (!this.set) throw new IllegalStateException("Illegal attempt to access Once value.");
		return this.value;
	}

	public boolean isSet() {
		return this.set;
	}
}