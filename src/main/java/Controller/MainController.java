package Controller;

import DAO.StockDAO;
import DAO.StockDAOImpl;
import Model.StockModel;

import java.sql.SQLException;
import java.util.List;

public class MainController {
    private final StockDAO stockDAO;

    public MainController(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public List<StockModel> getAllStocks() throws SQLException {
        List<StockModel> stocks = stockDAO.getAllStocks();
        return stocks;
    }

    public List<StockModel> searchStockByName(String name) {
        return stockDAO.searchStockByName(name);
    }

    public void insertStock(StockModel stockModel) throws SQLException {
        stockDAO.insertStockUnsaved(stockModel);
    }

    public void deleteStock(int id) {
        stockDAO.deleteStock(id);
    }

    public StockModel getStockById(int id) {
        return stockDAO.getStockById(id);
    }

    public void updateStock(StockModel stockModel) {
        stockDAO.updateStockUnsaved(stockModel);
    }

    public List<StockModel> unSavedUpdateStock() {
        return StockDAOImpl.getGetStockModelsUnSaveUpdate();
    }

    public List<StockModel> unSavedInsertStock() {
        return StockDAOImpl.getStockModelsUnSaveInsert();
    }

    public void saveStock() {
        stockDAO.saveStock();
    }

}

