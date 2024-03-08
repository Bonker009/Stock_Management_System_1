
import Controller.MainController;
import DAO.StockDAO;
import DAO.StockDAOImpl;
import DatabaseManager.DatabaseManager;
import View.StockView;

import java.sql.SQLException;
import java.sql.Connection;


public class Main {
    public static void main(String[] args) throws SQLException {
        StockDAO stockDAOImpl = null;
        Connection connection = null;
        try {
            connection = DatabaseManager.getConnection();
            DatabaseManager.createStockTableIfNotExists();
            DatabaseManager.dataInitializer();
            stockDAOImpl = new StockDAOImpl(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        MainController mainController = new MainController(stockDAOImpl);
        StockView stockView = new StockView(mainController);
//        DatabaseManager.exportDataBackUp();
        assert connection != null;
        stockView.start();

    }


}