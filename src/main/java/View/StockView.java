package View;

import Controller.MainController;
import Model.StockModel;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.io.IOException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static View.ColorCode.red;
import static View.ColorCode.reset;

public class StockView {
    private Scanner scanner;
    private static Integer currentPage = 1;
    private static Integer pageSize = 1;
    private static Integer rowPerPage = 4;
    private final MainController mainController;


    public StockView(MainController mainController) {
        this.mainController = mainController;
        this.scanner = new Scanner(System.in);
    }

    public void displayMenu() throws SQLException {
        System.out.println("🌞".repeat(42));
        CellStyle textAlign = new CellStyle(CellStyle.HorizontalAlign.CENTER);
        Table table = new Table(5, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);

        System.out.println(table.render());
        System.out.print("F) First");
        System.out.print("\t\tP) Previous");
        System.out.print("\t\tN) Next");
        System.out.print("\t\tL) Last");
        System.out.println("\t\tG) Go to\n");
        System.out.println("*) Display\n");
        System.out.print("W) Write ");
        System.out.print("\t\tR) Read");
        System.out.print("\t\tU) Update");
        System.out.print("\t\tD) Delete");
        System.out.print("\t\tS) Search");
        System.out.println("\t\tSe) Set Row\n");
        System.out.print("Sa) Save ");
        System.out.print("\t\tBa) Backup");
        System.out.print("\t\tRe) Restore");
        System.out.println("\t\tE) Exit\n");
        System.out.println("🌞".repeat(42));
    }

    public void start() throws SQLException {
        int currentPage = 1;

        String choice;
        while (true) {
            displayProducts(mainController.getAllStocks(), currentPage);
            displayMenu();
            choice = validateInput(scanner, "Enter your option: ", "^[A-Za-z]+$", red + "The option can only be entered in text format!!" + reset);
            switch (choice.toUpperCase()) {
                case "F":
                    currentPage = 1;
                    break;
                case "P":
                    currentPage = Math.max(currentPage - 1, 1);
                    break;
                case "N":
                    currentPage = Math.min(currentPage + 1, pageSize);
                    break;
                case "L":
                    currentPage = pageSize;
                    break;
                case "G":
                    System.out.print("Enter page number: ");
                    currentPage = Math.min(scanner.nextInt(), pageSize);
                    break;
                case "W":
                    String name = validateInput(scanner, "Enter the product name: ", "^[A-Za-z]+$", "incorrect product name format");
                    double unit_price = Double.parseDouble(validateInput(scanner, "Enter the product unit price: ", "^[0-9]+$", "The Unit price can be in number format only"));
                    int qty = Integer.parseInt(validateInput(scanner, "Enter the quantity of product: ", "^[0-9]+$", "The quantity can be in number format only!!!"));
                    mainController.insertStock(new StockModel(name, unit_price, qty));
                    break;
                case "R":
                    int idToRead = Integer.parseInt(validateInput(scanner, "Enter Product ID to Display: ", "^[0-9]+$", "The Id can be in number format only!!!!"));
                    StockModel stockModel2 = mainController.getStockById(idToRead);
                    displayProductDetails(stockModel2);
                    scanner.nextLine();
                    break;
                case "U":
                    StockModel stockModel1 = null;
                    while (stockModel1 == null) {
                        int idToUpdate = Integer.parseInt(validateInput(scanner, "Enter Product Id to update: ", "^[0-9]+$", "The Id can be number format only!!!!"));
                        stockModel1 = mainController.getStockById(idToUpdate);

                        if (stockModel1 == null) {
                            System.out.println("No stock found with ID: " + idToUpdate);
                            System.out.println("Please enter a valid Product ID.");
                        }
                    }
                    displayProductDetails(stockModel1);
                    String newName = validateInput(scanner, "Enter the new name : ", "^[A-Za-z]+$", "Incorrect name format!!");
                    double newUnit = Double.parseDouble(validateInput(scanner, "Enter new unit price : ", "^[0-9]+$", "Incorrect unit price format!!"));
                    int newQty = Integer.parseInt(validateInput(scanner, "Enter the new Qty : ", "^[0-9]+$", "Incorrect qty format!!"));
                    mainController.updateStock(new StockModel(stockModel1.getId(), newName, newUnit, newQty, stockModel1.getImportedDate()));
                    break;
                case "D":
                    int idToDelete = Integer.parseInt(validateInput(scanner, "Enter Product Id to delete", "[0-9]+$", "The ID can be in number format only !!!!"));
                    mainController.deleteStock(idToDelete);
                    break;
                case "S":
                    Table table5 = new Table(5, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.ALL);

                    String searchName = validateInput(scanner, "Enter Product Name to Search : ", "^[A-Za-z]+$", "The Name can be in text only!!!!");
                    List<StockModel> stockModel = mainController.searchStockByName(searchName);
                    addTableHeader(table5);
                    table5.setColumnWidth(0, 5, 30);
                    table5.setColumnWidth(1, 15, 35);
                    table5.setColumnWidth(2, 15, 20);
                    table5.setColumnWidth(3, 15, 20);
                    table5.setColumnWidth(4, 15, 20);
                    stockModel.forEach(stockModel3 -> {
                        addProductRow(table5,stockModel3);
                    } );
                    System.out.println(table5.render());
                    scanner.nextLine();
                    break;
                case "Se":
                    setRow();
                    break;
                case "Sa":

                    break;
                case "Ba":
                    // Handle backup option
                    break;
                case "Re":
                    // Handle restore option
                    break;
                case "E":
                    System.exit(0);
                default:
                    System.out.println("Your choice is not in the options (Please Try Again)");
            }
        }
    }

