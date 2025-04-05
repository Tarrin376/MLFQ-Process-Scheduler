import java.util.*;

public class JobQueue {
    private final Map<Integer, List<Job>> blockedJobs;
    public final Deque<Job> jobs;

    private final int allotment;
    private final int quantum;

    public JobQueue(final int allotment, final int quantum) {
        this.jobs = new ArrayDeque<>();
        this.blockedJobs = new HashMap<>();
        this.allotment = allotment;
        this.quantum = quantum;
    }
}
