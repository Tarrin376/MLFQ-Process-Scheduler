import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;

public class MLFQ {
    private final LinkedList<JobQueue> jobQueues;
    private final Map<Integer, List<Job>> readyJobs;
    private final Map<String, Job> jobPIDs;

    private final CommandHandler commandHandler;
    private final int priorityBoost;

    private volatile AtomicBoolean paused;
    private volatile AtomicBoolean running;

    private int completedJobs;
    private int startedJobs;
    private int activeTime;
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
        sb.append("\n===================================== MLFQ Queues =====================================");

        for (JobQueue queue : jobQueues) {
            sb.append(queue);
        }

        sb.append("=======================================================================================");
        return sb.toString();
    }

    public void run() throws InterruptedException {
        System.out.println(TextColour.PURPLE + "\n[Simulation started. Type 'help' for commands.]" + TextColour.RESET);
        System.out.println("\n=======================================================================================");

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
        System.out.println("\n[" + TextColour.GREEN + "Time:" + TextColour.RESET + " " + timer + "ms]");
        if (priorityBoost > 0 && timer > 0 && timer % priorityBoost == 0) {
            triggerPriorityBoost();
        }

        addReadyJobsToQueue();
        checkBlockedJobs();

        JobQueue firstQueue = findFirstUnemptyQueue();
        if (firstQueue != null) {
            processJob(firstQueue.jobs.getFirst(), firstQueue);
            activeTime++;
        } else {
            System.out.println("  -> No job scheduled - CPU is idle.");
        }

        System.out.println("\n---------------------------------------------------------------------------------------");
    }

    private void processJob(final Job job, final JobQueue queue) {
        if (job.getState() == JobState.READY) {
            job.setState(JobState.RUNNING);
        }

        if (job.getProgress() == 0) {
            job.setResponseTime(timer - job.getArrivalTime());
            startedJobs++;
        }

        System.out.println(job.getJobMessage("Running on queue #" + queue.getQueueNumber()));
        job.process();

        if (job.hasFinished()) {
            job.setTurnaroundTime(timer - job.getArrivalTime() + 1);
            job.completeJob(queue);
            completedJobs++;
        } else if (job.isAllotmentUsed(queue) && queue.getQueueNumber() < jobQueues.size()) {
            job.demoteJob(queue, jobQueues.get(queue.getQueueNumber()));
        } else if (job.isQuantumUsed(queue)) {
            job.rotateJob(queue);
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
                job.block(io);
                
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
                    
                    job.block(nextIO);
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
        JobQueue firstQueue = jobQueues.get(0);

        for (JobQueue queue : jobQueues) {
            int size = queue.jobs.size();
            for (int i = 0; i < size; i++) {
                Job job = queue.jobs.removeFirst();
                firstQueue.jobs.addLast(job);

                job.setQuantumUsed(0);
                job.setAllotmentUsed(0);
            }
        }

        System.out.println("  -> " + TextColour.PURPLE + "Priority boost triggered" + TextColour.RESET);
    }

    public void addJob(final String pid, final int arrivalTime, final int endTime) {
        if (jobPIDs.containsKey(pid)) {
            System.out.println(TextColour.getErrorMessage("The pid: " + pid + " is already in use by another job."));
            return;
        }
        
        if (!InputUtils.validTimeWindow(arrivalTime, endTime, timer)) {
            return;
        }

        Job job = new Job(pid, arrivalTime, endTime);
        List<Job> jobsAtTimestamp = readyJobs.getOrDefault(arrivalTime, new ArrayList<>());
        jobsAtTimestamp.add(job);

        readyJobs.put(arrivalTime, jobsAtTimestamp);
        jobPIDs.put(pid, job);

        System.out.println("Job with pid: " + pid + " has been added to the scheduler.");
    }

    public void addIO(final String ioName, final String pid, final int arrivalTime, final int endTime) {
        if (!jobPIDs.containsKey(pid)) {
            System.out.println("Job with pid: " + pid + " does not exist.");
            return;
        } 
        
        if (!InputUtils.validTimeWindow(arrivalTime, endTime, timer)) {
            return;
        } 

        Job job = jobPIDs.get(pid);
        if (arrivalTime < job.getArrivalTime() || endTime > job.getEndTime()) {
            System.out.println("IO: \"" + ioName + "\" must occur within the lifetime of job: " + pid + ".");
            return;
        }

        job.ioQueue.offer(new IO(ioName, arrivalTime, endTime));
        System.out.println("IO: \"" + ioName + "\" has been added to the IO queue of job: " + pid + ".");
    }

    public double getAvgTurnaroundTime() {
        if (completedJobs > 0) {
            return (double)jobPIDs
                .values()
                .stream()
                .map(x -> x.getTurnaroundTime())
                .reduce(0, (a, b) -> a + b) / completedJobs;
        }
        
        return 0;
    }

    public double getAvgResponseTime() {
        if (startedJobs > 0) {
            return (double)jobPIDs
                .values()
                .stream()
                .map(x -> x.getResponseTime())
                .reduce(0, (a, b) -> a + b) / startedJobs;
        }

        return 0;
    }

    public double getAvgWaitingTime() {
        if (completedJobs > 0) {
            return (double)jobPIDs
                .values()
                .stream()
                .filter(x -> x.getState() == JobState.COMPLETED)
                .map(x -> x.getWaitingTime())
                .reduce(0, (a, b) -> a + b) / completedJobs;
        }

        return 0;
    }

    public double getCPUUtilization() {
        return timer == 0 ? 0 : (double)activeTime / timer;
    }

    public Optional<Job> getJobByPid(final String pid) {
        return Optional.of(jobPIDs.getOrDefault(pid, null));
    }

    public void setRunning(final boolean running) { this.running.set(running); }
    public boolean getRunning() { return running.get(); }

    public void setPaused(final boolean paused) { this.paused.set(paused); }
    public boolean getPaused() { return paused.get(); }
    public int getTimer() { return timer; }
}