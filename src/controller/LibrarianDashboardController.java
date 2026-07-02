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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import java.util.Optional;

public class LibrarianDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label totalBooksLabel;
    @FXML private Label pendingLoansLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label overdueLoansLabel;
    @FXML private Button btnOverview;
    @FXML private Button btnBooks;
    @FXML private Button btnValidateLoans;
    @FXML private Button btnReturns;
    @FXML private StackPane contentArea;
    @FXML private VBox overviewPane;

    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final UserDAO userDAO = new UserDAO();

    // ✅ helper SearchBar (barre avec icône + clear)
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
            welcomeLabel.setText("Bienvenue, " + currentUser.getFullName());
        } else {
            welcomeLabel.setText("Bienvenue, Bibliothécaire");
        }

        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        loadStatistics();
        showOverview();
    }

    private void loadStatistics() {
        try {
            totalBooksLabel.setText(String.valueOf(bookDAO.countAll()));
            pendingLoansLabel.setText(String.valueOf(loanDAO.countPendingLoans()));
            activeLoansLabel.setText(String.valueOf(loanDAO.countActiveLoans()));
            overdueLoansLabel.setText(String.valueOf(loanDAO.countOverdueLoans()));
        } catch (Exception e) {
            e.printStackTrace();
            totalBooksLabel.setText("0");
            pendingLoansLabel.setText("0");
            activeLoansLabel.setText("0");
            overdueLoansLabel.setText("0");
        }
    }

    @FXML private void onShowOverview() { setActiveTab(btnOverview); showOverview(); }
    @FXML private void onShowBooks() { setActiveTab(btnBooks); loadBookCrudView(); }
    @FXML private void onShowValidateLoans() { setActiveTab(btnValidateLoans); showValidateLoansView(); }
    @FXML private void onShowReturns() { setActiveTab(btnReturns); showReturnsView(); }

    @FXML
    private void onLogout() {
        SessionController.clearSession();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        ViewHelper.show(stage, "/view/LoginView.fxml", "Connexion - Library EMSI");
    }

    private void setActiveTab(Button activeButton) {
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #718096; -fx-font-size: 14px; -fx-padding: 12px 24px; -fx-background-radius: 8px;";
        String active = "-fx-background-color: white; -fx-text-fill: #2d3748; -fx-font-size: 14px; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-border-color: #e0e6ed; -fx-border-width: 1; -fx-border-radius: 8px;";

        btnOverview.setStyle(inactive);
        btnBooks.setStyle(inactive);
        btnValidateLoans.setStyle(inactive);
        btnReturns.setStyle(inactive);
        activeButton.setStyle(active);
    }

    // =======================
    // ✅ OVERVIEW
    // =======================
    private void showOverview() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent;");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("Statistiques de la Bibliothèque");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        VBox cardLeft = createCardSection(
                "Répartition des Emprunts par Statut",
                "Distribution actuelle des emprunts : En attente, Actifs, En retard, Retournés.",
                createLoanStatusPieChart()
        );

        VBox cardRight = createCardSection(
                "Évolution des Emprunts (6 derniers mois)",
                "Nombre total d'emprunts effectués chaque mois sur les 6 derniers mois.",
                createMonthlyLoansBarChart()
        );

        HBox chartsRow = new HBox(20, cardLeft, cardRight);
        chartsRow.setAlignment(Pos.TOP_CENTER);

        HBox.setHgrow(cardLeft, Priority.ALWAYS);
        HBox.setHgrow(cardRight, Priority.ALWAYS);
        cardLeft.setMaxWidth(Double.MAX_VALUE);
        cardRight.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(title, chartsRow);

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

    // =======================
    // BOOK CRUD (inchangé)
    // =======================
    private void loadBookCrudView() {
        try {
            String[] paths = {"view/BookCrud.fxml", "/view/BookCrud.fxml", "../view/BookCrud.fxml"};
            FXMLLoader loader = null;

            for (String path : paths) {
                java.net.URL url = path.startsWith("/") ? getClass().getResource(path) : getClass().getClassLoader().getResource(path);
                if (url != null) {
                    loader = new FXMLLoader(url);
                    break;
                }
            }

            if (loader == null) throw new Exception("BookCrud.fxml introuvable");

            Parent content = loader.load();

            VBox wrapper = new VBox(20);
            wrapper.setPadding(new Insets(30));
            wrapper.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

            Label titleLabel = new Label("Gestion des Livres");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

            wrapper.getChildren().addAll(titleLabel, content);
            contentArea.getChildren().setAll(wrapper);

        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Erreur: Impossible de charger la gestion des livres.");
            err.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
            VBox box = new VBox(err);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-padding: 50px;");
            contentArea.getChildren().setAll(box);
        }
    }

    // =======================
    // ✅ VALIDATE LOANS (avec filtres tabs)
    // =======================
    private void showValidateLoansView() {
        VBox c = new VBox(14);
        c.setPadding(new Insets(30));
        c.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

        Label t = new Label("Validation des Emprunts");
        t.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label subtitle = new Label("Approuvez ou rejetez les demandes d'emprunt (filtres rapides par statut)");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-padding: 0 0 5 0;");

        // ✅ SearchBar style clean
        SearchBar sb = createSearchBar("Rechercher par nom du membre...");
        TextField search = sb.field;

        TableView<Loan> tb = new TableView<>();
        tb.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ✅ Colonne Membre
        TableColumn<Loan, String> memberCol = new TableColumn<>("Membre");
        memberCol.setPrefWidth(220);
        memberCol.setCellValueFactory(cellData -> {
            UserAccount u = userDAO.getById(cellData.getValue().getUserId());
            String name = (u != null) ? u.getFullName() : ("Membre #" + cellData.getValue().getUserId());
            return new SimpleStringProperty(name);
        });
        memberCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                setText(name);
                setGraphic(null);
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-padding: 0 0 0 10px;");
            }
        });

        TableColumn<Loan, String> bookTitleCol = new TableColumn<>("Titre du Livre");
        bookTitleCol.setPrefWidth(260);
        bookTitleCol.setCellValueFactory(cellData -> {
            Book book = bookDAO.getById(cellData.getValue().getBookId());
            String title = (book != null) ? book.getTitle() : "Livre #" + cellData.getValue().getBookId();
            return new SimpleStringProperty(title);
        });
        bookTitleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String title, boolean empty) {
                super.updateItem(title, empty);
                if (empty || title == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(title);
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-padding: 0 0 0 10px;");
            }
        });

        TableColumn<Loan, LocalDate> dateCol = new TableColumn<>("Date Demande");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        dateCol.setPrefWidth(140);

        // ✅ Statut calculé (EN ATTENTE / APPROUVÉ / REJETÉ / RETOURNÉ)
        TableColumn<Loan, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(140);
        statusCol.setCellValueFactory(cd -> new SimpleStringProperty(computeValidationStatus(cd.getValue())));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                switch (status) {
                    case "EN ATTENTE" -> setStyle("-fx-background-color: #FFF3CD; -fx-text-fill: #856404; -fx-font-weight: bold;");
                    case "APPROUVÉ"  -> setStyle("-fx-background-color: #D4EDDA; -fx-text-fill: #155724; -fx-font-weight: bold;");
                    case "REJETÉ"    -> setStyle("-fx-background-color: #F8D7DA; -fx-text-fill: #721C24; -fx-font-weight: bold;");
                    case "RETOURNÉ"  -> setStyle("-fx-background-color: #D1ECF1; -fx-text-fill: #0C5460; -fx-font-weight: bold;");
                    default -> setStyle("-fx-text-fill: #2d3748;");
                }
            }
        });

        TableColumn<Loan, Void> actCol = new TableColumn<>("Actions");
        actCol.setMinWidth(260);
        actCol.setCellFactory(p -> new TableCell<>() {
            private final Button appBtn = new Button("✓ Approuver");
            private final Button rejBtn = new Button("✗ Rejeter");
            private final HBox btns = new HBox(10, appBtn, rejBtn);
            private final Label lbl = new Label();

            {
                appBtn.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand;");
                rejBtn.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand;");
                btns.setAlignment(Pos.CENTER);

                appBtn.setOnAction(e -> approveLoan(getTableView().getItems().get(getIndex())));
                rejBtn.setOnAction(e -> rejectLoan(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Loan loan = getTableRow().getItem();
                String status = computeValidationStatus(loan);

                // si déjà approuvé/rejeté/retourné => label
                if ("APPROUVÉ".equals(status)) {
                    lbl.setText("✅ Approuvé");
                    lbl.setStyle("-fx-text-fill: #155724; -fx-font-weight: bold;");
                    setGraphic(lbl);
                } else if ("REJETÉ".equals(status)) {
                    lbl.setText("❌ Rejeté");
                    lbl.setStyle("-fx-text-fill: #721C24; -fx-font-weight: bold;");
                    setGraphic(lbl);
                } else if ("RETOURNÉ".equals(status)) {
                    lbl.setText("↩ Retourné");
                    lbl.setStyle("-fx-text-fill: #0C5460; -fx-font-weight: bold;");
                    setGraphic(lbl);
                } else {
                    setGraphic(btns); // EN ATTENTE
                }
            }
        });

        tb.getColumns().setAll(memberCol, bookTitleCol, dateCol, statusCol, actCol);

        ObservableList<Loan> base = FXCollections.observableArrayList(loanDAO.getAllLoansForValidation());
        FilteredList<Loan> filtered = new FilteredList<>(base, l -> true);
        tb.setItems(filtered);

        // ✅ Filtres “tabs” comme admin
        ToggleGroup tg = new ToggleGroup();
        HBox filters = createFilterTabs(tg,
                "Tous", "EN ATTENTE", "APPROUVÉ", "REJETÉ", "RETOURNÉ"
        );

        // valeur par défaut: EN ATTENTE
        ToggleButton defaultBtn = (ToggleButton) filters.getChildren().get(1);
        defaultBtn.setSelected(true);

        Runnable applyPredicate = () -> {
            String q = (search.getText() == null) ? "" : search.getText().trim().toLowerCase();
            String selected = getSelectedTabText(tg); // ex: "EN ATTENTE"

            filtered.setPredicate(loan -> {
                // filtre statut
                String status = computeValidationStatus(loan);
                boolean statusOk = "Tous".equals(selected) || status.equals(selected);

                if (!statusOk) return false;

                // filtre search (nom membre)
                if (q.isEmpty()) return true;
                UserAccount u = userDAO.getById(loan.getUserId());
                String name = (u != null && u.getFullName() != null) ? u.getFullName().toLowerCase() : "";
                return name.contains(q);
            });
        };

        // listeners
        search.textProperty().addListener((obs, old, val) -> applyPredicate.run());
        tg.selectedToggleProperty().addListener((obs, old, val) -> applyPredicate.run());
        applyPredicate.run();

        c.getChildren().addAll(t, subtitle, filters, sb.root, tb);
        contentArea.getChildren().setAll(c);
    }

    // =======================
    // ✅ RETURNS VIEW (avec tabs Tous / Actifs / En retard)
    // =======================
    private void showReturnsView() {
        VBox c = new VBox(14);
        c.setPadding(new Insets(30));
        c.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

        Label t = new Label("Gestion des Retours");
        t.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label subtitle = new Label("Confirmez les retours (filtre rapide : Actifs / En retard)");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-padding: 0 0 5 0;");

        SearchBar sb = createSearchBar("Rechercher par nom du membre...");
        TextField search = sb.field;

        TableView<Loan> tb = new TableView<>();
        tb.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Loan, String> memberCol = new TableColumn<>("Membre");
        memberCol.setPrefWidth(220);
        memberCol.setCellValueFactory(cellData -> {
            UserAccount u = userDAO.getById(cellData.getValue().getUserId());
            String name = (u != null) ? u.getFullName() : ("Membre #" + cellData.getValue().getUserId());
            return new SimpleStringProperty(name);
        });
        memberCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                setText(name);
                setGraphic(null);
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-padding: 0 0 0 10px;");
            }
        });

        TableColumn<Loan, String> bookTitleCol = new TableColumn<>("Titre du Livre");
        bookTitleCol.setPrefWidth(260);
        bookTitleCol.setCellValueFactory(cellData -> {
            Book book = bookDAO.getById(cellData.getValue().getBookId());
            String title = (book != null) ? book.getTitle() : "Livre #" + cellData.getValue().getBookId();
            return new SimpleStringProperty(title);
        });
        bookTitleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String title, boolean empty) {
                super.updateItem(title, empty);
                if (empty || title == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(title);
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-padding: 0 0 0 10px;");
            }
        });

        TableColumn<Loan, LocalDate> loanCol = new TableColumn<>("Date Emprunt");
        loanCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        loanCol.setPrefWidth(140);

        TableColumn<Loan, LocalDate> dueCol = new TableColumn<>("Date Retour");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("returnDue"));
        dueCol.setPrefWidth(140);

        TableColumn<Loan, Double> penCol = new TableColumn<>("Pénalité (MAD)");
        penCol.setCellValueFactory(new PropertyValueFactory<>("penalty"));
        penCol.setPrefWidth(140);

        TableColumn<Loan, String> statCol = new TableColumn<>("Statut");
        statCol.setPrefWidth(140);
        statCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().isOverdue() ? "EN RETARD" : "ACTIF"));
        statCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                if ("EN RETARD".equals(item)) {
                    setText("⚠ En retard");
                    setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                } else {
                    setText("Actif");
                    setStyle("-fx-text-fill: #4299e1; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Loan, Void> actCol = new TableColumn<>("Actions");
        actCol.setMinWidth(220);
        actCol.setCellFactory(p -> new TableCell<>() {
            private final Button btnReturn = new Button("↩ Confirmer Retour");

            {
                btnReturn.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnReturn.setOnAction(e -> confirmReturn(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Loan loan = getTableRow().getItem();

                // ✅ comme ton code: si en retard => pas de bouton
                if (loan.isOverdue()) {
                    setGraphic(null);
                } else {
                    setGraphic(btnReturn);
                }
            }
        });

        tb.getColumns().setAll(memberCol, bookTitleCol, loanCol, dueCol, penCol, statCol, actCol);

        ObservableList<Loan> base = FXCollections.observableArrayList(loanDAO.getActiveLoans());
        FilteredList<Loan> filtered = new FilteredList<>(base, l -> true);
        tb.setItems(filtered);

        // ✅ Tabs Tous / Actifs / En retard (comme admin)
        ToggleGroup tg = new ToggleGroup();
        HBox filters = createFilterTabs(tg, "Tous", "ACTIF", "EN RETARD");

        // défaut: Tous
        ((ToggleButton) filters.getChildren().get(0)).setSelected(true);

        Runnable applyPredicate = () -> {
            String q = (search.getText() == null) ? "" : search.getText().trim().toLowerCase();
            String selected = getSelectedTabText(tg); // "ACTIF", "EN RETARD", "Tous"

            filtered.setPredicate(loan -> {
                boolean overdue = loan.isOverdue();
                boolean statusOk =
                        "Tous".equals(selected)
                        || ("ACTIF".equals(selected) && !overdue)
                        || ("EN RETARD".equals(selected) && overdue);

                if (!statusOk) return false;

                if (q.isEmpty()) return true;
                UserAccount u = userDAO.getById(loan.getUserId());
                String name = (u != null && u.getFullName() != null) ? u.getFullName().toLowerCase() : "";
                return name.contains(q);
            });
        };

        search.textProperty().addListener((obs, old, val) -> applyPredicate.run());
        tg.selectedToggleProperty().addListener((obs, old, val) -> applyPredicate.run());
        applyPredicate.run();

        c.getChildren().addAll(t, subtitle, filters, sb.root, tb);
        contentArea.getChildren().setAll(c);
    }

    // =======================
    // ✅ Confirmation paiement pénalité + retour (inchangé)
    // =======================
    private void confirmReturn(Loan loan) {
        try {
            if (loan.isOverdue()) {
                return;
            }

            if (loan.getPenalty() > 0) {
                Alert pay = new Alert(Alert.AlertType.CONFIRMATION);
                pay.setTitle("Paiement pénalité");
                pay.setHeaderText("Confirmer le paiement ?");
                pay.setContentText("Montant : " + loan.getPenalty() + " MAD");

                Optional<ButtonType> res = pay.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) {
                    return;
                }
            }

            loan.setReturned(true);
            loan.setReturnedDate(LocalDate.now());

            boolean loanUpdated = loanDAO.update(loan);

            Book book = bookDAO.getById(loan.getBookId());
            if (book != null) {
                book.setQuantity(book.getQuantity() + 1);
                bookDAO.update(book);
            }

            if (loanUpdated) {
                showReturnsView();
                loadStatistics();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =======================
    // APPROVE / REJECT (inchangé)
    // =======================
    private void approveLoan(Loan loan) {
        try {
            UserAccount currentLibrarian = SessionController.getCurrentUser();
            if (currentLibrarian == null) {
                showAlert("Erreur", "Session expirée", Alert.AlertType.ERROR);
                return;
            }

            Book book = bookDAO.getById(loan.getBookId());
            if (book == null) {
                showAlert("Erreur", "Livre introuvable", Alert.AlertType.ERROR);
                return;
            }

            if (book.getQuantity() <= 0) {
                showAlert("Stock insuffisant",
                        "Ce livre n'est plus disponible.\nQuantité actuelle : " + book.getQuantity(),
                        Alert.AlertType.WARNING);
                return;
            }

            loan.setValidatedBy(currentLibrarian.getId());
            book.setQuantity(book.getQuantity() - 1);

            boolean loanUpdated = loanDAO.update(loan);
            boolean bookUpdated = bookDAO.update(book);

            if (loanUpdated && bookUpdated) {
                showValidateLoansView();
                loadStatistics();
            } else {
                showAlert("Erreur", "Échec de l'approbation", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void rejectLoan(Loan loan) {
        try {
            UserAccount currentLibrarian = SessionController.getCurrentUser();
            if (currentLibrarian == null) {
                showAlert("Erreur", "Session expirée", Alert.AlertType.ERROR);
                return;
            }

            loan.setValidatedBy(-1);

            if (loanDAO.update(loan)) {
                showValidateLoansView();
                loadStatistics();
            } else {
                showAlert("Erreur", "Échec du rejet", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =======================
    // ✅ STATUS HELPERS
    // =======================
    private String computeValidationStatus(Loan loan) {
        // ✅ On se base sur ce que ton code utilise déjà :
        // - validatedBy > 0 => APPROUVÉ
        // - validatedBy == -1 => REJETÉ
        // - sinon => EN ATTENTE
        // + si retourné => RETOURNÉ (si ton model a isReturned())
        try {
            if (loan.isReturned()) return "RETOURNÉ";
        } catch (Exception ignored) { }

        if (loan.getValidatedBy() > 0) return "APPROUVÉ";
        if (loan.getValidatedBy() == -1) return "REJETÉ";
        return "EN ATTENTE";
    }

    // =======================
    // ✅ UI: Tabs / SearchBar
    // =======================
    private HBox createFilterTabs(ToggleGroup group, String... labels) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 6, 0));

        for (String label : labels) {
            ToggleButton b = new ToggleButton(label);
            b.setToggleGroup(group);

            // style "tab"
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

        // si rien sélectionné, force le premier
        if (group.getToggles().size() > 0) {
            group.selectToggle(group.getToggles().get(0));
        }

        return box;
    }

    private String getSelectedTabText(ToggleGroup group) {
        if (group.getSelectedToggle() instanceof ToggleButton tb) {
            return tb.getText();
        }
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

    // =======================
    // CHARTS (inchangé)
    // =======================
    private javafx.scene.chart.PieChart createLoanStatusPieChart() {
        int p = loanDAO.countPendingLoans();
        int a = loanDAO.countActiveLoans();
        int o = loanDAO.countOverdueLoans();
        int r = loanDAO.countAll() - p - a - o;

        javafx.scene.chart.PieChart pc = new javafx.scene.chart.PieChart(
                FXCollections.observableArrayList(
                        new javafx.scene.chart.PieChart.Data("En attente (" + p + ")", p),
                        new javafx.scene.chart.PieChart.Data("Actifs (" + a + ")", a),
                        new javafx.scene.chart.PieChart.Data("En retard (" + o + ")", o),
                        new javafx.scene.chart.PieChart.Data("Retournés (" + r + ")", r)
                )
        );

        pc.setTitle("Statut Actuel des Emprunts");
        pc.setLegendVisible(true);
        pc.setLabelsVisible(true);
        pc.setPrefHeight(360);

        Platform.runLater(() -> styleChartText(pc));
        return pc;
    }

    private javafx.scene.chart.BarChart<String, Number> createMonthlyLoansBarChart() {
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();

        xAxis.setLabel("Mois");
        xAxis.setTickLabelFill(Color.BLACK);

        yAxis.setLabel("Emprunts");
        yAxis.setTickLabelFill(Color.BLACK);
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(true);

        javafx.scene.chart.BarChart<String, Number> bc = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        bc.setTitle("Emprunts par Mois");
        bc.setLegendVisible(false);
        bc.setPrefHeight(360);

        javafx.scene.chart.XYChart.Series<String, Number> s = new javafx.scene.chart.XYChart.Series<>();
        LocalDate now = LocalDate.now();
        String[] mois = {"JAN", "FEV", "MAR", "AVR", "MAI", "JUN", "JUL", "AOU", "SEP", "OCT", "NOV", "DEC"};

        for (int i = 5; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            String monthName = mois[m.getMonthValue() - 1] + " " + m.getYear();

            int count = (int) loanDAO.getAll().stream()
                    .filter(loan -> loan.getLoanDate().getYear() == m.getYear()
                            && loan.getLoanDate().getMonth() == m.getMonth())
                    .count();

            s.getData().add(new javafx.scene.chart.XYChart.Data<>(monthName, count));
        }

        bc.getData().add(s);

        Platform.runLater(() -> {
            styleChartText(bc);
            styleAxisLabels(bc);
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

        chart.lookupAll(".axis").forEach(axis -> axis.lookupAll(".tick-label").forEach(label -> {
            if (label instanceof Text text) {
                text.setFill(Color.BLACK);
                text.setStyle("-fx-font-size: 12px;");
            }
        }));
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

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
