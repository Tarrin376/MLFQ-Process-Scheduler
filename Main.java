import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MLFQBuilder builder = new MLFQBuilder();

        System.out.println("\n==========================================================\r\n" + TextColour.GREEN +
                        "           MULTI-LEVEL FEEDBACK QUEUE SCHEDULER  \r\n" + TextColour.RESET +
                        "==========================================================");

        System.out.print("\nEnter priority boost interval (ms): ");

        int priorityBoost = scanner.nextInt();
        builder.setPriorityBoost(priorityBoost);

        System.out.print("Enter the number of priority levels (job queues): ");
        int numJobQueues = scanner.nextInt();

        for (int i = 0; i < numJobQueues; i++) {
            System.out.println("\nJob Queue " + i + (i == 0 ? " (Highest Priority)" : i == numJobQueues - 1 ? " (Lowest Priority)" : ""));

            System.out.print("- Enter time quantum (ms): ");
            int quantum = scanner.nextInt();

            System.out.print("- Enter allotment (number of quanta before demotion): ");
            int allotment = scanner.nextInt();
            builder.addJobQueue(allotment, quantum);
        }

        try {
            MLFQ scheduler = builder.build();
            scheduler.run();
        } catch (InterruptedException ie) {
            System.out.println("Main thread was interrupted.");
        }
    }
}
