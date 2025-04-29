package os;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSFileStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;
import java.time.Duration;

public class SystemHealthMonitor extends JFrame {

    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = systemInfo.getHardware();
    private final OperatingSystem os = systemInfo.getOperatingSystem();
    private final DecimalFormat df = new DecimalFormat("0.00");
    private long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();

    // UI Components
    private JPanel mainPanel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private JProgressBar cpuBar;
    private JProgressBar memoryBar;
    private JLabel uptimeLabel;
    private JTextArea detailsArea;

    // Section panels
    private JPanel overviewPanel;
    private JPanel cpuPanel;
    private JPanel memoryPanel;
    private JPanel storagePanel;
    private JPanel gpuPanel;
    private JPanel sensorsPanel;

    // Constants for styling
    private final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private final Color PANEL_COLOR = new Color(30, 30, 30);
    private final Color ACCENT_COLOR = new Color(75, 145, 230);
    private final Color TEXT_COLOR = new Color(220, 220, 220);
    private final Color SECONDARY_TEXT_COLOR = new Color(180, 180, 180);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private final int PADDING = 15;

    public SystemHealthMonitor() {
        setTitle("System Health Monitor");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupUI();

        Timer timer = new Timer(2000, e -> updateStats());
        timer.start();

        setVisible(true);
    }

