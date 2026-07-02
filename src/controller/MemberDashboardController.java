package controller;

import dao.BookDAO;
import dao.LoanDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Book;
import model.Loan;
import model.UserAccount;
import utils.ViewHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MemberDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;

    @FXML private Label activeLoansLabel;
    @FXML private Label overdueLoansLabel;
    @FXML private Label totalLoansLabel;
    @FXML private Label availableSlotsLabel;

    @FXML private Button btnHome;
    @FXML private Button btnCatalogue;
    @FXML private Button btnMyLoans;
    @FXML private Button btnReturn;

    @FXML private StackPane contentArea;

    private final LoanDAO loanDAO = new LoanDAO();
    private final BookDAO bookDAO = new BookDAO();

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
            welcomeLabel.setText("Welcome back");
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateLabel.setText(today.format(formatter));

        loadStatistics();
        setActiveTab(btnHome);
        showHome();
    }

    // -------------------------
    // STATS CARDS TOP
    // -------------------------
    private void loadStatistics() {
        try {
            UserAccount currentUser = SessionController.getCurrentUser();
            if (currentUser == null) {
                activeLoansLabel.setText("0");
                overdueLoansLabel.setText("0");
                totalLoansLabel.setText("0");
                availableSlotsLabel.setText("5");
                return;
            }

            int userId = currentUser.getId();

            List<Loan> myLoans;
            try {
                myLoans = loanDAO.findByUserId(userId);
            } catch (Exception ex) {
                myLoans = loanDAO.getAll().stream().filter(l -> l.getUserId() == userId).toList();
            }

            int activeLoans = (int) myLoans.stream()
                    .filter(l -> !l.isReturned() && l.isValidated())
                    .count();

            int overdueLoans = (int) myLoans.stream()
                    .filter(l -> !l.isReturned() && l.isValidated() && l.getReturnDue().isBefore(LocalDate.now()))
                    .count();

            int totalLoans = myLoans.size();
            int availableSlots = Math.max(0, 5 - activeLoans);

            activeLoansLabel.setText(String.valueOf(activeLoans));
            overdueLoansLabel.setText(String.valueOf(overdueLoans));
            totalLoansLabel.setText(String.valueOf(totalLoans));
            availableSlotsLabel.setText(String.valueOf(availableSlots));

        } catch (Exception e) {
            e.printStackTrace();
            activeLoansLabel.setText("0");
            overdueLoansLabel.setText("0");
            totalLoansLabel.setText("0");
            availableSlotsLabel.setText("5");
        }
    }

    // -------------------------
    // NAV actions (FXML)
    // -------------------------
    @FXML private void onShowHome() { setActiveTab(btnHome); showHome(); }
    @FXML private void onOpenCatalogue() { setActiveTab(btnCatalogue); showCatalogueView(); }
    @FXML private void onMyLoans() { setActiveTab(btnMyLoans); showMyLoansView(); }
    @FXML private void onReturnBook() { setActiveTab(btnReturn); showReturnBookView(); }

    // -------------------------
    // HOME
    // -------------------------
    private void showHome() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

        Label title = new Label("Vue d'Ensemble");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        VBox charts = createChartsSection();

        root.getChildren().addAll(title, charts);
        sp.setContent(root);

        contentArea.getChildren().setAll(sp);
    }

    private VBox createChartsSection() {
        VBox wrapper = new VBox(20);

        VBox pieCard = createCardSection(
                "Répartition de mes Emprunts par Statut",
                "Actifs, en retard, retournés, en attente de validation.",
                createMyLoansPieChartStyled()
        );

        VBox genreCard = createCardSection(
                "Mes Genres Préférés (Top 3)",
                "Les genres que vous empruntez le plus.",
                createTopGenresBarChartStyled()
        );

        HBox row = new HBox(20, pieCard, genreCard);
        row.setAlignment(Pos.TOP_CENTER);

        HBox.setHgrow(pieCard, Priority.ALWAYS);
        HBox.setHgrow(genreCard, Priority.ALWAYS);
        pieCard.setMaxWidth(Double.MAX_VALUE);
        genreCard.setMaxWidth(Double.MAX_VALUE);

        wrapper.getChildren().add(row);
        return wrapper;
    }

    private VBox createCardSection(String title, String description, Node chart) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14px;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 14px;" +
                        "-fx-border-width: 1;"
        );

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label d = new Label(description);
        d.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        d.setWrapText(true);

        if (chart instanceof Region r) {
            r.setMinHeight(360);
            r.setPrefHeight(380);
            r.setMaxWidth(Double.MAX_VALUE);
        }

        card.getChildren().addAll(t, d, chart);
        return card;
    }

    private PieChart createMyLoansPieChartStyled() {
        PieChart pc = new PieChart();
        UserAccount currentUser = SessionController.getCurrentUser();
        if (currentUser == null) return pc;

        List<Loan> myLoans;
        try {
            myLoans = loanDAO.findByUserId(currentUser.getId());
        } catch (Exception ex) {
            myLoans = loanDAO.getAll().stream().filter(l -> l.getUserId() == currentUser.getId()).toList();
        }

        int active = (int) myLoans.stream().filter(l -> !l.isReturned() && l.isValidated()).count();
        int overdue = (int) myLoans.stream().filter(l -> !l.isReturned() && l.isValidated() && l.getReturnDue().isBefore(LocalDate.now())).count();
        int returned = (int) myLoans.stream().filter(Loan::isReturned).count();
        int pending = (int) myLoans.stream().filter(l -> !l.isValidated() && l.getValidatedBy() == 0).count();

        pc.setData(FXCollections.observableArrayList(
                new PieChart.Data("Actifs (" + active + ")", active),
                new PieChart.Data("En retard (" + overdue + ")", overdue),
                new PieChart.Data("Retournés (" + returned + ")", returned),
                new PieChart.Data("En attente (" + pending + ")", pending)
        ));

        pc.setLegendVisible(true);
        pc.setLabelsVisible(true);
        pc.setPrefHeight(360);

        Platform.runLater(() -> {
            styleChartText(pc);

            String[] colors = {"#3b82f6", "#ef4444", "#10b981", "#f59e0b"};
            for (int i = 0; i < pc.getData().size(); i++) {
                PieChart.Data d = pc.getData().get(i);
                if (d.getNode() != null && i < colors.length) {
                    d.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
                }
            }
        });

        return pc;
    }

    private BarChart<Number, String> createTopGenresBarChartStyled() {

        NumberAxis xAxis = new NumberAxis(0, 4.0, 0.5);
        CategoryAxis yAxis = new CategoryAxis();

        xAxis.setLabel("Nombre d'emprunts");
        yAxis.setLabel("");

        xAxis.setTickLabelFill(Color.BLACK);
        yAxis.setTickLabelFill(Color.BLACK);

        xAxis.setMinorTickVisible(false);
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number object) {
                int v = (int) Math.round(object.doubleValue() * 10.0);
                return String.valueOf(v);
            }
            @Override
            public Number fromString(String string) {
                try {
                    return Double.parseDouble(string) / 10.0;
                } catch (Exception e) {
                    return 0;
                }
            }
        });

        BarChart<Number, String> bc = new BarChart<>(xAxis, yAxis);
        bc.setLegendVisible(false);
        bc.setPrefHeight(360);
        bc.setAnimated(false);

        UserAccount currentUser = SessionController.getCurrentUser();
        if (currentUser == null) return bc;

        List<Loan> myLoans;
        try {
            myLoans = loanDAO.findByUserId(currentUser.getId());
        } catch (Exception ex) {
            myLoans = loanDAO.getAll().stream().filter(l -> l.getUserId() == currentUser.getId()).toList();
        }

        Map<String, Long> counts = myLoans.stream().collect(Collectors.groupingBy(l -> {
            Book b = bookDAO.getById(l.getBookId());
            return (b != null && b.getGenre() != null && !b.getGenre().isBlank()) ? b.getGenre() : "Non défini";
        }, Collectors.counting()));

        List<Map.Entry<String, Long>> top = counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .toList();

        XYChart.Series<Number, String> s = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : top) {
            s.getData().add(new XYChart.Data<>(e.getValue().doubleValue(), e.getKey()));
        }

        bc.getData().add(s);

        Platform.runLater(() -> {
            styleChartText(bc);

            String[] colors = {"#3b82f6", "#10b981", "#8b5cf6"};
            for (int i = 0; i < s.getData().size(); i++) {
                XYChart.Data<Number, String> d = s.getData().get(i);
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill: " + colors[i % colors.length] + ";");
                }
            }
        });

        return bc;
    }

    // -------------------------
    // CATALOGUE
    // -------------------------
    private void showCatalogueView() {
        VBox wrapper = new VBox(15);
        wrapper.setPadding(new Insets(25));
        wrapper.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

        Label title = new Label("Catalogue des Livres");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label subtitle = new Label("Recherchez rapidement un livre par titre, auteur ou genre.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");

        SearchBar sb = createSearchBar("Rechercher...");
        TextField searchField = sb.field;

        TableView<Book> table = createCatalogueTable();

        ObservableList<Book> master = FXCollections.observableArrayList(bookDAO.getAll());
        FilteredList<Book> filtered = new FilteredList<>(master, b -> true);

        searchField.textProperty().addListener((obs, old, query) -> {
            String q = (query == null) ? "" : query.trim().toLowerCase();
            filtered.setPredicate(book -> {
                if (q.isEmpty()) return true;
                String titleTxt = (book.getTitle() == null ? "" : book.getTitle()).toLowerCase();
                String authorTxt = (book.getAuthor() == null ? "" : book.getAuthor()).toLowerCase();
                String genreTxt = (book.getGenre() == null ? "" : book.getGenre()).toLowerCase();
                return titleTxt.contains(q) || authorTxt.contains(q) || genreTxt.contains(q);
            });
        });

        table.setItems(filtered);

        wrapper.getChildren().addAll(title, subtitle, sb.root, table);
        contentArea.getChildren().setAll(wrapper);
    }

    // ✅ FIX: Quantité ne doit pas devenir "..." -> on force min/pref widths
    private TableView<Book> createCatalogueTable() {
        TableView<Book> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Book, String> colTitle = new TableColumn<>("Titre");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setMinWidth(260);

        TableColumn<Book, String> colAuthor = new TableColumn<>("Auteur");
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colAuthor.setMinWidth(220);

        TableColumn<Book, Integer> colQty = new TableColumn<>("Quantité");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(120);
        colQty.setMinWidth(120);
        colQty.setMaxWidth(140);

        TableColumn<Book, String> colStatus = new TableColumn<>("Disponibilité");
        colStatus.setPrefWidth(170);
        colStatus.setMinWidth(170);
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                Book b = getTableRow().getItem();
                if (b.getQuantity() > 0) {
                    setText("✅ Disponible");
                    setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                } else {
                    setText("❌ Non disponible");
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Book, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(180);
        colAction.setMinWidth(180);
        colAction.setMaxWidth(220);
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnBorrow = new Button("Emprunter");
            private final HBox box = new HBox(btnBorrow);

            {
                box.setAlignment(Pos.CENTER);

                btnBorrow.setStyle(
                        "-fx-background-color: #38bdf8;" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 8px 16px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-weight: bold;"
                );

                btnBorrow.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    borrowBook(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Book b = getTableRow().getItem();
                setGraphic(b.getQuantity() > 0 ? box : null);
            }
        });

        table.getColumns().addAll(colTitle, colAuthor, colQty, colStatus, colAction);
        return table;
    }

    private void borrowBook(Book book) {
        UserAccount currentUser = SessionController.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour emprunter un livre", Alert.AlertType.ERROR);
            return;
        }

        List<Loan> myLoans;
        try {
            myLoans = loanDAO.findByUserId(currentUser.getId());
        } catch (Exception ex) {
            myLoans = loanDAO.getAll().stream().filter(l -> l.getUserId() == currentUser.getId()).toList();
        }

        long activeCount = myLoans.stream().filter(l -> !l.isReturned() && l.isValidated()).count();
        if (activeCount >= 5) {
            showAlert("Limite atteinte", "Vous avez déjà 5 emprunts actifs.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer l'emprunt");
        confirm.setHeaderText("Emprunter « " + book.getTitle() + " » ?");
        confirm.setContentText("Durée d'emprunt: 14 jours\nDate de retour prévue: " +
                LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                Loan newLoan = new Loan();
                newLoan.setUserId(currentUser.getId());
                newLoan.setBookId(book.getId());
                newLoan.setLoanDate(LocalDate.now());
                newLoan.setReturnDue(LocalDate.now().plusDays(14));
                newLoan.setValidated(false);
                newLoan.setValidatedBy(0);
                newLoan.setReturned(false);

                if (loanDAO.insert(newLoan)) {
                    showAlert("Succès", "Demande d'emprunt envoyée (en attente de validation).", Alert.AlertType.INFORMATION);
                    loadStatistics();
                    showCatalogueView();
                } else {
                    showAlert("Erreur", "Échec de la demande d'emprunt", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // -------------------------
    // MY LOANS
    // -------------------------
    private void showMyLoansView() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(25));
        view.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

        Label title = new Label("Mes Emprunts");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        SearchBar sb = createSearchBar("Rechercher (titre, statut) ...");
        TextField searchField = sb.field;

        TableView<Loan> table = createMyLoansTableWithSearch(searchField);

        view.getChildren().addAll(title, sb.root, table);
        contentArea.getChildren().setAll(view);
    }

    private TableView<Loan> createMyLoansTableWithSearch(TextField searchField) {
        TableView<Loan> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Loan, String> colBook = new TableColumn<>("Titre du livre");
        colBook.setCellValueFactory(cd -> new SimpleStringProperty(getBookTitle(cd.getValue().getBookId())));
        colBook.setPrefWidth(260);

        TableColumn<Loan, LocalDate> colLoanDate = new TableColumn<>("Date emprunt");
        colLoanDate.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colLoanDate.setPrefWidth(140);

        TableColumn<Loan, LocalDate> colDue = new TableColumn<>("Date retour");
        colDue.setCellValueFactory(new PropertyValueFactory<>("returnDue"));
        colDue.setPrefWidth(140);

        TableColumn<Loan, Double> colPenalty = new TableColumn<>("Pénalité");
        colPenalty.setCellValueFactory(new PropertyValueFactory<>("penalty"));
        colPenalty.setPrefWidth(120);

        TableColumn<Loan, String> colStatus = new TableColumn<>("Statut");
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
                setText(loan.getStatusText());
                setStyle("-fx-text-fill: " + loan.getStatusColor() + "; -fx-font-weight: bold;");
            }
        });

        table.getColumns().addAll(colBook, colLoanDate, colDue, colPenalty, colStatus);

        UserAccount currentUser = SessionController.getCurrentUser();
        List<Loan> myLoans = new ArrayList<>();
        if (currentUser != null) {
            try {
                myLoans = loanDAO.findByUserId(currentUser.getId());
            } catch (Exception ex) {
                myLoans = loanDAO.getAll().stream().filter(l -> l.getUserId() == currentUser.getId()).toList();
            }
        }

        myLoans = myLoans.stream()
                .sorted((a, b) -> {
                    if (a.getLoanDate() == null || b.getLoanDate() == null) return 0;
                    return b.getLoanDate().compareTo(a.getLoanDate());
                })
                .toList();

        ObservableList<Loan> base = FXCollections.observableArrayList(myLoans);
        FilteredList<Loan> filtered = new FilteredList<>(base, l -> true);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, old, val) -> {
            String q = (val == null) ? "" : val.trim().toLowerCase();
            filtered.setPredicate(loan -> {
                if (q.isEmpty()) return true;
                String title = getBookTitle(loan.getBookId()).toLowerCase();
                String status = (loan.getStatusText() == null) ? "" : loan.getStatusText().toLowerCase();
                return title.contains(q) || status.contains(q);
            });
        });

        return table;
    }

    private String getBookTitle(int bookId) {
        Book book = bookDAO.getById(bookId);
        return book != null ? book.getTitle() : "Livre #" + bookId;
    }

    // -------------------------
    // RETURN BOOK
    // -------------------------
    private void showReturnBookView() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(25));
        view.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

        Label title = new Label("Retourner un Livre");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label info = new Label("Sélectionnez un emprunt actif pour le retourner (5 MAD/jour en retard).");
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        info.setWrapText(true);

        SearchBar sb = createSearchBar("Rechercher (titre du livre) ...");
        TextField searchField = sb.field;

        TableView<Loan> table = createReturnableLoansTableWithSearch(searchField);

        view.getChildren().addAll(title, info, sb.root, table);
        contentArea.getChildren().setAll(view);
    }

    private TableView<Loan> createReturnableLoansTableWithSearch(TextField searchField) {
        TableView<Loan> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Loan, String> colBook = new TableColumn<>("Livre");
        colBook.setCellValueFactory(cd -> new SimpleStringProperty(getBookTitle(cd.getValue().getBookId())));
        colBook.setPrefWidth(320);

        TableColumn<Loan, LocalDate> colLoanDate = new TableColumn<>("Emprunté le");
        colLoanDate.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colLoanDate.setPrefWidth(150);

        TableColumn<Loan, LocalDate> colDue = new TableColumn<>("À retourner le");
        colDue.setCellValueFactory(new PropertyValueFactory<>("returnDue"));
        colDue.setPrefWidth(150);

        TableColumn<Loan, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(180);
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnReturnNow = new Button("Retourner");
            private final HBox box = new HBox(btnReturnNow);

            {
                box.setAlignment(Pos.CENTER);

                btnReturnNow.setStyle(
                        "-fx-background-color: #10b981;" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 8px 16px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-weight: bold;"
                );

                btnReturnNow.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    returnLoan(loan);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(colBook, colLoanDate, colDue, colAction);

        UserAccount currentUser = SessionController.getCurrentUser();
        List<Loan> activeLoans = new ArrayList<>();

        if (currentUser != null) {
            List<Loan> myLoans;
            try {
                myLoans = loanDAO.findByUserId(currentUser.getId());
            } catch (Exception ex) {
                myLoans = loanDAO.getAll().stream().filter(l -> l.getUserId() == currentUser.getId()).toList();
            }

            activeLoans = myLoans.stream()
                    .filter(l -> !l.isReturned() && l.isValidated())
                    .sorted(Comparator.comparing(Loan::getReturnDue))
                    .toList();
        }

        ObservableList<Loan> base = FXCollections.observableArrayList(activeLoans);
        FilteredList<Loan> filtered = new FilteredList<>(base, l -> true);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, old, val) -> {
            String q = (val == null) ? "" : val.trim().toLowerCase();
            filtered.setPredicate(loan -> q.isEmpty() || getBookTitle(loan.getBookId()).toLowerCase().contains(q));
        });

        if (activeLoans.isEmpty()) {
            Label noLoans = new Label("Aucun emprunt actif à retourner");
            noLoans.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-padding: 20px;");
            table.setPlaceholder(noLoans);
        }

        return table;
    }

    private void returnLoan(Loan loan) {
        String title = getBookTitle(loan.getBookId());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le Retour");
        confirm.setHeaderText("Retourner « " + title + " » ?");
        confirm.setContentText("Êtes-vous sûr de vouloir retourner ce livre ?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                loan.setReturned(true);
                loan.setActualReturnDate(LocalDate.now());

                if (loan.isOverdue()) {
                    loan.setPenalty(loan.calculatePenalty());
                }

                Book book = bookDAO.getById(loan.getBookId());
                if (book != null) {
                    book.setQuantity(book.getQuantity() + 1);
                    bookDAO.update(book);
                }

                if (loanDAO.update(loan)) {
                    showAlert("Succès", "Livre retourné avec succès !", Alert.AlertType.INFORMATION);
                    loadStatistics();
                    showReturnBookView();
                } else {
                    showAlert("Erreur", "Échec du retour du livre", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // -------------------------
    // ✅ SEARCH BAR (plus petite + propre)
    // -------------------------
    private SearchBar createSearchBar(String prompt) {

        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        // ✅ moins grand
        box.setPadding(new Insets(4, 10, 4, 10));
        box.setPrefHeight(32);

        // ✅ si tu veux limiter la largeur (optionnel)
        // box.setMaxWidth(900);

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

        clear.setOnMouseEntered(e -> clear.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #6b7280;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0 6 0 6;"
        ));
        clear.setOnMouseExited(e -> clear.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #9ca3af;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0 6 0 6;"
        ));

        clear.setOnAction(e -> tf.clear());

        tf.focusedProperty().addListener((obs, was, is) -> box.setStyle(is ? focusStyle : normalStyle));

        box.getChildren().addAll(icon, tf, clear);
        return new SearchBar(box, tf);
    }

    // -------------------------
    // UI helpers
    // -------------------------
    private void styleChartText(Node chart) {
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

    private void setActiveTab(Button activeButton) {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #718096; -fx-font-size: 14px; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #2d3748; -fx-font-size: 14px; -fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-border-color: #e0e6ed; -fx-border-width: 1; -fx-border-radius: 8px;";

        btnHome.setStyle(inactiveStyle);
        btnCatalogue.setStyle(inactiveStyle);
        btnMyLoans.setStyle(inactiveStyle);
        btnReturn.setStyle(inactiveStyle);

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
