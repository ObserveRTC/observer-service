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

import org.observertc.webrtc.observer.entities.WeakLockEntity;
import org.observertc.webrtc.observer.repositories.stores.WeakLocksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

class WeakSpinLock implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(WeakSpinLock.class);
	private final WeakLocksRepository weakLocksRepository;
    private final WeakLockEntity lockEntity;

    public WeakSpinLock(WeakLocksRepository weakLocksRepository, WeakLockEntity lockEntity) {
        this.weakLocksRepository = weakLocksRepository;
        this.lockEntity = lockEntity;
	}

	private boolean tryLock(AtomicReference<WeakLockEntity> actualHolder) {
        Optional<WeakLockEntity> lockResult = this.weakLocksRepository.saveIfAbsent(lockEntity.name, lockEntity);
        if (!lockResult.isPresent()) {
            return true;
        }
        WeakLockEntity actual = lockResult.get();
        if (this.lockEntity.equals(actual)) {
            return true;
        }
        if (Objects.nonNull(actualHolder)) {
            actualHolder.set(actual);
        }
        return false;
    }

	public void lock(int maxWaitingTimeInS) {
        int consecutiveNoActualLockCounter = 0;
        Random random = new Random();
        AtomicReference<WeakLockEntity> actualHolder = new AtomicReference<>();
        if (this.tryLock(actualHolder)) {
            return;
        }
        WeakLockEntity actual;
        do {
            long millis = random.nextInt(1500) + 500;
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (this.tryLock(actualHolder)) {
                return;
            }

            actual = actualHolder.get();
            if (Objects.isNull(actual)) {
                logger.warn("There was no actual lock registered in the database, although the locking mechanism is failed. consecutiveNoActualLockCounter is {}", consecutiveNoActualLockCounter);
                if (++consecutiveNoActualLockCounter < 3) {
                    continue;
                }
                logger.warn("The number of tries to retrieve actual lock registered is {}, and now we forcefully save our ones as no alternative has given at this point.");
                break;
            } else {
                consecutiveNoActualLockCounter = 0;
            }

            if (Objects.isNull(actual.created)) {
                logger.warn("The actual lock {} does not have a created timestamp, therefore the lock is overridden and forced to this one {} ",
                        actual, this.lockEntity);
                this.weakLocksRepository.save(this.lockEntity.name, this.lockEntity);
                return;
            }
        } while(Duration.between(actual.created, Instant.now()).getSeconds() < maxWaitingTimeInS);

        if (Objects.isNull(actual)) {
            this.weakLocksRepository.save(this.lockEntity.name, this.lockEntity);
            return;
        }
        if (this.lockEntity.equals(actual)) {
            return;
        }
        logger.warn("Max timeout ({}) is elapsed for lock {}, " +
                "it is going to be forcefully replaced to {}", maxWaitingTimeInS,
                actual, this.lockEntity);
        this.weakLocksRepository.save(this.lockEntity.name, this.lockEntity);
	}

	public void unlock() {
        try {
            Optional<WeakLockEntity> actualHolder = weakLocksRepository.find(this.lockEntity.name);
            if (!actualHolder.isPresent()) {
                logger.warn("{} was not presented in the locktable. Has it unlocked before?", this.lockEntity.name);
                return;
            }
            WeakLockEntity actual = actualHolder.get();
            if (Objects.isNull(actual)) {
                logger.warn("Expected {} but was null.", this.lockEntity);
                weakLocksRepository.delete(this.lockEntity.name);
                return;
            }
            if (!actual.equals(this.lockEntity)) {
                logger.warn("Expected {}, but was found {}", this.lockEntity, actual);
                return;
            }
            weakLocksRepository.delete(this.lockEntity.name);
        } catch (Throwable t) {
            logger.error("Cannot unlock " + this.lockEntity.toString(), t);
        }
    }

    @Override
    public void close() throws Exception {
        this.unlock();
    }
}
