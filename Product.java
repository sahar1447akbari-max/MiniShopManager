public class Product {
    private String name;
    private double price;
    private int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void sell(int amount) {
        if (amount <= quantity) {
            quantity -= amount;
        } else {
            System.out.println("Not enough stock!");
        }
    }

    public boolean isLowStock() {
        return quantity < 5;
    }

    @Override
    public String toString() {
        return name + " | Price: " + price + " | Quantity: " + quantity;
    }
}