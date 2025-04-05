import java.util.*;

public class JobQueue {
    public final Map<Integer, List<Job>> blockedJobs;
    public final Deque<Job> jobs;

    private final int queueNumber;
    private final int allotment;
    private final int quantum;

    public JobQueue(final int queueNumber, final int allotment, final int quantum) {
        this.jobs = new ArrayDeque<>();
        this.blockedJobs = new HashMap<>();

        this.queueNumber = queueNumber;
        this.allotment = allotment;
        this.quantum = quantum;
    }

    public int getQueueNumber() { return queueNumber; }
    public int getAllotment() { return allotment; }
    public int getQuantum() { return quantum; }
}
