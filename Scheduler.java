    import java.util.*;
    import java.awt.Color;
    public class Scheduler {
        private List<Process> processes;
        private List<GanttChartPanel.GanttBlock> ganttBlocks = new ArrayList<>();

        public List<GanttChartPanel.GanttBlock> getRawGanttBlocks() {
            return ganttBlocks;
        }

    public Scheduler(List<Process> processes) {
        this.processes = new ArrayList<>();
        for (Process p : processes) {
            this.processes.add(new Process(p.pid, p.arrivalTime, p.burstTime));
        }
    }
            public static class GanttBlock {
            String pid;
            int start;
            int end;

            public GanttBlock(String pid, int start, int end) {
                this.pid = pid;
                this.start = start;
                this.end = end;
            }
        }
            private Map<String, Color> colorMap = new HashMap<>();
            private Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK};
            private int colorIndex = 0;

            private Color getColorForPID(String pid) {
                if (!colorMap.containsKey(pid)) {
                    colorMap.put(pid, colors[colorIndex % colors.length]);
                    colorIndex++;
                }
                return colorMap.get(pid);
            }


    public String runFIFO() {
        StringBuilder sb = new StringBuilder();
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        double totalWaiting = 0, totalTurnaround = 0;
        ganttBlocks.clear();
        sb.append("=== FIFO Scheduling ===\n");
        sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

        for (Process p : processes) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            int waiting = currentTime - p.arrivalTime;
            int turnaround = waiting + p.burstTime;
            totalWaiting += waiting;
            totalTurnaround += turnaround;

            int start = currentTime;
            int end = currentTime + p.burstTime;
            ganttBlocks.add(new GanttChartPanel.GanttBlock(p.pid, start, end, getColorForPID(p.pid)));

            sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n", p.pid, p.arrivalTime, p.burstTime, waiting, turnaround));
            currentTime += p.burstTime;
        }


        int n = processes.size();
        sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
        sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
        return sb.toString();
    }

    public String runRR(int quantum) {
        StringBuilder sb = new StringBuilder();
        Queue<Process> queue = new LinkedList<>();
        List<Process> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        double totalWaiting = 0, totalTurnaround = 0;
        int index = 0;

        sb.append("=== Round Robin Scheduling ===\n");
        sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

        Map<String, Integer> waitingTimes = new HashMap<>();
        Map<String, Integer> turnaroundTimes = new HashMap<>();
        Map<String, Integer> startTimes = new HashMap<>();

        while (!queue.isEmpty() || index < arrivalList.size()) {
            while (index < arrivalList.size() && arrivalList.get(index).arrivalTime <= currentTime) {
                queue.offer(arrivalList.get(index));
                index++;
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            Process p = queue.poll();

            if (!startTimes.containsKey(p.pid)) {
                startTimes.put(p.pid, currentTime);
            }

            int execTime = Math.min(quantum, p.remainingTime);
            p.remainingTime -= execTime;
            ganttBlocks.add(new GanttChartPanel.GanttBlock(p.pid, currentTime, currentTime + execTime, getColorForPID(p.pid)));

            currentTime += execTime;

            while (index < arrivalList.size() && arrivalList.get(index).arrivalTime <= currentTime) {
                queue.offer(arrivalList.get(index));
                index++;
            }

            if (p.remainingTime > 0) {
                queue.offer(p);
            } else {
                int turnaround = currentTime - p.arrivalTime;
                int waiting = turnaround - p.burstTime;
                totalWaiting += waiting;
                totalTurnaround += turnaround;

                sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n", p.pid, p.arrivalTime, p.burstTime, waiting, turnaround));
            }
        }

        int n = processes.size();
        sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
        sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
        return sb.toString();
    }

   
    public String runSJF() {
        StringBuilder sb = new StringBuilder();
        List<Process> all = new ArrayList<>(processes);
        int time = 0, completed = 0;
        double totalWaiting = 0, totalTurnaround = 0;
        boolean[] done = new boolean[all.size()];

        sb.append("=== SJF (Non-Preemptive) Scheduling ===\n");
        sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

        while (completed < all.size()) {
            List<Process> ready = new ArrayList<>();
            for (int i = 0; i < all.size(); i++) {
                if (!done[i] && all.get(i).arrivalTime <= time) {
                    ready.add(all.get(i));
                }
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            ready.sort(Comparator.comparingInt(p -> p.burstTime));
            Process current = ready.get(0);
            int index = all.indexOf(current);

            int waiting = time - current.arrivalTime;
            if (waiting < 0) waiting = 0;

            time += current.burstTime;
            int turnaround = time - current.arrivalTime;

            totalWaiting += waiting;
            totalTurnaround += turnaround;
            done[index] = true;
            completed++;

            sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n", current.pid, current.arrivalTime, current.burstTime, waiting, turnaround));
        }

        int n = all.size();
        sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
        sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
        return sb.toString();
    }

        public String runSRTF() {
        StringBuilder sb = new StringBuilder();
        ganttBlocks.clear();
        List<Process> all = new ArrayList<>(processes);
        int time = 0, completed = 0;
        double totalWaiting = 0, totalTurnaround = 0;
        Process current = null;

        Map<String, Integer> startTimeMap = new HashMap<>();

        sb.append("=== SRTF (Preemptive SJF) Scheduling ===\n");
        sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

        while (completed < all.size()) {
            List<Process> ready = new ArrayList<>();
            for (Process p : all) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    ready.add(p);
                }
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            ready.sort(Comparator.comparingInt(p -> p.remainingTime));
            current = ready.get(0);

            ganttBlocks.add(new GanttChartPanel.GanttBlock(current.pid, time, time + 1, getColorForPID(current.pid)));
            current.remainingTime--;
            time++;

            if (current.remainingTime == 0) {
                completed++;
                int turnaround = time - current.arrivalTime;
                int waiting = turnaround - current.burstTime;
                totalWaiting += waiting;
                totalTurnaround += turnaround;

                sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n", current.pid, current.arrivalTime, current.burstTime, waiting, turnaround));
            }
        }

        int n = all.size();
        sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
        sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
        return sb.toString();
    }    
 public String runMLFQ(int [] quantum) {
    StringBuilder sb = new StringBuilder();
    ganttBlocks.clear();
    int numQueues = 3; // or any number you want
    List<Queue<Process>> queues = new ArrayList<>();
    for (int i = 0; i < numQueues; i++) queues.add(new LinkedList<>());

    List<Process> arrivalList = new ArrayList<>(processes);
    arrivalList.sort(Comparator.comparingInt(p -> p.arrivalTime));

    int currentTime = 0;
    int index = 0;
    double totalWaiting = 0, totalTurnaround = 0;
    int completed = 0;
    Map<Process, Integer> levelMap = new HashMap<>();

    sb.append("=== Multilevel Feedback Queue (MLFQ) Scheduling ===\n");
    sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

    while (completed < processes.size()) {
        while (index < arrivalList.size() && arrivalList.get(index).arrivalTime <= currentTime) {
            Process p = arrivalList.get(index);
            queues.get(0).add(p);
            levelMap.put(p, 0);
            index++;
        }

        boolean executed = false;
        for (int i = 0; i < queues.size(); i++) {
            if (!queues.get(i).isEmpty()) {
                Process p = queues.get(i).poll();
                int exec = Math.min(p.remainingTime, quantum);

                ganttBlocks.add(new GanttChartPanel.GanttBlock(p.pid, currentTime, currentTime + exec, getColorForPID(p.pid)));
                p.remainingTime -= exec;
                currentTime += exec;

                while (index < arrivalList.size() && arrivalList.get(index).arrivalTime <= currentTime) {
                    Process newP = arrivalList.get(index);
                    queues.get(0).add(newP);
                    levelMap.put(newP, 0);
                    index++;
                }

                if (p.remainingTime > 0) {
                    int nextLevel = Math.min(i + 1, queues.size() - 1);
                    queues.get(nextLevel).add(p);
                    levelMap.put(p, nextLevel);
                } else {
                    int turnaround = currentTime - p.arrivalTime;
                    int waiting = turnaround - p.burstTime;
                    totalWaiting += waiting;
                    totalTurnaround += turnaround;
                    completed++;
                    sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n", p.pid, p.arrivalTime, p.burstTime, waiting, turnaround));
                }

                executed = true;
                break;
            }
        }

        if (!executed) currentTime++;
    }

    int n = processes.size();
    sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
    sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
    return sb.toString();
}
}
