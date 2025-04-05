import java.util.*;

public class MLFQBuilder {
    public final LinkedList<JobQueue> jobQueues;
    public int priorityBoost;

    public MLFQBuilder() {
        jobQueues = new LinkedList<>();
    }

    public MLFQBuilder setPriorityBoost(final int priorityBoost) {
        this.priorityBoost = priorityBoost;
        return this;
    }

    public MLFQBuilder addJobQueue(final int allotment, final int quantum) {
        jobQueues.add(new JobQueue(jobQueues.size() + 1, allotment, quantum));
        return this;
    }

    public MLFQ build() throws InterruptedException {
        return new MLFQ(this);
    }
}