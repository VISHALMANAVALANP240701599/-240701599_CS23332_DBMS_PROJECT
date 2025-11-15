import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.json.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginPage() {
        setTitle("Event Manager Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(230, 240, 250));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(230, 240, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        // pastel blue button
        loginButton.setBackground(new Color(180, 200, 255));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);

        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        try {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("password", password);

            // Backend API endpoint
            URL url = new URL("http://127.0.0.1:5000/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;

            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            JSONObject res = new JSONObject(response.toString());
            String message = res.getString("message");

            if (message.equals("Login successful")) {
                String role = res.getString("role");
                int userId = res.getInt("user_id");

                JOptionPane.showMessageDialog(this, "Welcome " + role + "!");
                dispose();

                if (role.equals("admin")) {
                    new AdminPage();  // open admin page
                } else {
                    new UserPage(userId); // ✅ pass userId correctly
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Server error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
