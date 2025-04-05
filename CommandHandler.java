import java.util.Scanner;

public class CommandHandler implements Runnable {
    private Scanner scanner;
    private MLFQ scheduler;

    public CommandHandler(final MLFQ scheduler) {
        this.scanner = new Scanner(System.in);
        this.scheduler = scheduler;
    }

    public void run() {
        while (true) {
            if (scheduler.getPaused()) {
                System.out.print("> ");
            }

            while (scheduler.getIsExecutingIteration()) {
                continue;
            }

            String command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (parts.length > 0) {
                if (scheduler.getPaused()) executeCommand(parts);
                else if (parts[0].equals("-p")) scheduler.setPaused(true);
            }
        }
    }

    public void executeCommand(final String[] command) {
        switch (command[0].toLowerCase()) {
            case "help":
                help();
                break;
            case "add-job":
                scheduler.addJob(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));
                break;
            case "add-io":
                scheduler.addIO(command[1], command[2], Integer.parseInt(command[3]), Integer.parseInt(command[4]));
                break;
            case "show-queues":
                break;
            case "show-job":
                System.out.println(scheduler.getJob(command[1]));
                break;
            case "resume":
                scheduler.setPaused(false);
                break;
            case "exit":
                scheduler.setRunning(false);
                break;
            default:
                System.out.println("Not a valid command, type 'help' for commands.");
                break;
        }
    }

    public void help() {
        System.out.println("\n==========================================================\r\n" + //
                        "MULTI-LEVEL FEEDBACK QUEUE (MLFQ) SCHEDULER COMMANDS\r\n" + //
                        "==========================================================\r\n" + //
                        "\r\n" + //
                        "Process Management:\r\n" + //
                        "  add-job <pid> <arrival_time> <end_time>\r\n" + //
                        "      - Adds a new job to the system.\r\n" + //
                        "      - Example: add-job P1 0 20\r\n" + //
                        "\r\n" + //
                        "  add-io <io_name> <pid> <arrival_time> <end_time>\r\n" + //
                        "      - Adds an I/O event for a job.\r\n" + //
                        "      - Example: add-io upload-file P1 5 10\r\n" + //
                        "\r\n" + //
                        "System Information:\r\n" + //
                        "  show-queues\r\n" + //
                        "      - Displays the current state of all job queues.\r\n" + //
                        "\r\n" + //
                        "  show-job <pid>\r\n" + //
                        "      - Displays details of a specific job.\r\n" + //
                        "      - Example: show-job P1\r\n" + //
                        "\r\n" + //
                        "Simulation Controls:\r\n" + //
                        "  -p\r\n" + //
                        "      - Pauses the simulation (only command that can be used during runtime).\r\n" + //
                        "\r\n" + //
                        "  resume\r\n" + //
                        "      - Resumes the simulation after a pause.\r\n" + //
                        "\r\n" + //
                        "  exit\r\n" + //
                        "      - Stops the simulation and exits the program.");
    }
}
