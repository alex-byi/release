package io.bootique.tools.release.model.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.tools.release.service.job.BatchForkJoinTask;
import io.bootique.value.Percent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

public class BatchJob<T, R> {

    private final long id;
    private final long startTimeNanos;

    @JsonIgnore
    private final List<BatchForkJoinTask<BatchJobResult<T, R>>> tasks;

    public BatchJob(long id, List<BatchForkJoinTask<BatchJobResult<T, R>>> tasks) {
        this.id = id;
        this.startTimeNanos = System.nanoTime();
        this.tasks = tasks;
    }

    public long getId() {
        return id;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    @JsonIgnore
    public Duration elapsedTime() {
        return Duration.ofNanos(System.nanoTime() - startTimeNanos);
    }

    @JsonProperty
    public int getTotal() {
        return tasks.size();
    }

    @JsonProperty
    public int getDone() {
        return (int)tasks.stream().filter(ForkJoinTask::isDone).count();
    }

    @JsonIgnore
    public Percent getProgress() {
        return new Percent((double)getDone() / getTotal());
    }

    @JsonIgnore
    public boolean isDone() {
        return getDone() == getTotal();
    }

    @JsonProperty
    public List<BatchJobResult<T, R>> getResults() {
        return tasks.stream()
                .map(task -> {
                    try {
                        return task.isDone() ? task.get() : task.getInitialState();
                    } catch (InterruptedException | ExecutionException | CancellationException ex) {
                        return task.getInitialState();
                    }
                })
                .collect(Collectors.toList());
    }
}
