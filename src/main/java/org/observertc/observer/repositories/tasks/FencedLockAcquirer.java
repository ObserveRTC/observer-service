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

package org.observertc.observer.repositories.tasks;

import com.hazelcast.cp.lock.FencedLock;
import io.micronaut.context.annotation.Prototype;
import java.util.concurrent.TimeUnit;

import org.observertc.observer.ObserverHazelcast;

@Prototype
class FencedLockAcquirer implements AutoCloseable {

	private final ObserverHazelcast observerHazelcast;
	private String lockName;
	private FencedLock fencedLock;
	private volatile boolean ownTheLock;

	public FencedLockAcquirer(ObserverHazelcast observerHazelcast) {
		this.observerHazelcast = observerHazelcast;
	}

	public FencedLockAcquirer forLockName(String value) {
		this.lockName = value;
		return this;
	}

	protected FencedLockAcquirer acquire() {
		this.validate();
		this.fencedLock = this.observerHazelcast.getCPSubsystem().getLock(this.lockName);
		int timeToLiveSeconds = this.observerHazelcast.getInstance().getConfig().getCPSubsystemConfig().getSessionTimeToLiveSeconds();
		if (!this.fencedLock.tryLock(timeToLiveSeconds, TimeUnit.SECONDS)) {
			throw new IllegalStateException("Failed to acquire the lock");
		}
		this.ownTheLock = true;
		return this;
	}


	private void validate() {
		if (this.lockName == null) {
			throw new IllegalStateException("To perform the task it is required to have a lockName");
		}
	}

	@Override
	public void close() throws Exception {
		if (this.fencedLock == null || !this.ownTheLock) {
			return;
		}
		this.fencedLock.unlock();
	}
}
