package ru.ik.deploy.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.*;
import javafx.stage.*;
import java.io.IOException;
import javafx.scene.layout.*;
import javafx.fxml.*;

public class RootLayoutController {

  @FXML
  private void handleFileClose() {
    System.exit(0);
  }

  @FXML
  private void handleHelpAbout() {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("About");
    alert.setHeaderText("Deploy queue GUI");
    alert.setContentText("alexzaikin, 2016-2017");

    alert.showAndWait();
  }

  @FXML
  private void handleEditPreferences() throws IOException {
    try {
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("AppPreferences.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Preferences");
        dialogStage.initModality(Modality.WINDOW_MODAL);
//        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();

    } catch (IOException e) {
        e.printStackTrace();
    }
  }

}

