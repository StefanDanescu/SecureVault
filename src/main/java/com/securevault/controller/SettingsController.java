package com.securevault.controller;

import com.securevault.crypto.SecureMemory;
import com.securevault.model.VaultSettings;
import com.securevault.service.VaultService;
import com.securevault.util.Constants;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Controller for the settings dialog.
 */
public class SettingsController {

    @FXML private Spinner<Integer> autoLockSpinner;
    @FXML private Spinner<Integer> clipboardClearSpinner;
    @FXML private CheckBox showPasswordsCheck;
    @FXML private Spinner<Integer> defaultLengthSpinner;
    @FXML private CheckBox defaultUppercaseCheck;
    @FXML private CheckBox defaultLowercaseCheck;
    @FXML private CheckBox defaultNumbersCheck;
    @FXML private CheckBox defaultSymbolsCheck;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label changePasswordStatusLabel;

    @FXML private TextField exportPathField;
    @FXML private Label exportStatusLabel;

    private VaultService vaultService;

    @FXML
    public void initialize() {
        // Setup spinners
        autoLockSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 5));
        clipboardClearSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 30));
        defaultLengthSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 64, 16));
    }

    public void setVaultService(VaultService vaultService) {
        this.vaultService = vaultService;
        loadSettings();
    }

    private void loadSettings() {
        VaultSettings settings = vaultService.getCurrentVault().getSettings();
        
        autoLockSpinner.getValueFactory().setValue(settings.getAutoLockTimeoutMinutes());
        clipboardClearSpinner.getValueFactory().setValue(settings.getClipboardClearSeconds());
        showPasswordsCheck.setSelected(settings.isShowPasswordsByDefault());
        defaultLengthSpinner.getValueFactory().setValue(settings.getDefaultPasswordLength());
        defaultUppercaseCheck.setSelected(settings.isIncludeUppercase());
        defaultLowercaseCheck.setSelected(settings.isIncludeLowercase());
        defaultNumbersCheck.setSelected(settings.isIncludeNumbers());
        defaultSymbolsCheck.setSelected(settings.isIncludeSymbols());
    }

    @FXML
    private void handleSave() {
        VaultSettings settings = vaultService.getCurrentVault().getSettings();
        
        settings.setAutoLockTimeoutMinutes(autoLockSpinner.getValue());
        settings.setClipboardClearSeconds(clipboardClearSpinner.getValue());
        settings.setShowPasswordsByDefault(showPasswordsCheck.isSelected());
        settings.setDefaultPasswordLength(defaultLengthSpinner.getValue());
        settings.setIncludeUppercase(defaultUppercaseCheck.isSelected());
        settings.setIncludeLowercase(defaultLowercaseCheck.isSelected());
        settings.setIncludeNumbers(defaultNumbersCheck.isSelected());
        settings.setIncludeSymbols(defaultSymbolsCheck.isSelected());

        try {
            vaultService.saveVault();
            returnToVaultView();
        } catch (Exception e) {
            showError("Failed to save settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        returnToVaultView();
    }

    @FXML
    private void handleChangePassword() {
        changePasswordStatusLabel.setText("");
        changePasswordStatusLabel.setStyle("");

        char[] current = currentPasswordField.getText().toCharArray();
        char[] newPass = newPasswordField.getText().toCharArray();
        char[] confirm = confirmNewPasswordField.getText().toCharArray();

        try {
            if (current.length == 0) {
                setChangePasswordError("Enter your current password.");
                return;
            }
            if (newPass.length < Constants.MIN_PASSWORD_LENGTH) {
                setChangePasswordError("New password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters.");
                return;
            }
            if (!Arrays.equals(newPass, confirm)) {
                setChangePasswordError("New password and confirmation do not match.");
                return;
            }
            if (Arrays.equals(current, newPass)) {
                setChangePasswordError("New password must be different from the current password.");
                return;
            }

            vaultService.changeMasterPassword(current, newPass);

            changePasswordStatusLabel.setText("Master password changed successfully.");
            changePasswordStatusLabel.setStyle("-fx-text-fill: #2ecc71;");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();
        } catch (IllegalArgumentException e) {
            setChangePasswordError(e.getMessage());
        } catch (Exception e) {
            setChangePasswordError("Failed to change password: " + e.getMessage());
        } finally {
            SecureMemory.clear(current);
            SecureMemory.clear(newPass);
            SecureMemory.clear(confirm);
        }
    }

    private void setChangePasswordError(String message) {
        changePasswordStatusLabel.setText(message);
        changePasswordStatusLabel.setStyle("-fx-text-fill: #ff6b6b;");
    }

    @FXML
    private void handleExportBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export vault to...");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Vault Files", "*.vault", "*.json")
        );
        chooser.setInitialFileName("vault-export.vault");
        Path currentPath = vaultService.getCurrentVaultPath();
        if (currentPath != null && currentPath.getParent() != null) {
            try {
                chooser.setInitialDirectory(currentPath.getParent().toFile());
            } catch (Exception ignored) { }
        }
        java.io.File file = chooser.showSaveDialog(exportPathField.getScene().getWindow());
        if (file != null) {
            String path = file.getAbsolutePath();
            if (!path.endsWith(".vault") && !path.endsWith(".json")) {
                path = path + ".vault";
            }
            exportPathField.setText(path);
        }
    }

    @FXML
    private void handleExport() {
        exportStatusLabel.setText("");
        exportStatusLabel.setStyle("");

        String pathStr = exportPathField.getText();
        if (pathStr == null || pathStr.trim().isEmpty()) {
            exportStatusLabel.setText("Choose a location first (Browse...).");
            exportStatusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            return;
        }

        try {
            Path target = Path.of(pathStr.trim());
            vaultService.exportVault(target);
            exportStatusLabel.setText("Vault exported successfully to: " + target);
            exportStatusLabel.setStyle("-fx-text-fill: #2ecc71;");
        } catch (Exception e) {
            exportStatusLabel.setText("Export failed: " + e.getMessage());
            exportStatusLabel.setStyle("-fx-text-fill: #ff6b6b;");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void returnToVaultView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vault.fxml"));
            Parent root = loader.load();

            VaultController controller = loader.getController();
            controller.setVaultService(vaultService);

            Stage stage = (Stage) autoLockSpinner.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open vault: " + e.getMessage());
        }
    }
}
