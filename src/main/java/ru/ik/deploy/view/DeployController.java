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
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import ru.ik.deploy.AppPreferences;
import ru.ik.deploy.DeployFileGenerator;
import javafx.scene.control.SplitMenuButton;

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
    private SplitMenuButton addToSvn;            

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
        String domainName = AppPreferences.getInstance().get(AppPreferences.DOMAIN_NAME);
        if (domainName == null || domainName.trim().length() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Domain name (for email) not specified in preferences.");
            alert.setContentText("Specify domain name (for email) in preferences.");
            alert.showAndWait();
            return;
        }        
        
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
        String filename = generator.getDeployFileName();
        // Validate filename and possibly truncate
        String deployPath = AppPreferences.getInstance().get(AppPreferences.DEPLOY_PATH);
        if (!deployPath.endsWith("/") && !deployPath.endsWith("\\")) {
            deployPath += File.separator;
        }
        String path = deployPath + filename;
        int length = path.length();
        if (length > 255) {
            int maxFilenameLength = 255 - deployPath.length();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Full path exceeds 255 characters.");
            alert.setContentText("Truncate filename to \"" + filename.substring(0, maxFilenameLength) + "\"?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                filename = filename.substring(0, maxFilenameLength);
            }
        }
        deployFileName.setText(filename);
        deployFileContent.setText(generator.getDeployFileContent());
    }
    
    @FXML
    private void handleCommitToSvn() throws IOException, InterruptedException {
        handleAddToSvn();
        String svnExePath = AppPreferences.getInstance().get(AppPreferences.SVN_EXE_PATH);
        Runtime runtime = Runtime.getRuntime();
        String deployPath = AppPreferences.getInstance().get(AppPreferences.DEPLOY_PATH);
        String filename = deployFileName.getText(); 
        Process process = runtime.exec(new String[] {svnExePath, "ci", "-m", "\"" + filename + "\"", "--force-log", filename}, 
                null, 
                new File(deployPath));
        int exitValue = process.waitFor();
        if (exitValue != 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Svn commit failed.");
            alert.setContentText("Failed to commit to svn.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleAddToSvn() throws IOException, InterruptedException {
        String svnExePath = AppPreferences.getInstance().get(AppPreferences.SVN_EXE_PATH);
        if (svnExePath == null || svnExePath.trim().length() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Path to svn executable not given in preferences.");
            alert.setContentText("Specify path to svn executable in preferences.");
            alert.showAndWait();
            return;
        }   
        handleDeployToInstallQueue();
        Runtime runtime = Runtime.getRuntime();
        String deployPath = AppPreferences.getInstance().get(AppPreferences.DEPLOY_PATH);
        Process process = runtime.exec(
                new String[] {svnExePath, "add", deployFileName.getText()}, 
                null, 
                new File(deployPath));
        int exitValue = process.waitFor();
        if (exitValue != 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Svn add failed.");
            alert.setContentText("Failed to add to svn.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeployToInstallQueue() throws IOException {
        String filename = deployFileName.getText();
        if (filename == null || filename.trim().length() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Filename missing.");
            alert.setContentText("Supply filename.");
            alert.showAndWait();
            deployFileName.requestFocus();
            return;
        }        
        
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
        String path = deployPath + filename;
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
