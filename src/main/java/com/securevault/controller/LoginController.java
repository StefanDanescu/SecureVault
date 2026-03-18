package com.securevault.controller;

import java.io.File;
import java.nio.file.Path;

import com.securevault.crypto.SecureMemory;
import com.securevault.service.VaultService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for the login view.
 * 
 * Handles master password authentication and vault creation/opening.
 */
public class LoginController {

    @FXML private PasswordField masterPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordButton;
    @FXML private Button unlockButton;
    @FXML private Button createButton;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;
    @FXML private Label confirmLabel;

    private final VaultService vaultService;
    private boolean isCreateMode = false;
    private boolean passwordVisible = false;
    private Path selectedVaultPath;

    public LoginController() {
        this.vaultService = new VaultService();
    }

    @FXML
    public void initialize() {
        // Check if default vault exists
        if (vaultService.defaultVaultExists()) {
            setUnlockMode();
            selectedVaultPath = vaultService.getDefaultVaultPath();
        } else {
            setCreateMode();
        }

        // Bind visible password field to password field
        passwordVisibleField.textProperty().bindBidirectional(masterPasswordField.textProperty());
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);

        // Enter key triggers unlock/create
        masterPasswordField.setOnAction(e -> handlePrimaryAction());
        confirmPasswordField.setOnAction(e -> handlePrimaryAction());
    }

    private void setUnlockMode() {
        isCreateMode = false;
        titleLabel.setText("Unlock Vault");
        unlockButton.setVisible(true);
        unlockButton.setManaged(true);
        createButton.setText("Create New");
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setManaged(false);
        confirmLabel.setVisible(false);
        confirmLabel.setManaged(false);
    }

    private void setCreateMode() {
        isCreateMode = true;
        titleLabel.setText("Create New Vault");
        unlockButton.setVisible(false);
        unlockButton.setManaged(false);
        createButton.setText("Create Vault");
        confirmPasswordField.setVisible(true);
        confirmPasswordField.setManaged(true);
        confirmLabel.setVisible(true);
        confirmLabel.setManaged(true);
        selectedVaultPath = vaultService.getDefaultVaultPath();
    }

    @FXML
    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);
        masterPasswordField.setVisible(!passwordVisible);
        masterPasswordField.setManaged(!passwordVisible);
        togglePasswordButton.setText(passwordVisible ? "Hide" : "Show");
    }

    @FXML
    private void handleUnlock() {
        char[] password = masterPasswordField.getText().toCharArray();

        if (password.length == 0) {
            showError("Please enter your master password");
            return;
        }

        try {
            statusLabel.setText("Unlocking vault...");
            vaultService.openVault(selectedVaultPath, password);
            openVaultView();
        } catch (Exception e) {
            showError("Failed to unlock vault. Check your password.");
            e.printStackTrace();
        } finally {
            SecureMemory.clear(password);
        }
    }

    @FXML
    private void handleCreate() {
        if (!isCreateMode) {
            setCreateMode();
            return;
        }

        char[] password = masterPasswordField.getText().toCharArray();
        char[] confirm = confirmPasswordField.getText().toCharArray();

        try {
            if (password.length < 8) {
                showError("Password must be at least 8 characters");
                return;
            }

            if (!java.util.Arrays.equals(password, confirm)) {
                showError("Passwords do not match");
                return;
            }

            statusLabel.setText("Creating vault...");

            // Ensure parent directory exists
            if (selectedVaultPath.getParent() != null) {
                java.nio.file.Files.createDirectories(selectedVaultPath.getParent());
            }

            vaultService.createVault(selectedVaultPath, password);
            openVaultView();
        } catch (Exception e) {
            showError("Failed to create vault: " + e.getMessage());
            e.printStackTrace();
        } finally {
            SecureMemory.clear(password);
            SecureMemory.clear(confirm);
        }
    }

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Vault File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Vault Files", "*.vault", "*.json")
        );

        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            selectedVaultPath = file.toPath();
            setUnlockMode();
            statusLabel.setText("Selected: " + file.getName());
        }
    }

    private void handlePrimaryAction() {
        if (isCreateMode) {
            handleCreate();
        } else {
            handleUnlock();
        }
    }

    private void openVaultView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vault.fxml"));
            Parent root = loader.load();

            VaultController controller = loader.getController();
            controller.setVaultService(vaultService);

            Stage stage = getStage();
            double w = stage.getScene().getWidth();
            double h = stage.getScene().getHeight();
            Scene scene = new Scene(root, w, h);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open vault view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
    }

    private Stage getStage() {
        return (Stage) masterPasswordField.getScene().getWindow();
    }
}
