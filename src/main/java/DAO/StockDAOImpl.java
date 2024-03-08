package DAO;

import Model.StockModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static View.StockView.validateInput;

public class StockDAOImpl implements StockDAO {
    private final Connection connection;
    public static List<StockModel> stockModelsUnSaveInsert = new ArrayList<>();
    public static List<StockModel> getStockModelsUnSaveUpdate = new ArrayList<>();

    public StockDAOImpl(Connection connection) {
        this.connection = connection;
    }

    public static List<StockModel> getStockModelsUnSaveInsert() {
        return stockModelsUnSaveInsert;
    }

    public static List<StockModel> getGetStockModelsUnSaveUpdate() {
        return getStockModelsUnSaveUpdate;
    }

    @Override
    public List<StockModel> getAllStocks() throws SQLException {
        List<StockModel> stocks = new ArrayList<>();
        String sql = "SELECT id, name, unit_price, qty, imported_date FROM stock ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double unitPrice = resultSet.getDouble("unit_price");
                int qty = resultSet.getInt("qty");
                Date importedDate = resultSet.getDate("imported_date");
                StockModel stock = new StockModel(id, name, unitPrice, qty, importedDate);
                stocks.add(stock);

            }
        }
        return stocks;
    }

    @Override
    public void insertStockUnsaved(StockModel stock) {
        try {
            // Add the stock model to the unSaveInsert list
            stockModelsUnSaveInsert.add(stock);
            System.out.println("Stock added to unSaveInsert list.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertStock(StockModel stock) {
        String insertQuery = "INSERT INTO stock (name, unit_price, qty) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, stock.getName());
            statement.setDouble(2, stock.getUnitPrice());
            statement.setInt(3, stock.getQty());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Stock inserted successfully.");
            } else {
                System.out.println("Failed to insert stock.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public StockModel getStockById(int id) {
        String selectSql = "SELECT id, name, unit_price, qty, imported_date FROM stock WHERE id = ?";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            selectStatement.setInt(1, id);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    double unitPrice = resultSet.getDouble("unit_price");
                    int qty = resultSet.getInt("qty");
                    Date importedDate = resultSet.getDate("imported_date");
                    return new StockModel(id, name, unitPrice, qty, importedDate);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving stock by ID: " + id, e);
        }
    }

    @Override
    public void deleteStock(int id) {
        try {
            Scanner scanner = new Scanner(System.in);
            StockModel stock = getStockById(id);
            if (stock != null) {
                System.out.println("Stock Details ");
                System.out.println("ID : " + stock.getId());
                System.out.println("Name : " + stock.getName());
                System.out.println("Unit Price : " + stock.getUnitPrice());
                System.out.println("Quantity : " + stock.getQty());
                System.out.println("Imported date : " + stock.getImportedDate());
                String confirmation = validateInput(scanner, "Enter Y to confirm or Enter any other key to cancel: ", "^[A-Za-z]+$", "Invalid input. Please enter Y or N.");
                String deleteSql = "DELETE FROM stock WHERE id = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setInt(1, id);
                    int rowsDeleted = deleteStatement.executeUpdate();
                    if (rowsDeleted > 0) {
                        System.out.println("Stock with id : " + id + " deleted successfully ");
                    } else {
                        System.out.println("Failed to delete stock with id : " + id);
                    }
                }
            } else {
                System.out.println("Stock with ID " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting stock with ID: " + id, e);
        }
    }

    @Override
    public void updateStock(StockModel stock) {
        System.out.println(stock);
        String updateSql = "UPDATE stock SET name = ?, unit_price = ?, qty = ? WHERE id = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, stock.getName());
            updateStatement.setDouble(2, stock.getUnitPrice());
            updateStatement.setInt(3, stock.getQty());
            updateStatement.setInt(4, stock.getId());

            int rowsUpdated = updateStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Stock with ID " + stock.getId() + " updated successfully");
            } else {
                System.out.println("Failed to update stock with ID " + stock.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating stock with ID: " + stock.getId(), e);
        }
    }


    @Override
    public void updateStockUnsaved(StockModel stock) {
        try {
            getStockModelsUnSaveUpdate.add(stock);
            System.out.println("Stock added to getStockModelsUnSaveUpdate list.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<StockModel> searchStockByName(String name) {
        List<StockModel> matchingStocks = new ArrayList<>();
        String searchSql = "SELECT id, name, unit_price, qty, imported_date FROM stock WHERE name LIKE ?";
        try (PreparedStatement searchStatement = connection.prepareStatement(searchSql)) {
            searchStatement.setString(1, "%" + name + "%");
            try (ResultSet resultSet = searchStatement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String stockName = resultSet.getString("name");
                    double unitPrice = resultSet.getDouble("unit_price");
                    int qty = resultSet.getInt("qty");
                    Date importedDate = resultSet.getDate("imported_date");
                    StockModel stock = new StockModel(id, stockName, unitPrice, qty, importedDate);
                    matchingStocks.add(stock);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching stock by name: " + name, e);
        }
        return matchingStocks;
    }


    @Override
    public void saveStock() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Do you want to save Unsaved Insertions or Unsaved Updates? Please choose one of them:");
            System.out.println("'Ui' to save Unsaved Insertions");
            System.out.println("'Uu' to save Unsaved Updates");
            System.out.println("'B' to go back to the main menu");
            System.out.print("Enter your option : ");

            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "ui":
                    if (!stockModelsUnSaveInsert.isEmpty()) {
                        for (StockModel stock : stockModelsUnSaveInsert) {
                            insertStock(stock);
                        }
                        stockModelsUnSaveInsert.clear();
                        System.out.println("Unsaved insertions saved successfully.");
                    } else {
                        System.out.println("No unsaved insertions available.");
                    }
                    break;
                case "uu":
                    if (!getStockModelsUnSaveUpdate.isEmpty()) {
                        for (StockModel stock : getStockModelsUnSaveUpdate) {
                            updateStock(stock);
                        }
                        getStockModelsUnSaveUpdate.clear();
                        System.out.println("Unsaved updates saved successfully.");
                    } else {
                        System.out.println("No unsaved updates available.");
                    }
                    break;
                case "b":
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 'Ui', 'Uu', or 'B'.");
                    break;
            }
        }
    }



}
