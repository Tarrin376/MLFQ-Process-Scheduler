import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        MLFQBuilder builder = new MLFQBuilder();
        System.out.println("\n=======================================================================================\n" + TextColour.GREEN +
                        "                         MULTI-LEVEL FEEDBACK QUEUE SCHEDULER\n" + TextColour.RESET +
                        "=======================================================================================");

        builder.setPriorityBoost(getPriorityBoost());
        int numJobQueues = getNumJobQueues();

        for (int i = 0; i < numJobQueues; i++) {
            System.out.println(TextColour.GREEN + "\nJob Queue " + i + (i == 0 ? " (Highest Priority)" : i == numJobQueues - 1 ? " (Lowest Priority)" : "") + TextColour.RESET);
            int quantum = getQuantum();
            int allotment = getAllotment(quantum);
            builder.addJobQueue(allotment, quantum);
        }

        try {
            MLFQ scheduler = builder.build();
            scheduler.run();
        } catch (InterruptedException ie) {
            System.out.println("Main thread was interrupted.");
        }
    }

    private static int getQuantum() {
        while (true) {
            System.out.print("\n- Enter time quantum (ms): ");
            String quantum = scanner.nextLine();

            if (InputUtils.isNonNegativeInteger(quantum)) {
                return Integer.parseInt(quantum);
            }
        }
    }

    private static int getAllotment(final int quantum) {
        while (true) {
            System.out.print("\n- Enter allotment (number of quanta before demotion): ");
            String allotment = scanner.nextLine();

            if (!InputUtils.isNonNegativeInteger(allotment)) {
                continue;
            }

            if (Integer.parseInt(allotment) < quantum) {
                System.out.println(TextColour.getErrorMessage("Invalid input: Allotment must be greater than or equal to quantum."));
                continue;
            }

            return Integer.parseInt(allotment);
        }
    }

    private static int getPriorityBoost() {
        while (true) {
            System.out.print("\nEnter priority boost interval (ms): ");
            String priorityBoost = scanner.nextLine();

            if (InputUtils.isNonNegativeInteger(priorityBoost)) {
                return Integer.parseInt(priorityBoost);
            }
        }
    }

    private static int getNumJobQueues() {
        while (true) {
            System.out.print("\nEnter the number of priority levels (job queues): ");
            String numJobQueues = scanner.nextLine();

            if (InputUtils.isNonNegativeInteger(numJobQueues)) {
                return Integer.parseInt(numJobQueues);
            }
        }
    }
}
