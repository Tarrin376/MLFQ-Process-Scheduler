import java.text.DecimalFormat;
import java.util.*;

enum JobState {
    READY,
    RUNNING,
    BLOCKED
}

public class Job {
    public final PriorityQueue<IO> ioQueue;
    private final String pid;

    private final int startTime;
    private final int endTime;

    private JobState state = JobState.READY;
    private int progress;
    private int allotmentUsed;
    private int quantumUsed;

    public Job(final String pid, final int startTime, final int endTime) {
        ioQueue = new PriorityQueue<>((a, b) -> Long.compare(a.getStartTime(), b.getStartTime()));
        this.pid = pid;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updateJobProgress() {
        progress++;
        allotmentUsed++;
        quantumUsed++;
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

    public String getProgressPercentage() {
        return new DecimalFormat("#.##").format(((double)progress / (endTime - startTime)) * 100) + "%";
    }

    public String getJobMessage(final String message) {
        return "  -> " + AnsiColour.GREEN + "Job:" + AnsiColour.RESET + " [" + pid + "] " + "(progress: " + 
        AnsiColour.CYAN + getProgressPercentage() + AnsiColour.RESET + ") " + message;
    }

    public int getStartTime() { return startTime; }
    public int getEndTime() { return endTime; }

    public int getProgress() { return progress; }
    public int getAllotmentUsed() { return allotmentUsed; }
    public int getQuantumUsed() { return quantumUsed; }

    public void setProgress(int progress) { this.progress = progress; };
    public void setAllotmentUsed(int allotmentUsed) { this.allotmentUsed = allotmentUsed; }
    public void setQuantumUsed(int quantumUsed) { this.quantumUsed = quantumUsed; }
    public void setState(JobState state) { this.state = state; }

    public String getPID() { return pid; }
    public JobState getState() { return state; }
}
