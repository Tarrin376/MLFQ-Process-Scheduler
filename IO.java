public class IO {
    private final String name;
    private final int startTime;
    private final int endTime;

    public IO(final String name, final int startTime, final int endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("{ name: \"%s\", startTime: %d, endTime: %d }", name, startTime, endTime);
    }

    public int getStartTime() { return startTime; }
    public int getEndTime() { return endTime; }
    public String getName() { return name; }
}
