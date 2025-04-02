import java.util.*;

public class MLFQ {
    private JobQueue[] jobQueues;
    private int priorityBoost = -1;
    private Scanner scanner;

    public MLFQ() {
        this.scanner = new Scanner(System.in);
        setupMLFQ();
        showConfigSummary();
        run();
    }

    private void run() {
        System.out.println("\nConfiguration Complete! Starting simulation...");
        System.out.println("\n==========================================================\n");
    }

    private void showConfigSummary() {
        System.out.println("\n==========================================================\r\n" +
                        "MULTI-LEVEL FEEDBACK QUEUE SUMMARY  \r\n" +
                        "==========================================================");

        System.out.println("\nPriority boost " + (priorityBoost == -1 ? "has been disabled." : "has been set to every " + priorityBoost + " seconds."));

        System.out.println("\nJOB QUEUES (priority queues):\n");
        for (int i = 0; i < jobQueues.length; i++) {
            JobQueue queue = jobQueues[i];
            System.out.print("Job Queue " + i + (i == 0 ? " (Highest Priority)" : i == jobQueues.length - 1 ? " (Lowest Priority)" : "") + " -> ");
            System.out.print("Allotment: " + queue.getAllotment() + " | Quantum: " + queue.getQuantum() + "\n");
        }

        System.out.print("\nAre you happy to continue with this configuration and run the simulation? (yes/no) ");
        String acceptConfig = scanner.next();

        if (!acceptConfig.equals("yes")) {
            setupMLFQ();
        }
    }

    private void setupMLFQ() {
        System.out.println("==========================================================\r\n" +
                        "MULTI-LEVEL FEEDBACK QUEUE SCHEDULER  \r\n" +
                        "==========================================================");

        System.out.println("\nSetup Phase: Configure the MLFQ Scheduler");

        System.out.print("\nEnable priority boosting? (yes/no) ");
        String enablePriorityBoost = scanner.next();

        if (enablePriorityBoost.equals("yes")) {
            System.out.print("Enter priority boost interval (ms): ");
            priorityBoost = scanner.nextInt();
        }

        System.out.print("Enter the number of priority levels (queues): ");
        int numJobQueues = scanner.nextInt();
        jobQueues = new JobQueue[numJobQueues];

        System.out.println("\nConfiguring job queues (from highest priority to lowest):");
        for (int i = 0; i < numJobQueues; i++) {
            setupJobQueue(i);
        }
    }

    private void setupJobQueue(int queueNumber) {
        System.out.println("\nJob Queue " + queueNumber + (queueNumber == 0 ? " (Highest Priority)" : queueNumber == jobQueues.length - 1 ? " (Lowest Priority)" : ""));

        System.out.print("- Enter time quantum (ms): ");
        int quantum = scanner.nextInt();

        System.out.print("- Enter allotment (number of quanta before demotion): ");
        int allotment = scanner.nextInt();

        jobQueues[queueNumber] = new JobQueue(allotment, quantum);
    }
}