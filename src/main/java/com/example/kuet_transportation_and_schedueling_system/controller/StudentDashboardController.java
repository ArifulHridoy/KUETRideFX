package com.example.kuet_transportation_and_schedueling_system.controller;
import com.example.kuet_transportation_and_schedueling_system.dao.*;
import com.example.kuet_transportation_and_schedueling_system.model.*;
import com.example.kuet_transportation_and_schedueling_system.util.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class StudentDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label studentInfoLabel;
    @FXML private Label nameLabel;
    @FXML private Label departmentLabel;
    @FXML private Button logoutBtn;

    @FXML private Label assignedBusLabel;
    @FXML private Label assignedRouteLabel;
    @FXML private Label routeDetailsLabel;
    @FXML private Label routeStartLabel;
    @FXML private Label routeEndLabel;
    @FXML private Label busCapacityLabel;

    // Card panes replacing tables
    @FXML private FlowPane scheduleCardsPane;
    @FXML private FlowPane requestsCardsPane;
    @FXML private FlowPane messagesCardsPane;

    @FXML private Button refreshScheduleBtn;
    @FXML private ComboBox<String> requestTypeCombo;
    @FXML private TextArea requestDescriptionArea;
    @FXML private Label requestStatusLabel;
    @FXML private Button submitRequestBtn;
    @FXML private Button refreshRequestsBtn;
    @FXML private Button refreshMessagesBtn;
    @FXML private Label unreadCountLabel;

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final RouteDAO routeDAO = new RouteDAO();
    private final BusDAO busDAO = new BusDAO();

    private Student currentStudent;

    private Thread messageRefreshThread;
    private volatile boolean keepRefreshing = true;

    @FXML public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser instanceof Student) {
            currentStudent = (Student) currentUser;

            // Set welcome message
            welcomeLabel.setText("Welcome, " + currentStudent.getName());
            if (nameLabel != null) {
                nameLabel.setText(currentStudent.getName());
            }
            if (studentIdLabel != null) {
                studentIdLabel.setText(currentStudent.getStudentId());
            }
            if (departmentLabel != null) {
                departmentLabel.setText(currentStudent.getDepartment());
            }
            if (studentInfoLabel != null) {
                studentInfoLabel.setText(currentStudent.getStudentId() + " | " + currentStudent.getDepartment());
            }

            loadProfileInfo();
            loadSchedules();
            loadRequests();
            loadMessages();

            startMessageRefreshThread();
        }
    }

    //Profile Info

    private void loadProfileInfo() {
        try {
            // Reload student data to get latest assignments
            Student updatedStudent = studentDAO.getStudentByStudentId(currentStudent.getStudentId());
            if (updatedStudent != null) {
                currentStudent = updatedStudent;
                SessionManager.getInstance().setCurrentUser(currentStudent);
            }

            // Display assigned bus
            if (currentStudent.getAssignedBusId() != null) {
                Bus bus = busDAO.getBusById(currentStudent.getAssignedBusId());
                if (bus != null) {
                    if (assignedBusLabel != null) {
                        assignedBusLabel.setText(bus.getBusNumber());
                    }
                    if (busCapacityLabel != null) {
                        busCapacityLabel.setText(String.valueOf(bus.getCapacity()));
                    }
                } else {
                    if (assignedBusLabel != null) {
                        assignedBusLabel.setText("Not Assigned");
                    }
                    if (busCapacityLabel != null) {
                        busCapacityLabel.setText("-");
                    }
                }
            } else {
                if (assignedBusLabel != null) {
                    assignedBusLabel.setText("Not Assigned");
                }
                if (busCapacityLabel != null) {
                    busCapacityLabel.setText("-");
                }
            }

            // Display assigned route
            if (currentStudent.getAssignedRouteId() != null) {
                Route route = routeDAO.getRouteById(currentStudent.getAssignedRouteId());
                if (route != null) {
                    if (assignedRouteLabel != null) {
                        assignedRouteLabel.setText(route.getName());
                    }
                    if (routeStartLabel != null) {
                        routeStartLabel.setText(route.getStartPoint());
                    }
                    if (routeEndLabel != null) {
                        routeEndLabel.setText(route.getEndPoint());
                    }
                    if (routeDetailsLabel != null) {
                        routeDetailsLabel.setText(route.getStartPoint() + " → " + route.getEndPoint());
                    }
                } else {
                    if (assignedRouteLabel != null) {
                        assignedRouteLabel.setText("Not Assigned");
                    }
                    if (routeStartLabel != null) {
                        routeStartLabel.setText("-");
                    }
                    if (routeEndLabel != null) {
                        routeEndLabel.setText("-");
                    }
                    if (routeDetailsLabel != null) {
                        routeDetailsLabel.setText("");
                    }
                }
            } else {
                if (assignedRouteLabel != null) {
                    assignedRouteLabel.setText("Not Assigned");
                }
                if (routeStartLabel != null) {
                    routeStartLabel.setText("-");
                }
                if (routeEndLabel != null) {
                    routeEndLabel.setText("-");
                }
                if (routeDetailsLabel != null) {
                    routeDetailsLabel.setText("");
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to load profile info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Schedules

    private VBox createScheduleCard(Schedule schedule) {
        VBox card = new VBox(12);
        card.getStyleClass().add("data-card");

        // Calendar icon
        Label icon = new Label("📅");
        icon.setStyle("-fx-font-size: 32px;");

        // Bus info with icon
        HBox busBox = new HBox(8);
        busBox.setAlignment(Pos.CENTER_LEFT);
        Label busIcon = new Label("🚌");
        Label busLabel = new Label(schedule.getBusNumber());
        busLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        busBox.getChildren().addAll(busIcon, busLabel);

        // Route info
        HBox routeBox = new HBox(8);
        routeBox.setAlignment(Pos.CENTER_LEFT);
        Label routeIcon = new Label("🗺️");
        Label routeLabel = new Label(schedule.getRouteName());
        routeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        routeBox.getChildren().addAll(routeIcon, routeLabel);

        // Date and time
        HBox dateTimeBox = new HBox(15);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label("📆 " + schedule.getScheduleDate());
        Label timeLabel = new Label("🕐 " + schedule.getScheduleTime());
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        card.getChildren().addAll(icon, busBox, routeBox, new Separator(), dateTimeBox);
        return card;
    }

    private void loadSchedules() {
        try {
            if (scheduleCardsPane == null) return;
            scheduleCardsPane.getChildren().clear();

            // Get all schedules for the student
            ObservableList<Schedule> schedules = scheduleDAO.getStudentSchedules(currentStudent.getId());

            for (Schedule schedule : schedules) {
                scheduleCardsPane.getChildren().add(createScheduleCard(schedule));
            }

            if (schedules.isEmpty()) {
                Label emptyLabel = new Label("No schedules available.\nPlease contact admin for route and bus assignment.");
                emptyLabel.setWrapText(true);
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20; -fx-text-alignment: center;");
                scheduleCardsPane.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            System.err.println("Failed to load schedules: " + e.getMessage());
        }
    }

    @FXML private void handleRefreshSchedule(ActionEvent event) {
        loadSchedules();
    }

    //Requests

    private VBox createRequestCard(Request request) {
        VBox card = new VBox(12);
        card.getStyleClass().add("data-card");

        // Status color coding
        String statusColor = "#f39c12"; // Pending - orange
        if (request.getStatus() == RequestStatus.APPROVED) {
            statusColor = "#27ae60"; // Green
        } else if (request.getStatus() == RequestStatus.REJECTED) {
            statusColor = "#e74c3c"; // Red
        }

        // Status badge
        Label statusLabel = new Label(request.getStatus().toString());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-background-radius: 10; -fx-padding: 5 12; -fx-font-weight: bold;");

        // Type label
        Label typeLabel = new Label(request.getType());
        typeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Description
        Label descLabel = new Label(request.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(260);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        // Date - using request ID as placeholder since createdAt might not exist
        Label dateLabel = new Label("Request ID: " + request.getRequestId());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");

        card.getChildren().addAll(statusLabel, typeLabel, new Separator(), descLabel, dateLabel);
        return card;
    }

    @FXML private void handleSubmitRequest(ActionEvent event) {
        // Use FXML form fields directly - no dialog needed
        String type = requestTypeCombo != null ? requestTypeCombo.getValue() : null;
        String description = requestDescriptionArea != null ? requestDescriptionArea.getText().trim() : "";

        // Validation
        if (type == null || type.isEmpty()) {
            showError("Please select a request type!");
            return;
        }

        if (description.isEmpty()) {
            showError("Please enter a description!");
            return;
        }

        try {
            // Submit request
            requestDAO.addRequest(currentStudent.getStudentId(), type, description);

            // Log action
            Logger.getInstance().studentAction(currentStudent.getUsername(),
                    "Submitted request: " + type);

            // Clear form
            if (requestTypeCombo != null) {
                requestTypeCombo.setValue(null);
            }
            if (requestDescriptionArea != null) {
                requestDescriptionArea.clear();
            }

            // Show success and refresh
            showSuccess("Request submitted successfully! Admin will review it.");
            loadRequests();

        } catch (Exception e) {
            showError("Failed to submit request: " + e.getMessage());
        }
    }

    @FXML private void handleRefreshRequests(ActionEvent event) {
        loadRequests();
    }

    private void loadRequests() {
        try {
            if (requestsCardsPane == null) return;
            requestsCardsPane.getChildren().clear();

            ObservableList<Request> requests = requestDAO.getRequestsByStudentId(currentStudent.getStudentId());
            for (Request request : requests) {
                requestsCardsPane.getChildren().add(createRequestCard(request));
            }

            if (requests.isEmpty()) {
                Label emptyLabel = new Label("No requests submitted yet.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
                requestsCardsPane.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            showError("Failed to load requests: " + e.getMessage());
        }
    }

    //Messages

    private VBox createMessageCard(Message message) {
        VBox card = new VBox(10);
        card.getStyleClass().add("data-card");

        // Message icon
        Label icon = new Label("📬");
        icon.setStyle("-fx-font-size: 28px;");

        // Date/time
        Label dateLabel = new Label(message.getMessageDate() != null ? message.getMessageDate().toString() : "");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // Content
        Label contentLabel = new Label(message.getMessage());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(280);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // Read status
        if (!message.isRead()) {
            Label unreadBadge = new Label("NEW");
            unreadBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: #e74c3c; -fx-background-radius: 8; -fx-padding: 3 8; -fx-font-weight: bold;");
            card.getChildren().add(unreadBadge);
        }

        card.getChildren().addAll(icon, dateLabel, new Separator(), contentLabel);
        return card;
    }

    private void loadMessages() {
        try {
            if (messagesCardsPane == null) return;
            messagesCardsPane.getChildren().clear();

            ObservableList<Message> messages = messageDAO.getMessagesByUserId(currentStudent.getId());

            // Count unread
            long unreadCount = messages.stream().filter(m -> !m.isRead()).count();
            if (unreadCountLabel != null) {
                unreadCountLabel.setText(unreadCount + " unread");
            }

            for (Message message : messages) {
                messagesCardsPane.getChildren().add(createMessageCard(message));
            }

            if (messages.isEmpty()) {
                Label emptyLabel = new Label("No messages yet.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-padding: 20;");
                messagesCardsPane.getChildren().add(emptyLabel);
            }

            // Mark messages as read
            if (!messages.isEmpty()) {
                for (Message msg : messages) {
                    if (!msg.isRead()) {
                        messageDAO.markAsRead(msg.getMessageId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load messages: " + e.getMessage());
        }
    }

    @FXML private void handleRefreshMessages(ActionEvent event) {
        loadMessages();
    }

    private void startMessageRefreshThread() {
        messageRefreshThread = new Thread(() -> {
            while (keepRefreshing) {
                try {
                    Thread.sleep(10000); // Sleep for 10 seconds

                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            int unreadCount = messageDAO.getUnreadMessageCount(currentStudent.getId());
                            if (unreadCount > 0 && unreadCountLabel != null) {
                                unreadCountLabel.setText(unreadCount + " unread");
                                unreadCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                loadMessages(); // Refresh messages
                            }
                        } catch (Exception e) {
                            // Silent fail for background refresh
                        }
                    });
                } catch (InterruptedException e) {
                    break; // Exit thread if interrupted
                }
            }
        });

        messageRefreshThread.setDaemon(true); // Make it a daemon thread
        messageRefreshThread.start();
        Logger.getInstance().info("Message auto-refresh thread started for student: " + currentStudent.getStudentId());
    }

    private void stopMessageRefreshThread() {
        keepRefreshing = false;
        if (messageRefreshThread != null) {
            messageRefreshThread.interrupt();
        }
        Logger.getInstance().info("Message auto-refresh thread stopped for student: " + currentStudent.getStudentId());
    }

    //Lggout

    @FXML private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Logout");
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Stop message refresh thread
                stopMessageRefreshThread();

                Logger.getInstance().studentAction(currentStudent.getUsername(), "Logged out");
                SessionManager.getInstance().logout();
                SceneManager.switchScene("/com/example/kuet_transportation_and_schedueling_system/view/Login.fxml",
                        "Login");
            } catch (IOException e) {
                showError("Failed to logout: " + e.getMessage());
            }
        }
    }

    //Utility Methods

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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        stopMessageRefreshThread();
    }
}

