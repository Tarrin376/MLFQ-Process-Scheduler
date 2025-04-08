# 🧠 MLFQ Process Scheduler (CLI)

A command-line simulation of a **Multi-Level Feedback Queue (MLFQ)** scheduler in Java, using the **Round-Robin algorithm** at each level for fair and responsive process management.

---

## 🚀 Features

- 🧩 Multi-level ready queues with configurable quantum & allotment per level
- ⏱️ Accurate tracking of:
  - Average Turnaround Time
  - Response Time
  - Waiting Time
  - CPU Utilization
- 🧵 I/O blocking and unblocking with scheduling effects
- 🎛️ Adjustable parameters: number of queues, boost/reset intervals, quantum and allotment times, and more!
- 🖥️ Interactive Command-Line Interface for job management and live control
- 📊 Visual representation of queues and job scheduling

---

## 📦 Example Usage

```bash
> 
> add-job P1 0 20
> add-io disk-read P1 5 10
> show-mlfq
> show-job P1
> resume # Resumes the simulation

# During execution:
# 📌 Press ENTER at any time to pause the simulation and enter command mode
```

---

## 🛠️ Getting Started

1. Clone the Repository
```bash
> git clone https://github.com/Tarrin376/MLFQ-Process-Scheduler.git
> cd MLFQ-Process-Scheduler
```

2. Compile and run the project
```bash
> javac *.java
> java Main.java
```

---

## 🧾 Commands

```bash
> add-job <pid> <arrival_time> <end_time> # Adds a new job to the scheduler e.g. add-job P1 0 20
> add-io <io_name> <pid> <arrival_time> <end_time> # Adds an I/O event for a job e.g. add-io disk-read P1 5 10
> show-mlfq # Displays the current state of the MLFQ
> show-metrics # Displays the metrics of the MLFQ, including CPU utilisation, average response time, and more
> show-job <pid> # Displays details of a specific job e.g. show-job P1
> resume # Resumes the simulation after a pause
> exit # Stops the simulation and exits the program
```

---

## 📊 Sample Output

```bash
=======================================================================================
                         MULTI-LEVEL FEEDBACK QUEUE SCHEDULER
=======================================================================================

Enter priority boost interval (ms): 70

Enter the number of priority levels (job queues): 2

Job Queue 0 (Highest Priority)

- Enter time quantum (ms): 2

- Enter allotment (number of quanta before demotion): 4

Job Queue 1 (Lowest Priority)

- Enter time quantum (ms): 3

- Enter allotment (number of quanta before demotion): 6

[Simulation started. Type 'help' for commands.]

=======================================================================================

[Time: 0ms]
  -> No job scheduled - CPU is idle.

---------------------------------------------------------------------------------------

[Time: 1ms]
  -> No job scheduled - CPU is idle.

---------------------------------------------------------------------------------------

[Simulation paused. Type 'resume' to continue.]
>
> add-job P1 2 10
Job with pid: P1 has been added to the scheduler.
> add-io disk-read P1 5 7
IO: "disk-read" has been added to the IO queue of job: P1.
> add-job P2 5 9
Job with pid: P2 has been added to the scheduler.
> add-job P3 2 15
Job with pid: P3 has been added to the scheduler.
> add-io mouse-click P3 4 7
IO: "mouse-click" has been added to the IO queue of job: P3.
> resume

[Time: 2ms]
  -> Job: [P1] (progress: 0%) Running on queue #1

---------------------------------------------------------------------------------------

[Time: 3ms]
  -> Job: [P1] (progress: 12.5%) Running on queue #1
  -> Job: [P1] (progress: 25%) Quantum expired (2ms) - moved to the back of queue #1

---------------------------------------------------------------------------------------

[Time: 4ms]
  -> Job: [P3] (progress: 0%) Started IO "mouse-click"
  -> Job: [P1] (progress: 25%) Running on queue #1

---------------------------------------------------------------------------------------

[Time: 5ms]
  -> Job: [P1] (progress: 37.5%) Started IO "disk-read"
  -> Job: [P2] (progress: 0%) Running on queue #1

---------------------------------------------------------------------------------------

[Time: 6ms]
  -> Job: [P2] (progress: 25%) Running on queue #1
  -> Job: [P2] (progress: 50%) Quantum expired (2ms) - moved to the back of queue #1

---------------------------------------------------------------------------------------

[Time: 7ms]
  -> Job: [P3] (progress: 0%) Completed IO "mouse-click"
  -> Job: [P3] (progress: 0%) Unblocked and re-entered queue #1
  -> Job: [P1] (progress: 37.5%) Completed IO "disk-read"
  -> Job: [P1] (progress: 37.5%) Unblocked and re-entered queue #1
  -> Job: [P2] (progress: 50%) Running on queue #1

---------------------------------------------------------------------------------------

[Simulation paused. Type 'resume' to continue.]
>
> show-mlfq

===================================== MLFQ Queues =====================================

Queue 1:
  [Ready / Running]
    |__ P2 | State: RUNNING | Progress: 75% | Time in Queue: 3ms
    |__ P3 | State: READY | Progress: 0% | Time in Queue: 0ms
    |__ P1 | State: READY | Progress: 37.5% | Time in Queue: 3ms

  [Blocked]

---------------------------------------------------------------------------------------

Queue 2:
  [Ready / Running]

  [Blocked]

=======================================================================================
> show-job P2
Job {
  PID: P2
  Arrival Time: 5ms
  End Time: 9ms
  Progress: 3
  Allotment Used: 3ms
  Quantum Used: 1ms
  State: RUNNING
  IO Queue: []
}
> show-job P1
Job {
  PID: P1
  Arrival Time: 2ms
  End Time: 10ms
  Progress: 3
  Allotment Used: 3ms
  Quantum Used: 1ms
  State: READY
  IO Queue: []
}
> show-metrics

======================================= Metrics =======================================

-> Average Turnaround Time: 0.0ms
-> Average Response Time: 0.0ms
-> Average Waiting Time: 0.0ms
-> CPU Utilization: 75%

=======================================================================================
```