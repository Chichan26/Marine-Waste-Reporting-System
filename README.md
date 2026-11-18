# BantayDagat

Marine Waste Reporting & Incident Management System built with Java 17+, JavaFX, and Maven.

## Run the app

Open PowerShell in the folder containing `pom.xml`. Install a JDK 17+ and Maven 3, then run:

```powershell
java -version
mvn -version
mvn clean javafx:run
```

If Maven is available through IntelliJ IDEA, open `pom.xml` as a project and run `clean javafx:run` in its Maven tool window. Maven needs access to Maven Central the first time to download JavaFX and build plugins.

The first launch seeds classroom demonstration accounts and sample reports. Review `DemoData.java` and change the demo credentials before using the application outside a local classroom demo. Runtime files such as `data.ser` and `uploads/` are generated locally and should not be committed.

## Print a numbered copy

```powershell
java PrintNumbered.java "src/main/java/ph/bantaydagat/IncidentManager.java"
```

The block labels help locate deleted lines. `BantayDagat-Full-Source.md` contains the numbered source and debugging guide.
