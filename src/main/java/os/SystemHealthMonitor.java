package os;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSFileStore;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.time.Duration;

public class SystemHealthMonitor extends JFrame {

    private JTextArea systemInfoArea;
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = systemInfo.getHardware();
    private final OperatingSystem os = systemInfo.getOperatingSystem();
    private final DecimalFormat df = new DecimalFormat("0.00");
    private long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();

    public SystemHealthMonitor() {
        setTitle("Full System Information");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        systemInfoArea = new JTextArea();
        systemInfoArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        systemInfoArea.setEditable(false);
        systemInfoArea.setBackground(new Color(30, 30, 30));
        systemInfoArea.setForeground(new Color(0, 255, 0));

        JScrollPane scrollPane = new JScrollPane(systemInfoArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Info"));
        add(scrollPane, BorderLayout.CENTER);

        Timer timer = new Timer(3000, e -> updateStats());
        timer.start();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateStats() {
        StringBuilder sb = new StringBuilder();

        // OS Info
        sb.append("Operating System: ").append(os).append("\n\n");

        // CPU Info
        CentralProcessor cpu = hal.getProcessor();
        sb.append("CPU: ").append(cpu.getProcessorIdentifier().getName()).append("\n");
        sb.append("Physical Cores: ").append(cpu.getPhysicalProcessorCount()).append("\n");
        sb.append("Logical Cores: ").append(cpu.getLogicalProcessorCount()).append("\n");
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks);
        prevTicks = cpu.getSystemCpuLoadTicks();
        sb.append("CPU Load: ").append(df.format(cpuLoad * 100)).append("%\n\n");

        // Memory Info
        GlobalMemory mem = hal.getMemory();
        sb.append("Memory Total: ").append(formatBytes(mem.getTotal())).append("\n");
        sb.append("Memory Available: ").append(formatBytes(mem.getAvailable())).append("\n\n");

        // Disk Info
        List<OSFileStore> fsList = os.getFileSystem().getFileStores();
        for (OSFileStore fs : fsList) {
            sb.append("Disk: ").append(fs.getMount()).append(" - ")
                    .append(formatBytes(fs.getUsableSpace())).append(" / ")
                    .append(formatBytes(fs.getTotalSpace())).append("\n");
        }
        sb.append("\n");

        // Sensors
        Sensors sensors = hal.getSensors();
        sb.append("CPU Temperature: ").append(df.format(sensors.getCpuTemperature())).append(" °C\n");
        sb.append("Fan Speeds: ");
        for (int speed : sensors.getFanSpeeds()) {
            sb.append(speed).append(" RPM ");
        }
        sb.append("\nBattery Voltage: ").append(sensors.getCpuVoltage()).append(" V\n\n");

        // Graphics Cards
        List<GraphicsCard> gpus = hal.getGraphicsCards();
        for (GraphicsCard gpu : gpus) {
            sb.append("GPU: ").append(gpu.getName()).append("\n");
            sb.append("Vendor: ").append(gpu.getVendor()).append("\n");
            sb.append("Version: ").append(gpu.getVersionInfo()).append("\n");
            sb.append("VRAM: ").append(formatBytes(gpu.getVRam())).append("\n\n");
        }

        // Display Info
        List<Display> displays = hal.getDisplays();
        int displayIndex = 1;
        for (Display d : displays) {
            byte[] edid = d.getEdid();
            sb.append("Display ").append(displayIndex++).append(": EDID Length = ")
                    .append(edid != null ? edid.length : 0).append(" bytes\n");
        }

        // Uptime (get from OperatingSystem not CPU)
        long uptimeSec = os.getSystemUptime();
        Duration uptime = Duration.ofSeconds(uptimeSec);
        long hours = uptime.toHours();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();
        sb.append("\nSystem Uptime: ").append(String.format("%d h %d m %d s", hours, minutes, seconds)).append("\n");

        systemInfoArea.setText(sb.toString());
    }

    private String formatBytes(long bytes) {
        double gb = bytes / (1024.0 * 1024 * 1024);
        return df.format(gb) + " GB";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SystemHealthMonitor::new);
    }
}