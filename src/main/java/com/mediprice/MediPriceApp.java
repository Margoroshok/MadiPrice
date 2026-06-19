package com.mediprice;

import com.mediprice.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for MediPrice JavaFX application.
 */
public class MediPriceApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MediPriceApp.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database on startup
            DatabaseConnection.initializeDatabase();

            // Load the main login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("MediPrice — Medicine Price Comparison");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(550);
            primaryStage.show();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
        LOGGER.info("Application stopped.");
    }

    /**
     * Launches the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
