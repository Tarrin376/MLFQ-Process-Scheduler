import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MLFQ {
    private final LinkedList<JobQueue> jobQueues;
    private final Map<Integer, List<Job>> readyJobs;
    private final Map<String, Job> jobPIDs;
    private final CommandHandler commandHandler;
    private final int priorityBoost;

    private AtomicBoolean paused;
    private AtomicBoolean running;
    private int timer;

    public MLFQ(final MLFQBuilder builder) {
        jobQueues = builder.jobQueues;
        priorityBoost = builder.priorityBoost;
        
        readyJobs = new HashMap<>();
        jobPIDs = new HashMap<>();
        commandHandler = new CommandHandler(this);

        paused = new AtomicBoolean(false);
        running = new AtomicBoolean(true);
    }

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
            runIteration();

            timer++;
            System.out.println("(Done) Time elapsed: " + timer + "ms");
            Thread.sleep(1000);
        }
    }

    private void runIteration() {
        if (priorityBoost > 0 && timer % priorityBoost == 0) {
            triggerPriorityBoost();
        }

        addReadyJobsToQueue();
        checkBlockedJobs();

        JobQueue firstQueue = findFirstUnemptyQueue();
        if (firstQueue == null) {
            System.out.println("CPU is idle...");
            return;
        }

        processJob(firstQueue.jobs.getFirst(), firstQueue);
    }

    private void processJob(Job job, JobQueue queue) {
        if (job.getState() == JobState.READY) {
            job.setState(JobState.RUNNING);
        }

        System.out.println("pid: " + job.getPID() + " is running!");
        job.updateJobProgress();

        if (job.getProgress() == job.getEndTime() - job.getStartTime() + 1) {
            System.out.println("pid: " + job.getPID() + " has now finished!");
            queue.jobs.removeFirst();
        } else if (job.getAllotmentUsed() == queue.getAllotment() && queue.getQueueNumber() < jobQueues.size()) {
            System.out.println("pid: " + job.getPID() + " reached the allotment limit of " + queue.getAllotment() + "ms on queue No. " + 
            queue.getQueueNumber() + ". Moving down to queue No. " + (queue.getQueueNumber() + 1) + "...");

            job.setAllotmentUsed(0);
            job.setQuantumUsed(0);

            JobQueue nextJobQueue = jobQueues.get(queue.getQueueNumber());
            nextJobQueue.jobs.addLast(job);
            queue.jobs.removeFirst();
        } else if (job.getQuantumUsed() == queue.getQuantum()) {
            System.out.println("pid: " + job.getPID() + " reached the quantum limit of " + queue.getQuantum() + "ms on queue No. " + 
            queue.getQueueNumber() + ". Moving to the back of the queue...");

            job.setQuantumUsed(0);
            queue.jobs.removeFirst();
            queue.jobs.addLast(job);
        }
    }

    private JobQueue findFirstUnemptyQueue() {
        for (JobQueue queue : jobQueues) {
            while (queue.jobs.size() > 0) {
                Job job = queue.jobs.getFirst();
                IO io = job.ioQueue.size() > 0 ? job.ioQueue.peek() : null;

                if (io == null || io.getStartTime() < timer) {
                    return queue;
                }

                System.out.println("pid: " + job.getPID() + " -> IO: " + io.getName() + " has started running!");
                job.setState(JobState.BLOCKED);
                
                List<Job> blockedJobs = queue.blockedJobs.getOrDefault(io.getEndTime() + 1, new ArrayList<>());
                blockedJobs.add(job);

                queue.blockedJobs.put(io.getEndTime() + 1, blockedJobs);
                queue.jobs.removeFirst();
            }
        }

        return null;
    }

    private void checkBlockedJobs() {
        for (JobQueue queue : jobQueues) {
            List<Job> blockedJobs = queue.blockedJobs.getOrDefault(timer, new ArrayList<>());
            for (Job job : blockedJobs) {
                IO finishedIO = job.ioQueue.poll();
                IO nextIO = job.ioQueue.peek();
                System.out.println("pid: " + job.getPID() + " -> IO: " + finishedIO.getName() + " has finished at " + timer + "ms!");

                if (nextIO == null || nextIO.getStartTime() > timer) {
                    job.setState(JobState.READY);
                    System.out.println("pid: " + job.getPID() + " is now unblocked!");
                    queue.jobs.addLast(job);
                } else {
                    System.out.println("pid: " + job.getPID() + " -> IO: " + nextIO.getName() + " has started running at " + timer + "ms!");
                    List<Job> blockedJobsAtEnd = queue.blockedJobs.getOrDefault(nextIO.getEndTime() + 1, new ArrayList<>());

                    blockedJobsAtEnd.add(job);
                    queue.blockedJobs.put(nextIO.getEndTime() + 1, blockedJobsAtEnd);
                }
            }

            queue.blockedJobs.remove(timer);
        }
    }

    private void addReadyJobsToQueue() {
        List<Job> jobs = readyJobs.getOrDefault(timer, new ArrayList<>());
        for (Job job : jobs) {
            jobQueues.get(0).jobs.addLast(job);
        }

        readyJobs.remove(timer);
    }

    private void triggerPriorityBoost() {
        // To be implemented... move all jobs to the first queue
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
        if (arrivalTime < timer) {
            System.out.println("Arrival time must be on or after " + timer);
            return false;
        } else if (arrivalTime >= endTime) {
            System.out.println("End time must be greater than the arrival time.");
            return false;
        } 

        return true;
    }

    public void setRunning(boolean running) { this.running.set(running); }
    public boolean getRunning() { return running.get(); }

    public void setPaused(boolean paused) { this.paused.set(paused); }
    public boolean getPaused() { return paused.get(); }

    public String getJob(final String pid) {
        return jobPIDs.containsKey(pid) ? jobPIDs.get(pid).toString() : "Job with pid: " + pid + " was not found.";
    }
}