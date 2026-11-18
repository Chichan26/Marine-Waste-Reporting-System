// FILE 28 of 28: PrintNumbered.java
// [F28-B01] Standalone utility dependencies
import java.io.IOException; // [F28-B01] Handle unreadable source files
import java.nio.file.Files; // [F28-B01] Read UTF-8 source
import java.nio.file.Path; // [F28-B01] Convert the command-line argument

public final class PrintNumbered {
    // [F28-B02] Check the argument and print the numbered source
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java PrintNumbered.java <source-file>");
            System.exit(1);
        }
        try {
            print(Path.of(args[0]));
        } catch (IOException e) {
            System.err.println("Could not read source: " + e.getMessage());
            System.exit(1);
        }
    }

    // [F28-B03] Count and print each line, including blank lines
    private static void print(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            int number = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.printf("%4d | %s%n", number++, line); // [F28-B03] Increment after printing
            }
        }
    }
}

