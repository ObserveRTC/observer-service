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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Job implements Task {
	private static final Logger logger = LoggerFactory.getLogger(Job.class);

	private final Map<Task, List<Task>> taskGraph;
	private final String name;
	private String description;
	private volatile boolean run = false;

	public Job(String name) {
		this.taskGraph = new HashMap<>();
		if (name == null) {
			this.name = this.getClass().getName();
		} else {
			this.name = name;
		}
	}

	public Job() {
		this(null);
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public Job withDescription(String description) {
		this.description = description;
		return this;
	}

	@Override
	public void execute(Map<String, Map<String, Object>> results) {
		this.perform();
	}

	@Override
	public Map<String, Object> getResults() {
		return new HashMap<>();
	}

	public Job withTask(Task task, Task... dependencies) {
		if (this.taskGraph.containsKey(task)) {
			throw new IllegalStateException("Task " + task.getName() + "has already been added to the job. Adding twice violates the " +
					"the rule of universe.");
		}
		List<Task> listOfDependencies = Arrays.asList(dependencies);
		this.taskGraph.put(task, listOfDependencies);
		return this;
	}


	public void perform() {
		this.checkCycles();
		Set<Task> visited = new HashSet<>();
		Queue<Task> tasksToExecute = new LinkedList<>();
		this.taskGraph.keySet().stream().map(t -> this.getTopologicalOrder(t, visited)).forEach(tasksToExecute::addAll);
		while (0 < tasksToExecute.size()) {
			Task task = tasksToExecute.poll();
			Map<String, Map<String, Object>> results = new HashMap<>();
			List<Task> adjacents = this.taskGraph.get(task);
			if (adjacents != null) {
				Iterator<Task> it = adjacents.iterator();
				for (; it.hasNext(); ) {
					Task adjacent = it.next();
					Map<String, Object> adjacentResults = adjacent.getResults();
					results.put(adjacent.getName(), adjacentResults);
				}
			}
			logger.info("Executing task {}. Description: {}", task.getName(), task.getDescription());
			task.execute(results);
		}
	}

	private Queue<Task> getTopologicalOrder(Task task, Set<Task> visited) {
		Queue<Task> result = new LinkedList<>();
		if (visited.contains(task)) {
			return result;
		}
		visited.add(task);
		List<Task> adjacents = this.taskGraph.get(task);
		if (adjacents != null) {
			Iterator<Task> it = adjacents.iterator();
			for (; it.hasNext(); ) {
				Task adjacent = it.next();
				if (visited.contains(adjacent)) {
					continue;
				}
				Queue<Task> toExecute = this.getTopologicalOrder(adjacent, visited);
				result.addAll(toExecute);
			}
		}
		result.add(task);
		return result;
	}

	private void checkCycles() {
		Iterator<Map.Entry<Task, List<Task>>> it = this.taskGraph.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<Task, List<Task>> entry = it.next();
			Task task = entry.getKey();
			Map<Task, Boolean> recursiveFlags = new HashMap<>();
			this.checkCycle(task, recursiveFlags);
		}
	}

	private void checkCycle(Task task, Map<Task, Boolean> recursiveFlags) throws IllegalStateException {
		Boolean recursiveFlag = recursiveFlags.get(task);
		if (recursiveFlag != null) {
			if (recursiveFlag == true) {
				throw new IllegalStateException("There is a recursive dependency for task: " + task.getName());
			}
		}
		recursiveFlags.put(task, true);
		List<Task> adjacents = this.taskGraph.get(task);
		if (adjacents != null) {
			Iterator<Task> it = adjacents.iterator();
			for (; it.hasNext(); ) {
				Task adjacent = it.next();
				this.checkCycle(adjacent, recursiveFlags);
			}
		}
		recursiveFlags.put(task, false);
	}

}
