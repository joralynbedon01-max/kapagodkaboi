import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// ================= DATABASE CONNECTION =================
class DBConnection {
    static final String URL = "jdbc:mysql://localhost:3306/pos_db";
    static final String USER = "root";
    static final String PASS = ""; // change if your MySQL has password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

// ================= REGISTRATION PAGE =================
class RegisterPage extends JFrame {
    JTextField txtUser;
    JPasswordField txtPass;

    public RegisterPage() {
        setTitle("Register Account");
        setSize(400,250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel center = new JPanel(new GridLayout(2,2,10,10));
        center.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));

        center.add(new JLabel("Username:"));
        txtUser = new JTextField(); center.add(txtUser);
        center.add(new JLabel("Password:"));
        txtPass = new JPasswordField(); center.add(txtPass);
        add(center, BorderLayout.CENTER);

        JButton btnRegister = new JButton("Register");
        btnRegister.addActionListener(e -> register());
        add(btnRegister, BorderLayout.SOUTH);

        setVisible(true);
    }

    void register() {
        try(Connection con = DBConnection.getConnection()){
            String sql = "INSERT INTO users(username,password) VALUES(?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txtUser.getText());
            ps.setString(2, new String(txtPass.getPassword()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registered successfully");
            dispose();
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Registration failed");
        }
    }
}

// ================= LOGIN PAGE =================
class LoginPage extends JFrame {
    JTextField txtUser;
    JPasswordField txtPass;

    public LoginPage() {
        setTitle("POS Login");
        setSize(400,260);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel center = new JPanel(new GridLayout(2,2,10,10));
        center.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));
        center.add(new JLabel("Username:")); txtUser = new JTextField(); center.add(txtUser);
        center.add(new JLabel("Password:")); txtPass = new JPasswordField(); center.add(txtPass);
        add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");
        buttons.add(btnLogin); buttons.add(btnRegister);
        add(buttons, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> new RegisterPage());

        setVisible(true);
    }

    void login() {
        try(Connection con = DBConnection.getConnection()){
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txtUser.getText());
            ps.setString(2, new String(txtPass.getPassword()));
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                JOptionPane.showMessageDialog(this, "Login successful");
                dispose();
                new POSSystem();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Database error");
        }
    }
}

// ================= POS SYSTEM =================
class POSSystem extends JFrame {
    DefaultTableModel model;
    JTextField txtSubtotal, txtTax, txtTotal;
    double taxRate = 0.12;

    public POSSystem() {
        setTitle("POS System");
        setSize(800,500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"Product","Price","Qty","Total"},0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel products = new JPanel(new GridLayout(2,3,5,5));
        addProduct(products, "Burger", 50);
        addProduct(products, "Fries", 30);
        addProduct(products, "Coffee", 60);
        add(products, BorderLayout.WEST);

        JPanel totals = new JPanel(new GridLayout(3,2));
        txtSubtotal = new JTextField(); txtTax = new JTextField(); txtTotal = new JTextField();
        txtSubtotal.setEditable(false); txtTax.setEditable(false); txtTotal.setEditable(false);
        totals.add(new JLabel("Subtotal")); totals.add(txtSubtotal);
        totals.add(new JLabel("Tax")); totals.add(txtTax);
        totals.add(new JLabel("Total")); totals.add(txtTotal);
        add(totals, BorderLayout.SOUTH);

        setVisible(true);
    }

    void addProduct(JPanel panel, String name, double price) {
        JButton btn = new JButton(name);
        btn.addActionListener(e -> {
            model.addRow(new Object[]{name, price, 1, price});
            compute();
        });
        panel.add(btn);
    }

    void compute() {
        double subtotal = 0;
        for(int i=0;i<model.getRowCount();i++) subtotal += (double)model.getValueAt(i,3);
        double tax = subtotal * taxRate;
        double total = subtotal + tax;
        txtSubtotal.setText(Strin
