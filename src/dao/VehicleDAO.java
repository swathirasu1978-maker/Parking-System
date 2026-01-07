package dao;

import model.Vehicle;

import java.sql.*;
import java.util.ArrayList;

import java.util.List;

public class VehicleDAO {

    // Add a vehicle (active parking)
    public boolean addVehicle(Vehicle v) {
        String sql = "INSERT INTO vehicles(vehicle_number, vehicle_type, slot_number) VALUES(?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, v.getVehicleNumber());
            p.setString(2, v.getVehicleType());
            p.setInt(3, v.getSlotNumber());
            int r = p.executeUpdate();
            if (r > 0) {
                try (ResultSet rs = p.getGeneratedKeys()) {
                    if (rs.next()) v.setId(rs.getInt(1));
                }
                // load DB entry_time
                Vehicle fresh = findById(v.getId());
                if (fresh != null) v.setEntryTime(fresh.getEntryTime());
                return true;
            }
        } catch (SQLException e) {
            System.out.println("addVehicle error: " + e.getMessage());
        }
        return false;
    }

    // List active parked vehicles (exit_time IS NULL)
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE exit_time IS NULL ORDER BY slot_number";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.out.println("getAllVehicles error: " + e.getMessage()); }
        return list;
    }

    // Find by id (active)
    public Vehicle findById(int id) {
        String sql = "SELECT * FROM vehicles WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { System.out.println("findById error: " + e.getMessage()); }
        return null;
    }

    // Find active by vehicle number
    public Vehicle findByNumber(String number) {
        String sql = "SELECT * FROM vehicles WHERE vehicle_number=? AND exit_time IS NULL";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, number);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { System.out.println("findByNumber error: " + e.getMessage()); }
        return null;
    }

    // Update vehicle details (number/type/slot)
    public boolean updateVehicle(Vehicle v) {
        String sql = "UPDATE vehicles SET vehicle_number=?, vehicle_type=?, slot_number=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, v.getVehicleNumber());
            p.setString(2, v.getVehicleType());
            p.setInt(3, v.getSlotNumber());
            p.setInt(4, v.getId());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("updateVehicle error: " + e.getMessage()); }
        return false;
    }

    // Update exit details (exit_time, hours, fee) in vehicles table before archiving (optional)
    public boolean updateVehicleExit(Vehicle v) {
        String sql = "UPDATE vehicles SET exit_time=?, parking_hours=?, fee=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, v.getExitTime());
            p.setDouble(2, v.getParkingHours());
            p.setDouble(3, v.getFee());
            p.setInt(4, v.getId());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("updateVehicleExit error: " + e.getMessage()); }
        return false;
    }

    // Add to history table
    public boolean addToHistory(Vehicle v) {
        String sql = "INSERT INTO parking_history(vehicle_number, vehicle_type, slot_number, entry_time, exit_time, parking_hours, fee) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, v.getVehicleNumber());
            p.setString(2, v.getVehicleType());
            p.setInt(3, v.getSlotNumber());
            p.setTimestamp(4, v.getEntryTime());
            p.setTimestamp(5, v.getExitTime());
            p.setDouble(6, v.getParkingHours());
            p.setDouble(7, v.getFee());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("addToHistory error: " + e.getMessage()); }
        return false;
    }

    // Delete from vehicles (active)
    public boolean deleteVehicle(int id) {
        String sql = "DELETE FROM vehicles WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("deleteVehicle error: " + e.getMessage()); }
        return false;
    }

    // Determine next available slot (smallest missing positive slot)
    public int getNextAvailableSlot() {
        String sql = "SELECT slot_number FROM vehicles WHERE exit_time IS NULL ORDER BY slot_number";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            int slot = 1;
            while (rs.next()) {
                int s = rs.getInt("slot_number");
                if (s == slot) slot++;
                else if (s > slot) break;
            }
            return slot;
        } catch (SQLException e) { System.out.println("getNextAvailableSlot error: " + e.getMessage()); }
        return 1;
    }

    // Search active vehicles by number/type/slot
    public List<Vehicle> searchActive(String q) {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE (vehicle_number LIKE ? OR vehicle_type LIKE ? OR slot_number = ?) AND exit_time IS NULL";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + q + "%");
            p.setString(2, "%" + q + "%");
            int slotVal = -1;
            try { slotVal = Integer.parseInt(q); } catch (Exception ignored) {}
            p.setInt(3, slotVal);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { System.out.println("searchActive error: " + e.getMessage()); }
        return list;
    }

    // Get total fee between timestamps (for reports)
    public double getTotalFeeBetween(Timestamp from, Timestamp to) {
        String sql = "SELECT IFNULL(SUM(fee),0) as total FROM parking_history WHERE exit_time BETWEEN ? AND ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, from);
            p.setTimestamp(2, to);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) { System.out.println("getTotalFeeBetween error: " + e.getMessage()); }
        return 0;
    }

    // Get history (last n)
    public List<Vehicle> getHistory(int limit) {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM parking_history ORDER BY exit_time DESC LIMIT ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, limit);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    Vehicle v = new Vehicle();
                    v.setId(rs.getInt("id"));
                    v.setVehicleNumber(rs.getString("vehicle_number"));
                    v.setVehicleType(rs.getString("vehicle_type"));
                    v.setSlotNumber(rs.getInt("slot_number"));
                    v.setEntryTime(rs.getTimestamp("entry_time"));
                    v.setExitTime(rs.getTimestamp("exit_time"));
                    v.setParkingHours(rs.getDouble("parking_hours"));
                    v.setFee(rs.getDouble("fee"));
                    list.add(v);
                }
            }
        } catch (SQLException e) { System.out.println("getHistory error: " + e.getMessage()); }
        return list;
    }

    // helper: map ResultSet row to Vehicle
    private Vehicle map(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getInt("id"));
        v.setVehicleNumber(rs.getString("vehicle_number"));
        v.setVehicleType(rs.getString("vehicle_type"));
        v.setSlotNumber(rs.getInt("slot_number"));
        v.setEntryTime(rs.getTimestamp("entry_time"));
        v.setExitTime(rs.getTimestamp("exit_time"));
        v.setParkingHours(rs.getDouble("parking_hours"));
        v.setFee(rs.getDouble("fee"));
        return v;
    }
}

            