package DatabaseManager;

import Model.StockModel;
import View.Animation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;


public class DatabaseManager {
    private static Connection connection;
    private static final String databaseName = "stock";
    private static final String tableName = "stock";
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


            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println(DatabaseManager.URL);
            handleSQLException(e);
        }
        return connection;
    }

    private static void handleSQLException(SQLException e) {
        e.printStackTrace();
        System.err.println("SQL Error: " + e.getMessage());
    }

    public static void createStockTableIfNotExists() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(200) NOT NULL, " +
                    "unit_price DECIMAL(10, 2) NOT NULL, " +
                    "qty INTEGER NOT NULL, " +
                    "imported_date DATE NOT NULL DEFAULT CURRENT_DATE)";

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
                System.out.println("Table created successfully: " + tableName);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public static void dataInitializer() {
        try {
            String sql = "INSERT INTO " + tableName + " (name, unit_price, qty, imported_date) VALUES " +
                    "('Granola bars (assorted)', 2.99, 20, '2024-03-05')," +
                    "('Coffee beans (1kg)', 14.99, 15, '2024-03-04')," +
                    "('Bottled water (500ml)', 0.89, 50, '2024-03-03')," +
                    "('Apples (organic)', 2.49, 30, '2024-03-02')," +
                    "('Canned tuna (200g)', 1.79, 25, '2024-03-01')," +
                    "('Oatmeal (instant)', 2.29, 10, '2024-02-29')," +
                    "('Pasta (whole wheat)', 1.99, 40, '2024-02-28')," +
                    "('Crackers (cheese)', 3.49, 12, '2024-02-27')," +
                    "('Orange juice (1L)', 2.79, 20, '2024-02-26')," +
                    "('Frozen vegetables (mixed)', 3.99, 15, '2024-02-25')";

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
                System.out.println("Data initialized successfully.");
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

    public static void generateNextBackup(String fileNameFormat, List<StockModel> stockModelList) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        int nextBackupNumber = findNextBackupNumber(fileNameFormat, currentDate);
        String fileName = "BackUpDataStorage/" + fileNameFormat + "_" + String.format("%02d", nextBackupNumber) + "_" + currentDate + ".sql";
        String content = generateSQLScript(stockModelList);
        writeToFile(fileName, content);
    }
    public static int findNextBackupNumber(String baseFileName, String currentDate) {
        int nextBackupNumber = 1;
        while (new File("BackUpDataStorage/" + baseFileName + "_" + String.format("%02d", nextBackupNumber) + "_" + currentDate + ".sql").exists()) {
            nextBackupNumber++;
        }
        return nextBackupNumber;
    }

    public static String generateSQLScript(List<StockModel> stockModelList) {
        StringBuilder insertCommand = new StringBuilder();
        insertCommand.append("INSERT INTO ").append(tableName).append(" (name, unit_price, qty, imported_date) VALUES ");
        boolean isFirst = true;
        for (StockModel stockModel : stockModelList) {
            if (!isFirst) {
                insertCommand.append(", ");
            }
            insertCommand.append("(")
                    .append("'").append(stockModel.getName()).append("', ")
                    .append(stockModel.getUnitPrice()).append(", ")
                    .append(stockModel.getQty()).append(", ")
                    .append("'").append(stockModel.getImportedDate()).append("'")
                    .append(")");
            isFirst = false;
        }
        insertCommand.append(";");

        return insertCommand.toString();
    }

    public static void writeToFile(String fileName, String content) {
        try {
            Path path = Paths.get(fileName);

            Files.write(path, content.getBytes());
            System.out.println("SQL file " + fileName + " created successfully.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static String[] getBackupFileNames(String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new Exception("The provided path is not a directory.");
        }
        return directory.list();
    }
    public static void executeBackupFile(String filename) throws IOException {
        String content = Files.readString(Paths.get(filename));
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE "+tableName);
            statement.executeUpdate(content);
            System.out.println("Database restore successful!.");
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

}