    private void setupUI() {
        // Main layout setup
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Create sidebar
        createSidebar();

        // Create content area with card layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BACKGROUND_COLOR);

        // Create different sections
        createOverviewPanel();
        createCpuPanel();
        createMemoryPanel();
        createStoragePanel();
        createGpuPanel();
        createSensorsPanel();

        // Add panels to card layout
        cardPanel.add(overviewPanel, "Overview");
        cardPanel.add(cpuPanel, "CPU");
        cardPanel.add(memoryPanel, "Memory");
        cardPanel.add(storagePanel, "Storage");
        cardPanel.add(gpuPanel, "GPU");
        cardPanel.add(sensorsPanel, "Sensors");

        // Add components to main panel
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Set main panel as content pane
        setContentPane(mainPanel);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(PANEL_COLOR);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        sidebarPanel.setPreferredSize(new Dimension(180, getHeight()));

        // App title
        JLabel titleLabel = new JLabel("System Monitor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Navigation buttons
        String[] sections = {"Overview", "CPU", "Memory", "Storage", "GPU", "Sensors"};
        for (String section : sections) {
            JButton navButton = createNavButton(section);
            sidebarPanel.add(navButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Add components to sidebar
        sidebarPanel.add(titleLabel, 0);

        // System uptime at bottom
        sidebarPanel.add(Box.createVerticalGlue());
        uptimeLabel = new JLabel("Uptime: Calculating...");
        uptimeLabel.setFont(SMALL_FONT);
        uptimeLabel.setForeground(SECONDARY_TEXT_COLOR);
        uptimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(uptimeLabel);
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(PANEL_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(150, 40));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(45, 45, 45));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PANEL_COLOR);
            }
        });

        // Add action to show corresponding panel
        button.addActionListener(e -> cardLayout.show(cardPanel, text));

        return button;
    }

    private void createOverviewPanel() {
        overviewPanel = createBasePanel(new BorderLayout(15, 15));

        JLabel titleLabel = createSectionTitle("System Overview");
        overviewPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBackground(BACKGROUND_COLOR);

        // CPU usage card
        JPanel cpuCard = createInfoCard("CPU Usage");
        cpuBar = createProgressBar();
        cpuCard.add(cpuBar);

        // Memory usage card
        JPanel memoryCard = createInfoCard("Memory Usage");
        memoryBar = createProgressBar();
        memoryCard.add(memoryBar);

        // OS info card
        JPanel osCard = createInfoCard("Operating System");
        JLabel osLabel = new JLabel(os.toString());
        osLabel.setForeground(TEXT_COLOR);
        osLabel.setFont(REGULAR_FONT);
        osCard.add(osLabel);

        // System uptime card
        JPanel uptimeCard = createInfoCard("System Uptime");
        JLabel uptimeValueLabel = new JLabel("Calculating...");
        uptimeValueLabel.setForeground(TEXT_COLOR);
        uptimeValueLabel.setFont(REGULAR_FONT);
        uptimeCard.add(uptimeValueLabel);

        statsPanel.add(cpuCard);
        statsPanel.add(memoryCard);
        statsPanel.add(osCard);
        statsPanel.add(uptimeCard);

        overviewPanel.add(statsPanel, BorderLayout.CENTER);

        // Details area
        detailsArea = new JTextArea();
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailsArea.setEditable(false);
        detailsArea.setBackground(PANEL_COLOR);
        detailsArea.setForeground(TEXT_COLOR);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        detailsArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(getWidth(), 200));
        overviewPanel.add(scrollPane, BorderLayout.SOUTH);
    }

    private void createCpuPanel() {
        cpuPanel = createBasePanel(new BorderLayout(15, 15));

        JLabel titleLabel = createSectionTitle("CPU Information");
        cpuPanel.add(titleLabel, BorderLayout.NORTH);

        // CPU details panel
        JPanel cpuDetailsPanel = new JPanel();
        cpuDetailsPanel.setLayout(new BoxLayout(cpuDetailsPanel, BoxLayout.Y_AXIS));
        cpuDetailsPanel.setBackground(PANEL_COLOR);
        cpuDetailsPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // CPU info will be populated in updateStats()

        JScrollPane scrollPane = new JScrollPane(cpuDetailsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cpuPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createMemoryPanel() {
        memoryPanel = createBasePanel(new BorderLayout(15, 15));

        JLabel titleLabel = createSectionTitle("Memory Information");
        memoryPanel.add(titleLabel, BorderLayout.NORTH);

        // Memory details will be populated in updateStats()
    }

    private void createStoragePanel() {
        storagePanel = createBasePanel(new BorderLayout(15, 15));

        JLabel titleLabel = createSectionTitle("Storage Information");
        storagePanel.add(titleLabel, BorderLayout.NORTH);

        // Storage details will be populated in updateStats()
    }

    private void createGpuPanel() {
        gpuPanel = createBasePanel(new BorderLayout(15, 15));

        JLabel titleLabel = createSectionTitle("GPU Information");
        gpuPanel.add(titleLabel, BorderLayout.NORTH);

        // GPU details will be populated in updateStats()
    }

    private void createSensorsPanel() {
        sensorsPanel = createBasePanel(new BorderLayout(15, 15));

        JLabel titleLabel = createSectionTitle("System Sensors");
        sensorsPanel.add(titleLabel, BorderLayout.NORTH);

        // Sensors details will be populated in updateStats()
    }

    private JPanel createBasePanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        return panel;
    }

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JPanel createInfoCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL_COLOR);
        card.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SMALL_FONT);
        titleLabel.setForeground(SECONDARY_TEXT_COLOR);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        return card;
    }

    private JProgressBar createProgressBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(true);
        bar.setForeground(ACCENT_COLOR);
        bar.setBackground(new Color(50, 50, 50));
        bar.setBorderPainted(false);
        return bar;
    }

    private void updateStats() {
        StringBuilder sb = new StringBuilder();

        // Update CPU info
        CentralProcessor cpu = hal.getProcessor();
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks);
        prevTicks = cpu.getSystemCpuLoadTicks();
        int cpuLoadPercentage = (int) (cpuLoad * 100);

        // Update Memory info
        GlobalMemory mem = hal.getMemory();
        long totalMemory = mem.getTotal();
        long availableMemory = mem.getAvailable();
        long usedMemory = totalMemory - availableMemory;
        int memoryPercentage = (int) (((double) usedMemory / totalMemory) * 100);

        // Update progress bars
        cpuBar.setValue(cpuLoadPercentage);
        cpuBar.setString(cpuLoadPercentage + "%");
        memoryBar.setValue(memoryPercentage);
        memoryBar.setString(memoryPercentage + "% (" + formatBytes(usedMemory) + " / " + formatBytes(totalMemory) + ")");

        // Update uptime
        long uptimeSec = os.getSystemUptime();
        Duration uptime = Duration.ofSeconds(uptimeSec);
        long days = uptime.toDays();
        long hours = uptime.toHoursPart();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();
        String uptimeStr = String.format("%d days, %d h %d m %d s", days, hours, minutes, seconds);
        uptimeLabel.setText("Uptime: " + uptimeStr);

        // Build details text for overview
        sb.append("System Information Summary\n\n");

        // OS Info
        sb.append("OS: ").append(os).append("\n");

        // CPU Info
        sb.append("\nCPU: ").append(cpu.getProcessorIdentifier().getName()).append("\n");
        sb.append("Physical Cores: ").append(cpu.getPhysicalProcessorCount()).append("\n");
        sb.append("Logical Cores: ").append(cpu.getLogicalProcessorCount()).append("\n");
        sb.append("CPU Load: ").append(df.format(cpuLoad * 100)).append("%\n");

        // Memory Info
        sb.append("\nMemory Total: ").append(formatBytes(totalMemory)).append("\n");
        sb.append("Memory Available: ").append(formatBytes(availableMemory)).append("\n");
        sb.append("Memory Used: ").append(formatBytes(usedMemory)).append(" (")
                .append(df.format(((double) usedMemory / totalMemory) * 100)).append("%)\n");

        // Disk Info
        sb.append("\nStorage:\n");
        List<OSFileStore> fsList = os.getFileSystem().getFileStores();
        for (OSFileStore fs : fsList) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            double usagePercentage = ((double) (total - usable) / total) * 100;

            sb.append("  ").append(fs.getName()).append(" (").append(fs.getMount()).append("): ")
                    .append(formatBytes(total - usable)).append(" / ")
                    .append(formatBytes(total)).append(" (")
                    .append(df.format(usagePercentage)).append("%)\n");
        }

        // Sensors
        Sensors sensors = hal.getSensors();
        double cpuTemp = sensors.getCpuTemperature();
        if (cpuTemp > 0) {  // Some systems might not report temperature correctly
            sb.append("\nCPU Temperature: ").append(df.format(cpuTemp)).append(" °C\n");
        }

        // Update the details area
        detailsArea.setText(sb.toString());

        // Update detail panels (these would be more comprehensive in a real implementation)
        updateCpuPanel(cpu);
        updateMemoryPanel(mem);
        updateStoragePanel(fsList);
        updateGpuPanel();
        updateSensorsPanel(sensors);
    }

    private void updateCpuPanel(CentralProcessor cpu) {
        JPanel cpuDetailsPanel = (JPanel) ((JScrollPane) cpuPanel.getComponent(1)).getViewport().getView();
        cpuDetailsPanel.removeAll();

        // CPU Name and basic info
        JPanel infoCard = createInfoCard("Processor");
        JLabel cpuNameLabel = new JLabel(cpu.getProcessorIdentifier().getName());
        cpuNameLabel.setForeground(TEXT_COLOR);
        cpuNameLabel.setFont(REGULAR_FONT);
        infoCard.add(cpuNameLabel);
        cpuDetailsPanel.add(infoCard);
        cpuDetailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // CPU cores
        JPanel coresCard = createInfoCard("Cores");
        JLabel coresLabel = new JLabel("Physical: " + cpu.getPhysicalProcessorCount() +
                " | Logical: " + cpu.getLogicalProcessorCount());
        coresLabel.setForeground(TEXT_COLOR);
        coresLabel.setFont(REGULAR_FONT);
        coresCard.add(coresLabel);
        cpuDetailsPanel.add(coresCard);
        cpuDetailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // CPU load
        JPanel loadCard = createInfoCard("Current Load");
        JProgressBar cpuLoadBar = createProgressBar();
        double cpuLoad = cpu.getSystemCpuLoad(10);
        int cpuLoadPercentage = (int) (cpuLoad * 100);
        cpuLoadBar.setValue(cpuLoadPercentage);
        cpuLoadBar.setString(cpuLoadPercentage + "%");
        loadCard.add(cpuLoadBar);
        cpuDetailsPanel.add(loadCard);

        cpuDetailsPanel.revalidate();
        cpuDetailsPanel.repaint();
    }

    private void updateMemoryPanel(GlobalMemory mem) {
        // Clear existing components
        memoryPanel.removeAll();

        JLabel titleLabel = createSectionTitle("Memory Information");
        memoryPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel memoryContentPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        memoryContentPanel.setBackground(BACKGROUND_COLOR);

        // Memory usage card
        JPanel usageCard = createInfoCard("Memory Usage");
        long totalMemory = mem.getTotal();
        long availableMemory = mem.getAvailable();
        long usedMemory = totalMemory - availableMemory;
        int memoryPercentage = (int) (((double) usedMemory / totalMemory) * 100);

        JProgressBar memBar = createProgressBar();
        memBar.setValue(memoryPercentage);
        memBar.setString(memoryPercentage + "% (" + formatBytes(usedMemory) + " / " + formatBytes(totalMemory) + ")");
        usageCard.add(memBar);

        // Memory details card
        JPanel detailsCard = createInfoCard("Memory Details");
        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        detailsGrid.setBackground(PANEL_COLOR);

        addDetailRow(detailsGrid, "Total Memory:", formatBytes(totalMemory));
        addDetailRow(detailsGrid, "Used Memory:", formatBytes(usedMemory));
        addDetailRow(detailsGrid, "Available Memory:", formatBytes(availableMemory));

        detailsCard.add(detailsGrid);

        memoryContentPanel.add(usageCard);
        memoryContentPanel.add(detailsCard);

        memoryPanel.add(memoryContentPanel, BorderLayout.CENTER);

        memoryPanel.revalidate();
        memoryPanel.repaint();
    }

    private void updateStoragePanel(List<OSFileStore> fsList) {
        // Clear existing components
        storagePanel.removeAll();

        JLabel titleLabel = createSectionTitle("Storage Information");
        storagePanel.add(titleLabel, BorderLayout.NORTH);

        JPanel storageContentPanel = new JPanel();
        storageContentPanel.setLayout(new BoxLayout(storageContentPanel, BoxLayout.Y_AXIS));
        storageContentPanel.setBackground(BACKGROUND_COLOR);

        for (OSFileStore fs : fsList) {
            JPanel diskCard = createInfoCard(fs.getName() + " (" + fs.getMount() + ")");

            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            long used = total - usable;
            int usagePercentage = (int) (((double) used / total) * 100);

            JProgressBar diskBar = createProgressBar();
            diskBar.setValue(usagePercentage);
            diskBar.setString(usagePercentage + "% (" + formatBytes(used) + " / " + formatBytes(total) + ")");
            diskCard.add(diskBar);

            JLabel typeLabel = new JLabel("Type: " + fs.getType());
            typeLabel.setForeground(SECONDARY_TEXT_COLOR);
            typeLabel.setFont(SMALL_FONT);
            diskCard.add(Box.createRigidArea(new Dimension(0, 10)));
            diskCard.add(typeLabel);

            storageContentPanel.add(diskCard);
            storageContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        JScrollPane scrollPane = new JScrollPane(storageContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        storagePanel.add(scrollPane, BorderLayout.CENTER);

        storagePanel.revalidate();
        storagePanel.repaint();
    }

    private void updateGpuPanel() {
        // Clear existing components
        gpuPanel.removeAll();

        JLabel titleLabel = createSectionTitle("GPU Information");
        gpuPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel gpuContentPanel = new JPanel();
        gpuContentPanel.setLayout(new BoxLayout(gpuContentPanel, BoxLayout.Y_AXIS));
        gpuContentPanel.setBackground(BACKGROUND_COLOR);

        List<GraphicsCard> gpus = hal.getGraphicsCards();
        if (gpus.isEmpty()) {
            JPanel noGpuCard = createInfoCard("No GPU Information");
            JLabel noGpuLabel = new JLabel("No graphics card information available");
            noGpuLabel.setForeground(TEXT_COLOR);
            noGpuLabel.setFont(REGULAR_FONT);
            noGpuCard.add(noGpuLabel);
            gpuContentPanel.add(noGpuCard);
        } else {
            for (GraphicsCard gpu : gpus) {
                JPanel gpuCard = createInfoCard("GPU: " + gpu.getName());

                JPanel gpuDetails = new JPanel(new GridLayout(3, 1, 5, 5));
                gpuDetails.setBackground(PANEL_COLOR);

                JLabel vendorLabel = new JLabel("Vendor: " + gpu.getVendor());
                vendorLabel.setForeground(TEXT_COLOR);
                vendorLabel.setFont(REGULAR_FONT);

                JLabel versionLabel = new JLabel("Version: " + gpu.getVersionInfo());
                versionLabel.setForeground(TEXT_COLOR);
                versionLabel.setFont(REGULAR_FONT);

                JLabel vramLabel = new JLabel("VRAM: " + formatBytes(gpu.getVRam()));
                vramLabel.setForeground(TEXT_COLOR);
                vramLabel.setFont(REGULAR_FONT);

                gpuDetails.add(vendorLabel);
                gpuDetails.add(versionLabel);
                gpuDetails.add(vramLabel);

                gpuCard.add(gpuDetails);
                gpuContentPanel.add(gpuCard);
                gpuContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(gpuContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        gpuPanel.add(scrollPane, BorderLayout.CENTER);

        gpuPanel.revalidate();
        gpuPanel.repaint();
    }

    private void updateSensorsPanel(Sensors sensors) {
        // Clear existing components
        sensorsPanel.removeAll();

        JLabel titleLabel = createSectionTitle("System Sensors");
        sensorsPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel sensorsContentPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        sensorsContentPanel.setBackground(BACKGROUND_COLOR);

        // CPU Temperature
        JPanel tempCard = createInfoCard("CPU Temperature");
        double cpuTemp = sensors.getCpuTemperature();
        if (cpuTemp > 0) {
            JProgressBar tempBar = createProgressBar();
            tempBar.setMaximum(100);  // Assuming 100°C is max safe
            tempBar.setValue((int) cpuTemp);

            // Change color based on temperature
            if (cpuTemp > 80) {
                tempBar.setForeground(new Color(232, 65, 24));  // Red for high temp
            } else if (cpuTemp > 60) {
                tempBar.setForeground(new Color(255, 165, 0));  // Orange for moderate temp
            } else {
                tempBar.setForeground(new Color(46, 204, 113));  // Green for good temp
            }

            tempBar.setString(df.format(cpuTemp) + " °C");
            tempCard.add(tempBar);
        } else {
            JLabel noTempLabel = new JLabel("Temperature information not available");
            noTempLabel.setForeground(TEXT_COLOR);
            noTempLabel.setFont(REGULAR_FONT);
            tempCard.add(noTempLabel);
        }

        // Fan Speeds
        JPanel fanCard = createInfoCard("Fan Speeds");
        int[] fanSpeeds = sensors.getFanSpeeds();
        if (fanSpeeds.length > 0) {
            JPanel fansPanel = new JPanel(new GridLayout(fanSpeeds.length, 1, 5, 5));
            fansPanel.setBackground(PANEL_COLOR);

            for (int i = 0; i < fanSpeeds.length; i++) {
                JLabel fanLabel = new JLabel("Fan " + (i + 1) + ": " + fanSpeeds[i] + " RPM");
                fanLabel.setForeground(TEXT_COLOR);
                fanLabel.setFont(REGULAR_FONT);
                fansPanel.add(fanLabel);
            }

            fanCard.add(fansPanel);
        } else {
            JLabel noFanLabel = new JLabel("Fan speed information not available");
            noFanLabel.setForeground(TEXT_COLOR);
            noFanLabel.setFont(REGULAR_FONT);
            fanCard.add(noFanLabel);
        }

        // CPU Voltage
        JPanel voltageCard = createInfoCard("CPU Voltage");
        double voltage = sensors.getCpuVoltage();
        if (voltage > 0) {
            JLabel voltageLabel = new JLabel(df.format(voltage) + " V");
            voltageLabel.setForeground(TEXT_COLOR);
            voltageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            voltageCard.add(voltageLabel);
        } else {
            JLabel noVoltageLabel = new JLabel("Voltage information not available");
            noVoltageLabel.setForeground(TEXT_COLOR);
            noVoltageLabel.setFont(REGULAR_FONT);
            voltageCard.add(noVoltageLabel);
        }

        // Power consumption (if available)
        JPanel powerCard = createInfoCard("Power Consumption");
        JLabel noPowerLabel = new JLabel("Power information not available");
        noPowerLabel.setForeground(TEXT_COLOR);
        noPowerLabel.setFont(REGULAR_FONT);
        powerCard.add(noPowerLabel);

        sensorsContentPanel.add(tempCard);
        sensorsContentPanel.add(fanCard);
        sensorsContentPanel.add(voltageCard);
        sensorsContentPanel.add(powerCard);

        sensorsPanel.add(sensorsContentPanel, BorderLayout.CENTER);

        sensorsPanel.revalidate();
        sensorsPanel.repaint();
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(SECONDARY_TEXT_COLOR);
        labelComponent.setFont(REGULAR_FONT);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setForeground(TEXT_COLOR);
        valueComponent.setFont(REGULAR_FONT);

        panel.add(labelComponent);
        panel.add(valueComponent);
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) {
            return "N/A";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return df.format(kb) + " KB";
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return df.format(mb) + " MB";
        }
        double gb = mb / 1024.0;
        return df.format(gb) + " GB";
    }

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Apply custom styling to UI components
        customizeUIDefaults();

        // Launch application
        SwingUtilities.invokeLater(SystemHealthMonitor::new);
    }

    private static void customizeUIDefaults() {
        // Customize global UI components
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbDarkShadow", new Color(30, 30, 30));
        UIManager.put("ScrollBar.thumb", new Color(80, 80, 80));
        UIManager.put("ScrollBar.thumbHighlight", new Color(80, 80, 80));
        UIManager.put("ScrollBar.thumbShadow", new Color(80, 80, 80));
        UIManager.put("ScrollBar.track", new Color(45, 45, 45));
        UIManager.put("ScrollPane.background", new Color(18, 18, 18));
        UIManager.put("TextArea.selectionBackground", new Color(75, 110, 175));
    }


}