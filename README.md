# System Monitor TUI

System Monitor TUI is a small Java application that displays real-time system information in a terminal user interface. The goal of this project is to provide a lightweight, easy-to-run, and easy-to-extend system monitor without a traditional GUI or native dependencies.

The application collects metrics using [OSHI](https://github.com/oshi/oshi) and renders them in a text-based dashboard built with [Lanterna](https://github.com/mabe02/lanterna). The codebase is intentionally split between service classes that retrieve system data (CPU, memory, OS, disks, network, processes, etc.) and a TUI layer responsible only for layout and drawing, making it straightforward to maintain and extend.

## Features

The dashboard currently shows:

- **CPU**
    - Human-readable CPU model name.
    - Physical and logical core counts.
    - Base frequency in GHz (when available).
    - Current system CPU usage as a percentage.

- **Memory**
    - RAM usage as a percentage.
    - Total and used physical memory (GB).
    - Total and used swap space (GB).

- **Disks**
    - List of mounted file systems, automatically filtering technical/system-only mounts (e.g., tmpfs, proc, sysfs, EFI, special Windows device paths).
    - For each disk: name, mount point, used percentage, and used/total capacity in GB.
    - `S` toggle to show/hide “hidden” disks (technical/virtual mounts).

- **Operating System**
    - OS family (Windows, Linux, macOS, etc.).
    - OS version string.
    - Bitness (32/64 bit).
    - Uptime formatted as `X days, Y hours, Z minutes`.
    - Current process and thread counts.

- **Network**
    - Hostname.
    - IPv4 default gateway.
    - Configured DNS servers (or an explicit “none configured” message).

- **Top processes (CPU)**
    - List of the most CPU-intensive processes, sorted by decreasing CPU usage.
    - For each process: PID, name, cumulative CPU usage percentage, and resident memory size in megabytes.

## TUI Layout

The UI is composed of multiple rectangular boxes with dynamically computed dimensions:

- A top row of boxes: **CPU**, **RAM**, **Disks**, and **Network**, laid out side by side horizontally.
- A horizontal separator line under the top row.
- A **“Top processes (CPU)”** box aligned under the CPU box in the lower section.

The `Box` class is responsible for:

- Computing the exact height from the number of content lines plus optional internal separators.
- Drawing the border, title, and content consistently.

The layout logic:

- Computes per-box height using the same formula as `Box`.
- Finds the tallest box in the top row and uses that to place the separator line deterministically.
- Calculates the required terminal rows and columns for the full dashboard (including an extra safety margin and the footer) and shows a **“Terminal too small”** warning if the current terminal size is insufficient.

## Keyboard Controls

While the application is running:

- `Q` – quit the application.
- `S` – toggle visibility of “hidden” disks (system/technical mounts).

The dashboard is continuously redrawn in a loop, updating system metrics in real time.

## Why this project is interesting

This project is designed to showcase several useful skills for backend / systems / Java roles:

- **System-level monitoring in Java**  
  Uses OSHI to read real hardware and OS metrics (CPU, memory, disks, processes, network) in a cross-platform way.

- **Terminal UI design and layout**  
  Demonstrates non-trivial TUI layout with Lanterna: dynamic box sizing, separator lines, resize handling, and graceful degradation when the terminal is too small.

- **Clean separation of concerns**  
  Service classes encapsulate data collection, while `DashboardTuiApp` focuses on rendering and input handling. This makes the codebase easier to extend (e.g., adding GPU metrics, temperature sensors, or a “Top processes (MEM)” view by reusing the same structure).

## Requirements

To build or run the project you need:

- Java 21 installed on your system.
- Maven, if you want to build the project from source.
- A standard terminal/console on Windows, Linux, or macOS.

You can verify your environment with:

```bash
java -version
mvn -version
```

## Build

From the project root:

```bash
mvn clean package
```

This compiles the project and produces a JAR file under the `target/` directory:

```text
target/<artifact-name>-<version>.jar
```

## Run

### Run from JAR

Once the JAR is built, the most common way to start the application is:

```bash
java -jar target/<artifact-name>-<version>.jar
```

If the project is configured to produce a runnable (fat/uber) JAR including dependencies, this command works the same on all major operating systems that have Java installed.

### Run from source with Maven

To run directly from source using Maven:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="it.ximno.DashboardTuiApp"
```

## OS-specific notes

### Windows

From `cmd.exe` or PowerShell:

```powershell
cd path\to\project
mvn clean package
java -jar target\<artifact-name>-<version>.jar
```

### Linux

From a shell:

```bash
cd /path/to/project
mvn clean package
java -jar target/<artifact-name>-<version>.jar
```

### macOS

From Terminal:

```bash
cd /path/to/project
mvn clean package
java -jar target/<artifact-name>-<version>.jar
```

## Notes

- Once you know the actual JAR name, it is recommended to replace `<artifact-name>` and `<version>` placeholders with the real values.
- If the project does not yet produce a fully runnable JAR, you may want to document or add the Maven plugin configuration used for packaging and running the application (e.g., shade or assembly plugin).
- The code is intentionally designed to be easy to extend; new boxes or metrics can be added with minimal changes to the existing layout and services.