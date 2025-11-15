import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import org.json.*;

public class UserPage extends JFrame {
    JTable table;
    JButton refreshBtn, bookBtn, myBookingsBtn;
    int userId;

    public UserPage(int userId) {
        this.userId = userId;
        setTitle("🎟 User Portal - Book Your Events");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245, 239, 255)); // pastel lavender
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Available Events", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(85, 85, 125));
        add(title, BorderLayout.NORTH);

        table = new JTable();
        table.setBackground(new Color(255, 249, 253));
        table.setGridColor(new Color(220, 210, 230));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(240, 230, 250));

        refreshBtn = new JButton("🔄 Refresh");
        bookBtn = new JButton("✅ Book Event");
        myBookingsBtn = new JButton("📅 My Bookings");

        Color btnLav = new Color(170, 160, 230);
        Color btnBlue = new Color(120, 180, 255);
        Color btnPink = new Color(250, 170, 200);

        for (JButton b : new JButton[]{refreshBtn, bookBtn, myBookingsBtn}) {
            b.setFocusPainted(false);
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setForeground(Color.WHITE);
            b.setPreferredSize(new Dimension(160, 35));
        }

        refreshBtn.setBackground(btnLav);
        bookBtn.setBackground(btnBlue);
        myBookingsBtn.setBackground(btnPink);

        bottom.add(refreshBtn);
        bottom.add(bookBtn);
        bottom.add(myBookingsBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadEvents());
        bookBtn.addActionListener(e -> bookEvent());
        myBookingsBtn.addActionListener(e -> loadBookings());

        loadEvents();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadEvents() {
        try {
            URL url = new URL("http://localhost:5000/view_events");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            JSONArray arr = new JSONArray(response.toString());
            String[] cols = {"ID", "Date", "Name", "Time", "Venue"};
            String[][] data = new String[arr.length()][5];

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                data[i][0] = String.valueOf(obj.getInt("id"));
                data[i][1] = obj.getString("event_date");
                data[i][2] = obj.getString("event_name");
                data[i][3] = obj.getString("event_time");
                data[i][4] = obj.getString("venue");
            }

            table.setModel(new javax.swing.table.DefaultTableModel(data, cols));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + ex.getMessage());
        }
    }

    private void loadBookings() {
        try {
            URL url = new URL("http://localhost:5000/view_bookings?user_id=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            JSONArray arr = new JSONArray(response.toString());
            String[] cols = {"Booking ID", "Event Name", "Date", "Time", "Venue"};
            String[][] data = new String[arr.length()][5];

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                data[i][0] = String.valueOf(obj.getInt("id"));
                data[i][1] = obj.getString("event_name");
                data[i][2] = obj.getString("event_date");
                data[i][3] = obj.getString("event_time");
                data[i][4] = obj.getString("venue");
            }

            table.setModel(new javax.swing.table.DefaultTableModel(data, cols));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + ex.getMessage());
        }
    }

    private void bookEvent() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event first!");
            return;
        }
        int eventId = Integer.parseInt(table.getValueAt(row, 0).toString());
        try {
            URL url = new URL("http://localhost:5000/book_event");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("user_id", userId);
            data.put("event_id", eventId);

            OutputStream os = conn.getOutputStream();
            os.write(data.toString().getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            JSONObject res = new JSONObject(response.toString());
            JOptionPane.showMessageDialog(this, res.getString("message"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error booking event: " + ex.getMessage());
        }
    }

    // optional main to test UI
    public static void main(String[] args) {
        new UserPage(1); // test with userId=1
    }
}
