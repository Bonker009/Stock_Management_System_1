package DAO;

import Model.StockModel;

import java.sql.SQLException;
import java.util.List;

public interface StockDAO {
    List<StockModel> getAllStocks() throws SQLException;
    void insertStock(StockModel stock);
    StockModel getStockById(int id);
    void deleteStock(int id);
    void updateStock(StockModel stock);
    List<StockModel> searchStockByName(String name);
    void saveStock(StockModel stock)  throws SQLException;
    void unSaveStock();
}
