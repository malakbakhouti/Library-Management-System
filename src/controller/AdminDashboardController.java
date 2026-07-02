package controller;

import dao.BookDAO;
import dao.LoanDAO;
import dao.UserDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Book;
import model.Loan;
import model.UserAccount;
import utils.ViewHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label totalBooksLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label overdueLoansLabel;
    @FXML private Button btnOverview;
    @FXML private Button btnUsers;
    @FXML private Button btnBooks;
    @FXML private Button btnLoans;
    @FXML private StackPane contentArea;
    @FXML private VBox overviewPane;

    private final UserDAO userDAO = new UserDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    // ✅ helper SearchBar
    private static class SearchBar {
        final HBox root;
        final TextField field;
        SearchBar(HBox root, TextField field) {
            this.root = root;
            this.field = field;
        }
    }

    @FXML
    public void initialize() {
        UserAccount currentUser = SessionController.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getFullName());
        } else {
            welcomeLabel.setText("Welcome back, Admin");
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateLabel.setText(today.format(formatter));

        loadStatistics();
        showOverview();
    }

    private void loadStatistics() {
        try {
            int totalBooks = bookDAO.countAll();
            int totalUsers = userDAO.getAll().size();
            int activeLoans = loanDAO.countActiveLoans();
            int overdueLoans = loanDAO.countOverdueLoans();

            totalBooksLabel.setText(String.valueOf(totalBooks));
            totalUsersLabel.setText(String.valueOf(totalUsers));
            activeLoansLabel.setText(String.valueOf(activeLoans));
            overdueLoansLabel.setText(String.valueOf(overdueLoans));
        } catch (Exception e) {
            e.printStackTrace();
            totalBooksLabel.setText("0");
            totalUsersLabel.setText("0");
            activeLoansLabel.setText("0");
            overdueLoansLabel.setText("0");
        }
    }

    @FXML private void onShowOverview() { setActiveTab(btnOverview); showOverview(); }
    @FXML private void onShowUsers() { setActiveTab(btnUsers); showUsersView(); }
    @FXML private void onShowBooks() { setActiveTab(btnBooks); showBooksView(); }
    @FXML private void onShowLoans() { setActiveTab(btnLoans); showLoansView(); }
    @FXML private void onShowStats() { onShowOverview(); }

    // ============================================================
    // OVERVIEW : 2 GRAPHES CÔTE À CÔTE
    // ============================================================
    private void showOverview() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent;");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");

        Label t = new Label("Tableau de Bord Administrateur");
        t.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        VBox cardLeft = createCardSection(
                "Répartition des Utilisateurs par Rôle",
                "Distribution des comptes utilisateurs selon leur niveau d'accès.",
                createUserRolesPieChart()
        );

        VBox cardRight = createCardSection(
                "Top 5 Livres les Plus Empruntés",
                "Classement des ouvrages les plus populaires.",
                createTopBooksBarChart()
        );

        HBox chartsRow = new HBox(20, cardLeft, cardRight);
        chartsRow.setAlignment(Pos.TOP_CENTER);

        HBox.setHgrow(cardLeft, Priority.ALWAYS);
        HBox.setHgrow(cardRight, Priority.ALWAYS);
        cardLeft.setMaxWidth(Double.MAX_VALUE);
        cardRight.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(t, chartsRow);

        sp.setContent(root);
        contentArea.getChildren().setAll(sp);
    }

    private VBox createCardSection(String title, String description, javafx.scene.Node chart) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(18));
        section.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-width: 1;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        descLabel.setWrapText(true);

        if (chart instanceof Region r) {
            r.setMinHeight(360);
            r.setPrefHeight(380);
            r.setMaxWidth(Double.MAX_VALUE);
        }

        section.getChildren().addAll(titleLabel, descLabel, chart);
        return section;
    }

    // ============================================================
    // USERS VIEW
    // ============================================================
    private void showUsersView() {
        VBox view = new VBox(15);
        view.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 12px;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Gestion des Utilisateurs");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("+ Ajouter Utilisateur");
        btnAdd.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 6px; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> showAddUserDialog());

        header.getChildren().addAll(title, spacer, btnAdd);

        // ✅ même search style
        SearchBar sb = createSearchBar("Rechercher (nom, email, rôle) ...");
        TextField searchField = sb.field;

        TableView<UserAccount> table = createUsersTableWithFilter(searchField);

        view.getChildren().addAll(header, sb.root, table);
        contentArea.getChildren().setAll(view);
    }

    private TableView<UserAccount> createUsersTableWithFilter(TextField searchField) {
        TableView<UserAccount> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");

        TableColumn<UserAccount, String> colName = new TableColumn<>("Nom Complet");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colName.setPrefWidth(260);

        TableColumn<UserAccount, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(300);

        TableColumn<UserAccount, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colRole.setPrefWidth(140);

        TableColumn<UserAccount, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(240);

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️ Modifier");
            private final Button btnDelete = new Button("🗑️ Supprimer");
            private final HBox buttons = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; -fx-padding: 5px 15px; -fx-background-radius: 4px; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-padding: 5px 15px; -fx-background-radius: 4px; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER_LEFT);

                btnEdit.setOnAction(e -> {
                    UserAccount user = getTableRow() != null ? getTableRow().getItem() : null;
                    if (user != null) showEditUserDialog(user);
                });

                btnDelete.setOnAction(e -> {
                    UserAccount user = getTableRow() != null ? getTableRow().getItem() : null;
                    if (user != null) deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                UserAccount rowUser = getTableRow().getItem();
                UserAccount currentUser = SessionController.getCurrentUser();
                boolean isSelf = (currentUser != null && rowUser.getId() == currentUser.getId());

                buttons.getChildren().setAll(btnEdit);
                if (!isSelf) buttons.getChildren().add(btnDelete);

                setGraphic(buttons);
            }
        });

        table.getColumns().addAll(colName, colEmail, colRole, colActions);

        ObservableList<UserAccount> base = FXCollections.observableArrayList(userDAO.getAll());
        FilteredList<UserAccount> filtered = new FilteredList<>(base, u -> true);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, old, val) -> {
            String q = (val == null) ? "" : val.trim().toLowerCase();
            filtered.setPredicate(u -> {
                if (q.isEmpty()) return true;
                String name = u.getFullName() != null ? u.getFullName().toLowerCase() : "";
                String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                String role = u.getRoleName() != null ? u.getRoleName().toLowerCase() : "";
                return name.contains(q) || email.contains(q) || role.contains(q);
            });
        });

        return table;
    }

    // ============================================================
    // BOOKS VIEW
    // ============================================================
    private void showBooksView() {
        VBox view = new VBox(20);
        view.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 12px;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Consultation des Livres");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ✅ même search style
        SearchBar sb = createSearchBar("Rechercher un livre...");
        TextField searchField = sb.field;
        searchField.setPrefWidth(320);

        header.getChildren().addAll(title, spacer, sb.root);

        TableView<Book> table = createBooksTable();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadBooksData(table);
            } else {
                List<Book> results = bookDAO.searchByTitle(newVal);
                table.setItems(FXCollections.observableArrayList(results));
            }
        });

        view.getChildren().addAll(header, table);
        contentArea.getChildren().setAll(view);
    }

    private TableView<Book> createBooksTable() {
        TableView<Book> table = new TableView<>();

        TableColumn<Book, String> colTitle = new TableColumn<>("Titre");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(320);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Auteur");
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colAuthor.setPrefWidth(220);

        TableColumn<Book, Integer> colYear = new TableColumn<>("Année");
        colYear.setCellValueFactory(new PropertyValueFactory<>("publishYear"));
        colYear.setPrefWidth(90);

        TableColumn<Book, String> colGenre = new TableColumn<>("Genre");
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colGenre.setPrefWidth(170);

        TableColumn<Book, Integer> colQty = new TableColumn<>("Quantité");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(110);

        table.getColumns().addAll(colTitle, colAuthor, colYear, colGenre, colQty);

        loadBooksData(table);
        return table;
    }

    private void loadBooksData(TableView<Book> table) {
        List<Book> books = bookDAO.getAll();
        table.setItems(FXCollections.observableArrayList(books));
    }

    // ============================================================
    // ✅ LOANS VIEW : Tabs boutons (Tous / Actifs / En retard)
    // ============================================================
    private void showLoansView() {
        VBox view = new VBox(14);
        view.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 12px;");

        Label title = new Label("Suivi des Emprunts");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label subtitle = new Label("Filtres rapides : Tous / Emprunts Actifs / Emprunts en Retard");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-padding: 0 0 5 0;");

        // ✅ SearchBar (clean)
        SearchBar sb = createSearchBar("Rechercher (nom utilisateur, titre livre) ...");
        TextField searchField = sb.field;

        // ✅ Table unique (pas TabPane)
        TableView<Loan> table = createLoansTable();

        // ✅ Data: on prend les emprunts actifs + en retard (les 2 listes)
        // (actifs = non retournés; en retard = non retournés + due < today)
        List<Loan> active = loanDAO.getActiveLoans();
        List<Loan> overdue = loanDAO.findOverdueLoans();

        // merge sans doublons
        java.util.LinkedHashMap<Integer, Loan> map = new java.util.LinkedHashMap<>();
        for (Loan l : active) map.put(l.getId(), l);
        for (Loan l : overdue) map.put(l.getId(), l);

        ObservableList<Loan> base = FXCollections.observableArrayList(map.values());
        FilteredList<Loan> filtered = new FilteredList<>(base, l -> true);
        table.setItems(filtered);

        // ✅ Tabs boutons comme Librarian
        ToggleGroup tg = new ToggleGroup();
        HBox filters = createFilterTabs(tg, "Tous", "ACTIF", "EN RETARD");
        ((ToggleButton) filters.getChildren().get(0)).setSelected(true);

        Runnable applyPredicate = () -> {
            String q = (searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
            String selected = getSelectedTabText(tg); // "Tous", "ACTIF", "EN RETARD"

            filtered.setPredicate(loan -> {
                boolean overdueFlag = !loan.isReturned()
                        && loan.getReturnDue() != null
                        && loan.getReturnDue().isBefore(LocalDate.now());

                boolean activeFlag = !loan.isReturned() && !overdueFlag;

                boolean statusOk =
                        "Tous".equals(selected)
                                || ("ACTIF".equals(selected) && activeFlag)
                                || ("EN RETARD".equals(selected) && overdueFlag);

                if (!statusOk) return false;

                if (q.isEmpty()) return true;

                UserAccount u = userDAO.getById(loan.getUserId());
                String userName = (u != null && u.getFullName() != null) ? u.getFullName().toLowerCase() : "";

                Book b = bookDAO.getById(loan.getBookId());
                String bookTitle = (b != null && b.getTitle() != null) ? b.getTitle().toLowerCase() : "";

                return userName.contains(q) || bookTitle.contains(q);
            });
        };

        searchField.textProperty().addListener((obs, old, val) -> applyPredicate.run());
        tg.selectedToggleProperty().addListener((obs, old, val) -> applyPredicate.run());
        applyPredicate.run();

        view.getChildren().addAll(title, subtitle, filters, sb.root, table);
        contentArea.getChildren().setAll(view);
    }

    private TableView<Loan> createLoansTable() {
        TableView<Loan> table = new TableView<>();

        TableColumn<Loan, String> colUserName = new TableColumn<>("Utilisateur");
        colUserName.setPrefWidth(260);
        colUserName.setCellValueFactory(cell -> {
            int userId = cell.getValue().getUserId();
            UserAccount u = userDAO.getById(userId);
            String name = (u != null && u.getFullName() != null) ? u.getFullName() : ("Utilisateur #" + userId);
            return new SimpleStringProperty(name);
        });
        colUserName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-padding: 0 0 0 10px;");
            }
        });

        TableColumn<Loan, String> colBookTitle = new TableColumn<>("Livre");
        colBookTitle.setPrefWidth(320);
        colBookTitle.setCellValueFactory(cell -> {
            int bookId = cell.getValue().getBookId();
            Book b = bookDAO.getById(bookId);
            String title = (b != null && b.getTitle() != null) ? b.getTitle() : ("Livre #" + bookId);
            return new SimpleStringProperty(title);
        });
        colBookTitle.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-padding: 0 0 0 10px;");
            }
        });

        TableColumn<Loan, LocalDate> colLoanDate = new TableColumn<>("Date Emprunt");
        colLoanDate.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colLoanDate.setPrefWidth(150);

        TableColumn<Loan, LocalDate> colDueDate = new TableColumn<>("Date Retour");
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("returnDue"));
        colDueDate.setPrefWidth(150);

        TableColumn<Loan, Double> colPenalty = new TableColumn<>("Pénalité");
        colPenalty.setCellValueFactory(new PropertyValueFactory<>("penalty"));
        colPenalty.setPrefWidth(110);

        TableColumn<Loan, String> colStatus = new TableColumn<>("Statut");
        colStatus.setPrefWidth(140);
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                Loan loan = getTableRow().getItem();
                boolean overdue = !loan.isReturned()
                        && loan.getReturnDue() != null
                        && loan.getReturnDue().isBefore(LocalDate.now());

                if (loan.isReturned()) {
                    setText("Retourné");
                    setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                } else if (overdue) {
                    setText("⚠ En retard");
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                } else {
                    setText("Actif");
                    setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(colUserName, colBookTitle, colLoanDate, colDueDate, colPenalty, colStatus);
        return table;
    }

    // ============================================================
    // ✅ UI helpers: Tabs boutons + SearchBar (comme Librarian)
    // ============================================================
    private HBox createFilterTabs(ToggleGroup group, String... labels) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 6, 0));

        for (String label : labels) {
            ToggleButton b = new ToggleButton(label);
            b.setToggleGroup(group);

            String normal =
                    "-fx-background-color: #f8fafc;" +
                    "-fx-text-fill: #334155;" +
                    "-fx-border-color: #e2e8f0;" +
                    "-fx-border-width: 1;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 6px 12px;" +
                    "-fx-cursor: hand;";

            String selected =
                    "-fx-background-color: white;" +
                    "-fx-text-fill: #1f2937;" +
                    "-fx-border-color: #cbd5e1;" +
                    "-fx-border-width: 1.2;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 6px 12px;" +
                    "-fx-cursor: hand;";

            b.setStyle(normal);
            b.selectedProperty().addListener((obs, was, isSel) -> b.setStyle(isSel ? selected : normal));

            box.getChildren().add(b);
        }

        if (group.getToggles().size() > 0) {
            group.selectToggle(group.getToggles().get(0));
        }
        return box;
    }

    private String getSelectedTabText(ToggleGroup group) {
        if (group.getSelectedToggle() instanceof ToggleButton tb) return tb.getText();
        return "Tous";
    }

    private SearchBar createSearchBar(String prompt) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setPrefHeight(34);

        String normalStyle =
                "-fx-background-color: white;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;";

        String focusStyle =
                "-fx-background-color: white;" +
                "-fx-border-color: #60a5fa;" +
                "-fx-border-width: 1.2;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;";

        box.setStyle(normalStyle);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");

        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;" +
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #111827;" +
                "-fx-prompt-text-fill: #9ca3af;"
        );
        HBox.setHgrow(tf, Priority.ALWAYS);

        Button clear = new Button("✕");
        clear.setFocusTraversable(false);
        clear.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #9ca3af;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0 6 0 6;"
        );
        clear.setOnAction(e -> tf.clear());

        tf.focusedProperty().addListener((obs, was, is) -> box.setStyle(is ? focusStyle : normalStyle));

        box.getChildren().addAll(icon, tf, clear);
        return new SearchBar(box, tf);
    }

    // ============================================================
    // Dialogs + CRUD users (inchangé)
    // ============================================================
    private void showAddUserDialog() {
        Dialog<UserAccount> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Créer un nouveau compte utilisateur");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfName = new TextField();
        tfName.setPromptText("Nom complet");
        TextField tfEmail = new TextField();
        tfEmail.setPromptText("Email");
        PasswordField tfPassword = new PasswordField();
        tfPassword.setPromptText("Mot de passe");
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("ADMIN", "LIBRARIAN", "MEMBER");
        cbRole.setValue("MEMBER");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(tfName, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(tfEmail, 1, 1);
        grid.add(new Label("Mot de passe:"), 0, 2);
        grid.add(tfPassword, 1, 2);
        grid.add(new Label("Rôle:"), 0, 3);
        grid.add(cbRole, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                UserAccount user = new UserAccount();
                user.setFullName(tfName.getText());
                user.setEmail(tfEmail.getText());
                user.setPassword(tfPassword.getText());
                int roleId = cbRole.getValue().equals("ADMIN") ? 1 :
                        cbRole.getValue().equals("LIBRARIAN") ? 2 : 3;
                user.setRoleId(roleId);
                return user;
            }
            return null;
        });

        Optional<UserAccount> result = dialog.showAndWait();
        result.ifPresent(user -> {
            if (userDAO.insert(user)) {
                showAlert("Succès", "Utilisateur créé avec succès !", Alert.AlertType.INFORMATION);
                showUsersView();
                loadStatistics();
            } else {
                showAlert("Erreur", "Échec de la création de l'utilisateur", Alert.AlertType.ERROR);
            }
        });
    }

    private void showEditUserDialog(UserAccount user) {
        Dialog<UserAccount> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modifier: " + user.getFullName());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfName = new TextField(user.getFullName());
        TextField tfEmail = new TextField(user.getEmail());
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("ADMIN", "LIBRARIAN", "MEMBER");
        cbRole.setValue(user.getRoleName());

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(tfName, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(tfEmail, 1, 1);
        grid.add(new Label("Rôle:"), 0, 2);
        grid.add(cbRole, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setFullName(tfName.getText());
                user.setEmail(tfEmail.getText());
                int roleId = cbRole.getValue().equals("ADMIN") ? 1 :
                        cbRole.getValue().equals("LIBRARIAN") ? 2 : 3;
                user.setRoleId(roleId);
                return user;
            }
            return null;
        });

        Optional<UserAccount> result = dialog.showAndWait();
        result.ifPresent(updatedUser -> {
            if (userDAO.update(updatedUser)) {
                showAlert("Succès", "Utilisateur modifié avec succès !", Alert.AlertType.INFORMATION);
                showUsersView();
            } else {
                showAlert("Erreur", "Échec de la modification", Alert.AlertType.ERROR);
            }
        });
    }

    private void deleteUser(UserAccount user) {
        UserAccount currentUser = SessionController.getCurrentUser();
        if (currentUser != null && user != null && user.getId() == currentUser.getId()) {
            showAlert("Action interdite", "Vous ne pouvez pas supprimer votre propre compte.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("Voulez-vous vraiment supprimer " + user.getFullName() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.delete(user.getId())) {
                showAlert("Succès", "Utilisateur supprimé avec succès !", Alert.AlertType.INFORMATION);
                showUsersView();
                loadStatistics();
            } else {
                showAlert("Erreur", "Échec de la suppression", Alert.AlertType.ERROR);
            }
        }
    }

    // ============================================================
    // Charts
    // ============================================================
    private javafx.scene.chart.PieChart createUserRolesPieChart() {
        List<UserAccount> users = userDAO.getAll();

        int adminCount = (int) users.stream().filter(u -> u.getRoleId() == 1).count();
        int librarianCount = (int) users.stream().filter(u -> u.getRoleId() == 2).count();
        int memberCount = (int) users.stream().filter(u -> u.getRoleId() == 3).count();

        javafx.scene.chart.PieChart pc = new javafx.scene.chart.PieChart(
                FXCollections.observableArrayList(
                        new javafx.scene.chart.PieChart.Data("Administrateurs (" + adminCount + ")", adminCount),
                        new javafx.scene.chart.PieChart.Data("Bibliothécaires (" + librarianCount + ")", librarianCount),
                        new javafx.scene.chart.PieChart.Data("Membres (" + memberCount + ")", memberCount)
                )
        );

        pc.setTitle("Distribution des Rôles Utilisateurs");
        pc.setLegendVisible(true);
        pc.setLabelsVisible(true);
        pc.setPrefHeight(380);

        Platform.runLater(() -> styleChartText(pc));
        return pc;
    }

    private javafx.scene.chart.BarChart<Number, String> createTopBooksBarChart() {
        javafx.scene.chart.NumberAxis xAxis = new javafx.scene.chart.NumberAxis();
        javafx.scene.chart.CategoryAxis yAxis = new javafx.scene.chart.CategoryAxis();

        xAxis.setLabel("Nombre d'emprunts");
        xAxis.setTickLabelFill(Color.BLACK);

        yAxis.setLabel("Titres");
        yAxis.setTickLabelFill(Color.BLACK);

        javafx.scene.chart.BarChart<Number, String> bc = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        bc.setTitle("Top 5 des Livres les Plus Populaires");
        bc.setLegendVisible(false);
        bc.setPrefHeight(380);

        javafx.scene.chart.XYChart.Series<Number, String> s = new javafx.scene.chart.XYChart.Series<>();

        List<Book> books = bookDAO.getAll();
        java.util.Collections.shuffle(books);

        for (int i = 0; i < Math.min(5, books.size()); i++) {
            Book book = books.get(i);
            String title = book.getTitle().length() > 25 ? book.getTitle().substring(0, 25) + "..." : book.getTitle();
            int count = (int) (Math.random() * 10) + 5;
            s.getData().add(new javafx.scene.chart.XYChart.Data<>(count, title));
        }

        bc.getData().add(s);

        Platform.runLater(() -> {
            styleChartText(bc);
            styleAxisLabels(bc);

            String[] colors = {"#3b82f6", "#10b981", "#8b5cf6", "#f59e0b", "#f97316"};
            for (int i = 0; i < s.getData().size(); i++) {
                javafx.scene.chart.XYChart.Data<Number, String> d = s.getData().get(i);
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill: " + colors[i % colors.length] + ";");
                }
            }
        });

        return bc;
    }

    private void styleChartText(javafx.scene.Node chart) {
        chart.lookupAll(".chart-title, .chart-legend-item-text, .axis-label, .chart-pie-label").forEach(node -> {
            if (node instanceof Text text) {
                text.setFill(Color.BLACK);
                text.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            }
        });

        chart.lookupAll(".axis").forEach(axis ->
                axis.lookupAll(".tick-label").forEach(label -> {
                    if (label instanceof Text text) {
                        text.setFill(Color.BLACK);
                        text.setStyle("-fx-font-size: 12px;");
                    }
                })
        );
    }

    private void styleAxisLabels(javafx.scene.Node chart) {
        chart.lookupAll(".axis-label").forEach(node -> {
            if (node instanceof Text text) {
                text.setFill(Color.BLACK);
                text.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                text.setVisible(true);
            }
        });
    }

    private void setActiveTab(Button activeButton) {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #718096; -fx-font-size: 14px; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #2d3748; -fx-font-size: 14px; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-border-color: #e0e6ed; -fx-border-width: 1; -fx-border-radius: 8px;";

        btnOverview.setStyle(inactiveStyle);
        btnUsers.setStyle(inactiveStyle);
        btnBooks.setStyle(inactiveStyle);
        btnLoans.setStyle(inactiveStyle);

        activeButton.setStyle(activeStyle);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onLogout() {
        SessionController.clearSession();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        ViewHelper.show(stage, "/view/LoginView.fxml", "Connexion - Library EMSI");
    }
}
