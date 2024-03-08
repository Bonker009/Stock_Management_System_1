package DAO;

import Model.StockModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static View.StockView.validateInput;

public class StockDAOImpl implements StockDAO {
    private final Connection connection;

    public StockDAOImpl(Connection connection) {
        this.connection = connection;
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
        try{
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
    public void saveStock(StockModel stock) {
        try {
            if (getStockById(stock.getId()) != null) {
                System.out.println("Stock with ID " + stock.getId() + " already exists in the database.");
                return;
            }
            Scanner scanner = new Scanner(System.in);
            System.out.println("Are you sure you want to save the stock? (Y/N)");
            String confirmation = scanner.next().toUpperCase().trim();
            if ("Y".equalsIgnoreCase(confirmation)) {
                String insertSql = "INSERT INTO stock (id, name, unit_price, qty, imported_date) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    insertStatement.setInt(1, stock.getId());
                    insertStatement.setString(2, stock.getName());
                    insertStatement.setDouble(3, stock.getUnitPrice());
                    insertStatement.setInt(4, stock.getQty());
                    insertStatement.setDate(5, new java.sql.Date(stock.getImportedDate().getTime()));

                    int rowsInserted = insertStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Stock with ID " + stock.getId() + " saved successfully.");
                    } else {
                        System.out.println("Failed to save stock with ID " + stock.getId() + ".");
                    }
                }
            } else {
                System.out.println("Save operation canceled.");
            }
            scanner.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving stock with ID: " + stock.getId(), e);
        }
    }

    @Override
    public void unSaveStock() {

    }

}
