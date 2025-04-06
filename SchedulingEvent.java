class SchedulingEvent {
    public final String pid;
    public final int queueNumber;
    public final boolean isBlocked;

    public SchedulingEvent(final String pid, final int queueNumber, final boolean isBlocked) {
        this.pid = pid;
        this.queueNumber = queueNumber;
        this.isBlocked = isBlocked;
    }
}