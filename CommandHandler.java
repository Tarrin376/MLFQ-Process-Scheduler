import java.util.Scanner;

public class CommandHandler {
    private Scanner scanner;
    private MLFQ scheduler;

    public CommandHandler(MLFQ scheduler) {
        this.scanner = new Scanner(System.in);
        this.scheduler = scheduler;
    }

    public void listen() {
        while (true) {
            if (scheduler.getPaused()) {
                System.out.print("\n> ");
            }

            String command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (parts.length == 0) {
                continue;
            }

            switch (parts[0].toLowerCase()) {
                case "help":
                    help();
                    break;
                case "-p":
                    scheduler.pause();
                    break;
                case "-r":
                    scheduler.resume();
                    break;
                case "-e":
                    scheduler.exit();
                    break;
            }
        }
    }

    public void executePauseTimeCommands() {

    }

    public void help() {
        System.out.println("\n==========================================================\r\n" + //
                        "MULTI-LEVEL FEEDBACK QUEUE (MLFQ) SCHEDULER COMMANDS\r\n" + //
                        "==========================================================\r\n" + //
                        "\r\n" + //
                        "Process Management:\r\n" + //
                        "  add-process <pid> <arrivalTime> <burstTime>\r\n" + //
                        "      - Adds a new process to the system.\r\n" + //
                        "      - Example: add-process P1 0 20\r\n" + //
                        "\r\n" + //
                        "  add-io <io_name> <pid> <arrivalTime> <duration>\r\n" + //
                        "      - Adds an I/O event for a process.\r\n" + //
                        "      - Example: add-io upload-file P1 5 10\r\n" + //
                        "\r\n" + //
                        "System Information:\r\n" + //
                        "  show-queues\r\n" + //
                        "      - Displays the current state of all job queues.\r\n" + //
                        "\r\n" + //
                        "  show-process <pid>\r\n" + //
                        "      - Displays details of a specific process.\r\n" + //
                        "      - Example: show-process P1\r\n" + //
                        "\r\n" + //
                        "Simulation Controls:\r\n" + //
                        "  -p\r\n" + //
                        "      - Pauses the simulation.\r\n" + //
                        "\r\n" + //
                        "  -r\r\n" + //
                        "      - Resumes the simulation after a pause.\r\n" + //
                        "\r\n" + //
                        "  -e\r\n" + //
                        "      - Stops the simulation and exits the program.");
    }
}
