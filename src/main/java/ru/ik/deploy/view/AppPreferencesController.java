package ru.ik.deploy.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.ik.deploy.AppPreferences;

public class AppPreferencesController {

  @FXML private TextField deployFileDestination;

  @FXML
  private void handleOkButton() {
    Stage stage = (Stage)deployFileDestination.getScene().getWindow();
    stage.close();
    AppPreferences pref = AppPreferences.getInstance();
    pref.put(AppPreferences.DEPLOY_PATH, deployFileDestination.getText());
    pref.save();
  }

  @FXML
  private void handleCancelButton() {
    Stage stage = (Stage)deployFileDestination.getScene().getWindow();
    stage.close();
  }

  @FXML
  private void initialize() {
    AppPreferences pref = AppPreferences.getInstance();
    deployFileDestination.setText(pref.get(AppPreferences.DEPLOY_PATH));
  }

}

