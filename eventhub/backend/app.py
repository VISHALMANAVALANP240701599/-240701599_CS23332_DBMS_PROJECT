from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

DB_NAME = "events.db"

def get_conn():
    conn = sqlite3.connect(DB_NAME)
    conn.row_factory = sqlite3.Row
    return conn

# -------------------------
# LOGIN
# -------------------------
@app.route('/login', methods=['POST'])
def login():
    data = request.json
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("SELECT id, role FROM users WHERE username=? AND password=?",
                (data['username'], data['password']))
    row = cur.fetchone()
    conn.close()
    if row:
        return jsonify({
            "message": "Login successful",
            "role": row["role"],
            "user_id": row["id"]
        })
    return jsonify({"message": "Invalid credentials"}), 401


# -------------------------
# ADD EVENT (Admin)
# -------------------------
@app.route('/add_event', methods=['POST'])
def add_event():
    data = request.json
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("""
        INSERT INTO events (event_date, event_name, event_time, venue)
        VALUES (?, ?, ?, ?)
    """, (data['event_date'], data['event_name'], data['event_time'], data['venue']))
    conn.commit()
    conn.close()
    return jsonify({"message": "✅ Event added!"})


# -------------------------
# UPDATE EVENT (Admin)
# -------------------------
@app.route('/update_event', methods=['POST'])
def update_event():
    data = request.json
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("""
        UPDATE events
        SET event_name=?, event_time=?, venue=?
        WHERE event_date=?
    """, (data['event_name'], data['event_time'], data['venue'], data['event_date']))
    conn.commit()
    conn.close()
    return jsonify({"message": "✅ Event updated!"})


# -------------------------
# VIEW EVENTS (User/Admin)
# -------------------------
@app.route('/view_events', methods=['GET'])
def view_events():
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("SELECT id, event_date, event_name, event_time, venue FROM events")
    rows = cur.fetchall()
    conn.close()
    events = [dict(r) for r in rows]
    return jsonify(events)


# -------------------------
# BOOK EVENT (User)
# -------------------------
@app.route('/book_event', methods=['POST'])
def book_event():
    data = request.json
    user_id = data['user_id']
    event_id = data['event_id']

    conn = get_conn()
    cur = conn.cursor()

    # Prevent duplicate booking
    cur.execute("SELECT * FROM bookings WHERE user_id=? AND event_id=?", (user_id, event_id))
    if cur.fetchone():
        conn.close()
        return jsonify({"message": "❌ Already booked this event!"})

    cur.execute("INSERT INTO bookings (user_id, event_id) VALUES (?, ?)", (user_id, event_id))
    conn.commit()
    conn.close()
    return jsonify({"message": "✅ Event booked successfully!"})


# -------------------------
# VIEW MY BOOKINGS (User)
# -------------------------
@app.route('/view_bookings', methods=['GET'])
def view_user_bookings():
    user_id = request.args.get('user_id')
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("""
        SELECT b.id, e.event_name, e.event_date, e.event_time, e.venue
        FROM bookings b
        JOIN events e ON b.event_id = e.id
        WHERE b.user_id = ?
    """, (user_id,))
    rows = cur.fetchall()
    conn.close()
    bookings = [dict(r) for r in rows]
    return jsonify(bookings)


# -------------------------
# VIEW ALL BOOKINGS (Admin)
# -------------------------
@app.route('/admin_bookings', methods=['GET'])
def admin_bookings():
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("""
        SELECT u.username, e.event_name, e.event_date, e.event_time, e.venue
        FROM bookings b
        JOIN users u ON b.user_id = u.id
        JOIN events e ON b.event_id = e.id
    """)
    rows = cur.fetchall()
    conn.close()
    bookings = [
        {"username": r["username"], "event_name": r["event_name"],
         "event_date": r["event_date"], "event_time": r["event_time"], "venue": r["venue"]}
        for r in rows
    ]
    return jsonify(bookings)


if __name__ == '__main__':
    app.run(debug=True)
