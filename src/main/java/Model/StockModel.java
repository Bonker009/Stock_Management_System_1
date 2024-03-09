package Model;

import java.sql.Date;
import java.time.LocalDate;

public class StockModel {
    private int id;
    private String name;
    private double unitPrice;
    private int qty;
    private Date importedDate;
    public StockModel(){};
    public StockModel(String name, double unitPrice, int qty) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.qty = qty;
        this.importedDate = Date.valueOf(LocalDate.now());
    }

    public StockModel(int id, String name, double unitPrice, int qty,Date importedDate) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.qty = qty;
        this.importedDate = importedDate;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
    public Date getImportedDate() {
        return importedDate;
    }
    public void setImportedDate(Date importedDate) {
        this.importedDate = importedDate;
    }
    @Override
    public String toString() {
        return "Stock [id=" + id + ", name=" + name + ", unitPrice=" + unitPrice + ", quantity=" + qty + ", importedDate=" + importedDate + "]";
    }
}
