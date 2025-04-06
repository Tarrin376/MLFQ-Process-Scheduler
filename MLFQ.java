import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;

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
        priorityBoost = builder.priorityBoost;
        jobQueues = builder.jobQueues;
        
        readyJobs = new HashMap<>();
        jobPIDs = new HashMap<>();
        commandHandler = new CommandHandler(this);

        paused = new AtomicBoolean(false);
        running = new AtomicBoolean(true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=================================== MLFQ Queues ===================================");

        for (JobQueue queue : jobQueues) {
            sb.append(queue);
        }

        sb.append("===================================================================================");
        return sb.toString();
    }

    public void run() throws InterruptedException {
        System.out.println("\nSimulation started. Type 'help' for commands.");
        System.out.println("\n===================================================================================");

        Thread enterPauseListener = new Thread(new EnterPauseListener(this));
        enterPauseListener.setDaemon(true);
        enterPauseListener.start();
        running.set(true);

        while (running.get()) {
            if (paused.get()) {
                commandHandler.run();
                continue;
            }

            runIteration();
            timer++;
            Thread.sleep(1000);
        }
    }

    private void runIteration() {
        System.out.println("\n[" + AnsiColour.GREEN + "Time:" + AnsiColour.RESET + " " + timer + "ms]");
        if (priorityBoost > 0 && timer % priorityBoost == 0) {
            triggerPriorityBoost();
        }

        addReadyJobsToQueue();
        checkBlockedJobs();

        JobQueue firstQueue = findFirstUnemptyQueue();
        if (firstQueue == null) {
            System.out.println("  -> No job scheduled - CPU is idle.");
        } else {
            processJob(firstQueue.jobs.getFirst(), firstQueue);
        }

        System.out.println("\n-----------------------------------------------------------------------------------");
    }

    private void processJob(final Job job, final JobQueue queue) {
        if (job.getState() == JobState.READY) {
            job.setState(JobState.RUNNING);
        }

        System.out.println(job.getJobMessage("Running on queue #" + queue.getQueueNumber()));
        job.updateJobProgress();

        if (job.getProgress() == job.getEndTime() - job.getStartTime()) {
            System.out.println(job.getJobMessage("Completed"));
            queue.jobs.removeFirst();
            jobPIDs.remove(job.getPID());
        } else if (job.getAllotmentUsed() == queue.getAllotment() && queue.getQueueNumber() < jobQueues.size()) {
            System.out.println(job.getJobMessage("Allotment expired (" + queue.getAllotment() + "ms). Moved to back of queue #" + (queue.getQueueNumber() + 1)));
            job.setState(JobState.READY);
            job.setAllotmentUsed(0);
            job.setQuantumUsed(0);

            JobQueue nextJobQueue = jobQueues.get(queue.getQueueNumber());
            nextJobQueue.jobs.addLast(job);
            queue.jobs.removeFirst();
        } else if (job.getQuantumUsed() == queue.getQuantum()) {
            System.out.println(job.getJobMessage("Quantum expired (" + queue.getQuantum() + "ms). Moved to back of queue #" + queue.getQueueNumber()));
            job.setState(JobState.READY);
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

                if (io == null || io.getStartTime() != timer) {
                    return queue;
                }
                
                System.out.println(job.getJobMessage("Started IO \"" + io.getName() + "\""));
                job.setState(JobState.BLOCKED);
                
                List<Job> blockedJobs = queue.blockedJobs.getOrDefault(io.getEndTime(), new ArrayList<>());
                blockedJobs.add(job);

                queue.blockedJobs.put(io.getEndTime(), blockedJobs);
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
                System.out.println(job.getJobMessage("Completed IO \"" + finishedIO.getName() + "\""));

                if (nextIO == null || nextIO.getStartTime() > timer) {
                    job.setState(JobState.READY);
                    System.out.println(job.getJobMessage("Unblocked and re-entered queue #" + queue.getQueueNumber()));
                    queue.jobs.addLast(job);
                } else {
                    System.out.println(job.getJobMessage("Started IO \"" + nextIO.getName() + "\""));
                    int endTime = timer + (nextIO.getEndTime() - nextIO.getStartTime());
                    List<Job> blockedJobsAtEnd = queue.blockedJobs.getOrDefault(endTime, new ArrayList<>());

                    blockedJobsAtEnd.add(job);
                    queue.blockedJobs.put(endTime, blockedJobsAtEnd);
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

    public String getJobOutput(final String pid) {
        return jobPIDs.containsKey(pid) ? jobPIDs.get(pid).toString() : "Job with pid: " + pid + " was not found.";
    }

    public void setRunning(final boolean running) { this.running.set(running); }
    public boolean getRunning() { return running.get(); }

    public void setPaused(final boolean paused) { this.paused.set(paused); }
    public boolean getPaused() { return paused.get(); }
}