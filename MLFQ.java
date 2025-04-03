import java.util.*;

public class MLFQ {
    private CommandHandler commandHandler;
    private List<JobQueue> jobQueues;

    private volatile boolean running;
    private volatile boolean paused;

    private int priorityBoost;
    private int timer;

    public MLFQ(MLFQBuilder builder) {
        this.jobQueues = builder.jobQueues;
        this.priorityBoost = builder.priorityBoost;
        this.commandHandler = new CommandHandler(this);
    }

    public void run() throws InterruptedException {
        System.out.println("\nSimulation started. Type 'help' for commands.");
        System.out.println("\n==========================================================");
        System.out.print("\n> ");

        Thread commandThread = new Thread(() -> commandHandler.listen());
        commandThread.setDaemon(true);
        commandThread.start();
        running = true;
        
        while (running) {
            while (!paused && running) {
                System.out.print("\r                          \r" + timer + "\n> ");
                executeIteration();
                Thread.sleep(1000);
                timer++;
            }
        }
    }

    private void executeIteration() {
        // Execute 1 iteration of the MLFQ algo
    }

    public boolean getPaused() {
        return paused;
    }

    public void pause() { paused = true; }
    public void resume() { paused = false; }
    public void exit() { running = false; }

    public static class MLFQBuilder {
        private List<JobQueue> jobQueues;
        private int priorityBoost;

        public MLFQBuilder() {
            this.jobQueues = new ArrayList<>();
            this.priorityBoost = -1;
        }

        public MLFQBuilder setPriorityBoost(int priorityBoost) {
            this.priorityBoost = priorityBoost;
            return this;
        }

        public MLFQBuilder addJobQueue(int allotment, int quantum) {
            this.jobQueues.add(new JobQueue(allotment, quantum));
            return this;
        }

        public MLFQ build() {
            return new MLFQ(this);
        }
    }
}