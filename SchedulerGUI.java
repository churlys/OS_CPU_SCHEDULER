import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SchedulerGUI extends JFrame {
    private JTable table;
    private JTextArea outputArea;
    private JComboBox<String> algorithmBox;
    private JTextField timeQuantumField;
    private DefaultTableModel tableModel;
    private GanttChartPanel ganttChartPanel;
    private JTextField mlfqQ0Field;
    private JTextField mlfqQ1Field;
    private JTextField mlfqQ2Field;
    private JTextField mlfqQ3Field;
    private JTextField mlfqBoostField;

    public SchedulerGUI() {
        setTitle("CPU Scheduling Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        setContentPane(mainPanel);
        
        algorithmBox = new JComboBox<>(new String[]{
            "FIFO", "SJF", "SRTF", "Round Robin", "MLFQ"
        });

        timeQuantumField = new JTextField(5);
        JButton addRowButton = new JButton("Add Process");
        JButton randomButton = new JButton("Generate Random");
        JButton runButton = new JButton("Run Simulation");
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        algoPanel.add(new JLabel("Algorithm:"));
        algoPanel.add(algorithmBox);

        JPanel quantumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantumPanel.add(new JLabel("Quantum (For RR):"));
        quantumPanel.add(timeQuantumField);

        JPanel mlfqQ0Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqQ0Panel.add(new JLabel("Q0 (For MLFQ):"));
        JTextField mlfqQ0Field = new JTextField(10);
        mlfqQ0Panel.add(mlfqQ0Field); 

        JPanel mlfqQ1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqQ1Panel.add(new JLabel("Q1 (For MLFQ):"));
        JTextField mlfqQ1Field = new JTextField(10);
        mlfqQ1Panel.add(mlfqQ1Field);

        JPanel mlfqQ2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqQ2Panel.add(new JLabel("Q2 (For MLFQ):"));
        JTextField mlfqQ2Field = new JTextField(10);
        mlfqQ2Panel.add(mlfqQ2Field);

        JPanel mlfqQ3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqQ3Panel.add(new JLabel("Q3 (For MLFQ):"));
        JTextField mlfqQ3Field = new JTextField(10);
        mlfqQ3Panel.add(mlfqQ3Field);

        JPanel mlfqBoostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqBoostPanel.add(new JLabel("Boost Interval (For MLFQ):"));
        JTextField mlfqBoostField = new JTextField(5);
        mlfqBoostPanel.add(mlfqBoostField);

        JTextField numPIDsField = new JTextField(4); 

        JPanel numPIDsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        numPIDsPanel.add(new JLabel("No. of PIDs:"));
        numPIDsPanel.add(numPIDsField);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(addRowButton);
        buttonsPanel.add(randomButton);

        randomButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            Random rand = new Random();
            int n;
            String numText = numPIDsField.getText().trim();
            if (!numText.isEmpty()) {
                try {
                    n = Integer.parseInt(numText);
                    if (n <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid positive number for No. of PIDs.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                n = 5 + rand.nextInt(5); // Default random number of PIDs
            }
            for (int i = 1; i <= n; i++) {
                int arrival = rand.nextInt(5);
                int burst = 1 + rand.nextInt(9);
                tableModel.addRow(new Object[]{"P" + i, arrival, burst});
            }
        });

        controlsPanel.add(algoPanel);
        controlsPanel.add(numPIDsPanel);
        controlsPanel.add(quantumPanel);
        controlsPanel.add(mlfqQ0Panel);
        controlsPanel.add(mlfqQ1Panel);
        controlsPanel.add(mlfqQ2Panel);
        controlsPanel.add(mlfqQ3Panel);
        controlsPanel.add(mlfqBoostPanel);

        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Dimension combinedSize = new Dimension(
        addRowButton.getPreferredSize().width + randomButton.getPreferredSize().width + 5, // 8 for spacing
        runButton.getPreferredSize().height
        );
        runButton.setPreferredSize(combinedSize);
        runPanel.add(runButton);
        
        JPanel clearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(runButton.getPreferredSize());
        clearPanel.add(clearButton);

        clearButton.addActionListener(e -> {
            tableModel.setRowCount(0);                 
            outputArea.setText("");                    
            ganttChartPanel.setBlocks(new ArrayList<>()); 
            timeQuantumField.setText("");              
            numPIDsField.setText("");                  
            algorithmBox.setSelectedIndex(0);          
        });

        leftPanel.add(controlsPanel);
        leftPanel.add(Box.createVerticalStrut(18)); // optional spacing
        leftPanel.add(buttonsPanel);
        leftPanel.add(runPanel);
        leftPanel.add(clearPanel);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(500, leftPanel.getPreferredSize().height));
        tableModel = new DefaultTableModel(new String[]{"PID", "Arrival Time", "Burst Time"}, 0);
        table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(leftPanel);
        topPanel.add(Box.createHorizontalStrut(20)); // optional spacing
        topPanel.add(tablePanel);

        outputArea = new JTextArea(10, 70);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        
        ganttChartPanel = new GanttChartPanel();
        ganttChartPanel.setPreferredSize(new Dimension(800, 120));
        
        // UI IS DONEEEE (So far)

        mainPanel.add(topPanel);          
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(outputScroll);      
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(ganttChartPanel); 
        

        addRowButton.addActionListener(e -> {
            int pid = tableModel.getRowCount() + 1;
            tableModel.addRow(new Object[]{"P" + pid, 0, 0});
        });

        randomButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            Random rand = new Random();
            int n = 5 + rand.nextInt(5);
            for (int i = 1; i <= n; i++) {
                int arrival = rand.nextInt(5);
                int burst = 1 + rand.nextInt(9);
                tableModel.addRow(new Object[]{"P" + i, arrival, burst});
            }
        });

        runButton.addActionListener(e -> runSimulation());
    }

    private void runSimulation() {
        List<Process> processes = new ArrayList<>();

        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String pid = tableModel.getValueAt(i, 0).toString();
                int arrival = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                int burst = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                processes.add(new Process(pid, arrival, burst));
            }

            String algo = algorithmBox.getSelectedItem().toString();
            int quantum = 1;

            if (algo.equals("Round Robin") || algo.equals("MLFQ")) {
                String qt = timeQuantumField.getText().trim();
                if (qt.isEmpty()) throw new Exception("Quantum is required.");
                quantum = Integer.parseInt(qt);
                if (quantum <= 0) throw new Exception("Quantum must be > 0");
            }

            Scheduler scheduler = new Scheduler(processes);
            String result = "";
            
            List<GanttChartPanel.GanttBlock> blocks = new ArrayList<>();
            Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};

            switch (algo) {
                case "FIFO":
                    result = scheduler.runFIFO();
                    break;
                case "Round Robin":
                    result = scheduler.runRR(quantum);
                    break;
                case "SJF":
                    result = scheduler.runSJF();
                    break;
                case "SRTF":
                    result = scheduler.runSRTF();
                    break;
                case "MLFQ":
                    try {
                        int[] quanta = new int[4];
                        quanta[0] = Integer.parseInt(mlfqQ0Field.getText().trim());
                        quanta[1] = Integer.parseInt(mlfqQ1Field.getText().trim());
                        quanta[2] = Integer.parseInt(mlfqQ2Field.getText().trim());
                        quanta[3] = Integer.parseInt(mlfqQ3Field.getText().trim());

                        int boostInterval = Integer.parseInt(mlfqBoostField.getText().trim());

                        result = scheduler.runMLFQ(quanta, boostInterval);
                    } catch (NumberFormatException ex) {
                        throw new Exception("Please enter valid integer values for all MLFQ quanta and boost interval.");
                    }
                    break;  
                default:
                    result = "Algorithm \"" + algo + "\" not implemented yet.";
                    blocks = new ArrayList<>();
                    break;
            }

            outputArea.setText(result);
            ganttChartPanel.setBlocks(blocks);

            outputArea.setText(result);
            int currentTime = 0;
            int colorIndex = 0;

            for (Process p : processes) {
                int start = currentTime;
                int end = currentTime + p.burstTime;
                blocks.add(new GanttChartPanel.GanttBlock(p.pid, start, end, colors[colorIndex % colors.length]));
                currentTime = end;
                colorIndex++;
            }

            ganttChartPanel.setBlocks(blocks);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
    }
}
     
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchedulerGUI().setVisible(true));
    }
}
class GanttChartPanel extends JPanel {
    private List<GanttBlock> blocks = new ArrayList<>();

    public void setBlocks(List<GanttBlock> blocks) {
        this.blocks = blocks;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (blocks == null || blocks.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        int height = 50;
        int xStart = 50;
        int y = 40;
        int unitWidth = 30;

        for (GanttBlock block : blocks) {
            int width = (block.end - block.start) * unitWidth;
            g2.setColor(block.color);
            g2.fillRect(xStart, y, width, height);

            g2.setColor(Color.BLACK);
            g2.drawRect(xStart, y, width, height);
            g2.drawString(block.pid, xStart + width / 2 - 10, y + 30);
            g2.drawString(String.valueOf(block.start), xStart, y + 70);

            xStart += width;
        }
        // Draw the final time
        g2.drawString(String.valueOf(blocks.get(blocks.size() - 1).end), xStart, y + 70);
    }

    static class GanttBlock {
        String pid;
        int start, end;
        Color color;

        public GanttBlock(String pid, int start, int end, Color color) {
            this.pid = pid;
            this.start = start;
            this.end = end;
            this.color = color;
        }
    }
}
