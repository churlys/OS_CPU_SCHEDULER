import java.util.*;

public class Scheduler {
    private List<Process> processes;

    public Scheduler(List<Process> processes) {
        // Deep copy to avoid modifying the original list
        this.processes = new ArrayList<>();
        for (Process p : processes) {
            this.processes.add(new Process(p.pid, p.arrivalTime, p.burstTime));
        }
    }

    public String runFIFO() {
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
}
