import java.awt.*;
import java.lang.Process;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
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

    private final Color backgroundDark = new Color(30, 30, 30);

    public SchedulerGUI() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        setContentPane(mainPanel);

        JLabel titleLabel = new JLabel("CPU Scheduling Simulator");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        algorithmBox = new JComboBox<>(new String[]{
            "FIFO", "SJF", "SRTF", "Round Robin", "MLFQ"
        });

        timeQuantumField = new JTextField(5);
        RoundedButton addRowButton = new RoundedButton("Add Process", 20);
        RoundedButton randomButton = new RoundedButton("Generate Random", 20);
        RoundedButton runButton = new RoundedButton("Run Simulation", 20);

        styleButton(addRowButton);
        styleButton(randomButton);
        styleButton(runButton);
        
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
        mlfqQ0Field = new JTextField(10);
        mlfqQ0Panel.add(mlfqQ0Field); 

        JPanel mlfqQ1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqQ1Panel.add(new JLabel("Q1 (For MLFQ):"));
        mlfqQ1Field = new JTextField(10);
        mlfqQ1Panel.add(mlfqQ1Field);

        JPanel mlfqQ2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqQ2Panel.add(new JLabel("Q2 (For MLFQ):"));
        mlfqQ2Field = new JTextField(10);
        mlfqQ2Panel.add(mlfqQ2Field);

        JPanel mlfqQ3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mlfqQ3Panel.add(new JLabel("Q3 (For MLFQ):"));
        mlfqQ3Field = new JTextField(10);
        mlfqQ3Panel.add(mlfqQ3Field);

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

        algoPanel.setBackground(new Color(169, 169, 169));
        numPIDsPanel.setBackground(new Color(169, 169, 169));
        quantumPanel.setBackground(new Color(169, 169, 169));
        mlfqQ0Panel.setBackground(new Color(169, 169, 169));
        mlfqQ1Panel.setBackground(new Color(169, 169, 169));
        mlfqQ2Panel.setBackground(new Color(169, 169, 169));
        mlfqQ3Panel.setBackground(new Color(169, 169, 169));

        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Dimension combinedSize = new Dimension(
        addRowButton.getPreferredSize().width + randomButton.getPreferredSize().width + 5, // 8 for spacing
        runButton.getPreferredSize().height
        );
        runButton.setPreferredSize(combinedSize);
        runPanel.add(runButton);
        
        JPanel clearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        RoundedButton clearButton = new RoundedButton("Clear", 20);
        styleButton(clearButton);
        clearButton.setPreferredSize(runButton.getPreferredSize());
        clearPanel.add(clearButton);

        clearButton.addActionListener(e -> {
            tableModel.setRowCount(0);                 
            outputArea.setText("");                    
            ganttChartPanel.setBlocks(new ArrayList<>()); 
            timeQuantumField.setText("");              
            numPIDsField.setText("");                  
            algorithmBox.setSelectedIndex(0);
            mlfqQ0Field.setText("");
            mlfqQ1Field.setText("");
            mlfqQ2Field.setText("");
            mlfqQ3Field.setText("");
        });

        // Set layout for the main controls panel
        controlsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6); // spacing between rows
        gbc.fill = GridBagConstraints.HORIZONTAL; // do NOT stretch components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;;

        int row = 0;

        // Use FlowLayout for label + text field side-by-side (compact)
        Consumer<JPanel> setRowLayout = panel -> panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        // Set layout for all input rows (label + field)
        setRowLayout.accept(quantumPanel);
        setRowLayout.accept(mlfqQ0Panel);
        setRowLayout.accept(mlfqQ1Panel);
        setRowLayout.accept(mlfqQ2Panel);
        setRowLayout.accept(mlfqQ3Panel);
        setRowLayout.accept(algoPanel);
        setRowLayout.accept(numPIDsPanel);

        // Button panels: also keep buttons side-by-side
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        runPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        clearPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        // Add sub-panels to controlsPanel row by row
        gbc.gridy = row++; controlsPanel.add(algoPanel, gbc);
        gbc.gridy = row++; controlsPanel.add(numPIDsPanel, gbc);
        gbc.gridy = row++; controlsPanel.add(quantumPanel, gbc);
        gbc.gridy = row++; controlsPanel.add(mlfqQ0Panel, gbc);
        gbc.gridy = row++; controlsPanel.add(mlfqQ1Panel, gbc);
        gbc.gridy = row++; controlsPanel.add(mlfqQ2Panel, gbc);
        gbc.gridy = row++; controlsPanel.add(mlfqQ3Panel, gbc);
        gbc.gridy = row++; controlsPanel.add(buttonsPanel, gbc);
        gbc.gridy = row++; controlsPanel.add(runPanel, gbc);
        gbc.gridy = row++; controlsPanel.add(clearPanel, gbc);

        // Optional: add vertical space filler at the bottom
        gbc.gridy = row++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        controlsPanel.add(Box.createVerticalGlue(), gbc);



        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(1100, controlsPanel.getPreferredSize().height));
        tableModel = new DefaultTableModel(new String[]{"PID", "Arrival Time", "Burst Time"}, 0);
        table = new JTable(tableModel);

        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(22);
        table.getTableHeader().setBackground(Color.LIGHT_GRAY);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionBackground(new Color(0xBBDEFB)); 
        table.setSelectionForeground(Color.BLACK);

        JScrollPane tableScroll = new JScrollPane(table);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        controlsPanel.setMaximumSize(controlsPanel.getPreferredSize()); // 💡 Force compact width
        controlsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        Dimension controlsSize = controlsPanel.getPreferredSize();
        tablePanel.setPreferredSize(new Dimension(1100, controlsSize.height)); // Match height
        tablePanel.setMaximumSize(new Dimension(15000, controlsSize.height)); // Match height
        tablePanel.setAlignmentY(Component.TOP_ALIGNMENT);

        topPanel.add(controlsPanel);
        topPanel.add(tablePanel);


        outputArea = new JTextArea(10, 70);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        
        ganttChartPanel = new GanttChartPanel();
        ganttChartPanel.setPreferredSize(new Dimension(800, 120));
        
        // UI IS DONEEEE (So far)

        JLabel title = new JLabel("CPU Scheduling Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        mainPanel.setBackground(backgroundDark);
        topPanel.setBackground(backgroundDark);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(1, 20, 10, 20));
        controlsPanel.setBackground(new Color(169, 169, 169));
        buttonsPanel.setBackground(new Color(169, 169, 169));
        runPanel.setBackground(new Color(169, 169, 169));
        clearPanel.setBackground(new Color(169, 169, 169));
        runButton.setBackground(new Color(0x4CAF50)); // Green color
        clearButton.setBackground(new Color(0xF44336)); // Red color
        tablePanel.setBackground(Color.WHITE);
        outputArea.setBackground(Color.WHITE);
        outputArea.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        mainPanel.add(topPanel);          
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(outputScroll);      
        mainPanel.add(Box.createVerticalStrut(20));
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

    private void styleButton(JButton button) {
    button.setBackground(new Color(0x2196F3)); // Blue color
    button.setForeground(Color.WHITE);         // White text
    button.setFocusPainted(false);             // Removes border highlight
    button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Changes mouse cursor to hand
}

public class RoundedButton extends JButton {
    private int radius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE); // or any text color
        setBackground(new Color(118, 185, 0)); // NVIDIA green (customize this!)
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        // Text
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    public void paintBorder(Graphics g) {
        // No border by default; add if needed
    }

    @Override
    public boolean isContentAreaFilled() {
        return false; // custom paint handles fill
    }
}

    public class RoundedPanel extends JPanel {
    private int cornerRadius;

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false); // make sure background is transparent so we can paint it ourselves
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw rounded rectangle background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcs.width, arcs.height);
    }
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

            if (algo.equals("Round Robin")) {
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

                        result = scheduler.runMLFQ(quanta);
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