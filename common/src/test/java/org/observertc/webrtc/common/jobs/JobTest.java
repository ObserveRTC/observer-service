package org.observertc.webrtc.common.jobs;

import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class JobTest {


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