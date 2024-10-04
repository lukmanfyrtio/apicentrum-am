import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.nio.file.Paths;

public class ApiCentrumEngine {
    public static void main(String[] args) {
        String jarFilePath = "./ApiCentrumEngine.jar"; // Replace with your JAR file path
        String dirName = "apicentrum";
        Path dirPath = Paths.get("/tmp/" + dirName);
        try {

          if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            } 

            try (JarFile jarFile = new JarFile(jarFilePath)) {
                System.out.println("Extracting files from " + jarFilePath + ":");

                long startTime = System.currentTimeMillis(); // Start time for extraction
                long totalEntries = jarFile.size(); // Total entries in the JAR file
                AtomicLong extractedEntries = new AtomicLong(0); // Counter for extracted entries

                jarFile.stream().forEach(jarEntry -> {
                    if (!jarEntry.isDirectory()) { // Check if the entry is not a directory
                        Path file = dirPath.resolve(jarEntry.getName()); // Use the entry name for the file

                        // Create parent directories if needed
                        try {
                            Files.createDirectories(file.getParent());
                        } catch (IOException e) {
                            System.err.println("Error creating directories for " + file.toString() + ": " + e.getMessage());
                        }

                        // Check if the file already exists
                        if (Files.exists(file)) {
                            extractedEntries.incrementAndGet(); // Increment extracted entries even for skipped files
                        } else {
                            try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                                // Write the content of the JAR entry to the temporary file
                                Files.copy(inputStream, file);
                                extractedEntries.incrementAndGet(); // Increment counter for successfully extracted entries
                                if (jarEntry.getName().endsWith(".sh")) {
                                    setExecutablePermission(file);
                                }
                            } catch (IOException e) {
                                System.err.println("Error extracting " + jarEntry.getName() + ": " + e.getMessage());
                                e.printStackTrace(); // Print the stack trace for debugging
                            }
                        }

                        // Display loading progress
                        displayLoadingProgress(extractedEntries.get(), totalEntries);
                    }
                });

                long endTime = System.currentTimeMillis(); // End time for extraction
                System.out.println("\nExtraction completed in " + (endTime - startTime) + " milliseconds.");

                // Execute the api-manager.sh script
                runShellScript(dirPath.resolve("apicentrum-am-4.3.0/bin/api-manager.sh"));
            } catch (IOException e) {
                System.err.println("Error reading the JAR file: " + e.getMessage());
                e.printStackTrace(); // Print the stack trace for debugging
            }
        } catch (IOException e) {
            System.err.println("Error creating temporary directory: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }

private static void setExecutablePermission(Path scriptPath) {
    if (Files.exists(scriptPath)) {
        try {
            // Use ProcessBuilder to execute the "chmod +x" command
            ProcessBuilder processBuilder = new ProcessBuilder("chmod", "+x", scriptPath.toString());
            processBuilder.inheritIO(); // Use the current process's input and output
            Process process = processBuilder.start(); // Start the process
            int exitCode = process.waitFor(); // Wait for the process to complete
        } catch (IOException | InterruptedException e) {
            System.err.println("Error setting executable permission: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for debugging
        }
    } else {
        System.err.println("Script not found: " + scriptPath.toString());
    }
}


    private static void runShellScript(Path scriptPath) {
        if (Files.exists(scriptPath)) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", scriptPath.toString());
                processBuilder.inheritIO(); // Use the current process's input and output

                System.out.println("Starting apicentrum engine");
                Process process = processBuilder.start(); // Start the process
                int exitCode = process.waitFor(); // Wait for the process to complete
                System.out.println("Script executed with exit code: " + exitCode);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error executing script: " + e.getMessage());
                e.printStackTrace(); // Print the stack trace for debugging
            }
        } else {
            System.err.println("Script not found: " + scriptPath.toString());
        }
    }

    private static void displayLoadingProgress(long current, long total) {
        int percent = (int) ((current * 100) / total);
        StringBuilder loadingBar = new StringBuilder("\rLoading: [");

        int barLength = 50; // Length of the loading bar
        int progressLength = (percent * barLength) / 100;

        // Build the loading bar
        for (int i = 0; i < barLength; i++) {
            if (i < progressLength) {
                loadingBar.append("=");
            } else {
                loadingBar.append(" ");
            }
        }
        loadingBar.append("] ").append(percent).append("%");
        System.out.print(loadingBar.toString());
    }
}
