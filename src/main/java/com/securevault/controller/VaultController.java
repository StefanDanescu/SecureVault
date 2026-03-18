package com.securevault.controller;

import java.util.List;
import java.util.Optional;

import com.securevault.model.Category;
import com.securevault.model.PasswordEntry;
import com.securevault.model.Vault;
import com.securevault.service.ClipboardService;
import com.securevault.service.PasswordStrength;
import com.securevault.service.VaultService;
import com.securevault.util.AutoLockTimer;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Controller for the main vault view.
 * 
 * Manages the display and interaction with password entries.
 */
public class VaultController {

    @FXML private TreeView<Category> categoryTree;
    @FXML private TableView<PasswordEntry> entryTable;
    @FXML private TableColumn<PasswordEntry, String> titleColumn;
    @FXML private TableColumn<PasswordEntry, String> usernameColumn;
    @FXML private TableColumn<PasswordEntry, String> urlColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label entryCountLabel;

    // Detail pane
    @FXML private Label detailTitle;
    @FXML private Button detailsToggleButton;
    @FXML private TextField detailUsername;
    @FXML private PasswordField detailPassword;
    @FXML private TextField detailPasswordVisible;
    @FXML private TextField detailUrl;
    @FXML private TextArea detailNotes;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private SplitPane mainSplitPane;
    @FXML private javafx.scene.layout.VBox detailsBox;
    @FXML private javafx.scene.layout.VBox detailsContentBox;

    private VaultService vaultService;
    private ClipboardService clipboardService;
    private PasswordStrength passwordStrength;
    private AutoLockTimer autoLockTimer;

    private ObservableList<PasswordEntry> entryList;
    private PasswordEntry selectedEntry;
    private boolean passwordVisible = false;
    private boolean detailsCollapsed = true;

    public VaultController() {
        this.clipboardService = new ClipboardService();
        this.passwordStrength = new PasswordStrength();
        this.entryList = FXCollections.observableArrayList();
    }

    public void setVaultService(VaultService vaultService) {
        this.vaultService = vaultService;
        loadVault();
    }

