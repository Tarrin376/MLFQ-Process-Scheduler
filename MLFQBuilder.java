import java.util.*;

public class MLFQBuilder {
    public final List<JobQueue> jobQueues;
    public int priorityBoost;

    public MLFQBuilder() {
        this.jobQueues = new ArrayList<>();
        this.priorityBoost = -1;
    }

    public MLFQBuilder setPriorityBoost(final int priorityBoost) {
        this.priorityBoost = priorityBoost;
        return this;
    }

    public MLFQBuilder addJobQueue(final int allotment, final int quantum) {
        this.jobQueues.add(new JobQueue(allotment, quantum));
        return this;
    }

    public MLFQ build() throws InterruptedException {
        return new MLFQ(this);
    }
}