import java.util.*;

public class JobQueue {
    private Deque<Job> jobs;
    private Map<Integer, List<Job>> blockedJobs;
    private int allotment;
    private int quantum;

    public JobQueue(int allotment, int quantum) {
        this.jobs = new ArrayDeque<>();
        this.blockedJobs = new HashMap<>();
        this.allotment = allotment;
        this.quantum = quantum;
    }
}
