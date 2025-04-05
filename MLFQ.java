import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MLFQ {
    private final List<JobQueue> jobQueues;
    private final Map<Integer, List<Job>> blockedJobs;
    private final Map<Integer, List<Job>> readyJobs;
    private final Map<String, Job> jobPIDs;
    private final CommandHandler commandHandler;
    private final int priorityBoost;

    private AtomicBoolean paused;
    private AtomicBoolean running;
    private long timer;

    public MLFQ(final MLFQBuilder builder) {
        jobQueues = builder.jobQueues;
        priorityBoost = builder.priorityBoost;
        
        blockedJobs = new HashMap<>();
        readyJobs = new HashMap<>();
        jobPIDs = new HashMap<>();
        commandHandler = new CommandHandler(this);

        paused = new AtomicBoolean(false);
        running = new AtomicBoolean(true);
    }

    public void setRunning(boolean running) { this.running.set(running); }
    public boolean getRunning() { return running.get(); }

    public void setPaused(boolean paused) { this.paused.set(paused); }
    public boolean getPaused() { return paused.get(); }

    public void run() throws InterruptedException {
        System.out.println("\nSimulation started. Type 'help' for commands.");
        System.out.println("\n==========================================================");

        Thread enterPauseListener = new Thread(new EnterPauseListener(this));
        enterPauseListener.setDaemon(true);
        enterPauseListener.start();

        running.set(true);
        while (running.get()) {
            if (paused.get()) {
                commandHandler.run();
                continue;
            }

            System.out.println("\n(Executing...)");
            executeIteration();

            timer++;
            System.out.println("(Done) Time elapsed: " + timer + "ms");
            Thread.sleep(1000);
        }
    }

    private void executeIteration() {
        // logic
    }

    public void addJob(final String pid, final int arrivalTime, final int endTime) {
        if (!validTimeWindow(arrivalTime, endTime)) {
            return;
        } else if (jobPIDs.containsKey(pid)) {
            System.out.println("The pid: " + pid + " is already in use by another job, please use another pid.");
            return;
        }

        Job job = new Job(pid, arrivalTime, endTime);
        List<Job> jobsAtTimestamp = readyJobs.getOrDefault(arrivalTime, new ArrayList<>());
        jobsAtTimestamp.add(job);

        readyJobs.put(arrivalTime, jobsAtTimestamp);
        jobPIDs.put(pid, job);
    }

    public void addIO(final String ioName, final String pid, final int arrivalTime, final int endTime) {
        if (!validTimeWindow(arrivalTime, endTime)) {
            return;
        } else if (!jobPIDs.containsKey(pid)) {
            System.out.println("Job with pid: " + pid + " does not exist.");
            return;
        }

        Job job = jobPIDs.get(pid);
        job.ioQueue.offer(new IO(ioName, arrivalTime, endTime));
    }

    private boolean validTimeWindow(final int arrivalTime, final int endTime) {
        if (arrivalTime < 0) {
            System.out.println("Arrival time must be a non-negative integer.");
            return false;
        } if (arrivalTime >= endTime) {
            System.out.println("End time must be greater than the arrival time.");
            return false;
        } 

        return true;
    }

    public String getJob(final String pid) {
        return jobPIDs.containsKey(pid) ? jobPIDs.get(pid).toString() : "Job with pid: " + pid + " was not found.";
    }
}