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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (queueNumber > 1) {
            sb.append("---------------------------------------------------------------------------------------");
        }

        sb.append("\n\n" + AnsiColour.GREEN + "Queue " + queueNumber + ":" + AnsiColour.RESET + "\n");
        sb.append("  [Ready / Running]\n");

        for (Job job : jobs) {
            sb.append("    |__ " + job.getPID() + " | State: " + job.getJobStateColour() + " | Progress: " + AnsiColour.CYAN +
            job.getProgressPercentage() + AnsiColour.RESET + " | Time in Queue: " + job.getAllotmentUsed() + "ms\n");
        }

        sb.append("\n  [Blocked]");
        for (Map.Entry<Integer, List<Job>> entry : blockedJobs.entrySet()) {
            int blockedUntil = entry.getKey();
            List<Job> blocked = entry.getValue();

            for (Job job : blocked) {
                sb.append("\n    |__ " + job.getPID() + " | Blocked Until: " + blockedUntil + "ms | IO: \"" + job.ioQueue.peek().getName() + "\"");
            }
        }

        sb.append("\n\n");
        return sb.toString();
    }
    
    public int getQueueNumber() { return queueNumber; }
    public int getAllotment() { return allotment; }
    public int getQuantum() { return quantum; }
}
