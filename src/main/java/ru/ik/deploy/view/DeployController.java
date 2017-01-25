package ru.ik.deploy.view;

import java.awt.Desktop;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.SelectionMode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javafx.scene.control.Alert;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import ru.ik.deploy.AppPreferences;
import ru.ik.deploy.DeployFileGenerator;

public class DeployController implements Observer {

    @FXML
    private TextArea patchList;
    @FXML
    private ListView cloneList;
    @FXML
    private CheckBox needAdcgnjar;
    @FXML
    private CheckBox needOacorerestart;
    @FXML
    private Button generateDeployData;
    @FXML
    private Button deployToInstallQueue;
    @FXML
    private TextArea deployFileContent;
    @FXML
    private TextField deployFileName;
    @FXML
    private CheckBox skipUsePatchPath;

    @FXML
    private void initialize() {
        initializeClonesList();
    }

    private void initializeClonesList() {
        String clonesList = AppPreferences.getInstance().get(AppPreferences.CLONES_LIST);
        if (clonesList != null) {
            ObservableList<String> cloneNames = FXCollections.observableArrayList(clonesList.split(","));
            cloneList.setItems(cloneNames);
        }
        cloneList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void handleGenerateDeployData() {
        DeployFileGenerator generator = new DeployFileGenerator();
        generator.setPatchList(patchList.getText().split("\n"));
        generator.setSkipUsePatchPath(skipUsePatchPath.isSelected());
        generator.setNeedAdcgnjar(needAdcgnjar.isSelected());
        generator.setNeedOacoreRestart(needOacorerestart.isSelected());
        generator.setCloneList(getCloneList());
        generator.setSinglePatchInstallTimeout(1000);
        String username = System.getProperty("user.name");
        generator.setEmailCC(username.concat("@").concat(AppPreferences.getInstance().get(AppPreferences.DOMAIN_NAME)));
        generator.setGenuineCommiter(username);
        generator.generate();
        deployFileName.setText(generator.getDeployFileName());
        deployFileContent.setText(generator.getDeployFileContent());
    }

    @FXML
    private void handleDeployToInstallQueue() throws IOException {
        String deployPath = AppPreferences.getInstance().get(AppPreferences.DEPLOY_PATH);
        if (deployPath == null || deployPath.trim().length() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Deployment path not specified.");
            alert.setContentText("Set deployment path in preferences.");
            alert.showAndWait();
            return;
        }
        if (!deployPath.endsWith("/") && !deployPath.endsWith("\\")) {
            deployPath += File.separator;
        }
        String path = deployPath + deployFileName.getText();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path))) {
            w.write(deployFileContent.getText());
            w.flush();
        }
        Desktop.getDesktop().open(new File(deployPath));
    }

    private String getCloneList() {
        ObservableList<String> selectedItems = cloneList.getSelectionModel().getSelectedItems();
        StringBuilder sb = new StringBuilder();
        selectedItems.forEach((o) -> {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(o);
        });
        return sb.toString();
    }

    @Override
    public void update(Observable o, Object arg) {
        initializeClonesList();
    }
    
    @FXML
    public void handlePatchListDragOver(DragEvent event) {
          /* data is dragged over the target */
          /* accept it only if it is not dragged from the same node 
           * and if it has a string data */
          if (event.getGestureSource() != patchList &&
                  event.getDragboard().hasFiles()) {
              /* allow for moving */
              event.acceptTransferModes(TransferMode.ANY);
          }
          
          event.consume();
      }
    
    @FXML
    public void handlePatchListDragEntered(DragEvent event) {
        /* the drag-and-drop gesture entered the target */
        /* show to the user that it is an actual gesture target */
        if (event.getGestureSource() != patchList
                && event.getDragboard().hasFiles()) {
        }

        event.consume();
    }
    
    @FXML
    public void handlePatchListDragExited(DragEvent event) {
          /* mouse moved away, remove the graphical cues */
          event.consume();
      }
    
    @FXML
    public void handlePatchListDragDropped(DragEvent event) throws IOException {
        System.out.println("drag dropped");
        /* data dropped */
        /* if there is a string data on dragboard, read it and use it */
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            for (File f : db.getFiles()) {
                if (patchList.getLength() > 0) {
                    patchList.appendText("\n");
                }
                patchList.appendText(f.getCanonicalPath());
            }
            success = true;
        }
        /* let the source know whether the string was successfully 
         * transferred and used */
        event.setDropCompleted(success);

        event.consume();
    }

}
