package com.example.privateclinic.Controllers;

import com.example.privateclinic.DataAccessObject.HistoryDAO;
import com.example.privateclinic.DataAccessObject.UserDAO;
import com.example.privateclinic.ForeignKeyViolationException;
import com.example.privateclinic.Models.History;
import com.example.privateclinic.Models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.net.URL;
import java.text.Normalizer;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.util.Collections.replaceAll;

public class EmployeeController implements Initializable {
    public Button btnDeleteEmployee;
    public Button btnAddEmployee;
    public Button btnEditEmployee;
    public Button btnCancel;
    public ComboBox<String> cb_position;
    @FXML
    private TextField tfEmployee;
    @FXML
    private TextField tf_addName;
    @FXML
    private TextField tf_addcitizenId;
    @FXML
    private TextField tf_addAddress;
    @FXML
    private TextField tf_addPhoneNum;
    @FXML
    private TextField tf_addEmail;
    @FXML
    private TextField tf_addPosition;
    public TextField tf_maNV;
    @FXML
    private TableView<User> employeeTableView;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> citizenIdColumn;
    @FXML
    private TableColumn<User, String> addressColumn;
    @FXML
    private TableColumn<User, String> phoneNumColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> positionColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    public Pane paneProgress;
    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> employees;
    User user;
    private HistoryDAO historyDAO = new HistoryDAO();

    public void initData(User user) {
        this.user =user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureTableColumns();
        loadEmployeeData();
        setOnOffAddDeleteBtn();
        validatePhoneNumber(tf_addPhoneNum);
        btnDeleteEmployee.setOnAction(event -> handleDeleteAction());
        btnAddEmployee.setOnAction(event -> handleAddAction());
        btnEditEmployee.setOnAction(event -> handleEditAction()); // Add this line to handle edit action
        btnCancel.setOnAction(event -> handleCancel());
        tfEmployee.setOnKeyPressed(event -> handleSearchKeyPressed(event));
        ObservableList<String> statusList = FXCollections.observableArrayList( "Bác sĩ","Chủ","Tiếp tân");
        cb_position.setItems(statusList);
        addSelectionListener(); // Add this line to handle selection changes
        validateInput();
    }

