package com.example.kuet_transportation_and_schedueling_system;
import com.example.kuet_transportation_and_schedueling_system.dao.UserDAO;
import com.example.kuet_transportation_and_schedueling_system.db.DBConnection;
import com.example.kuet_transportation_and_schedueling_system.util.Logger;
import com.example.kuet_transportation_and_schedueling_system.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class HelloApplication extends Application {

    @Override public void start(Stage stage) throws IOException {
        SceneManager.setPrimaryStage(stage);

        initializeDatabase();

        FXMLLoader fxmlLoader = new FXMLLoader(
            HelloApplication.class.getResource("/com/example/kuet_transportation_and_schedueling_system/view/Login.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("KUET Transportation System - Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreen(false); // Use maximized instead of fullscreen for better usability
        stage.show();

        Logger.getInstance().info("Application started successfully");
    }

    private void initializeDatabase() {
        try {
            // Test database connection
            if (DBConnection.testConnection()) {
                System.out.println("✓ Database connection successful!");

                // Create default admin if not exists
                UserDAO userDAO = new UserDAO();
                userDAO.createDefaultAdmin();

                Logger.getInstance().info("Database initialized successfully");
            } else {
                System.err.println("✗ Database connection failed!");
                Logger.getInstance().error("Database connection failed");
                showDatabaseWarning();
            }
        } catch (SQLException e) {
            System.err.println("\n========================================");
            System.err.println("   DATABASE CONNECTION ERROR");
            System.err.println("========================================");
            System.err.println("MySQL Server is not running!");
            System.err.println("\nTo fix this:");
            System.err.println("1. Start MySQL Server (using XAMPP, MySQL Workbench, or Services)");
            System.err.println("2. Open MySQL and run: database_schema.sql");
            System.err.println("3. Restart this application");
            System.err.println("========================================\n");
            Logger.getInstance().error("Database not available: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            Logger.getInstance().error("Database initialization error: " + e.getMessage());
        }
    }

    private void showDatabaseWarning() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Database Connection Warning");
            alert.setHeaderText("MySQL Server Not Running");
            alert.setContentText("The application started but cannot connect to MySQL.\n\n" +
                               "Please:\n" +
                               "1. Start MySQL Server\n" +
                               "2. Create database using database_schema.sql\n" +
                               "3. Restart the application\n\n" +
                               "You can still see the UI, but login won't work.");
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        Logger.getInstance().info("Application closed");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