    public static String validateInput(Scanner scanner, String input, String pattern, String invalidMessage) {
        String userInput;
        Pattern regex = Pattern.compile(pattern);
        do {
            System.out.print(input);
            userInput = scanner.nextLine().trim();
            if (!regex.matcher(userInput).matches()) {
                System.out.println(invalidMessage);
            }
        } while (!regex.matcher(userInput).matches());
        return userInput;
    }

    public void displayProducts(List<StockModel> stockModelList, int currentPage) {
        CellStyle textAlign = new CellStyle(CellStyle.HorizontalAlign.CENTER);
        pageSize = (int) Math.ceil((double) stockModelList.size() / rowPerPage);
        int startIndex = (currentPage * rowPerPage) - rowPerPage;
        int endIndex = Math.min(startIndex + rowPerPage, stockModelList.size());
        Table table = new Table(5, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE,ShownBorders.ALL);
        table.setColumnWidth(0, 5, 30);
        table.setColumnWidth(1, 15, 35);
        /**/
        table.setColumnWidth(2, 15, 20);
        table.setColumnWidth(3, 15, 20);
        table.setColumnWidth(4, 15, 20);

        addTableHeader(table);
        for (int i = startIndex; i < endIndex; i++) {
            StockModel stock  = stockModelList.get(i);
            addProductRow(table, stock);
        }
        table.addCell(red+"Page : "+currentPage +" of "+pageSize,textAlign,2);
        table.addCell(red+"Total Record : "+ stockModelList.size(),textAlign,3);
        System.out.println(table.render());
    }

    private void addTableHeader(Table table) {
        CellStyle textAlign = new CellStyle(CellStyle.HorizontalAlign.CENTER);
        table.addCell(red + "ID" + reset, textAlign);
        table.addCell(red + "Name" + reset, textAlign);
        table.addCell(red + "Unit Price" + reset, textAlign);
        table.addCell(red + "QTY" + reset, textAlign);
        table.addCell(red + "Imported At" + reset, textAlign);
    }

    private void addProductRow(Table table, StockModel stockModel) {
        CellStyle textAlign = new CellStyle(CellStyle.HorizontalAlign.CENTER);
        table.addCell(red + stockModel.getId(), textAlign);
        table.addCell(red + stockModel.getName(), textAlign);
        table.addCell(red + stockModel.getUnitPrice(), textAlign);
        table.addCell(red + stockModel.getQty(), textAlign);
        table.addCell(red + stockModel.getImportedDate() + reset, textAlign);
    }

    public void setRow() {
        System.out.println("y".repeat(15));
        System.out.println("# Set rows to display in table");
        int rowInput = 0;
        boolean validInput = false;
        while (!validInput) {
            try {
                System.out.print("> Enter number of rows: ");
                rowInput = scanner.nextInt();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.next();
            }
        }

        scanner.nextLine();

        System.out.print("> Confirm to set rows display? [y/n]: ");
        String save = scanner.nextLine();
        if (save.equalsIgnoreCase("y")) {
            rowPerPage = rowInput;
            System.out.println("# Rows set successfully!");
        }
        System.out.println("#".repeat(15));
    }

    public void displayProductDetails(StockModel stockModel) {
        if (stockModel != null) {
            Table table = new Table(1, BorderStyle.UNICODE_ROUND_BOX,ShownBorders.SURROUND_HEADER_AND_COLUMNS);
            table.setColumnWidth(0,35,100);
            table.addCell(red+ "Product", new CellStyle(CellStyle.HorizontalAlign.CENTER));
            table.addCell(red+"Id : "+ stockModel.getId());
            table.addCell(red+"Name : "+ stockModel.getName());
            table.addCell(red+"Price : "+ stockModel.getUnitPrice());
            table.addCell(red+"QTY : "+ stockModel.getQty());
            table.addCell(red+"Imported date : "+stockModel.getImportedDate());
            System.out.println(table.render());
            System.out.println("-> Press any key to continue...");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Product not found.");
        }
    }

    public void previewUpdate(String code, String name, double price, int qty, String date) {
        Table table = new Table(1, BorderStyle.UNICODE_BOX_DOUBLE_BORDER_WIDE, ShownBorders.SURROUND);
        System.out.println("Product's All Details Preview After Update:");
        table.addCell(red + "ID" + " ".repeat(20) + ": " + code);
        table.addCell(red + "Name" + " ".repeat(18) + ": " + name);
        table.addCell(red + "Unit Price" + " ".repeat(12) + ": " + price);
        table.addCell(red + "Qty" + " ".repeat(19) + ": " + qty);
        table.addCell(red + "Imported Date" + " ".repeat(9) + ": " + date + reset);
        System.out.println(table.render());
    }
}