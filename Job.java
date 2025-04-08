import java.text.DecimalFormat;
import java.util.*;

enum JobState {
    READY,
    RUNNING,
    BLOCKED,
    COMPLETED
}

public class Job {
    public final PriorityQueue<IO> ioQueue;
    private final String pid;

    private final int arrivalTime;
    private final int endTime;

    private int turnaroundTime;
    private int responseTime;
    private int waitingTime;

    private JobState state = JobState.READY;
    private int progress;
    private int allotmentUsed;
    private int quantumUsed;

    public Job(final String pid, final int arrivalTime, final int endTime) {
        ioQueue = new PriorityQueue<>((a, b) -> Long.compare(a.getStartTime(), b.getStartTime()));
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Job {\n");
        sb.append("  PID: ").append(pid).append("\n");
        sb.append("  Arrival Time: ").append(arrivalTime).append("ms\n");
        sb.append("  End Time: ").append(endTime).append("ms\n");
        sb.append("  Progress: ").append(progress).append("\n");
        sb.append("  Allotment Used: ").append(allotmentUsed).append("ms\n");
        sb.append("  Quantum Used: ").append(quantumUsed).append("ms\n");
        sb.append("  State: ").append(getJobStateColour()).append("\n");
        sb.append("  IO Queue: [");

        for (IO io : ioQueue) {
            sb.append("\n     ").append(io.toString());
        }

        sb.append((ioQueue.size() > 0 ? "\n  " : "") + "]\n");
        sb.append("}");
        return sb.toString();
    }

    public boolean hasFinished() {
        return progress == endTime - arrivalTime;
    }

    public boolean isAllotmentUsed(final JobQueue queue) {
        return allotmentUsed == queue.getAllotment();
    }

    public boolean isQuantumUsed(final JobQueue queue) {
        return quantumUsed == queue.getQuantum();
    }

    public void completeJob(final JobQueue queue) {
        System.out.println(getJobMessage("Completed"));
        state = JobState.COMPLETED;
        queue.jobs.removeFirst();
    }

    public void demoteJob(final JobQueue queue, final JobQueue nextQueue) {
        System.out.println(getJobMessage("Allotment expired (" + queue.getAllotment() + "ms) - moved to the back of queue #" + (queue.getQueueNumber() + 1)));

        allotmentUsed = 0;
        quantumUsed = 0;
        state = JobState.READY;

        nextQueue.jobs.addLast(this);
        queue.jobs.removeFirst();
    }

    public void rotateJob(final JobQueue queue) {
        System.out.println(getJobMessage("Quantum expired (" + queue.getQuantum() + "ms) - moved to the back of queue #" + queue.getQueueNumber()));

        quantumUsed = 0;
        state = JobState.READY;

        queue.jobs.removeFirst();
        queue.jobs.addLast(this);
    }

    public void process() {
        progress++;
        quantumUsed++;
        allotmentUsed++;
    }

    public void block(IO io) {
        state = JobState.BLOCKED;
        waitingTime += io.getEndTime() - io.getStartTime();
    }

    public String getJobStateColour() {
        String colour;
        switch (state) {
            case RUNNING:
                colour = TextColour.PURPLE;
                break;
            case COMPLETED:
                colour = TextColour.GREEN;
                break;
            case READY:
                colour = TextColour.YELLOW;
                break;
            default:
                colour = TextColour.RED;
                break;
        }

        return colour + state.name() + TextColour.RESET;
    }

    public String getProgressPercentage() {
        return new DecimalFormat("#.##").format(((double)progress / (endTime - arrivalTime)) * 100) + "%";
    }

    public String getJobMessage(final String message) {
        return "  -> " + TextColour.GREEN + "Job:" + TextColour.RESET + " [" + pid + "] " + "(progress: " + 
        TextColour.CYAN + getProgressPercentage() + TextColour.RESET + ") " + message;
    }

    public int getArrivalTime() { return arrivalTime; }
    public int getEndTime() { return endTime; }

    public int getTurnaroundTime() { return turnaroundTime; }
    public int getResponseTime() { return responseTime; }
    public int getWaitingTime() { return waitingTime; }

    public void setTurnaroundTime(final int turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public void setResponseTime(final int responseTime) { this.responseTime = responseTime; }

    public int getProgress() { return progress; }
    public int getAllotmentUsed() { return allotmentUsed; }
    public int getQuantumUsed() { return quantumUsed; }

    public void setAllotmentUsed(final int allotmentUsed) { this.allotmentUsed = allotmentUsed; }
    public void setQuantumUsed(final int quantumUsed) { this.quantumUsed = quantumUsed; }

    public String getPID() { return pid; }

    public JobState getState() { return state; }
    public void setState(final JobState state) { this.state = state; }
}
