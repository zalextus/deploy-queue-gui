package ru.ik.deploy.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.ik.deploy.AppPreferences;

public class AppPreferencesController {

  @FXML private TextField deployFileDestination;
  @FXML private TextField clonesList;
  @FXML private TextField domainName;
  @FXML private TextField svnExePath;

  @FXML
  private void handleOkButton() {
    AppPreferences pref = AppPreferences.getInstance();
    pref.put(AppPreferences.DEPLOY_PATH, deployFileDestination.getText());
    pref.put(AppPreferences.CLONES_LIST, clonesList.getText());
    pref.put(AppPreferences.DOMAIN_NAME, domainName.getText());
    pref.put(AppPreferences.SVN_EXE_PATH, svnExePath.getText());
    pref.save();
    Stage stage = (Stage)deployFileDestination.getScene().getWindow();
    stage.close();
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
    clonesList.setText(pref.get(AppPreferences.CLONES_LIST));
    domainName.setText(pref.get(AppPreferences.DOMAIN_NAME));
    svnExePath.setText(pref.get(AppPreferences.SVN_EXE_PATH));
  }

}

