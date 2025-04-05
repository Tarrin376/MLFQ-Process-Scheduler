import java.util.Scanner;

public class CommandHandler {
    private Scanner scanner;
    private MLFQ scheduler;

    public CommandHandler(final MLFQ scheduler) {
        this.scanner = new Scanner(System.in);
        this.scheduler = scheduler;
    }

    public void run() {
        while (scheduler.getPaused()) {
            System.out.print("\r> ");
            String command = scanner.nextLine();
            String[] parts = command.split(" ");
    
            if (parts.length > 0) {
                executeCommand(parts);
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
                System.out.println("Job with pid: " + command[1] + " has been added to the scheduler");
                break;
            case "add-io":
                scheduler.addIO(command[1], command[2], Integer.parseInt(command[3]), Integer.parseInt(command[4]));
                System.out.println("IO operation: " + command[1] + " has been added to the IO queue of job with pid: " + command[2]);
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
