import java.util.*;

enum JobState {
    READY,
    RUNNING,
    BLOCKED
}

public class Job {
    private final String pid;
    private final int startTime;
    private final int endTime;

    private int progress;
    private int allotmentUsed;
    private int quantumUsed;

    public final PriorityQueue<IO> ioQueue;
    private JobState state = JobState.READY;

    public Job(final String pid, final int startTime, final int endTime) {
        ioQueue = new PriorityQueue<>((a, b) -> Integer.compare(a.getStartTime(), b.getStartTime()));
        this.pid = pid;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Job {\n");
        sb.append("  PID: ").append(pid).append("\n");
        sb.append("  Start Time: ").append(startTime).append("\n");
        sb.append("  End Time: ").append(endTime).append("\n");
        sb.append("  Progress: ").append(progress).append("\n");
        sb.append("  Allotment Used: ").append(allotmentUsed).append("\n");
        sb.append("  Quantum Used: ").append(quantumUsed).append("\n");
        sb.append("  State: ").append(state).append("\n");
        sb.append("  IO Queue: [");

        for (IO io : ioQueue) {
            sb.append("\n     ").append(io.toString());
        }

        sb.append((ioQueue.size() > 0 ? "\n  " : "") + "]\n");
        sb.append("}");
        return sb.toString();
    }
}
