# System Monitor TUI

System Monitor TUI is a small Java application that displays real-time system information in a terminal user interface. The goal of this project is to provide a lightweight, easy-to-run, and easy-to-extend system monitor without a traditional GUI or native dependencies.

The application collects metrics using OSHI and renders them in a text-based dashboard. The codebase is intentionally split between service classes that retrieve system data (CPU, memory, OS, disks, etc.) and a TUI layer responsible only for drawing, making it straightforward to maintain and extend.

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

This command compiles the project and produces a JAR file under the `target/` directory.

The exact file name depends on your `pom.xml`, but will typically look like:

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

If you prefer to run directly from source rather than using the JAR, you can use Maven. Assuming the Maven `exec` plugin is configured, the typical pattern looks like:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="it.ximno.DashboardTuiApp"
```

## Operating Systems

### Windows

From `cmd.exe` or PowerShell:

```powershell
cd path\to\project
mvn clean package
java -jar target\<artifact-name>-<version>.jar
```

As long as Java and Maven are on the `PATH`, there are no additional Windows-specific steps required.

### Linux

From a shell:

```bash
cd /path/to/project
mvn clean package
java -jar target/<artifact-name>-<version>.jar
```

On Linux, the application runs as a normal console program, provided Java is installed and available on the `PATH`.

### macOS

From Terminal:

```bash
cd /path/to/project
mvn clean package
java -jar target/<artifact-name>-<version>.jar
```

On macOS the flow is identical to Linux, with differences only in how Java and Maven are installed/configured on the system.

## Notes

- Once you know the actual JAR name, it is recommended to replace `<artifact-name>` and `<version>` placeholders with the real values.
- If the project does not yet produce a fully runnable JAR, you may want to document or add the Maven plugin configuration used for packaging and running the application.