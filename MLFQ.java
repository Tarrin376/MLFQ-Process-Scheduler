import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MLFQ {
    private final List<JobQueue> jobQueues;
    private final ConcurrentHashMap<Integer, List<Job>> blockedJobs;
    private final ConcurrentHashMap<Integer, List<Job>> readyJobs;
    private final ConcurrentHashMap<String, Job> jobPIDs;
    private final int priorityBoost;

    private AtomicBoolean isExecutingIteration;
    private AtomicBoolean running;
    private AtomicBoolean paused;
    private AtomicLong timer;

    public MLFQ(final MLFQBuilder builder) {
        this.jobQueues = builder.jobQueues;
        this.priorityBoost = builder.priorityBoost;
        
        this.blockedJobs = new ConcurrentHashMap<>();
        this.readyJobs = new ConcurrentHashMap<>();
        this.jobPIDs = new ConcurrentHashMap<>();

        this.isExecutingIteration = new AtomicBoolean(false);
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.timer = new AtomicLong(0);
    }

    public void setRunning(final boolean running) { this.running.set(running); }
    public void setPaused(final boolean paused) { this.paused.set(paused); }

    public boolean getRunning() { return running.get(); }
    public boolean getPaused() { return paused.get(); }
    public boolean isExecuting() { return isExecutingIteration.get(); }

    public void run() throws InterruptedException {
        System.out.println("\nSimulation started. Type 'help' for commands.");
        System.out.println("\n==========================================================");

        Thread commandThread = new Thread(new CommandHandler(this));
        commandThread.setDaemon(true);
        commandThread.start();
        running.set(true);
        
        while (running.get()) {
            while (!paused.get() && running.get()) {
                isExecutingIteration.set(true);
                System.out.println("\r                      \n(Executing...)");
                executeIteration();

                timer.set(timer.get() + 1);
                System.out.println("(Done) Time elapsed: " + timer + "ms");

                isExecutingIteration.set(false);
                System.out.print("> ");
                Thread.sleep(2000);
            }
        }
    }

    private void executeIteration() throws InterruptedException {
        Thread.sleep(500);
    }

    public synchronized void addJob(final String pid, final int arrivalTime, final int endTime) {
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

    public synchronized void addIO(final String ioName, final String pid, final int arrivalTime, final int endTime) {
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
        if (arrivalTime < timer.get()) {
            System.out.println("Arrival time is too late.");
            return false;
        } else if (arrivalTime >= endTime) {
            System.out.println("End time must be greater than the arrival time.");
            return false;
        } 

        return true;
    }

    public String getJob(final String pid) {
        return jobPIDs.containsKey(pid) ? jobPIDs.get(pid).toString() : "Job with pid: " + pid + " was not found.";
    }
}