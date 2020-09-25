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

package org.observertc.webrtc.common.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class JobTest {
	public class C {
		public int a;
		public int b;
	}

	class A {
		void init (A source) {
			
		}
		private ObjectMapper objectMapper = new ObjectMapper();
		private C config;
		private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
//			String jsonString = aInputStream.readUTF();
			InputStream iStream = aInputStream;
			this.config = this.objectMapper.readValue(iStream, C.class);
			

			// ensure that object state has not been corrupted or tampered with malicious code
			//validateUserInfo();
		}

		/**
		 * This is the default implementation of writeObject. Customize as necessary.
		 */
		private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

			//ensure that object is in desired state. Possibly run any business rules if applicable.
			//checkUserInfo();

			// perform the default serialization for all non-transient, non-static fields
			aOutputStream.defaultWriteObject();
		}
	}

	@Test
	public void shouldRunInOrder() {

		Task task1 = this.makeTask("Task 1", map -> System.out.println("executed"));
		Task task2 = this.makeTask("Task 2", map -> System.out.println("executed"));
		Task task3 = this.makeTask("Task 3", map -> System.out.println("executed"));
		Task task4 = this.makeTask("Task 4", map -> System.out.println("executed"));
		Task task5 = this.makeTask("Task 5", map -> System.out.println("executed"));
		Job job = new Job()
				.withTask(task1, task2, task3)
				.withTask(task2, task4)
				.withTask(task3, task4)
				.withTask(task4)
				.withTask(task5, task1);

		job.perform();
	}

	private Task makeTask(String name, Consumer<Map<String, Map<String, Object>>> consumer) {
		return new AbstractTask(name) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				consumer.accept(results);
			}
		}.withName(name);
	}
}