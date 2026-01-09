package com.example.kuet_transportation_and_schedueling_system.controller;

import com.example.kuet_transportation_and_schedueling_system.dao.*;
import com.example.kuet_transportation_and_schedueling_system.model.Route;
import com.example.kuet_transportation_and_schedueling_system.model.Bus;
import com.example.kuet_transportation_and_schedueling_system.model.Schedule;
import com.example.kuet_transportation_and_schedueling_system.model.Student;
import com.example.kuet_transportation_and_schedueling_system.model.Request;
import com.example.kuet_transportation_and_schedueling_system.model.RequestStatus;
import com.example.kuet_transportation_and_schedueling_system.model.User;
import com.example.kuet_transportation_and_schedueling_system.util.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class AdminDashboardController {

    // Tab references
    @FXML private TabPane mainTabPane;
    @FXML private Tab routesTab;
    @FXML private Tab busesTab;
    @FXML private Tab schedulesTab;
    @FXML private Tab studentsTab;
    @FXML private Tab requestsTab;
    @FXML private Tab messagesTab;

    // Card Panes (replacing TableViews)
    @FXML private FlowPane routesCardsPane;
    @FXML private FlowPane busesCardsPane;
    @FXML private FlowPane schedulesCardsPane;
    @FXML private FlowPane studentsCardsPane;
    @FXML private FlowPane requestsCardsPane;

    // Buttons
    @FXML private Button addRouteBtn;
    @FXML private Button editRouteBtn;
    @FXML private Button deleteRouteBtn;
    @FXML private Button refreshRoutesBtn;
    @FXML private Button addBusBtn;
    @FXML private Button editBusBtn;
    @FXML private Button deleteBusBtn;
    @FXML private Button refreshBusesBtn;
    @FXML private Button addScheduleBtn;
    @FXML private Button deleteScheduleBtn;
    @FXML private Button refreshSchedulesBtn;
    @FXML private Button assignStudentBtn;
    @FXML private Button removeAssignmentBtn;
    @FXML private Button refreshStudentsBtn;
    @FXML private Button approveRequestBtn;
    @FXML private Button rejectRequestBtn;
    @FXML private Button refreshRequestsBtn;

    // Messages Tab
    @FXML private TextArea messageTextArea;
    @FXML private Button sendMessageBtn;
    @FXML private Button sendToAllBtn;
    @FXML private Button sendToSelectedBtn;

    // Top bar
    @FXML private Label welcomeLabel;
    @FXML private Button logoutBtn;

    // Dashboard Statistics
    @FXML private Label totalRoutesLabel;
    @FXML private Label totalBusesLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label pendingRequestsLabel;

    // Selected items for edit/delete operations
    private Route selectedRoute = null;
    private Bus selectedBus = null;
    private Schedule selectedSchedule = null;
    private Student selectedStudent = null;
    private Request selectedRequest = null;

    // DAOs
    private final RouteDAO routeDAO = new RouteDAO();
    private final BusDAO busDAO = new BusDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    @FXML
    public void initialize() {
        // Set welcome message
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, Admin: " + currentUser.getUsername());
        }

        loadAllData();
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try {
            if (totalRoutesLabel != null) {
                int routeCount = routeDAO.getAllRoutes().size();
                totalRoutesLabel.setText(String.valueOf(routeCount));
            }

            if (totalBusesLabel != null) {
                int busCount = busDAO.getAllBuses().size();
                totalBusesLabel.setText(String.valueOf(busCount));
            }

            if (totalStudentsLabel != null) {
                int studentCount = studentDAO.getAllStudents().size();
                totalStudentsLabel.setText(String.valueOf(studentCount));
            }

            if (pendingRequestsLabel != null) {
                int pendingCount = (int) requestDAO.getAllRequests().stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .count();
                pendingRequestsLabel.setText(String.valueOf(pendingCount));
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard stats: " + e.getMessage());
        }
    }

    // ================ ROUTE MANAGEMENT ================

    private VBox createRouteCard(Route route) {
        VBox card = new VBox(12);
        card.getStyleClass().add("data-card");
        card.setOnMouseClicked(e -> {
            selectedRoute = route;
            highlightSelectedCard(routesCardsPane, card);
        });

        // Route name header
        Label nameLabel = new Label(route.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Separator
        Separator separator1 = new Separator();

        // Start point
        HBox startBox = new HBox(8);
        startBox.setAlignment(Pos.CENTER_LEFT);
        Label startIcon = new Label("🟢");
        startIcon.setStyle("-fx-font-size: 16px;");
        Label startLabel = new Label(route.getStartPoint());
        startLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e; -fx-font-weight: bold;");
        startBox.getChildren().addAll(startIcon, startLabel);

        // Midpoints (if any)
        VBox midpointsBox = new VBox(5);
        String endPointData = route.getEndPoint();
        String actualEndPoint = endPointData;
        String[] midpoints = new String[0];

        // Check if midpoints are stored (format: "endPoint|midpoint1,midpoint2,...")
        if (endPointData.contains("|")) {
            String[] parts = endPointData.split("\\|", 2);
            actualEndPoint = parts[0];
            if (parts.length > 1 && !parts[1].isEmpty()) {
                midpoints = parts[1].split(",");
            }
        }

        // Display midpoints
        if (midpoints.length > 0) {
            for (int i = 0; i < midpoints.length; i++) {
                HBox midpointBox = new HBox(8);
                midpointBox.setAlignment(Pos.CENTER_LEFT);
                midpointBox.setPadding(new Insets(0, 0, 0, 10));

                Label midIcon = new Label("🟡");
                midIcon.setStyle("-fx-font-size: 14px;");

                Label midLabel = new Label(midpoints[i].trim());
                midLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

                midpointBox.getChildren().addAll(midIcon, midLabel);
                midpointsBox.getChildren().add(midpointBox);
            }
        }

        // End point
        HBox endBox = new HBox(8);
        endBox.setAlignment(Pos.CENTER_LEFT);
        Label endIcon = new Label("🔴");
        endIcon.setStyle("-fx-font-size: 16px;");
        Label endLabel = new Label(actualEndPoint);
        endLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e; -fx-font-weight: bold;");
        endBox.getChildren().addAll(endIcon, endLabel);

        // ID badge
        Separator separator2 = new Separator();
        Label idLabel = new Label("ID: " + route.getRouteId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-padding: 3 8; -fx-background-color: #ecf0f1; -fx-background-radius: 10;");

        // Stops count badge
        HBox badgesBox = new HBox(8);
        badgesBox.getChildren().add(idLabel);

        if (midpoints.length > 0) {
            Label stopsLabel = new Label(midpoints.length + " Stop(s)");
            stopsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-color: #3498db; -fx-background-radius: 10; -fx-font-weight: bold;");
            badgesBox.getChildren().add(stopsLabel);
        }

        card.getChildren().addAll(nameLabel, separator1, startBox);
        if (midpointsBox.getChildren().size() > 0) {
            card.getChildren().add(midpointsBox);
        }
        card.getChildren().addAll(endBox, separator2, badgesBox);

        return card;
    }

    private void loadRoutes() {
        try {
            if (routesCardsPane == null) return;
            routesCardsPane.getChildren().clear();
            selectedRoute = null;

            ObservableList<Route> routes = routeDAO.getAllRoutes();
            for (Route route : routes) {
                routesCardsPane.getChildren().add(createRouteCard(route));
            }

            if (routes.isEmpty()) {
                Label emptyLabel = new Label("No routes available. Click 'Add Route' to create one.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
                routesCardsPane.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            showError("Failed to load routes: " + e.getMessage());
        }
    }

    @FXML private void handleAddRoute(ActionEvent event) {
        Dialog<Route> dialog = new Dialog<>();
        dialog.setTitle("Add New Route");
        dialog.setHeaderText("Enter route details (add midpoints between start and end)");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(10));

        // Route name
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Route Name:");
        nameLabel.setMinWidth(80);
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., KUET to City Center");
        nameField.setPrefWidth(300);
        nameBox.getChildren().addAll(nameLabel, nameField);

        // Start point
        HBox startBox = new HBox(10);
        startBox.setAlignment(Pos.CENTER_LEFT);
        Label startLabel = new Label("Start Point:");
        startLabel.setMinWidth(80);
        TextField startField = new TextField();
        startField.setPromptText("Starting location");
        startField.setPrefWidth(300);
        startBox.getChildren().addAll(startLabel, startField);

        // Midpoints container
        VBox midpointsContainer = new VBox(10);
        midpointsContainer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");

        Label midpointsLabel = new Label("Midpoints (Stops between start and end):");
        midpointsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        VBox midpointFields = new VBox(8);

        // Add midpoint button
        Button addMidpointBtn = new Button("➕ Add Midpoint");
        addMidpointBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        addMidpointBtn.setOnAction(e -> {
            HBox midpointBox = new HBox(8);
            midpointBox.setAlignment(Pos.CENTER_LEFT);

            Label stopLabel = new Label("🟡 Stop " + (midpointFields.getChildren().size() + 1) + ":");
            stopLabel.setMinWidth(70);

            TextField midpointField = new TextField();
            midpointField.setPromptText("Midpoint location");
            midpointField.setPrefWidth(220);

            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
            removeBtn.setOnAction(removeEvent -> {
                midpointFields.getChildren().remove(midpointBox);
                updateMidpointLabels(midpointFields);
            });

            midpointBox.getChildren().addAll(stopLabel, midpointField, removeBtn);
            midpointFields.getChildren().add(midpointBox);
        });

        midpointsContainer.getChildren().addAll(midpointsLabel, midpointFields, addMidpointBtn);

        // End point
        HBox endBox = new HBox(10);
        endBox.setAlignment(Pos.CENTER_LEFT);
        Label endLabel = new Label("End Point:");
        endLabel.setMinWidth(80);
        TextField endField = new TextField();
        endField.setPromptText("Final destination");
        endField.setPrefWidth(300);
        endBox.getChildren().addAll(endLabel, endField);

        mainContainer.getChildren().addAll(nameBox, new Separator(), startBox, midpointsContainer, endBox);

        // Wrap in ScrollPane to handle many midpoints
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setMaxHeight(600);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String routeName = nameField.getText().trim();
                    String startPoint = startField.getText().trim();
                    String endPoint = endField.getText().trim();

                    if (routeName.isEmpty() || startPoint.isEmpty() || endPoint.isEmpty()) {
                        showError("Please fill in all required fields (Name, Start, End)!");
                        return null;
                    }

                    // Collect midpoints
                    StringBuilder midpoints = new StringBuilder();
                    for (var node : midpointFields.getChildren()) {
                        if (node instanceof HBox) {
                            HBox box = (HBox) node;
                            for (var child : box.getChildren()) {
                                if (child instanceof TextField) {
                                    String midpoint = ((TextField) child).getText().trim();
                                    if (!midpoint.isEmpty()) {
                                        if (midpoints.length() > 0) {
                                            midpoints.append(",");
                                        }
                                        midpoints.append(midpoint);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    // Add route with midpoints (stored in endPoint as "endPoint|midpoints")
                    String fullEndPoint = endPoint;
                    if (midpoints.length() > 0) {
                        fullEndPoint = endPoint + "|" + midpoints.toString();
                    }

                    routeDAO.addRoute(routeName, startPoint, fullEndPoint);
                    Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                            "Added route: " + routeName);
                    loadRoutes();
                    loadDashboardStats();
                    showSuccess("Route added successfully with " +
                        (midpoints.length() > 0 ? midpoints.toString().split(",").length + " midpoint(s)!" : "no midpoints!"));
                } catch (Exception e) {
                    showError("Failed to add route: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateMidpointLabels(VBox midpointFields) {
        int index = 1;
        for (var node : midpointFields.getChildren()) {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                for (var child : box.getChildren()) {
                    if (child instanceof Label) {
                        ((Label) child).setText("🟡 Stop " + index + ":");
                        break;
                    }
                }
                index++;
            }
        }
    }

    @FXML private void handleEditRoute(ActionEvent event) {
        if (selectedRoute == null) {
            showError("Please select a route card to edit!");
            return;
        }

        Dialog<Route> dialog = new Dialog<>();
        dialog.setTitle("Edit Route");
        dialog.setHeaderText("Update route details and midpoints");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(10));

        // Parse existing data
        String endPointData = selectedRoute.getEndPoint();
        String actualEndPoint = endPointData;
        String[] existingMidpoints = new String[0];

        if (endPointData.contains("|")) {
            String[] parts = endPointData.split("\\|", 2);
            actualEndPoint = parts[0];
            if (parts.length > 1 && !parts[1].isEmpty()) {
                existingMidpoints = parts[1].split(",");
            }
        }

        // Route name
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Route Name:");
        nameLabel.setMinWidth(80);
        TextField nameField = new TextField(selectedRoute.getName());
        nameField.setPrefWidth(300);
        nameBox.getChildren().addAll(nameLabel, nameField);

        // Start point
        HBox startBox = new HBox(10);
        startBox.setAlignment(Pos.CENTER_LEFT);
        Label startLabel = new Label("Start Point:");
        startLabel.setMinWidth(80);
        TextField startField = new TextField(selectedRoute.getStartPoint());
        startField.setPrefWidth(300);
        startBox.getChildren().addAll(startLabel, startField);

        // Midpoints container
        VBox midpointsContainer = new VBox(10);
        midpointsContainer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5;");

        Label midpointsLabel = new Label("Midpoints (Stops between start and end):");
        midpointsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        VBox midpointFields = new VBox(8);

        // Add existing midpoints
        for (int i = 0; i < existingMidpoints.length; i++) {
            HBox midpointBox = new HBox(8);
            midpointBox.setAlignment(Pos.CENTER_LEFT);

            Label stopLabel = new Label("🟡 Stop " + (i + 1) + ":");
            stopLabel.setMinWidth(70);

            TextField midpointField = new TextField(existingMidpoints[i].trim());
            midpointField.setPrefWidth(220);

            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
            removeBtn.setOnAction(removeEvent -> {
                midpointFields.getChildren().remove(midpointBox);
                updateMidpointLabels(midpointFields);
            });

            midpointBox.getChildren().addAll(stopLabel, midpointField, removeBtn);
            midpointFields.getChildren().add(midpointBox);
        }

        // Add midpoint button
        Button addMidpointBtn = new Button("➕ Add Midpoint");
        addMidpointBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        addMidpointBtn.setOnAction(e -> {
            HBox midpointBox = new HBox(8);
            midpointBox.setAlignment(Pos.CENTER_LEFT);

            Label stopLabel = new Label("🟡 Stop " + (midpointFields.getChildren().size() + 1) + ":");
            stopLabel.setMinWidth(70);

            TextField midpointField = new TextField();
            midpointField.setPromptText("Midpoint location");
            midpointField.setPrefWidth(220);

            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
            removeBtn.setOnAction(removeEvent -> {
                midpointFields.getChildren().remove(midpointBox);
                updateMidpointLabels(midpointFields);
            });

            midpointBox.getChildren().addAll(stopLabel, midpointField, removeBtn);
            midpointFields.getChildren().add(midpointBox);
        });

        midpointsContainer.getChildren().addAll(midpointsLabel, midpointFields, addMidpointBtn);

        // End point
        HBox endBox = new HBox(10);
        endBox.setAlignment(Pos.CENTER_LEFT);
        Label endLabel = new Label("End Point:");
        endLabel.setMinWidth(80);
        TextField endField = new TextField(actualEndPoint);
        endField.setPrefWidth(300);
        endBox.getChildren().addAll(endLabel, endField);

        mainContainer.getChildren().addAll(nameBox, new Separator(), startBox, midpointsContainer, endBox);

        // Wrap in ScrollPane to handle many midpoints
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setMaxHeight(600);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    String routeName = nameField.getText().trim();
                    String startPoint = startField.getText().trim();
                    String endPoint = endField.getText().trim();

                    if (routeName.isEmpty() || startPoint.isEmpty() || endPoint.isEmpty()) {
                        showError("Please fill in all required fields (Name, Start, End)!");
                        return null;
                    }

                    // Collect midpoints
                    StringBuilder midpoints = new StringBuilder();
                    for (var node : midpointFields.getChildren()) {
                        if (node instanceof HBox) {
                            HBox box = (HBox) node;
                            for (var child : box.getChildren()) {
                                if (child instanceof TextField) {
                                    String midpoint = ((TextField) child).getText().trim();
                                    if (!midpoint.isEmpty()) {
                                        if (midpoints.length() > 0) {
                                            midpoints.append(",");
                                        }
                                        midpoints.append(midpoint);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    // Update route with midpoints
                    String fullEndPoint = endPoint;
                    if (midpoints.length() > 0) {
                        fullEndPoint = endPoint + "|" + midpoints.toString();
                    }

                    routeDAO.updateRoute(selectedRoute.getRouteId(), routeName, startPoint, fullEndPoint);
                    Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                            "Updated route: " + selectedRoute.getRouteId());
                    loadRoutes();
                    loadDashboardStats();
                    showSuccess("Route updated successfully!");
                } catch (Exception e) {
                    showError("Failed to update route: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteRoute(ActionEvent event) {
        if (selectedRoute == null) {
            showError("Please select a route card to delete!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Route");
        alert.setContentText("Are you sure you want to delete: " + selectedRoute.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                routeDAO.deleteRoute(selectedRoute.getRouteId());
                Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                        "Deleted route: " + selectedRoute.getRouteId());
                loadRoutes();
                loadDashboardStats();
                showSuccess("Route deleted successfully!");
            } catch (Exception e) {
                showError("Failed to delete route: " + e.getMessage());
            }
        }
    }

    @FXML private void handleRefreshRoutes(ActionEvent event) {
        loadRoutes();
        loadDashboardStats();
    }

    // ================ BUS MANAGEMENT ================

    private VBox createBusCard(Bus bus) {
        VBox card = new VBox(12);
        card.getStyleClass().add("data-card");
        card.setOnMouseClicked(e -> {
            selectedBus = bus;
            highlightSelectedCard(busesCardsPane, card);
        });

        // Bus icon and number
        Label busIcon = new Label("🚌");
        busIcon.setStyle("-fx-font-size: 36px;");

        Label busNumberLabel = new Label(bus.getBusNumber());
        busNumberLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Capacity info
        HBox capacityBox = new HBox(8);
        capacityBox.setAlignment(Pos.CENTER_LEFT);
        Label capacityIcon = new Label("👥");
        Label capacityLabel = new Label("Capacity: " + bus.getCapacity() + " seats");
        capacityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        capacityBox.getChildren().addAll(capacityIcon, capacityLabel);

        // ID badge
        Label idLabel = new Label("ID: " + bus.getBusId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-padding: 3 8; -fx-background-color: #ecf0f1; -fx-background-radius: 10;");

        card.getChildren().addAll(busIcon, busNumberLabel, new Separator(), capacityBox, idLabel);
        return card;
    }

    private void loadBuses() {
        try {
            if (busesCardsPane == null) return;
            busesCardsPane.getChildren().clear();
            selectedBus = null;

            ObservableList<Bus> buses = busDAO.getAllBuses();
            for (Bus bus : buses) {
                busesCardsPane.getChildren().add(createBusCard(bus));
            }

            if (buses.isEmpty()) {
                Label emptyLabel = new Label("No buses available. Click 'Add Bus' to create one.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
                busesCardsPane.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            showError("Failed to load buses: " + e.getMessage());
        }
    }

    @FXML private void handleAddBus(ActionEvent event) {
        Dialog<Bus> dialog = new Dialog<>();
        dialog.setTitle("Add New Bus");
        dialog.setHeaderText("Enter bus details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField numberField = new TextField();
        numberField.setPromptText("Bus Number (e.g., KUET-01)");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");

        grid.add(new Label("Bus Number:"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(new Label("Capacity:"), 0, 1);
        grid.add(capacityField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int capacity = Integer.parseInt(capacityField.getText());
                    if (!Validator.isValidCapacity(capacity)) {
                        showError("Capacity must be between 1 and 100!");
                        return null;
                    }
                    busDAO.addBus(numberField.getText(), capacity);
                    Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                            "Added bus: " + numberField.getText());
                    loadBuses();
                    showSuccess("Bus added successfully!");
                } catch (NumberFormatException e) {
                    showError("Invalid capacity! Please enter a number.");
                } catch (Exception e) {
                    showError("Failed to add bus: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML private void handleEditBus(ActionEvent event) {
        if (selectedBus == null) {
            showError("Please select a bus card to edit!");
            return;
        }

        Dialog<Bus> dialog = new Dialog<>();
        dialog.setTitle("Edit Bus");
        dialog.setHeaderText("Update bus details");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField numberField = new TextField(selectedBus.getBusNumber());
        TextField capacityField = new TextField(String.valueOf(selectedBus.getCapacity()));

        grid.add(new Label("Bus Number:"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(new Label("Capacity:"), 0, 1);
        grid.add(capacityField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    int capacity = Integer.parseInt(capacityField.getText());
                    busDAO.updateBus(selectedBus.getBusId(), numberField.getText(), capacity);
                    Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                            "Updated bus: " + selectedBus.getBusId());
                    loadBuses();
                    loadDashboardStats();
                    showSuccess("Bus updated successfully!");
                } catch (Exception e) {
                    showError("Failed to update bus: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML private void handleDeleteBus(ActionEvent event) {
        if (selectedBus == null) {
            showError("Please select a bus card to delete!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Bus");
        alert.setContentText("Are you sure you want to delete: " + selectedBus.getBusNumber() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                busDAO.deleteBus(selectedBus.getBusId());
                Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                        "Deleted bus: " + selectedBus.getBusId());
                loadBuses();
                loadDashboardStats();
                showSuccess("Bus deleted successfully!");
            } catch (Exception e) {
                showError("Failed to delete bus: " + e.getMessage());
            }
        }
    }

    @FXML private void handleRefreshBuses(ActionEvent event) {
        loadBuses();
        loadDashboardStats();
    }

    // ================ SCHEDULE MANAGEMENT ================

    private VBox createScheduleCard(Schedule schedule) {
        VBox card = new VBox(12);
        card.getStyleClass().add("data-card");
        card.setOnMouseClicked(e -> {
            selectedSchedule = schedule;
            highlightSelectedCard(schedulesCardsPane, card);
        });

        // Calendar icon
        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 32px;");

        // Bus info
        Label busLabel = new Label("Bus: " + schedule.getBusNumber());
        busLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Route info
        Label routeLabel = new Label("Route: " + schedule.getRouteName());
        routeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        // Date and time
        HBox dateTimeBox = new HBox(15);
        Label dateLabel = new Label("📆 " + schedule.getScheduleDate());
        Label timeLabel = new Label("🕐 " + schedule.getScheduleTime());
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        card.getChildren().addAll(icon, busLabel, routeLabel, new Separator(), dateTimeBox);
        return card;
    }

    private void loadSchedules() {
        try {
            if (schedulesCardsPane == null) return;
            schedulesCardsPane.getChildren().clear();
            selectedSchedule = null;

            ObservableList<Schedule> schedules = scheduleDAO.getAllSchedules();
            for (Schedule schedule : schedules) {
                schedulesCardsPane.getChildren().add(createScheduleCard(schedule));
            }

            if (schedules.isEmpty()) {
                Label emptyLabel = new Label("No schedules available. Click 'Create Schedule' to add one.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
                schedulesCardsPane.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            showError("Failed to load schedules: " + e.getMessage());
        }
    }

    // ================ STUDENT MANAGEMENT ================

    private VBox createStudentCard(Student student) {
        VBox card = new VBox(12);
        card.getStyleClass().add("data-card");
        card.setOnMouseClicked(e -> {
            selectedStudent = student;
            highlightSelectedCard(studentsCardsPane, card);
        });

        // Student icon
        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 32px;");

        // Name
        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Student ID
        Label idLabel = new Label("ID: " + student.getStudentId());
        idLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        // Department
        Label deptLabel = new Label("Dept: " + student.getDepartment());
        deptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        // Assignment info
        VBox assignmentBox = new VBox(5);
        try {
            String routeInfo = "Route: Not Assigned";
            String busInfo = "Bus: Not Assigned";

            if (student.getAssignedRouteId() != null) {
                Route route = routeDAO.getRouteById(student.getAssignedRouteId());
                routeInfo = "Route: " + (route != null ? route.getName() : "N/A");
            }

            if (student.getAssignedBusId() != null) {
                Bus bus = busDAO.getBusById(student.getAssignedBusId());
                busInfo = "Bus: " + (bus != null ? bus.getBusNumber() : "N/A");
            }

            Label routeLabel = new Label(routeInfo);
            Label busLabel = new Label(busInfo);
            routeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            busLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            assignmentBox.getChildren().addAll(routeLabel, busLabel);
        } catch (Exception e) {
            // Handle exception silently
        }

        card.getChildren().addAll(icon, nameLabel, idLabel, deptLabel, new Separator(), assignmentBox);
        return card;
    }

    private void loadStudents() {
        try {
            if (studentsCardsPane == null) return;
            studentsCardsPane.getChildren().clear();
            selectedStudent = null;

            ObservableList<Student> students = studentDAO.getAllStudents();
            for (Student student : students) {
                studentsCardsPane.getChildren().add(createStudentCard(student));
            }

            if (students.isEmpty()) {
                Label emptyLabel = new Label("No students registered yet.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
                studentsCardsPane.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            showError("Failed to load students: " + e.getMessage());
        }
    }

    // ================ REQUEST MANAGEMENT ================

    private VBox createRequestCard(Request request) {
        VBox card = new VBox(12);
        card.getStyleClass().add("data-card");
        card.setOnMouseClicked(e -> {
            selectedRequest = request;
            highlightSelectedCard(requestsCardsPane, card);
        });

        // Status color coding
        String statusColor = "#f39c12"; // Pending - orange
        if (request.getStatus() == RequestStatus.APPROVED) {
            statusColor = "#27ae60"; // Green
        } else if (request.getStatus() == RequestStatus.REJECTED) {
            statusColor = "#e74c3c"; // Red
        }

        // Status badge
        Label statusLabel = new Label(request.getStatus().toString());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-background-radius: 10; -fx-padding: 5 12;");

        // Student info
        Label studentLabel = new Label(request.getStudentId() + " - " + (request.getStudentName() != null ? request.getStudentName() : ""));
        studentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Request type
        Label typeLabel = new Label("Type: " + request.getType());
        typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        // Description
        Label descLabel = new Label(request.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(260);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        // Request ID badge
        Label idLabel = new Label("Request ID: " + request.getRequestId());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.getChildren().addAll(new Label("📋"), studentLabel);

        card.getChildren().addAll(topBox, statusLabel, typeLabel, new Separator(), descLabel, idLabel);
        return card;
    }

    private void loadRequests() {
        try {
            if (requestsCardsPane == null) return;
            requestsCardsPane.getChildren().clear();
            selectedRequest = null;

            ObservableList<Request> requests = requestDAO.getAllRequests();
            for (Request request : requests) {
                requestsCardsPane.getChildren().add(createRequestCard(request));
            }

            if (requests.isEmpty()) {
                Label emptyLabel = new Label("No requests submitted.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
                requestsCardsPane.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            showError("Failed to load requests: " + e.getMessage());
        }
    }

    // Helper method to highlight selected card
    private void highlightSelectedCard(FlowPane pane, VBox selectedCard) {
        if (pane == null) return;

        // Remove highlight from all cards
        for (var node : pane.getChildren()) {
            if (node instanceof VBox) {
                node.setStyle(node.getStyle().replace("-fx-border-color: #3498db;", "").replace("-fx-border-width: 2;", ""));
            }
        }

        // Highlight selected card
        String currentStyle = selectedCard.getStyle();
        if (!currentStyle.contains("-fx-border-color")) {
            selectedCard.setStyle(currentStyle + "-fx-border-color: #3498db; -fx-border-width: 2;");
        }
    }

    @FXML private void handleAddSchedule(ActionEvent event) {
        try {
            ObservableList<Bus> buses = busDAO.getAllBuses();
            ObservableList<Route> routes = routeDAO.getAllRoutes();

            if (buses.isEmpty() || routes.isEmpty()) {
                showError("Please add buses and routes first!");
                return;
            }

            Dialog<Schedule> dialog = new Dialog<>();
            dialog.setTitle("Add New Schedule");
            dialog.setHeaderText("Create bus schedule");

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            ComboBox<Bus> busCombo = new ComboBox<>(buses);
            ComboBox<Route> routeCombo = new ComboBox<>(routes);
            DatePicker datePicker = new DatePicker(LocalDate.now());
            TextField timeField = new TextField("08:00");
            timeField.setPromptText("HH:MM format");

            grid.add(new Label("Bus:"), 0, 0);
            grid.add(busCombo, 1, 0);
            grid.add(new Label("Route:"), 0, 1);
            grid.add(routeCombo, 1, 1);
            grid.add(new Label("Date:"), 0, 2);
            grid.add(datePicker, 1, 2);
            grid.add(new Label("Time:"), 0, 3);
            grid.add(timeField, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        Bus selectedBus = busCombo.getValue();
                        Route selectedRoute = routeCombo.getValue();
                        LocalDate date = datePicker.getValue();
                        LocalTime time = LocalTime.parse(timeField.getText());

                        if (selectedBus == null || selectedRoute == null) {
                            showError("Please select bus and route!");
                            return null;
                        }

                        scheduleDAO.addSchedule(selectedBus.getBusId(), selectedRoute.getRouteId(), date, time);
                        Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                                "Created schedule for bus " + selectedBus.getBusNumber());
                        loadSchedules();
                        showSuccess("Schedule added successfully!");
                    } catch (Exception e) {
                        showError("Failed to add schedule: " + e.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception e) {
            showError("Failed to load data: " + e.getMessage());
        }
    }

    @FXML private void handleDeleteSchedule(ActionEvent event) {
        if (selectedSchedule == null) {
            showError("Please select a schedule card to delete!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Schedule");
        alert.setContentText("Delete schedule for " + selectedSchedule.getBusNumber() + " on " + selectedSchedule.getScheduleDate() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                scheduleDAO.deleteSchedule(selectedSchedule.getScheduleId());
                Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                        "Deleted schedule: " + selectedSchedule.getScheduleId());
                loadSchedules();
                showSuccess("Schedule deleted successfully!");
            } catch (Exception e) {
                showError("Failed to delete schedule: " + e.getMessage());
            }
        }
    }

    @FXML private void handleRefreshSchedules(ActionEvent event) {
        loadSchedules();
    }


    @FXML private void handleAssignStudent(ActionEvent event) {
        if (selectedStudent == null) {
            showError("Please select a student card!");
            return;
        }

        try {
            ObservableList<Bus> buses = busDAO.getAllBuses();
            ObservableList<Route> routes = routeDAO.getAllRoutes();

            if (buses.isEmpty() || routes.isEmpty()) {
                showError("Please add buses and routes first!");
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Assign Bus & Route");
            dialog.setHeaderText("Assign to: " + selectedStudent.getName());

            ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            ComboBox<Bus> busCombo = new ComboBox<>(buses);
            ComboBox<Route> routeCombo = new ComboBox<>(routes);

            grid.add(new Label("Bus:"), 0, 0);
            grid.add(busCombo, 1, 0);
            grid.add(new Label("Route:"), 0, 1);
            grid.add(routeCombo, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    try {
                        Bus selectedBus = busCombo.getValue();
                        Route selectedRoute = routeCombo.getValue();

                        if (selectedBus == null || selectedRoute == null) {
                            showError("Please select both bus and route!");
                            return null;
                        }

                        studentDAO.assignBusAndRoute(selectedStudent.getStudentId(),
                                selectedBus.getBusId(), selectedRoute.getRouteId());
                        Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                                "Assigned student " + selectedStudent.getStudentId() + " to bus/route");
                        loadStudents();
                        showSuccess("Student assigned successfully!");
                    } catch (Exception e) {
                        showError("Failed to assign: " + e.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception e) {
            showError("Failed to load data: " + e.getMessage());
        }
    }

    @FXML private void handleRemoveAssignment(ActionEvent event) {
        if (selectedStudent == null) {
            showError("Please select a student card!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Remove Assignment");
        alert.setContentText("Remove bus/route assignment for: " + selectedStudent.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                studentDAO.removeAssignment(selectedStudent.getStudentId());
                Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                        "Removed assignment for student: " + selectedStudent.getStudentId());
                loadStudents();
                showSuccess("Assignment removed successfully!");
            } catch (Exception e) {
                showError("Failed to remove assignment: " + e.getMessage());
            }
        }
    }

    @FXML private void handleRefreshStudents(ActionEvent event) {
        loadStudents();
    }


    @FXML
    private void handleApproveRequest(ActionEvent event) {
        if (selectedRequest == null) {
            showError("Please select a request card!");
            return;
        }

        if (selectedRequest.getStatus() != RequestStatus.PENDING) {
            showError("Can only approve pending requests!");
            return;
        }

        try {
            requestDAO.approveRequest(selectedRequest.getRequestId());

            // Send notification to student
            Student student = studentDAO.getStudentByStudentId(selectedRequest.getStudentId());
            if (student != null) {
                messageDAO.sendMessage(student.getId(),
                        "Your request (" + selectedRequest.getType() + ") has been APPROVED.");
            }

            Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                    "Approved request: " + selectedRequest.getRequestId());
            loadRequests();
            loadDashboardStats();
            showSuccess("Request approved!");
        } catch (Exception e) {
            showError("Failed to approve request: " + e.getMessage());
        }
    }

    @FXML private void handleRejectRequest(ActionEvent event) {
        if (selectedRequest == null) {
            showError("Please select a request card!");
            return;
        }

        if (selectedRequest.getStatus() != RequestStatus.PENDING) {
            showError("Can only reject pending requests!");
            return;
        }

        try {
            requestDAO.rejectRequest(selectedRequest.getRequestId());
            Student student = studentDAO.getStudentByStudentId(selectedRequest.getStudentId());
            if (student != null) {
                messageDAO.sendMessage(student.getId(),
                        "Your request (" + selectedRequest.getType() + ") has been REJECTED.");
            }

            Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                    "Rejected request: " + selectedRequest.getRequestId());
            loadRequests();
            loadDashboardStats();
            showSuccess("Request rejected!");
        } catch (Exception e) {
            showError("Failed to reject request: " + e.getMessage());
        }
    }

    @FXML private void handleRefreshRequests(ActionEvent event) {
        loadRequests();
        loadDashboardStats();
    }

    @FXML private void handleSendMessage(ActionEvent event) {
        String message = messageTextArea.getText().trim();
        if (message.isEmpty()) {
            showError("Please enter a message!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Send");
        alert.setHeaderText("Send Message to All Students");
        alert.setContentText("Send this message to all registered students?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                messageDAO.sendMessageToAllStudents(message);
                Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                        "Sent broadcast message to all students");
                messageTextArea.clear();
                showSuccess("Message sent to all students!");
            } catch (Exception e) {
                showError("Failed to send message: " + e.getMessage());
            }
        }
    }

    @FXML private void handleSendToAll(ActionEvent event) {
        String message = messageTextArea.getText().trim();

        if (message.isEmpty()) {
            showError("Please enter a message!");
            return;
        }

        try {
            messageDAO.sendMessageToAllStudents(message);
            Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                    "Sent broadcast message to all students");
            messageTextArea.clear();
            showSuccess("Message sent to all students!");
        } catch (Exception e) {
            showError("Failed to send message: " + e.getMessage());
        }
    }

    @FXML private void handleSendToSelected(ActionEvent event) {
        String message = messageTextArea.getText().trim();

        if (message.isEmpty()) {
            showError("Please enter a message!");
            return;
        }

        try {
            messageDAO.sendMessageToAllStudents(message);
            Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                    "Sent message to students");
            messageTextArea.clear();
            showSuccess("Message sent successfully!");
        } catch (Exception e) {
            showError("Failed to send message: " + e.getMessage());
        }
    }

    @FXML private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Logout");
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Logger.getInstance().adminAction(SessionManager.getInstance().getCurrentUser().getUsername(),
                        "Logged out");
                SessionManager.getInstance().logout();
                SceneManager.switchScene("/com/example/kuet_transportation_and_schedueling_system/view/Login.fxml",
                        "Login");
            } catch (IOException e) {
                showError("Failed to logout: " + e.getMessage());
            }
        }
    }

    private void loadAllData() {
        loadRoutes();
        loadBuses();
        loadSchedules();
        loadStudents();
        loadRequests();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

