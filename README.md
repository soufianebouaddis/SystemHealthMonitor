# 🖥️ System Health Monitor

A cross-platform desktop application built with **Java Swing** and **OSHI (Operating System and Hardware Information)** to monitor full system health and hardware statistics on **Windows**, **Linux**, and **macOS**.

## 📋 Features

- 🧠 **CPU Info**: Name, physical and logical cores, real-time CPU usage
- 💾 **Memory**: Total and available RAM
- 💽 **Disks**: Usable and total storage for each mounted file system
- 🌡️ **Sensors**: CPU temperature, fan speeds, CPU voltage
- 🎮 **Graphics Cards**: Name, vendor, version, VRAM
- 🖥️ **Displays**: Number of monitors with EDID information
- ⏱️ **System Uptime**
- 🌗 **Dark UI**: Styled console-like interface using Java Swing

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven

### Clone & Run

```bash
git clone https://github.com/soufianebouaddis/system-health-monitor.git
cd system-health-monitor
mvn clean install
mvn exec:java -Dexec.mainClass="SystemHealthMonitor"
```
### ⚠️ Known Limitations
- Some hardware details (e.g., CPU temperature, battery voltage, or fan speeds) may not be available on all systems due to OS or manufacturer limitations.
- You might see benign WMI warnings on Windows if your system doesn't expose certain thermal zones.
