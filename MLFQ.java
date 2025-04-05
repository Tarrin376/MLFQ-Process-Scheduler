import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MLFQ {
    private final List<JobQueue> jobQueues;
    private final ConcurrentHashMap<Integer, List<Job>> blockedJobs;
    private final ConcurrentHashMap<Integer, List<Job>> readyJobs;
    private final ConcurrentHashMap<String, Job> pendingJobs;
    private volatile boolean isExecutingIteration = false;

    private final CommandHandler commandHandler;
    private final int priorityBoost;
    private long timer;

    public MLFQ(final MLFQBuilder builder) throws InterruptedException {
        this.jobQueues = builder.jobQueues;
        this.priorityBoost = builder.priorityBoost;
        
        this.blockedJobs = new ConcurrentHashMap<>();
        this.readyJobs = new ConcurrentHashMap<>();
        this.pendingJobs = new ConcurrentHashMap<>();
        this.commandHandler = new CommandHandler(this);
        run();
    }

    public boolean getIsExecutingIteration() {
        return isExecutingIteration;
    }

    private void run() throws InterruptedException {
        System.out.println("\nSimulation started. Type 'help' for commands.");
        System.out.println("\n==========================================================");

        Thread commandThread = new Thread(() -> commandHandler.listen());
        commandThread.setDaemon(true);
        commandThread.start();
        
        while (commandHandler.getRunning()) {
            while (!commandHandler.getPaused() && commandHandler.getRunning()) {
                isExecutingIteration = true;

                System.out.println("\n(Executing...)");
                executeIteration();
                System.out.println("(Done) Time elapsed: " + timer + "s");

                isExecutingIteration = false;

                System.out.print("> ");
                Thread.sleep(2000);
                timer++;
            }
        }
    }

    private void executeIteration() throws InterruptedException {
        Thread.sleep(500);
    }

    public synchronized void addJob(final String pid, final int arrivalTime, final int endTime) {
        if (arrivalTime < timer) {
            System.out.println("Arrival time must no less than " + timer);
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
            System.out.println("Arrival time must no less than " + timer);
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

    public static class MLFQBuilder {
        private final List<JobQueue> jobQueues;
        private int priorityBoost;

        public MLFQBuilder() {
            this.jobQueues = new ArrayList<>();
            this.priorityBoost = -1;
        }

        public MLFQBuilder setPriorityBoost(final int priorityBoost) {
            this.priorityBoost = priorityBoost;
            return this;
        }

        public MLFQBuilder addJobQueue(final int allotment, final int quantum) {
            this.jobQueues.add(new JobQueue(allotment, quantum));
            return this;
        }

        public MLFQ build() throws InterruptedException {
            return new MLFQ(this);
        }
    }
}