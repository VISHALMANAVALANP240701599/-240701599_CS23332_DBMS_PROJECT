import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import org.json.*;

public class AdminPage extends JFrame {
    JTextField dateField, nameField, timeField, venueField;
    JButton addBtn, updateBtn, viewBookingsBtn;
    JTable bookingTable;

    public AdminPage() {
        setTitle("🛠 Admin Portal - Manage Events");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(255, 240, 245)); // pastel pink background
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Admin Event Management", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(120, 80, 100));
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 60, 15, 60));
        form.setBackground(new Color(255, 240, 245));

        form.add(new JLabel("Event Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        form.add(dateField);

        form.add(new JLabel("Event Name:"));
        nameField = new JTextField();
        form.add(nameField);

        form.add(new JLabel("Event Time:"));
        timeField = new JTextField();
        form.add(timeField);

        form.add(new JLabel("Venue:"));
        venueField = new JTextField();
        form.add(venueField);

        addBtn = new JButton("➕ Add Event");
        updateBtn = new JButton("🔁 Update Event");
        viewBookingsBtn = new JButton("📋 View Bookings");

        addBtn.setBackground(new Color(180, 200, 255));
        updateBtn.setBackground(new Color(200, 170, 250));
        viewBookingsBtn.setBackground(new Color(170, 230, 200));

        form.add(addBtn);
        form.add(updateBtn);

        add(form, BorderLayout.CENTER);
        add(viewBookingsBtn, BorderLayout.SOUTH);

        bookingTable = new JTable();
        bookingTable.setBackground(new Color(255, 252, 255));

        addBtn.addActionListener(e -> addEvent());
        updateBtn.addActionListener(e -> updateEvent());
        viewBookingsBtn.addActionListener(e -> viewBookings());

        setVisible(true);
    }

    private void addEvent() {
        try {
            URL url = new URL("http://localhost:5000/add_event");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("event_date", dateField.getText());
            data.put("event_name", nameField.getText());
            data.put("event_time", timeField.getText());
            data.put("venue", venueField.getText());

            OutputStream os = conn.getOutputStream();
            os.write(data.toString().getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.readLine();
            in.close();

            JOptionPane.showMessageDialog(this, "✅ Event added successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateEvent() {
        try {
            URL url = new URL("http://localhost:5000/update_event");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("event_date", dateField.getText());
            data.put("event_name", nameField.getText());
            data.put("event_time", timeField.getText());
            data.put("venue", venueField.getText());

            OutputStream os = conn.getOutputStream();
            os.write(data.toString().getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            in.readLine();
            in.close();

            JOptionPane.showMessageDialog(this, "✅ Event updated!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void viewBookings() {
        try {
            URL url = new URL("http://localhost:5000/view_bookings");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            JSONArray arr = new JSONArray(response.toString());
            String[] cols = {"User", "Event", "Date", "Time", "Venue"};
            String[][] data = new String[arr.length()][5];

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                data[i][0] = obj.getString("username");
                data[i][1] = obj.getString("event");
                data[i][2] = obj.getString("date");
                data[i][3] = obj.getString("time");
                data[i][4] = obj.getString("venue");
            }

            JTable table = new JTable(data, cols);
            JScrollPane pane = new JScrollPane(table);
            JOptionPane.showMessageDialog(this, pane, "📋 Event Bookings", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + ex.getMessage());
        }
    }
}
