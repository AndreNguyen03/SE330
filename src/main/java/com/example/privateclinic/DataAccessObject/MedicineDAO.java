package com.example.privateclinic.DataAccessObject;

import com.example.privateclinic.ForeignKeyViolationException;
import com.example.privateclinic.Models.*;
import com.jfoenix.controls.JFXDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {
    ConnectDB connectDB = ConnectDB.getInstance();
    public ObservableList<Medicine> searchMedicineByIDorName(String idOrName) {
        ObservableList<Medicine> medicines = FXCollections.observableArrayList();
        String query = "SELECT * FROM thuoc t, donvitinh dvt, dangthuoc dt, cachdung cd " +
                "WHERE dvt.madvt = t.madvt and dt.madt=t.madt and cd.macd=t.macd ";
        boolean isInteger = false;
        if(!idOrName.isEmpty()){
            query+="and (unaccent(t.tenthuoc) ILIKE unaccent(?) ";
            try {
                int id = Integer.parseInt(idOrName);
                query += " OR t.mathuoc = ? ";
                isInteger = true;
            } catch (NumberFormatException e ){
            }
            query += ")";
        }
        query+=" ORDER BY t.mathuoc ASC";
        try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {
            if(!idOrName.trim().isEmpty()) {
                statement.setString(1, "%" + idOrName + "%");
                if (isInteger) statement.setInt(2, Integer.parseInt(idOrName));
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Medicine medicine = new Medicine();
                medicine.setMaThuoc(resultSet.getInt("mathuoc"));
                medicine.setTenThuoc(resultSet.getString("tenthuoc"));
                medicine.setMaCachDung(resultSet.getInt("macd"));
                medicine.setMaDangThuoc(resultSet.getInt("madt"));
                medicine.setMaDonViTinh(resultSet.getInt("madvt"));
                medicine.setTenDangThuoc(resultSet.getString("tendt"));
                medicine.setTenCachDung(resultSet.getString("tencd"));
                medicine.setTenDonViTinh(resultSet.getString("tendvt"));
                medicine.setSoLuong(resultSet.getInt("soluong"));
                medicine.setGiaBan(resultSet.getInt("giaban"));
                medicines.add(medicine);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return medicines;
    }
    public boolean UpdateMedicineAfterExam(int _maThuoc, int _soluongBan) {
        int newQuantity = getCurrentQuantityMedicine(_maThuoc) - _soluongBan;
        String query = "UPDATE thuoc SET soluong = ? WHERE mathuoc = ?";
        try(PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {
            statement.setInt(1,newQuantity);
            statement.setInt(2,_maThuoc);
            return statement.executeUpdate()>0;
        } catch (SQLException e ){
            e.printStackTrace();
            return false;
        }
    }

    public int getCurrentQuantityMedicine(int _mathuoc) {
        String query = "SELECT soluong FROM thuoc WHERE mathuoc = ?";
        try(PreparedStatement statement = connectDB.databaseLink.prepareStatement(query))
        {
            statement.setInt(1,_mathuoc);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e ){
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    public boolean addMedicine(Medicine medicine) {
        LocalDate now = LocalDate.now();

        String query = "INSERT INTO thuoc (mathuoc, tenthuoc, madvt, soluong, giaban, madt, macd) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {

            statement.setInt(1, medicine.getMaThuoc());
            statement.setString(2, medicine.getTenThuoc());
            statement.setInt(3, medicine.getMaDonViTinh());
            statement.setInt(4, medicine.getSoLuong());
            statement.setDouble(5, medicine.getGiaBan());
            statement.setInt(6, medicine.getMaDangThuoc());
            statement.setInt(7, medicine.getMaCachDung());


            return statement.executeUpdate() >0 ;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> medicines = new ArrayList<>();
        String query = "SELECT * FROM thuoc t, donvitinh dvt, dangthuoc dt, cachdung cd where dvt.madvt = t.madvt and dt.madt=t.madt" +
                " and cd.macd=t.macd " +
                " ORDER BY t.mathuoc ASC";

        try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Medicine medicine = new Medicine();
                medicine.setMaThuoc(resultSet.getInt("mathuoc"));
                medicine.setTenThuoc(resultSet.getString("tenthuoc"));
                medicine.setTenDonViTinh(resultSet.getString("tendvt"));
                medicine.setSoLuong(resultSet.getInt("soluong"));
                double roundedPrice = Math.round(resultSet.getDouble("giaban") * 100.0) / 100.0;
                medicine.setGiaBan(roundedPrice);
                medicine.setTenDangThuoc(resultSet.getString("tendt"));
                medicine.setTenCachDung(resultSet.getString("tencd"));

                medicines.add(medicine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medicines;
    }


    public boolean updateMedicine(Medicine medicine) {
        String query = "UPDATE thuoc SET tenthuoc = ?, madvt = ?, soluong = ?, giaban = ?, madt = ?, macd = ? WHERE mathuoc = ?";

        try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {

            statement.setString(1, medicine.getTenThuoc());
            statement.setInt(2, medicine.getMaDonViTinh());
            statement.setInt(3, medicine.getSoLuong());
            statement.setDouble(4, medicine.getGiaBan());
            statement.setInt(5, medicine.getMaDangThuoc());
            statement.setInt(6, medicine.getMaCachDung());
            statement.setInt(7, medicine.getMaThuoc());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMedicine(int medicineId) throws ForeignKeyViolationException{
        String query = "DELETE FROM thuoc WHERE mathuoc = ?";
        try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {
            statement.setInt(1, medicineId);
            return statement.executeUpdate() >0;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23503")) { // Mã lỗi cho vi phạm khóa ngoại
                throw new ForeignKeyViolationException("Không thể xoá sản phẩm, tồn tại một quan hệ sử dụng dữ liệu!");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public int getNextMedicineId() {
        String query = "SELECT MAX(mathuoc) FROM thuoc";

        try (Statement statement = connectDB.databaseLink.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                int maxId = resultSet.getInt(1);
                return maxId + 1;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi: " + e.getMessage());
        }
        return 1;
    }

    public void updateMedicineIds() {
        List<Pair<Integer, Integer>> idPairs = new ArrayList<>();

        String selectQuery = "SELECT mathuoc FROM thuoc ORDER BY mathuoc";
        try (Statement selectStatement = connectDB.databaseLink.createStatement()) {
            ResultSet resultSet = selectStatement.executeQuery(selectQuery);
            int newId = 1; // Bắt đầu từ ID đầu tiên
            while (resultSet.next()) {
                int oldId = resultSet.getInt("mathuoc");
                idPairs.add(new Pair<>(oldId, newId));
                newId++; // Tăng ID mới cho mục tiếp theo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Thực hiện cập nhật cho mỗi cặp cũ-mới
        String updateQuery = "UPDATE thuoc SET mathuoc = ? WHERE mathuoc = ?";
        try (PreparedStatement updateStatement = connectDB.databaseLink.prepareStatement(updateQuery)) {
            for (Pair<Integer, Integer> pair : idPairs) {
                updateStatement.setInt(1, pair.getValue());
                updateStatement.setInt(2, pair.getKey());
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isMedicineNameExists(String medicineName, int currentMedicineId) {
        String query = "SELECT COUNT(*) AS count FROM thuoc WHERE tenthuoc = ? AND mathuoc != ?";
        int count = 0;

        try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {
            statement.setString(1, medicineName);
            statement.setInt(2, currentMedicineId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count > 0;
    }

    public int getMedicineIDByName(String medicineName) {
        int medicineID = -1; // Giả sử không tìm thấy

        // Kiểm tra nếu tên thuốc tồn tại trong cơ sở dữ liệu
        if (isMedicineNameExists(medicineName,0)) {
            // Truy vấn cơ sở dữ liệu để lấy mã thuốc
            String query = "SELECT mathuoc FROM thuoc WHERE tenthuoc = ?";
            try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {
                statement.setString(1, medicineName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        medicineID = resultSet.getInt("mathuoc");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return medicineID;
    }

    public Medicine getMedicineByName(String medicineName) {
        String query = "SELECT * FROM thuoc WHERE tenthuoc = ?";
        try (PreparedStatement statement = connectDB.databaseLink.prepareStatement(query)) {
            statement.setString(1, medicineName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Medicine medicine = new Medicine();
                    medicine.setMaThuoc(resultSet.getInt("mathuoc"));
                    medicine.setTenThuoc(resultSet.getString("tenthuoc"));
                    medicine.setMaDonViTinh(resultSet.getInt("madvt"));
                    medicine.setSoLuong(resultSet.getInt("soluong"));
                    medicine.setGiaBan(resultSet.getDouble("giaban"));
                    medicine.setMaDangThuoc(resultSet.getInt("madt"));
                    medicine.setMaCachDung(resultSet.getInt("macd"));
                    return medicine;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
