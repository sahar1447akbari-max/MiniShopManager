import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static Scanner input = new Scanner(System.in);
    static double totalRevenue = 0;
    static int totalItemsSold = 0;

    public static void main(String[] args) {
        ArrayList<Product> products = loadProducts();
        loadSales();

        if (products.isEmpty()) {
            products.add(new Product("Notebook", 2.5, 10));
            products.add(new Product("Pen", 0.5, 50));
        }

        int choice = 0;

        while (choice != 6) {
            System.out.println("\n--- Mini Shop Manager ---");
            System.out.println("1. Show products");
            System.out.println("2. Add product");
            System.out.println("3. Sell product");
            System.out.println("4. Delete product");
            System.out.println("5. Sales report");
            System.out.println("6. Exit");
            choice = readInt("Choose: ");

            if (choice == 1) {
                if (products.isEmpty()) {
                    System.out.println("No products yet.");
                }
                for (Product p : products) {
                    if (p.isLowStock()) {
                        System.out.println(p + "  <-- LOW STOCK!");
                    } else {
                        System.out.println(p);
                    }
                }
            } else if (choice == 2) {
                System.out.print("Name: ");
                String name = input.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Name cannot be empty!");
                    continue;
                }
                double price = readDouble("Price: ");
                int quantity = readInt("Quantity: ");
                if (price < 0 || quantity < 0) {
                    System.out.println("Price and quantity cannot be negative!");
                    continue;
                }
                products.add(new Product(name, price, quantity));
                saveProducts(products);
                System.out.println("Product added!");
            } else if (choice == 3) {
                System.out.print("Product name: ");
                String name = input.nextLine().trim();
                boolean found = false;
                for (Product p : products) {
                    if (p.getName().equalsIgnoreCase(name)) {
                        int amount = readInt("Amount to sell: ");
                        if (amount <= 0) {
                            System.out.println("Amount must be positive!");
                        } else if (amount > p.getQuantity()) {
                            System.out.println("Not enough stock!");
                        } else {
                            p.sell(amount);
                            totalRevenue += amount * p.getPrice();
                            totalItemsSold += amount;
                            saveProducts(products);
                            saveSales();
                            System.out.println(p);
                            if (p.isLowStock()) {
                                System.out.println("Warning: low stock!");
                            }
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("Product not found!");
                }
            } else if (choice == 4) {
                System.out.print("Product name to delete: ");
                String name = input.nextLine().trim();
                boolean removed = false;
                for (int i = 0; i < products.size(); i++) {
                    if (products.get(i).getName().equalsIgnoreCase(name)) {
                        products.remove(i);
                        removed = true;
                        break;
                    }
                }
                if (removed) {
                    saveProducts(products);
                    System.out.println("Product deleted!");
                } else {
                    System.out.println("Product not found!");
                }
            } else if (choice == 5) {
                System.out.println("\n--- Sales Report ---");
                System.out.println("Items sold: " + totalItemsSold);
                System.out.println("Total revenue: " + totalRevenue);
            } else if (choice != 6) {
                System.out.println("Please choose a number from 1 to 6.");
            }
        }
        System.out.println("Goodbye!");
    }

    public static int readInt(String message) {
        while (true) {
            System.out.print(message);
            String line = input.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number!");
            }
        }
    }

    public static double readDouble(String message) {
        while (true) {
            System.out.print(message);
            String line = input.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number!");
            }
        }
    }

    public static void saveProducts(ArrayList<Product> products) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("products.txt"))) {
            for (Product p : products) {
                writer.println(p.getName() + "," + p.getPrice() + "," + p.getQuantity());
            }
        } catch (IOException e) {
            System.out.println("Could not save file!");
        }
    }

    public static ArrayList<Product> loadProducts() {
        ArrayList<Product> products = new ArrayList<>();
        File file = new File("products.txt");
        if (!file.exists()) {
            return products;
        }
        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    double price = Double.parseDouble(parts[1]);
                    int quantity = Integer.parseInt(parts[2]);
                    products.add(new Product(name, price, quantity));
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load file!");
        }
        return products;
    }

    public static void saveSales() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("sales.txt"))) {
            writer.println(totalItemsSold + "," + totalRevenue);
        } catch (IOException e) {
            System.out.println("Could not save sales!");
        }
    }

    public static void loadSales() {
        File file = new File("sales.txt");
        if (!file.exists()) {
            return;
        }
        try (Scanner reader = new Scanner(file)) {
            if (reader.hasNextLine()) {
                String[] parts = reader.nextLine().split(",");
                if (parts.length == 2) {
                    totalItemsSold = Integer.parseInt(parts[0]);
                    totalRevenue = Double.parseDouble(parts[1]);
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load sales!");
        }
    }
}