    @FXML
    public void initialize() {
        // Setup table columns
        titleColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getTitle()));
        usernameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUsername()));
        urlColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getUrl()));

        entryTable.setItems(entryList);
        // Make columns fill available width
        entryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Selection listener
        entryTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showEntryDetails(newVal));

        // Search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterEntries(newVal));

        // Password visibility binding
        detailPasswordVisible.textProperty().bindBidirectional(detailPassword.textProperty());
        detailPasswordVisible.setVisible(false);
        detailPasswordVisible.setManaged(false);

        // Double-click to edit
        entryTable.setOnMouseClicked(this::handleTableClick);

        // Start with details panel collapsed/hidden
        collapseDetailsPanel();

        // Initialize auto-lock timer
        autoLockTimer = new AutoLockTimer(this::handleLock);
    }

    private void loadVault() {
        if (vaultService == null || !vaultService.isUnlocked()) {
            return;
        }

        Vault vault = vaultService.getCurrentVault();
        
        // Load categories
        TreeItem<Category> root = new TreeItem<>(new Category("All"));
        root.setExpanded(true);
        
        for (Category category : vault.getCategories()) {
            root.getChildren().add(new TreeItem<>(category));
        }
        categoryTree.setRoot(root);

        // Category selection listener
        categoryTree.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    filterByCategory(newVal.getValue());
                }
            });

        // Load all entries initially
        entryList.setAll(vault.getEntries());
        updateEntryCount();

        // Setup auto-lock
        int timeout = vault.getSettings().getAutoLockTimeoutMinutes();
        autoLockTimer.setTimeoutMinutes(timeout);
        autoLockTimer.start();

        // Clipboard clear delay
        clipboardService.setClearDelaySeconds(vault.getSettings().getClipboardClearSeconds());

        // Apply "show passwords by default" to the detail pane
        passwordVisible = vault.getSettings().isShowPasswordsByDefault();
        detailPasswordVisible.setVisible(passwordVisible);
        detailPasswordVisible.setManaged(passwordVisible);
        detailPassword.setVisible(!passwordVisible);
        detailPassword.setManaged(!passwordVisible);

        statusLabel.setText("Vault unlocked");
    }

    private void showEntryDetails(PasswordEntry entry) {
        selectedEntry = entry;
        
        if (entry == null) {
            clearDetails();
            // Hide again when nothing is selected
            collapseDetailsPanel();
            return;
        }

        // Selecting any entry should expand and show details
        expandDetailsPanel();

        detailTitle.setText(entry.getTitle());
        detailUsername.setText(entry.getUsername());
        detailPassword.setText(entry.getPassword());
        detailUrl.setText(entry.getUrl());
        detailNotes.setText(entry.getNotes());

        // Update strength indicator
        updateStrengthIndicator(entry.getPassword());
    }

    @FXML
    private void handleToggleDetailsPanel() {
        if (detailsCollapsed) {
            expandDetailsPanel();
        } else {
            collapseDetailsPanel();
        }
    }

    private void collapseDetailsPanel() {
        detailsCollapsed = true;

        // Hide content but keep header toggle visible
        if (detailsContentBox != null) {
            detailsContentBox.setVisible(false);
            detailsContentBox.setManaged(false);
        }
        if (mainSplitPane != null) {
            mainSplitPane.setDividerPositions(1.0);
        }
        if (detailsToggleButton != null) {
            detailsToggleButton.setText("<");
        }
    }

    private void expandDetailsPanel() {
        detailsCollapsed = false;

        if (detailsContentBox != null) {
            detailsContentBox.setVisible(true);
            detailsContentBox.setManaged(true);
        }
        if (mainSplitPane != null) {
            mainSplitPane.setDividerPositions(0.65);
        }
        if (detailsToggleButton != null) {
            detailsToggleButton.setText(">");
        }
    }

    private void clearDetails() {
        detailTitle.setText("");
        detailUsername.setText("");
        detailPassword.setText("");
        detailUrl.setText("");
        detailNotes.setText("");
        strengthBar.setProgress(0);
        strengthLabel.setText("");
    }

    private void updateStrengthIndicator(String password) {
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

    private void filterEntries(String query) {
        if (query == null || query.trim().isEmpty()) {
            entryList.setAll(vaultService.getCurrentVault().getEntries());
        } else {
            List<PasswordEntry> filtered = vaultService.getCurrentVault().searchEntries(query);
            entryList.setAll(filtered);
        }
        updateEntryCount();
    }

    private void filterByCategory(Category category) {
        if (category == null || "All".equals(category.getName())) {
            entryList.setAll(vaultService.getCurrentVault().getEntries());
        } else {
            List<PasswordEntry> filtered = vaultService.getCurrentVault()
                .findEntriesByCategory(category.getId());
            entryList.setAll(filtered);
        }
        updateEntryCount();
    }

    private void updateEntryCount() {
        entryCountLabel.setText(entryList.size() + " entries");
    }

    @FXML
    private void handleAddEntry() {
        openEntryDialog(null);
    }

    @FXML
    private void handleEditEntry() {
        if (selectedEntry != null) {
            openEntryDialog(selectedEntry);
        }
    }

    @FXML
    private void handleDeleteEntry() {
        if (selectedEntry == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Entry");
        alert.setHeaderText("Delete " + selectedEntry.getTitle() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            vaultService.getCurrentVault().removeEntry(selectedEntry.getId());
            saveAndRefresh();
        }
    }

    @FXML
    private void handleCopyUsername() {
        if (selectedEntry != null && selectedEntry.getUsername() != null) {
            clipboardService.copy(selectedEntry.getUsername());
            statusLabel.setText("Username copied");
        }
    }

    @FXML
    private void handleCopyPassword() {
        if (selectedEntry != null && selectedEntry.getPassword() != null) {
            clipboardService.copyWithAutoClear(selectedEntry.getPassword());
            statusLabel.setText("Password copied (auto-clear in " + 
                clipboardService.getClearDelaySeconds() + "s)");
        }
    }

    @FXML
    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        detailPasswordVisible.setVisible(passwordVisible);
        detailPasswordVisible.setManaged(passwordVisible);
        detailPassword.setVisible(!passwordVisible);
        detailPassword.setManaged(!passwordVisible);
    }

    @FXML
    private void handleLock() {
        autoLockTimer.stop();
        vaultService.lockVault();
        openLoginView();
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            Parent root = loader.load();

            SettingsController controller = loader.getController();
            controller.setVaultService(vaultService);

            Stage stage = (Stage) entryTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open settings: " + e.getMessage());
        }
    }

    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedEntry != null) {
            openEntryDialog(selectedEntry);
        }
    }

    private void openEntryDialog(PasswordEntry entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/entry-dialog.fxml"));
            Parent root = loader.load();

            EntryDialogController controller = loader.getController();
            controller.setVault(vaultService.getCurrentVault());
            controller.setVaultService(vaultService);
            controller.setEntry(entry);

            Stage stage = (Stage) entryTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open entry dialog: " + e.getMessage());
        }
    }

    private void saveAndRefresh() {
        try {
            vaultService.saveVault();
            loadVault();
        } catch (Exception e) {
            showError("Failed to save vault: " + e.getMessage());
        }
    }

    private void openLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) entryTable.getScene().getWindow();
            double w = stage.getScene().getWidth();
            double h = stage.getScene().getHeight();
            Scene scene = new Scene(root, w, h);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
        } catch (Exception e) {
            showError("Failed to open login: " + e.getMessage());
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
    }
}
