package com.securevault.controller;

import com.securevault.model.Category;
import com.securevault.model.PasswordEntry;
import com.securevault.model.Vault;
import com.securevault.service.PasswordGenerator;
import com.securevault.service.PasswordStrength;
import com.securevault.service.VaultService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the add/edit entry dialog.
 */
public class EntryDialogController {

    @FXML private TextField titleField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private TextField urlField;
    @FXML private TextArea notesField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private Spinner<Integer> lengthSpinner;
    @FXML private CheckBox uppercaseCheck;
    @FXML private CheckBox lowercaseCheck;
    @FXML private CheckBox numbersCheck;
    @FXML private CheckBox symbolsCheck;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;

    private final PasswordGenerator passwordGenerator;
    private final PasswordStrength passwordStrength;

    private Vault vault;
    private VaultService vaultService;
    private PasswordEntry entry;
    private boolean isNew = true;
    private boolean saved = false;
    private boolean passwordVisible = false;
    private static final int PASSWORD_REUSE_CHECK_COUNT = 3;

    public EntryDialogController() {
        this.passwordGenerator = new PasswordGenerator();
        this.passwordStrength = new PasswordStrength();
    }

    @FXML
    public void initialize() {
        // Setup length spinner
        lengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 64, 16));

        // Password visibility binding
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);

        // Password strength listener
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateStrength(newVal));

        // Default generator options
        uppercaseCheck.setSelected(true);
        lowercaseCheck.setSelected(true);
        numbersCheck.setSelected(true);
        symbolsCheck.setSelected(true);
    }

    public void setVault(Vault vault) {
        this.vault = vault;
        
        // Populate categories
        categoryCombo.getItems().clear();
        categoryCombo.getItems().addAll(vault.getCategories());
        
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }

        // Apply "show passwords by default" setting
        passwordVisible = vault.getSettings().isShowPasswordsByDefault();
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
    }

    public void setEntry(PasswordEntry entry) {
        if (entry == null) {
            this.entry = new PasswordEntry();
            this.isNew = true;
        } else {
            this.entry = entry;
            this.isNew = false;
            populateFields();
        }
    }

    private void populateFields() {
        titleField.setText(entry.getTitle());
        usernameField.setText(entry.getUsername());
        passwordField.setText(entry.getPassword());
        urlField.setText(entry.getUrl());
        notesField.setText(entry.getNotes());

        // Select category
        if (entry.getCategoryId() != null) {
            for (Category cat : categoryCombo.getItems()) {
                if (cat.getId().equals(entry.getCategoryId())) {
                    categoryCombo.getSelectionModel().select(cat);
                    break;
                }
            }
        }
    }

    private void updateStrength(String password) {
        if (password == null || password.isEmpty()) {
            strengthBar.setProgress(0);
            strengthLabel.setText("");
            return;
        }

        PasswordStrength.StrengthResult result = passwordStrength.calculate(password);
        strengthBar.setProgress(result.getPercentage() / 100.0);
        strengthBar.setStyle("-fx-accent: " + result.getStrength().getColor() + ";");
        strengthLabel.setText(result.getStrength().getLabel());
    }

    @FXML
    private void handleGeneratePassword() {
        int length = lengthSpinner.getValue();
        String password = passwordGenerator.generate(
            length,
            uppercaseCheck.isSelected(),
            lowercaseCheck.isSelected(),
            numbersCheck.isSelected(),
            symbolsCheck.isSelected()
        );
        passwordField.setText(password);
    }

    @FXML
    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
    }

    @FXML
    private void handleSave() {
        // Validate required fields
        if (titleField.getText().trim().isEmpty()) {
            showError("Title is required");
            return;
        }
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required");
            return;
        }
        if (passwordField.getText().isEmpty()) {
            showError("Password is required");
            return;
        }

        String newPassword = passwordField.getText();
        if (!isNew && entry.getPassword() != null && !entry.getPassword().equals(newPassword)
                && entry.usesRecentPassword(newPassword, PASSWORD_REUSE_CHECK_COUNT)) {
            showError("You cannot reuse any of your last 3 passwords.");
            return;
        }

        // Update entry
        entry.setTitle(titleField.getText().trim());
        entry.setUsername(usernameField.getText().trim());
        entry.setPassword(newPassword);
        entry.setUrl(urlField.getText().trim());
        entry.setNotes(notesField.getText());

        Category selectedCategory = categoryCombo.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            entry.setCategoryId(selectedCategory.getId());
        }

        // Add to vault if new
        if (isNew) {
            vault.addEntry(entry);
        }

        try {
            vaultService.saveVault();
        } catch (Exception e) {
            showError("Failed to save entry: " + e.getMessage());
            return;
        }

        saved = true;
        returnToVaultView();
    }

    @FXML
    private void handleCancel() {
        returnToVaultView();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setVaultService(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    private void returnToVaultView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vault.fxml"));
            Parent root = loader.load();

            VaultController controller = loader.getController();
            controller.setVaultService(vaultService);

            Stage stage = (Stage) titleField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open vault: " + e.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
