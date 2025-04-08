import java.text.DecimalFormat;
import java.util.Optional;
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
                addJob(command);
                break;
            case "add-io":
                addIO(command);
                break;
            case "show-mlfq":
                System.out.println(scheduler);
                break;
            case "show-metrics":
                showMetrics();
                break;
            case "show-job":
                showJob(command);
                break;
            case "resume":
                scheduler.setPaused(false);
                break;
            case "exit":
                scheduler.setRunning(false);
                break;
        }
    }

    public void addJob(final String[] command) {
        if (command.length != 4) {
            System.out.println(TextColour.getErrorMessage("Invalid command format. Expected: add-job <pid> <arrival_time> <end_time>"));
            return;
        }

        if (!InputSanitizer.isNumeric(command[2]) || !InputSanitizer.isNumeric(command[3])) {
            System.out.println(TextColour.getErrorMessage("Invalid input: Arrival and end times must be valid whole numbers."));
            return;
        }

        scheduler.addJob(command[1], Integer.parseInt(command[2]), Integer.parseInt(command[3]));
    }

    public void addIO(final String[] command) {
        if (command.length != 5) {
            System.out.println(TextColour.getErrorMessage("Invalid command format. Expected: add-io <io_name> <pid> <arrival_time> <end_time>"));
            return;
        }

        if (!InputSanitizer.isNumeric(command[3]) || !InputSanitizer.isNumeric(command[4])) {
            System.out.println(TextColour.getErrorMessage("Invalid input: Arrival and end times must be valid whole numbers."));
            return;
        }

        scheduler.addIO(command[1], command[2], Integer.parseInt(command[3]), Integer.parseInt(command[4]));
    }

    public void showJob(final String[] command) {
        if (command.length != 2) {
            System.out.println(TextColour.getErrorMessage("Invalid command format. Expected: show-job <pid>"));
            return;
        }

        Optional<Job> job = scheduler.getJobByPid(command[1]);
        System.out.println(job.isEmpty() ? "Job with pid: " + command[1] + " was not found." : job.get());
    }

    public void showMetrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n======================================= Metrics =======================================\n\n");

        sb.append("-> " + TextColour.GREEN + "Average Turnaround Time: " + TextColour.RESET + scheduler.getAvgTurnaroundTime() + "ms\n");
        sb.append("-> " + TextColour.GREEN + "Average Response Time: " + TextColour.RESET + scheduler.getAvgResponseTime() + "ms\n");
        sb.append("-> " + TextColour.GREEN + "Average Waiting Time: " + TextColour.RESET + scheduler.getAvgWaitingTime() + "ms\n");

        String cpuUtilPercentage = new DecimalFormat("#.##").format(scheduler.getCPUUtilization() * 100) + "%";
        sb.append("-> " + TextColour.GREEN + "CPU Utilization: " + TextColour.RESET + cpuUtilPercentage);
        
        sb.append("\n\n=======================================================================================");
        System.out.println(sb.toString());
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
                        "  show-mlfq\r\n" + //
                        "      - Displays the current state of the MLFQ.\r\n" + //
                        "  show-metrics\r\n" + //
                        "      - Displays the metrics of the MLFQ, including CPU utilisation, average response time, and more.\r\n" + //
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
