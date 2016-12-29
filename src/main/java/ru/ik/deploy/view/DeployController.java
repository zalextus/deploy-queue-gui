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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.scene.control.Alert;
import ru.ik.deploy.AppPreferences;

public class DeployController {

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
    private void initialize() {
        String clonesList = AppPreferences.getInstance().get(AppPreferences.CLONES_LIST);
        if (clonesList != null) {
            ObservableList<String> cloneNames = FXCollections.observableArrayList(clonesList.split(","));
            cloneList.setItems(cloneNames);
        }
        cloneList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void handleGenerateDeployData() {
        String[] patchListArray = patchList.getText().split("\n");
        String deployFileNameText = "task_" + getOEBSList(patchListArray) + "_" + getCurrentDateFormatted();
        deployFileName.setText(deployFileNameText);

        StringBuilder sb = new StringBuilder("[patch_deploy]\n");
        boolean usePatchPath = getUsePatchPath(patchListArray);
        if (usePatchPath) {
            sb.append("use_patch_path = True\n");
        }
        sb.append("installorder = ").append(getPatchList(patchListArray));
        if (needAdcgnjar.isSelected()) {
            sb.append(",adcgnjar");
        }
        if (needOacorerestart.isSelected()) {
            sb.append(",oacorereload");
        }
        sb.append("\n");
        String cloneListStr = getCloneList();
        sb.append("installto = ").append(cloneListStr).append("\n");
        sb.append("single_patch_install_timeout = 1000\n");
        sb.append("patch_deploy_description = '").append(deployFileNameText).append(" ").append(cloneListStr).append("'\n");
        String username = System.getProperty("user.name");
        sb.append("email_cc = ").append(username).append("@yandex-team.ru\n");
        sb.append("genuine_commiter = ").append(username).append("\n");
        deployFileContent.setText(sb.toString());
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

    private String getPatchList(String[] patchListArray) {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile(".*(trunk/.*sh).*");
        for (String patch : patchListArray) {
            Matcher m = p.matcher(patch.replace('\\', '/'));
            if (m.matches()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(m.group(1));
            }
        }
        return sb.toString();
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

    private boolean getUsePatchPath(String[] patchListArray) {
        for (String patch : patchListArray) {
            if (patch.contains("PRODUCT")) {
                return true;
            }
        }

        return false;
    }

    private String getOEBSList(String[] patchListArray) {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile(".*(OEBS-\\d+).*");

        for (String patch : patchListArray) {
            Matcher m = p.matcher(patch);
            if (m.matches()) {
                if (sb.length() > 0) {
                    sb.append("_");
                }
                sb.append(m.group(1));
            }
        }

        return sb.toString();
    }

    private String getCurrentDateFormatted() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmss");
        return dateFormat.format(new Date());
    }

}
