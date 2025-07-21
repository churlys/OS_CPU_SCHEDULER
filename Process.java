public class Process {
    String pid;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int startTime = -1;
    int completionTime;

    public Process(String pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}
