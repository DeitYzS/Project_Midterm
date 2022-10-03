package archiver.controller;

import archiver.model.fileZip;
import archiver.model.threadFile;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;


public class MainViewController {
    LinkedHashMap<String, String> fileMap  = new LinkedHashMap<>();
    ArrayList<File> files = new ArrayList<>();
    int isSelected = 0;
    @FXML private ImageView image;
    @FXML private ListView<String> inputListView;
    @FXML private Button startButton;
    @FXML private Button extractButton;
    @FXML private Button clearButton;
    @FXML private Button zipButton;
    @FXML private Button rarButton;
    @FXML private Button tarButton;
    @FXML private MenuItem Close;
    @FXML private TextField inputFileName;
    @FXML private TextField password;
    @FXML private TextField Repassword;
    @FXML private Pane verificationPane;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Pane authenticationPane;
    @FXML private Button submitButton1;
    @FXML private Button cancelButton1;
    @FXML private TextField password1;
    @FXML private AnchorPane mainPane;
    @FXML private Rectangle overlay;
    Map<File, String> pass = new HashMap<>();


    public MainViewController() {

    }


    @FXML
    public void initialize() {
        inputFileName.setPromptText("Enter File Name");

        inputListView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        inputListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath;
                int total_files = db.getFiles().size();
                for (int i = 0; i < total_files; i++) {
                    File file = db.getFiles().get(i);
                    filePath = file.getAbsolutePath();
                    inputListView.getItems().add(file.getName());
                    fileMap.put(file.getName(), filePath);
                    files.add(file);
                }

                event.setDropCompleted(success);
                event.consume();
            }
            inputFileName.getText();
        });

        submitButton.setOnAction(event1 -> {
                    DirectoryChooser dc = new DirectoryChooser();
                    File destFile = dc.showDialog(new Stage());


                        if (!files.isEmpty()) {
                            if (password.getText().length() >= 2) {
                                if (password.getText().equals(Repassword.getText())) {
                                    ZipParameters zipParameters = new ZipParameters();
                                    zipParameters.setEncryptFiles(true);
                                    zipParameters.setEncryptionMethod(EncryptionMethod.AES);
                                    zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

                                    fileZip t2 = new fileZip(destFile.getAbsoluteFile(), inputFileName.getText(), isLast(isSelected), password.getText().toCharArray(), files);
                                    t2.fileCompress();

                                } else {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setContentText("Password do not matching");
                                    alert.showAndWait();
                                }
                            } else if (password.getText().equals("") && Repassword.getText().equals("")) {
                                fileZip t1 = new fileZip(destFile.getAbsoluteFile(), inputFileName.getText(), isLast(isSelected), files);
                                t1.fileCompressWithoutPassword();
                            }else {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setContentText("The password should be more.");
                                alert.showAndWait();
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setContentText("File Not Found!!");
                            alert.showAndWait();
                        }


            password.clear();
            Repassword.clear();
            authenticationPane.setVisible(false);
            overlay.setVisible(false);
        });

        extractButton.setOnAction(event -> {
            DirectoryChooser dc = new DirectoryChooser();
            File destfile = dc.showDialog(new Stage());
            password1.clear();
            //Stream
                     files.stream()
                             .filter(file -> file.getAbsolutePath().endsWith("rar"))
                             .filter(file -> file.getAbsolutePath().endsWith("zip"))
                             .filter(file -> file.getAbsolutePath().endsWith("tar"));


                     for (int i=0; i<files.size();i++) {
                         try {
                             if (new ZipFile(files.get(i).getAbsolutePath()).isEncrypted()) {
                                 TextInputDialog dialog = new TextInputDialog();
                                 dialog.setTitle("Password :"+ files.get(i).getName());
                                 dialog.setContentText("Password :");
                                 dialog.setHeaderText(null);
                                 dialog.setGraphic(null);
                                 Optional<String> popupPassword = dialog.showAndWait();

                                 if (popupPassword.isPresent()){
                                     pass.put(files.get(i), popupPassword.get());
                                 } else {
                                     Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                     alert.setContentText("File Not Found!!");
                                     alert.showAndWait();
                                 }
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }

            int lastIndex = files.size();
            files.subList(0, lastIndex);

                    ExecutorService executorService = Executors.newFixedThreadPool(2);
                    try {
                        threadFile t1 = new threadFile(files.subList(0, lastIndex / 2), destfile, pass);
                        threadFile t2 = new threadFile(files.subList(lastIndex / 2, lastIndex), destfile, pass);
                        executorService.submit(t1);
                        executorService.submit(t2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        executorService.shutdown();
                    }
        });

        submitButton1.setOnAction(event1 -> {
            verificationPane.setVisible(false);
            overlay.setVisible(false);
        });


        clearButton.setOnAction(event -> {
            inputListView.getItems().clear();
            files = new ArrayList<>();
        });

        zipButton.setOnMouseClicked(event -> {
            isSelected = 0;
            zipButton.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            rarButton.setBorder(null);
            tarButton.setBorder(null);
            zipButton.setOpacity(0.3);
            rarButton.setOpacity(1);
            tarButton.setOpacity(1);
        });

        rarButton.setOnMouseClicked(event -> {
            isSelected = 1;
            rarButton.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            zipButton.setBorder(null);
            tarButton.setBorder(null);
            rarButton.setOpacity(0.3);
            zipButton.setOpacity(1);
            tarButton.setOpacity(1);
        });

        tarButton.setOnMouseClicked(event -> {
            isSelected = 2;
            tarButton.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            zipButton.setBorder(null);
            rarButton.setBorder(null);
            tarButton.setOpacity(0.3);
            rarButton.setOpacity(1);
            zipButton.setOpacity(1);
        });

        Close.setOnAction(event -> {
            System.exit(0);
        });

        cancelButton.setOnAction(event -> {
            authenticationPane.setVisible(false);
            overlay.setVisible(false);
            mainPane.setOpacity(1);
        });

        startButton.setOnAction(event -> {
            authenticationPane.setVisible(true);
            overlay.setVisible(true);
            mainPane.setOpacity(0.8);
        });

        cancelButton1.setOnAction(event -> {
            verificationPane.setVisible(false);
            overlay.setVisible(false);
            mainPane.setOpacity(1);
        });

    }

    public String isLast(int isSelected){
        if(isSelected == 0){
            return ".zip";
        } else if (isSelected == 1) {
            return ".rar";
        } else if (isSelected == 2) {
            return ".tar";
        }
        return null;
    }
}

