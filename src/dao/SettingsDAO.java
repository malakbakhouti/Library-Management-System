package dao;

import model.Settings;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsDAO {

    public List<Settings> getAll() {
        List<Settings> list = new ArrayList<>();
        String sql = "SELECT * FROM settings";

        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Settings(
                        rs.getInt("id"),
                        rs.getString("key_name"),
                        rs.getString("value_text")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    public boolean update(Settings s) {
        String sql = "UPDATE settings SET value_text=? WHERE key_name=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getValueText());
            ps.setString(2, s.getKeyName());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
