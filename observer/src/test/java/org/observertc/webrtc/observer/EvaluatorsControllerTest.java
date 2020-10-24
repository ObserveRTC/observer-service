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

package org.observertc.webrtc.observer;

import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

@MicronautTest
public class EvaluatorsControllerTest {

//    @Inject
//    EmbeddedServer embeddedServer;
//
//    @Test
//    public void testIndex() throws Exception {
//        try(RxHttpClient client = embeddedServer.getApplicationContext().createBean(RxHttpClient.class, embeddedServer.getURL())) {
//            assertEquals(HttpStatus.OK, client.toBlocking().exchange("/evaluators").status());
//        }
//    }

	@Test
	public void t() throws InterruptedException {
		Supplier<Integer> numP = () -> this.source();
		Observable.just(numP)
//		this.source()
				.delay(1000, TimeUnit.MILLISECONDS)
				.repeat()
				.doOnError(throwable -> {
					throwable.printStackTrace();
				})
//				.toMap(numProvider  -> numProvider.get())
				.subscribeOn(Schedulers.io())
				.subscribe(numProvider -> {
					Integer num = numProvider.get();
					System.out.println("Received int:" + num + " hash: " + num.hashCode());
				});
		Thread.sleep(10000);
	}

	private Supplier<Integer> integerSupplier = () -> new Random().nextInt();

	private Integer source() {
//		Unsafe unsafe = getUnsafeInstance();
		Integer source = integerSupplier.get();
		Integer result;
		synchronized (EvaluatorsControllerTest.this) {
			System.out.println("Generated int:" + source + " hash: " + source.hashCode());
			result = source;
			System.out.println("Copied int:" + source + " hash: " + source.hashCode());
		}
		return result;
	}
}
