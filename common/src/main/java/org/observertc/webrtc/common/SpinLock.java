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

package org.observertc.webrtc.common;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spin lock with exponential backoff and timeout.
 *
 * @author vrodionov
 */
public class SpinLock {

	/**
	 * The lock.
	 */
	private AtomicBoolean lock = new AtomicBoolean(false);

	/**
	 * The BACKOF f_ min.
	 */
	private long BACKOFF_MIN = 20;

	/**
	 * The BACKOF f_ max.
	 */
	private long BACKOFF_MAX = 20000;

	/**
	 * The BACKOF f_ int.
	 */
	private long BACKOFF_INT = 20;

	/**
	 * The BACKOF f_ min_ sleep.
	 */
	private long BACKOFF_MIN_SLEEP = 1000; // ns - 1microsec

	/**
	 * The BACKOF f_ ma x_ sleep.
	 */
	private long BACKOFF_MAX_SLEEP = 1000000;// ns - 1 millisec

	/**
	 * The BACKOF f_ in t_ sleep.
	 */
	private long BACKOFF_INT_SLEEP = 1000;

	/**
	 * The shallow lock.
	 */
	volatile boolean shallowLock = false;

	/**
	 * Instantiates a new spin lock.
	 */
	public SpinLock() {
		// all defaults
	}

	/**
	 * Instantiates a new spin lock.
	 *
	 * @param backoffMin      the backoff min
	 * @param backoffMax      the backoff max
	 * @param backoffInt      the backoff int
	 * @param backoffMinSleep the backoff min sleep
	 * @param backoffMaxSleep the backoff max sleep
	 * @param backoffIntSleep the backoff int sleep
	 */
	public SpinLock(long backoffMin, long backoffMax, long backoffInt,
					long backoffMinSleep, long backoffMaxSleep, long backoffIntSleep) {
		BACKOFF_MIN = backoffMin;
		BACKOFF_MAX = backoffMax;
		BACKOFF_INT = backoffInt;
		BACKOFF_MIN_SLEEP = backoffMinSleep;
		BACKOFF_MAX_SLEEP = backoffMaxSleep;
		BACKOFF_INT_SLEEP = backoffIntSleep;

	}

	/**
	 * Lock.
	 *
	 * @return true, if successful
	 */
	public boolean lock() {
		long count = BACKOFF_MIN;
		long timeout = BACKOFF_MIN_SLEEP;
		while (!lock.compareAndSet(false, true)) {

			if (count < BACKOFF_MAX) {
				int counter = 0;
				while (counter++ < count) ;
				count += BACKOFF_INT;
			} else {
				count = BACKOFF_MIN;

				if (timeout > BACKOFF_MAX_SLEEP) {
					return false;
				} else {
					long millis = timeout / 1000000;
					long nanos = timeout - millis * 1000000;
					try {
						Thread.sleep(millis, (int) nanos);
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
					timeout += BACKOFF_INT_SLEEP;
				}
			}

		}
		return true;
	}

	/**
	 * Unlock.
	 */
	public void unlock() {
		lock.set(false);
	}
}