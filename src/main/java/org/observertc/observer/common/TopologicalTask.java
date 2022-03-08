//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.observertc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class TopologicalTask implements Task {
    private static final Logger logger = LoggerFactory.getLogger(TopologicalTask.class);
    private final Map<Task, List<Task>> taskGraph;
    private boolean succeeded = false;
    private boolean executed = false;
    public TopologicalTask(String name) {
        this.taskGraph = new HashMap();
    }

    public TopologicalTask() {
        this(null);
    }

    public TopologicalTask withTask(Task task, Task... dependencies) {
        if (this.taskGraph.containsKey(task)) {
            throw new IllegalStateException("Task " + task.getName() + "has already been added to the job. Adding twice violates the the rule of universe.");
        }

        List<Task> listOfDependencies = Arrays.asList(dependencies);
        this.taskGraph.put(task, listOfDependencies);
        return this;
    }

    @Override
    public Task execute() {
        this.checkCycles();
        Set<Task> visited = new HashSet();
        Iterator<Task> tasks = this.taskGraph.keySet().stream()
                .map(task -> this.getTopologicalOrder(task, visited))
                .flatMap(Queue::stream).iterator();

        for (; tasks.hasNext();) {
            try (Task task = tasks.next()) {
                task.execute();
            } catch (Throwable t) {
                logger.error("Task execution for is failed", t);
                return this;
            }
        }
        return this;
    }

    private Queue<Task> getTopologicalOrder(Task task, Set<Task> visited) {
        Queue<Task> result = new LinkedList();
        if (visited.contains(task)) {
            return result;
        } else {
            visited.add(task);
            List<Task> adjacents = (List)this.taskGraph.get(task);
            if (adjacents != null) {
                Iterator it = adjacents.iterator();

                while(it.hasNext()) {
                    Task adjacent = (Task)it.next();
                    if (!visited.contains(adjacent)) {
                        Queue<Task> toExecute = this.getTopologicalOrder(adjacent, visited);
                        result.addAll(toExecute);
                    }
                }
            }

            result.add(task);
            return result;
        }
    }

    private void checkCycles() {
        Iterator it = this.taskGraph.entrySet().iterator();

        while(it.hasNext()) {
            Entry<Task, List<Task>> entry = (Entry)it.next();
            Task task = (Task)entry.getKey();
            Map<Task, Boolean> recursiveFlags = new HashMap();
            this.checkCycle(task, recursiveFlags);
        }

    }

    private void checkCycle(Task task, Map<Task, Boolean> recursiveFlags) throws IllegalStateException {
        Boolean recursiveFlag = (Boolean)recursiveFlags.get(task);
        if (recursiveFlag != null && recursiveFlag) {
            throw new IllegalStateException("There is a recursive dependency for task: " + task.getName());
        } else {
            recursiveFlags.put(task, true);
            List<Task> adjacents = (List)this.taskGraph.get(task);
            if (adjacents != null) {
                Iterator it = adjacents.iterator();

                while(it.hasNext()) {
                    Task adjacent = (Task)it.next();
                    this.checkCycle(adjacent, recursiveFlags);
                }
            }

            recursiveFlags.put(task, false);
        }
    }


    @Override
    public boolean succeeded() {
        return false;
    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public Object getResultOrDefault(Object defaultValue) {
        return null;
    }
}
