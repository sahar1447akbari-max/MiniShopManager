import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class ShopWindow extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JLabel statusLabel;
    private ArrayList<Product> products;
    private ArrayList<Product> shown = new ArrayList<>();

    public ShopWindow() {
        products = Main.loadProducts();
        Main.loadSales();

        setTitle("Mini Shop Manager");
        setSize(850, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {"Name", "Price", "Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setForeground(Color.BLACK);
                    if (row < shown.size() && shown.get(row).isLowStock()) {
                        c.setBackground(new Color(255, 215, 215));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshTable(); }
            public void removeUpdate(DocumentEvent e) { refreshTable(); }
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        topPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JButton addButton = new JButton("Add product");
        JButton editButton = new JButton("Edit selected");
        JButton sellButton = new JButton("Sell selected");
        JButton deleteButton = new JButton("Delete selected");
        JButton reportButton = new JButton("Sales report");
        JButton backupButton = new JButton("Backup");

        addButton.addActionListener(e -> addProduct());
        editButton.addActionListener(e -> editProduct());
        sellButton.addActionListener(e -> sellProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        reportButton.addActionListener(e -> showReport());
        backupButton.addActionListener(e -> backupData());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(reportButton);
        buttonPanel.add(backupButton);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row == -1 || row >= shown.size()) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return null;
        }
        return shown.get(row);
    }

    private void addProduct() {
        String name = JOptionPane.showInputDialog(this, "Product name:");
        if (name == null) {
            return;
        }
        name = name.trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty!");
            return;
        }
        if (name.contains(",")) {
            JOptionPane.showMessageDialog(this, "Name cannot contain a comma!");
            return;
        }
        for (Product p : products) {
            if (p.getName().equalsIgnoreCase(name)) {
                JOptionPane.showMessageDialog(this, "This product already exists!");
                return;
            }
        }
        try {
            String priceText = JOptionPane.showInputDialog(this, "Price:");
            if (priceText == null) {
                return;
            }
            double price = Double.parseDouble(priceText.trim());

            String quantityText = JOptionPane.showInputDialog(this, "Quantity:");
            if (quantityText == null) {
                return;
            }
            int quantity = Integer.parseInt(quantityText.trim());

            if (price < 0 || quantity < 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity cannot be negative!");
                return;
            }

            products.add(new Product(name, price, quantity));
            Main.saveProducts(products);
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
        }
    }

    private void editProduct() {
        Product p = getSelectedProduct();
        if (p == null) {
            return;
        }
        String priceText = JOptionPane.showInputDialog(this,
                "New price for " + p.getName() + " (now " + p.getPrice() + "):");
        if (priceText == null) {
            return;
        }
        String quantityText = JOptionPane.showInputDialog(this,
                "New quantity for " + p.getName() + " (now " + p.getQuantity() + "):");
        if (quantityText == null) {
            return;
        }
        try {
            double price = Double.parseDouble(priceText.trim());
            int quantity = Integer.parseInt(quantityText.trim());
            if (price < 0 || quantity < 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity cannot be negative!");
                return;
            }
            p.setPrice(price);
            p.setQuantity(quantity);
            Main.saveProducts(products);
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
        }
    }

    private void sellProduct() {
        Product p = getSelectedProduct();
        if (p == null) {
            return;
        }
        String amountText = JOptionPane.showInputDialog(this, "Amount to sell of " + p.getName() + ":");
        if (amountText == null) {
            return;
        }
        try {
            int amount = Integer.parseInt(amountText.trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive!");
            } else if (amount > p.getQuantity()) {
                JOptionPane.showMessageDialog(this, "Not enough stock!");
            } else {
                p.sell(amount);
                double total = amount * p.getPrice();
                Main.totalRevenue += total;
                Main.totalItemsSold += amount;
                Main.saveProducts(products);
                Main.saveSales();
                logSale(p.getName(), amount, total);
                refreshTable();
                if (p.isLowStock()) {
                    JOptionPane.showMessageDialog(this,
                            "Warning: low stock for " + p.getName() + " (" + p.getQuantity() + " left)!");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number!");
        }
    }

    private void deleteProduct() {
        Product p = getSelectedProduct();
        if (p == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete " + p.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            products.remove(p);
            Main.saveProducts(products);
            refreshTable();
        }
    }

    private void logSale(String name, int amount, double total) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        try (PrintWriter writer = new PrintWriter(new FileWriter("sales_log.txt", true))) {
            writer.println(time + "," + name + "," + amount + "," + total);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not write the sales log!");
        }
    }

    private void showReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Items sold: ").append(Main.totalItemsSold).append("\n");
        sb.append(String.format("Total revenue: %.2f%n%n", Main.totalRevenue));
        sb.append("Recent sales (date, product, amount, total):\n");

        ArrayList<String> lines = new ArrayList<>();
        File file = new File("sales_log.txt");
        if (file.exists()) {
            try (Scanner reader = new Scanner(file)) {
                while (reader.hasNextLine()) {
                    lines.add(reader.nextLine());
                }
            } catch (Exception e) {
                sb.append("Could not read the sales log.\n");
            }
        }
        if (lines.isEmpty()) {
            sb.append("No sales recorded yet.\n");
        } else {
            int shownCount = 0;
            for (int i = lines.size() - 1; i >= 0 && shownCount < 15; i--) {
                sb.append(lines.get(i)).append("\n");
                shownCount++;
            }
        }

        JTextArea area = new JTextArea(sb.toString(), 18, 45);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Sales Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void backupData() {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File dir = new File("backup_" + stamp);
        dir.mkdirs();
        String[] names = {"products.txt", "sales.txt", "sales_log.txt"};
        int copied = 0;
        try {
            for (String name : names) {
                File source = new File(name);
                if (source.exists()) {
                    Files.copy(source.toPath(), new File(dir, name).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Backup done! " + copied + " file(s) saved in folder:\n" + dir.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Backup failed!");
        }
    }

    private void refreshTable() {
        String filter = searchField.getText().trim().toLowerCase();
        shown.clear();
        tableModel.setRowCount(0);
        int lowCount = 0;
        for (Product p : products) {
            if (p.isLowStock()) {
                lowCount++;
            }
            if (filter.isEmpty() || p.getName().toLowerCase().contains(filter)) {
                shown.add(p);
                tableModel.addRow(new Object[]{p.getName(), p.getPrice(), p.getQuantity()});
            }
        }
        statusLabel.setText(String.format("Products: %d  |  Low stock: %d  |  Items sold: %d  |  Revenue: %.2f",
                products.size(), lowCount, Main.totalItemsSold, Main.totalRevenue));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShopWindow().setVisible(true));
    }
}