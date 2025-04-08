import java.io.IOException;

public class EnterPauseListener implements Runnable {
    private final MLFQ scheduler;

    public EnterPauseListener(MLFQ scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        try {
            while (scheduler.getRunning()) {
                if (System.in.available() > 0 && !scheduler.getPaused()) {
                    System.out.println(TextColour.PURPLE + "\n[Simulation paused. Type 'resume' to continue.]" + TextColour.RESET);
                    scheduler.setPaused(true);
                }

                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to read keyboard inputs.");
        }
    }
}