    private void validateInput() {
        tf_addPhoneNum.textProperty().addListener((observable, oldValue, newValue) -> {
            // Loại bỏ bất kỳ ký tự không phải là số
            String formattedPhoneNumber = newValue.replaceAll("[^\\d]", "");

            // Kiểm tra điều kiện:
            // 1. Bắt đầu bằng 0.
            // 2. Chiều dài tối đa là 10 ký tự.
            if (formattedPhoneNumber.length() == 0 || formattedPhoneNumber.startsWith("0")) {
                if (formattedPhoneNumber.length() <= 10) {
                    tf_addPhoneNum.setText(formattedPhoneNumber);
                } else {
                    // Nếu chiều dài vượt quá 10 ký tự, cắt chuỗi thành 10 ký tự đầu tiên
                    tf_addPhoneNum.setText(formattedPhoneNumber.substring(0, 10));
                }
            } else {
                // Nếu không bắt đầu bằng 0, giữ nguyên giá trị cũ
                tf_addPhoneNum.setText(oldValue);
            }
        });
        tf_addcitizenId.textProperty().addListener((observable, oldValue, newValue) -> {
            // Loại bỏ bất kỳ ký tự không phải là số
            String formattedPhoneNumber = newValue.replaceAll("[^\\d]", "");

            // Kiểm tra điều kiện:
            // 1. Bắt đầu bằng 0.
            // 2. Chiều dài tối đa là 10 ký tự.
            if (formattedPhoneNumber.length() == 0 || formattedPhoneNumber.startsWith("0")) {
                if (formattedPhoneNumber.length() <= 12) {
                    tf_addcitizenId.setText(formattedPhoneNumber);
                } else {
                    // Nếu chiều dài vượt quá 10 ký tự, cắt chuỗi thành 10 ký tự đầu tiên
                    tf_addcitizenId.setText(formattedPhoneNumber.substring(0, 12));
                }
            }
        });
    }
    private void validatePhoneNumber(TextField tfPhoneNumber) {
        tfPhoneNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            // Loại bỏ bất kỳ ký tự không phải là số
            String formattedPhoneNumber = newValue.replaceAll("[^\\d]", "");

            // Kiểm tra điều kiện:
            // 1. Bắt đầu bằng 0.
            // 2. Chiều dài tối đa là 10 ký tự.
            if (formattedPhoneNumber.length() == 0 || formattedPhoneNumber.startsWith("0")) {
                if (formattedPhoneNumber.length() <= 10) {
                    tfPhoneNumber.setText(formattedPhoneNumber);
                } else {
                    // Nếu chiều dài vượt quá 10 ký tự, cắt chuỗi thành 10 ký tự đầu tiên
                    tfPhoneNumber.setText(formattedPhoneNumber.substring(0, 10));
                }
            } else {
                // Nếu không bắt đầu bằng 0, giữ nguyên giá trị cũ
                tfPhoneNumber.setText(oldValue);
            }
        });
    }
    private void setOnOffAddDeleteBtn() {
        btnDeleteEmployee.setDisable(true); // Bắt đầu bằng việc vô hiệu hóa nút Delete

    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("Employee_id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        citizenIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeCitizenId"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("employeeAddress"));
        phoneNumColumn.setCellValueFactory(new PropertyValueFactory<>("employeePhoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("employeeEmail"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("employeePosition"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeUsername"));
    }

    private void loadEmployeeData() {
        employees = userDAO.getAllEmployees();
        employeeTableView.setItems(employees);
    }

    private void handleSearchAction(String search) {
        employeeTableView.setItems(userDAO.seatchUser(search));
    }

    private void handleDeleteAction() {
        User selectedEmployee = employeeTableView.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            int sequence = ShowYesNoAlert("xoá "+selectedEmployee.getEmployeeName());
            if(sequence==JOptionPane.YES_OPTION) {
                try {
                    if (userDAO.deleteEmployee(selectedEmployee.getEmployee_id()))  {
                        employees.remove(selectedEmployee);
                        History history = new History(user.getEmployee_id(), STR."Đã xóa nhân viên ID: \{selectedEmployee.getEmployee_id()} - \{selectedEmployee.getEmployeeName()}");
                        historyDAO.addHistory(history);
                        loadEmployeeData();
                    }
                } catch (ForeignKeyViolationException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Cảnh báo");
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }

    @FXML
    private void handleSearchKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSearchAction(tfEmployee.getText());
        }
    }

    private void handleAddAction() {
        String name = tf_addName.getText();
        String citizenId = tf_addcitizenId.getText();
        String address = tf_addAddress.getText();
        String phoneNum = tf_addPhoneNum.getText();
        String email = tf_addEmail.getText();
        String position = cb_position.getValue();
        String username = tf_addEmail.getText();
        if (!name.isEmpty() && !citizenId.isEmpty() && !address.isEmpty() && !phoneNum.isEmpty() && checkEmail() && !cb_position.getValue().isEmpty()) {
        paneProgress.setVisible(true);
        new Thread(()->{
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    if(btnAddEmployee.getText().equals("Lưu")) {
                        int id = Integer.parseInt(tf_maNV.getText());
                        int sequence = ShowYesNoAlert("lưu "+name);
                        if(sequence==JOptionPane.YES_OPTION) {
                            User employee = new User(id,name, citizenId, address, phoneNum, email, position, username);
                            if (userDAO.updateEmployee(employee)) {
                                btnAddEmployee.setText("Thêm");
                                loadEmployeeData();
                                clearAddEmployeeFields();
                                cb_position.setVisible(false);
                                tf_addPosition.setVisible(true);
                                History history = new History(user.getEmployee_id(), STR."Đã chỉnh sửa thông tin nhân viên ID: \{employee.getEmployee_id()} - \{employee.getEmployeeName()}");
                                historyDAO.addHistory(history);
                            } else {
                                showAlert("Warning", "Error!");
                            }
                        }
                    } else {
                        int sequence = ShowYesNoAlert("thêm "+name);
                        if(sequence==JOptionPane.YES_OPTION) {
                            User newEmployee = new User(name, citizenId, address, phoneNum, email, position, username);
                            if(userDAO.addEmployee(newEmployee)) {
                                SendDefaultPassword(newEmployee.getEmployeeEmail());
                                loadEmployeeData();
                                clearAddEmployeeFields();
                                cb_position.setVisible(false);
                                tf_addPosition.setVisible(true);
                                History history = new History(user.getEmployee_id(), STR."Đã thêm nhân viên ID: \{newEmployee.getEmployee_id()} - \{newEmployee.getEmployeeName()}");
                                historyDAO.addHistory(history);
                            }
                        }
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                Platform.runLater(() -> {
                    paneProgress.setVisible(false);
                });
            }
        }).start();
        } else {
            showAlert("Warning","Kiểm tra lại thông tin!");
        }
    }

    private void AddEmployee() {

    }

    private boolean checkEmail() {
        if(tf_addEmail.getText().isEmpty()) return false;
        String email = tf_addEmail.getText();
        int atIndex = email.indexOf("@");
        if (atIndex <= 0) {
            showAlert("Warning","Email is invalid");
            return false;
        }
        return true;
    }

    private void handleEditAction() {
        User selectedEmployee = employeeTableView.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            btnAddEmployee.setText("Lưu");
            btnAddEmployee.setDisable(false);
            SetDisable(false);
            cb_position.setVisible(true);
            tf_addPosition.setVisible(false);
            cb_position.setValue(tf_addPosition.getText());
        }
    }
    private void handleCancel() {
        clearAddEmployeeFields();
        SetDisable(false);
        btnAddEmployee.setDisable(false);
        btnDeleteEmployee.setDisable(true);
        btnEditEmployee.setDisable(true);
        cb_position.setVisible(true);
        tf_addPosition.setVisible(false);
        btnAddEmployee.setText("Thêm");
    }
    private void addSelectionListener() {
        employeeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean employeeSelected = newValue != null;
            SetDisable(true);
            cb_position.setVisible(false);
            tf_addPosition.setVisible(true);
            btnDeleteEmployee.setDisable(false);
            btnAddEmployee.setDisable(true);
            btnEditEmployee.setDisable(false);
            if (employeeSelected) {
                // Hiển thị thông tin nhân viên lên các TextField
                tf_addName.setText(newValue.getEmployeeName());
                tf_addcitizenId.setText(newValue.getEmployeeCitizenId());
                tf_addAddress.setText(newValue.getEmployeeAddress());
                tf_addPhoneNum.setText(newValue.getEmployeePhoneNumber());
                tf_addEmail.setText(newValue.getEmployeeEmail());
                tf_addPosition.setText(newValue.getEmployeePosition());
                tf_maNV.setText(String.valueOf(newValue.getEmployee_id()));
            } else {
                clearAddEmployeeFields();
            }
        });
        tfEmployee.textProperty().addListener((observable, oldValue,newValue )-> {
            if(tfEmployee.getText().isEmpty()) {
                employeeTableView.setItems(employees);
            }
        });
    }

    private void SetDisable(boolean bool) {
        tf_addAddress.setDisable(bool);
        tf_addEmail.setDisable(bool);
        tf_addPosition.setDisable(bool);
        tf_addcitizenId.setDisable(bool);
        tf_addPhoneNum.setDisable(bool);
        tf_addName.setDisable(bool);
    }

    private void clearAddEmployeeFields() {
        tf_addName.clear();
        tf_addcitizenId.clear();
        tf_addAddress.clear();
        tf_addPhoneNum.clear();
        tf_addEmail.clear();
        tf_addPosition.clear();
        tf_maNV.clear();
    }
    private void showAlert(String tilte,String string) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tilte);
        alert.setHeaderText(null);
        alert.setContentText(string);
        alert.showAndWait();
    }
    private int ShowYesNoAlert(String string) {
        JFrame frame = new JFrame("Table Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        return JOptionPane.showConfirmDialog(frame, "Có phải bạn muốn "+string+"?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
    private void SendDefaultPassword(String email) {
        String defaultpassword = userDAO.getDefaultpassword();
        String fromEmail = "kiseryouta2003@gmail.com";
        String password = "qcqa slmu vkbr edha";
        String subject = "OTP code";
        String body = "Your default password for Clinic account is " + defaultpassword;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "Green UIT Clinic"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            Platform.runLater(() -> {
                showAlert("Warning"," Mật khẩu mặc định là "+defaultpassword+", đã gửi về email: "+email+" !");
                userDAO.setDefaultpassword(null);
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Warning","Failed to send OTP: " + e.getMessage());
        }
    }
}


