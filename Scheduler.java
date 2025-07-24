import java.awt.Color;
import java.util.*;

public class Scheduler {
    public static class GanttBlock {
        public String pid;
        public int start;
        public int end;
        public Color color;

        public GanttBlock(String pid, int start, int end) {
            this.pid = pid;
            this.start = start;
            this.end = end;
            this.color = null; // default color if none is given
        }
    }

    private List<GanttBlock> ganttBlocks = new ArrayList<>();

    public List<GanttBlock> getGanttBlocks(Color[] colors) {
        return ganttBlocks;
    }

    private List<Process> processes;

    public Scheduler(List<Process> processes) {
        this.processes = new ArrayList<>();
        for (Process p : processes) {
            this.processes.add(new Process(p.pid, p.arrivalTime, p.burstTime));
        }
    }

    public static class Process {
        public String pid;
        public int arrivalTime;
        public int burstTime;
        public int remainingTime;

        public Process(String pid, int arrivalTime, int burstTime) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }
    }

    public String runFIFO() {
        ganttBlocks.clear();

        StringBuilder sb = new StringBuilder();
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        double totalWaiting = 0, totalTurnaround = 0;

        sb.append("=== FIFO Scheduling ===\n");
        sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

        for (Process p : processes) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            int waiting = currentTime - p.arrivalTime;
            int turnaround = waiting + p.burstTime;
            ganttBlocks.add(new GanttBlock(p.pid, currentTime, currentTime + p.burstTime));
            totalWaiting += waiting;
            totalTurnaround += turnaround;

            sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n", p.pid, p.arrivalTime, p.burstTime, waiting, turnaround));
            currentTime += p.burstTime;
        }

        int n = processes.size();
        sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
        sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
        return sb.toString();
    }

    public String runRR(int quantum) {
        ganttBlocks.clear();

        StringBuilder sb = new StringBuilder();
        Queue<Process> queue = new LinkedList<>();
        List<Process> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        double totalWaiting = 0, totalTurnaround = 0;
        int index = 0;

        sb.append("=== Round Robin Scheduling ===\n");
        sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

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
            int startTime = currentTime;
            currentTime += execTime;
            p.remainingTime -= execTime;

            ganttBlocks.add(new GanttBlock(p.pid, startTime, currentTime));

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

                sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n",
                        p.pid, p.arrivalTime, p.burstTime, waiting, turnaround));
            }
        }

        int n = processes.size();
        sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
        sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
        return sb.toString();
    }

    public String runSJF() {
        ganttBlocks.clear();

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
            ganttBlocks.add(new GanttBlock(current.pid, time, time + current.burstTime));

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
        ganttBlocks.clear();

        StringBuilder sb = new StringBuilder();
        List<Process> all = new ArrayList<>(processes);
        int time = 0, completed = 0;
        double totalWaiting = 0, totalTurnaround = 0;

        sb.append("=== SRTF (Preemptive SJF) Scheduling ===\n");
        sb.append(String.format("%-5s %-10s %-10s %-15s %-15s\n", "PID", "Arrival", "Burst", "Waiting", "Turnaround"));

        Process lastProcess = null;
        int startTime = 0;

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
            Process current = ready.get(0);

            if (lastProcess != null && !lastProcess.pid.equals(current.pid)) {
                ganttBlocks.add(new GanttBlock(lastProcess.pid, startTime, time));
                startTime = time;
            }

            if (lastProcess == null || !lastProcess.pid.equals(current.pid)) {
                startTime = time;
            }

            current.remainingTime--;
            time++;

            if (current.remainingTime == 0) {
                ganttBlocks.add(new GanttBlock(current.pid, startTime, time));
                int turnaround = time - current.arrivalTime;
                int waiting = turnaround - current.burstTime;
                totalWaiting += waiting;
                totalTurnaround += turnaround;
                completed++;

                sb.append(String.format("%-5s %-10d %-10d %-15d %-15d\n", current.pid, current.arrivalTime, current.burstTime, waiting, turnaround));
                lastProcess = null;
            } else {
                lastProcess = current;
            }
        }

        int n = all.size();
        sb.append(String.format("\nAverage Waiting Time: %.2f\n", totalWaiting / n));
        sb.append(String.format("Average Turnaround Time: %.2f\n", totalTurnaround / n));
        return sb.toString();
    }

    public String runMLFQ(int[] quanta) {
        ganttBlocks.clear();

        StringBuilder sb = new StringBuilder();
        int numQueues = quanta.length;
        List<Queue<Process>> queues = new ArrayList<>();
        for (int i = 0; i < numQueues; i++) queues.add(new LinkedList<>());

        List<Process> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        int index = 0;
        double totalWaiting = 0, totalTurnaround = 0;
        int completed = 0;
        Map<Process, Integer> levelMap = new HashMap<>();

        sb.append("=== MLFQ Scheduling ===\n");
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
                    int exec = Math.min(p.remainingTime, quanta[i]);

                    ganttBlocks.add(new GanttBlock(p.pid, currentTime, currentTime + exec));
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