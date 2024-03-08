package DatabaseManager;

import View.Animation;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class DatabaseManager {
    private static Connection connection;
    private static final String databaseName = "stock";
    private static String URL = "jdbc:postgresql://localhost:5432/";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "1234";

    public static Connection getConnection() throws SQLException {
        Animation animation = new Animation();

        try {
            animation.starting();
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            if (!databaseExists(connection, databaseName)) {
                createDatabase(databaseName);
                System.out.println("Database connected!");
            }
            DatabaseManager.URL += databaseName;
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println(DatabaseManager.URL);
            handleSQLException(e);
        }
        return connection;
    }

    public static void createStockTableIfNotExists() throws SQLException {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS stock (" +
                    "id SERIAL NOT NULL PRIMARY KEY," +
                    "name VARCHAR(200) NOT NULL," +
                    "unit_price DOUBLE PRECISION NOT NULL," +
                    "qty INTEGER NOT NULL," +
                    "imported_date DATE NOT NULL DEFAULT CURRENT_DATE)";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);

            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void createDatabase(String databaseName) {
        try {
            String sql = "CREATE DATABASE " + databaseName;
            try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.executeUpdate();
                System.out.println("Database created: " + databaseName);
                createStockTableIfNotExists();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static boolean databaseExists(Connection connection, String databaseName) {
        try {
            String sql = "SELECT datname FROM pg_database WHERE datname = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, databaseName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }

    }

    private static void handleSQLException(SQLException e) {
        e.printStackTrace();
        System.err.println("SQL Error: " + e.getMessage());
    }
    public static void dataInitializer() throws SQLException {
        try {
            String sql = """
                    INSERT INTO stock (name, unit_price, qty, imported_date)
                    VALUES
                        ('Granola bars (assorted)', 2.99, 20, '2024-03-05'),
                        ('Coffee beans (1kg)', 14.99, 15, '2024-03-04'),
                        ('Bottled water (500ml)', 0.89, 50, '2024-03-03'),
                        ('Apples (organic)', 2.49, 30, '2024-03-02'),
                        ('Canned tuna (200g)', 1.79, 25, '2024-03-01'),
                        ('Oatmeal (instant)', 2.29, 10, '2024-02-29'),
                        ('Pasta (whole wheat)', 1.99, 40, '2024-02-28'),
                        ('Crackers (cheese)', 3.49, 12, '2024-02-27'),
                        ('Orange juice (1L)', 2.79, 20, '2024-02-26'),
                        ('Frozen vegetables (mixed)', 3.99, 15, '2024-02-25');
                    """;
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }
    public void restoreBackup(String backupFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(backupFilePath))) {
            String line;
            StringBuilder sqlStatements = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sqlStatements.append(line);
                if (line.trim().endsWith(";")) {
                    executeSqlStatement(sqlStatements.toString());
                    sqlStatements.setLength(0);
                }
            }
            System.out.println("Backup data restored successfully.");
        } catch (IOException e) {
            System.out.println("Error restoring backup data: " + e.getMessage());
        }
    }

    private void executeSqlStatement(String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error executing SQL statement: " + e.getMessage());
        }
    }
    private static String getFormattedDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        return now.format(formatter);
    }
    public static void exportDataBackUp() {
        String formattedDate = getFormattedDate();
        String outputFileName = formattedDate + "_Stock.sql";
        String projectRoot = System.getProperty("user.dir");

        String sqlFilePath = projectRoot + "/";

        try {

            FileWriter fileWriter = new FileWriter(sqlFilePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Export data to the SQL file
            String[] command = {
                    projectRoot,
                    "-U",
                    USERNAME,
                    "-d",
                    databaseName,
                    "--data-only",
                    "--column-inserts",
                    "--table=users",
                    "--file=" + sqlFilePath
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(sqlFilePath)));
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Data exported successfully. SQL file: " + sqlFilePath);
            } else {
                System.out.println("Failed to export data.");
            }

            // Close the file writer and buffered writer
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error exporting data: " + e.getMessage());
        }
    }
    public static void backupDatabase(String backupFilePath) {
        try {
            String currentDir = System.getProperty("user.dir");
            String pgDumpPath = currentDir + "/";

            ProcessBuilder processBuilder = new ProcessBuilder(
                    pgDumpPath, "-U", USERNAME, "-d", URL, "-f", backupFilePath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Backup process returned non-zero exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
