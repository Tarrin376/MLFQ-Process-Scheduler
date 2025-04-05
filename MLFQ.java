import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MLFQ {
    private final List<JobQueue> jobQueues;
    private final ConcurrentHashMap<Integer, List<Job>> blockedJobs;
    private final ConcurrentHashMap<Integer, List<Job>> readyJobs;
    private final ConcurrentHashMap<String, Job> pendingJobs;
    private final int priorityBoost;

    private volatile boolean isExecutingIteration;
    private volatile boolean running;
    private volatile boolean paused;
    private volatile long timer;

    public MLFQ(final MLFQBuilder builder) throws InterruptedException {
        this.jobQueues = builder.jobQueues;
        this.priorityBoost = builder.priorityBoost;
        
        this.blockedJobs = new ConcurrentHashMap<>();
        this.readyJobs = new ConcurrentHashMap<>();
        this.pendingJobs = new ConcurrentHashMap<>();
    }

    public void setRunning(final boolean running) { this.running = running; }
    public void setPaused(final boolean paused) { this.paused = paused; }

    public boolean getRunning() { return running; }
    public boolean getPaused() { return paused; }
    public boolean getIsExecutingIteration() { return isExecutingIteration; }

    public void run() throws InterruptedException {
        System.out.println("\nSimulation started. Type 'help' for commands.");
        System.out.println("\n==========================================================");

        Thread commandThread = new Thread(new CommandHandler(this));
        commandThread.setDaemon(true);
        commandThread.start();
        running = true;
        
        while (running) {
            while (!paused && running) {
                isExecutingIteration = true;
                System.out.println("\n(Executing...)");
                executeIteration();

                timer++;
                System.out.println("(Done) Time elapsed: " + timer + "ms");

                isExecutingIteration = false;
                System.out.print("> ");
                Thread.sleep(2000);
            }
        }
    }

    private void executeIteration() throws InterruptedException {
        Thread.sleep(500);
    }

    public synchronized void addJob(final String pid, final int arrivalTime, final int endTime) {
        if (arrivalTime < timer) {
            System.out.println("Arrival time is too late.");
            return;
        } else if (pendingJobs.containsKey(pid)) {
            System.out.println("The pid: " + pid + " is already in use by another job, please use another pid.");
            return;
        }

        Job job = new Job(pid, arrivalTime, endTime);
        List<Job> jobsAtTimestamp = readyJobs.getOrDefault(arrivalTime, new ArrayList<>());

        jobsAtTimestamp.add(job);
        readyJobs.put(arrivalTime, jobsAtTimestamp);
        pendingJobs.put(pid, job);
    }

    public synchronized void addIO(final String ioName, final String pid, final int arrivalTime, final int endTime) {
        if (arrivalTime < timer) {
            System.out.println("Arrival time is too late.");
            return;
        } else if (!pendingJobs.containsKey(pid)) {
            System.out.println("Job with pid: " + pid + " does not exist.");
            return;
        }

        Job job = pendingJobs.get(pid);
        job.ioQueue.offer(new IO(ioName, arrivalTime, endTime));
    }

    public String getJob(final String pid) {
        return pendingJobs.containsKey(pid) ? pendingJobs.get(pid).toString() : "Job with pid: " + pid + " was not found.";
    }
}