package network.pkgswitch.management.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import network.pkgswitch.management.system.models.NetworkSwitch;
import network.pkgswitch.management.system.services.NetworkSwitchServices;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class NetworkSwitchManagementDocumentController implements Initializable
{
    private ArrayList<NetworkSwitch> alNetworkSwitches;

    @FXML
    private Label lblWelcome;

    @FXML
    private TableView tblNetworkSwitches;

    @FXML
    private TextField txtName, txtIPAddress, txtMACAddress;

    @FXML
    private Label lblStatus;

    @FXML
    private Button btnCheckStatus, btnCheckStatusOfAllNetworkSwitches, btnExportToExcel;

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );

        btnReloadFromDatabaseAction ( null );
    }

    private TableColumn<NetworkSwitch, String> createEditableTableColumns ( final String title, final String tableName )
    {
        final TableColumn<NetworkSwitch, String> tableColumn = new TableColumn<> ( title );

        tableColumn.setStyle ( "-fx-alignment: CENTER;" );
        tableColumn.setCellValueFactory ( new PropertyValueFactory<> ( tableName ) );
        tableColumn.setCellFactory ( TextFieldTableCell.forTableColumn() );

        return tableColumn;
    }

    private void updateNetworkSwitchesTable ( final ObservableList<NetworkSwitch> tvObservableList )
    {
        final TableColumn<NetworkSwitch, Long> colNo = new TableColumn<> ( "No" );
        colNo.setStyle ( "-fx-alignment: CENTER;" );
        colNo.setCellValueFactory ( new PropertyValueFactory<> ( "no" ) );

        final TableColumn<NetworkSwitch, String> colName = createEditableTableColumns ( "Name", "name" );
        colName.setOnEditCommit ( new EventHandler<CellEditEvent<NetworkSwitch, String>>()
        {
            @Override
            public void handle ( CellEditEvent<NetworkSwitch, String> event )
            {
                
                final NetworkSwitch networkSwitch =
                    event.getTableView().getItems().get ( event.getTablePosition().getRow() );

                networkSwitch.setName ( event.getNewValue() );
            }
        } );
        
        final TableColumn<NetworkSwitch, String> colIpAddress = createEditableTableColumns ( "IP Address", "ipAddress" );
        colIpAddress.setOnEditCommit ( new EventHandler<CellEditEvent<NetworkSwitch, String>>()
        {
            @Override
            public void handle ( CellEditEvent<NetworkSwitch, String> event )
            {
                
                final NetworkSwitch networkSwitch =
                    event.getTableView().getItems().get ( event.getTablePosition().getRow() );

                networkSwitch.setIpAddress ( event.getNewValue() );
            }
        } );

        final TableColumn<NetworkSwitch, String> colMacAddress = createEditableTableColumns ( "MAC Address", "macAddress" );
        colMacAddress.setOnEditCommit ( new EventHandler<CellEditEvent<NetworkSwitch, String>>()
        {
            @Override
            public void handle ( CellEditEvent<NetworkSwitch, String> event )
            {
                
                final NetworkSwitch networkSwitch =
                    event.getTableView().getItems().get ( event.getTablePosition().getRow() );

                networkSwitch.setMacAddress ( event.getNewValue() );
            }
        } );

        final TableColumn<NetworkSwitch, String> colStatus = new TableColumn<> ( "Status" );
        colStatus.setStyle ( "-fx-alignment: CENTER;" );
        colStatus.setCellValueFactory ( new PropertyValueFactory<> ( "status" ) );

        tblNetworkSwitches.getColumns().clear();
        tblNetworkSwitches.getColumns().addAll ( colNo, colName, colIpAddress, colMacAddress, colStatus );

        tblNetworkSwitches.setItems ( tvObservableList );

        final TableColumn<NetworkSwitch, Void> colAction = new TableColumn ( "Action" );

        final Callback<TableColumn<NetworkSwitch, Void>, TableCell<NetworkSwitch, Void>> cellFactory =
                new Callback<TableColumn<NetworkSwitch, Void>, TableCell<NetworkSwitch, Void>>()
        {
            @Override
            public TableCell<NetworkSwitch, Void> call ( final TableColumn<NetworkSwitch, Void> param )
            {
                final TableCell<NetworkSwitch, Void> cell = new TableCell<NetworkSwitch, Void>()
                {
                    private final Button btnCheckStatus = new Button ( "Check Status" );
                    {
                        btnCheckStatus.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final NetworkSwitch networkSwitch = getTableView().getItems().get ( getIndex() );
                            final String ipAddress = networkSwitch.getIpAddress();

                            btnCheckStatus.setDisable ( true );

                            // use another thread to prevent blocking GUI
                            new Thread ( () ->
                            {
                                networkSwitch.setStatus ( Utils.pingNetworkSwitch ( ipAddress ) );
                                getTableView().getItems().set ( getIndex(), networkSwitch );

                                // update GUI using fx thread
                                Platform.runLater ( () -> btnCheckStatus.setDisable ( false ) );
                            } ).start();
                        } );
                    }

                    private final Button btnUpdate = new Button ( "Update" );
                    {
                        btnUpdate.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final NetworkSwitch networkSwitch = getTableView().getItems().get ( getIndex() );

                            try
                            {
                                NetworkSwitchServices.updateNetworkSwitch ( networkSwitch );
                                Utils.getAlert ( Alert.AlertType.INFORMATION, "Network switch information updated!",
                                    "Network switch information updated successfully to database!" ).showAndWait();
                            }
                            catch ( final Exception e )
                            {
                                e.printStackTrace();

                                Utils.getAlert ( Alert.AlertType.ERROR, "Error updating network switch!",
                                    "Unable to update network switch to database: " + e.getMessage() ).showAndWait();
                            }
                        } );
                    }

                    private final Button btnDelete = new Button ( "Delete" );
                    {
                        btnDelete.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final NetworkSwitch networkSwitch = getTableView().getItems().get ( getIndex() );

                            try
                            {
                                NetworkSwitchServices.deleteNetworkSwitch ( networkSwitch.getId() );

                                Utils.getAlert ( Alert.AlertType.INFORMATION, "Network switch deleted!",
                                    "Network switch deleted successfully from database!" ).showAndWait();

                                btnReloadFromDatabaseAction ( event );
                            }
                            catch ( final Exception e )
                            {
                                e.printStackTrace();

                                Utils.getAlert ( Alert.AlertType.ERROR, "Error deleting network switch!",
                                    "Unable to delete network switch from database: " + e.getMessage() ).showAndWait();
                            }
                        } );
                    }

                    private final HBox pane = new HBox ( btnCheckStatus, new Label ( " " ), btnUpdate, new Label ( " " ), btnDelete );
                    {
                        pane.setAlignment ( Pos.CENTER );
                    }

                    @Override
                    public void updateItem ( final Void item, final boolean isEmpty )
                    {
                        super.updateItem ( item, isEmpty );
                        setGraphic ( isEmpty ? null : pane );
                    }
                };

                return cell;
            }
        };

        colAction.setCellFactory ( cellFactory );
        tblNetworkSwitches.getColumns().add ( colAction );

        tblNetworkSwitches.setEditable ( true );
    }

    @FXML
    private void btnCheckStatusAction ( final ActionEvent event )
    {
        final String ipAddress = txtIPAddress.getText().trim();

        if ( ipAddress.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No IP address entered!",
                "Please enter IP address to check status for!" ).showAndWait();
            return;
        }

        btnCheckStatus.setDisable ( true );

        // use another thread to prevent blocking GUI
        new Thread ( () ->
        {
            // update GUI using fx thread
            Platform.runLater ( () ->
            {
                lblStatus.setText ( Utils.pingNetworkSwitch ( ipAddress ) );
                btnCheckStatus.setDisable ( false );
            } );
        } ).start();
    }

    @FXML
    private void btnSaveNetworkSwitchAction ( final ActionEvent event )
    {
        final String name = txtName.getText().trim();

        if ( name.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No name entered!",
                "Please enter name of network switch to create!" ).showAndWait();
            return;
        }

        final String ipAddress = txtIPAddress.getText().toString();

        if ( ipAddress.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No IP address entered!",
                "Please enter IP address of network switch to create!" ).showAndWait();
            return;
        }

        final String macAddress = txtMACAddress.getText().trim();

        if ( macAddress.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No MAC address entered!",
                "Please enter MAC address of network switch to create!" ).showAndWait();
            return;
        }

        try
        {
            if ( NetworkSwitchServices.addNetworkSwitch ( name, ipAddress, macAddress, Data.username ) )
            {
                Utils.getAlert ( Alert.AlertType.INFORMATION, "Network switch added to database!",
                    "Network switch successfully added to database!" ).showAndWait();
            }
            else
            {
                Utils.getAlert ( Alert.AlertType.ERROR, "Duplicate network switch detected!",
                    "An existing network switch exist in the database with the same name/IP adress/MAC address!" ).showAndWait();
            }
        }
        catch ( final Exception e )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "Unable to add network switch to database!",
                "Error adding network switch to database: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnReloadFromDatabaseAction ( final ActionEvent event )
    {
        final ObservableList<NetworkSwitch> tvObservableList = FXCollections.observableArrayList();

        try
        {
            alNetworkSwitches = NetworkSwitchServices.readNetworkSwitches();
            tvObservableList.addAll ( alNetworkSwitches.toArray ( new NetworkSwitch [ alNetworkSwitches.size() ] ) );
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }

        updateNetworkSwitchesTable ( tvObservableList );
    }

    @FXML
    private void btnCheckStatusOfAllNetworkSwitchesAction ( final ActionEvent event )
    {
        btnCheckStatusOfAllNetworkSwitches.setDisable ( true );

        for ( final NetworkSwitch networkSwitch : alNetworkSwitches )
            networkSwitch.setStatus ( "---" );

        final ObservableList<NetworkSwitch> tvObservableList = FXCollections.observableArrayList();
        tvObservableList.addAll ( alNetworkSwitches.toArray ( new NetworkSwitch [ alNetworkSwitches.size() ] ) );
        updateNetworkSwitchesTable ( tvObservableList );

        new Thread ( () ->
        {
            final ArrayList<Thread> alThreads = new ArrayList<>();

            for ( final NetworkSwitch networkSwitch : alNetworkSwitches )
            {
                // use another thread to prevent blocking GUI
                final Thread thread = new Thread ( () ->
                {
                    networkSwitch.setStatus ( Utils.pingNetworkSwitch ( networkSwitch.getIpAddress() ) );

                    // update GUI using fx thread
                    Platform.runLater ( () ->
                    {
                        final ObservableList<NetworkSwitch> tvObservableListCurrent = FXCollections.observableArrayList();
                        tvObservableListCurrent.addAll ( alNetworkSwitches.toArray ( new NetworkSwitch [ alNetworkSwitches.size() ] ) );
                        updateNetworkSwitchesTable ( tvObservableListCurrent );
                    } );
                } );

                alThreads.add ( thread );

                thread.start();
            }

            for ( final Thread thread : alThreads )
                try { thread.join(); } catch ( final InterruptedException e ) {}

            btnCheckStatusOfAllNetworkSwitches.setDisable ( false );
        } ).start();
    }

    @FXML
    private void btnExportToExcelAction ( final ActionEvent event )
    {
        final ObservableList<NetworkSwitch> tvObservableList = tblNetworkSwitches.getItems();

        if ( tvObservableList.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No saved network switches in database!",
                "No saved network switches available in database.\n\nAt least one saved network switch required to export to Excel!" ).showAndWait();
            return;
        }

        btnExportToExcel.setDisable ( true );

        new Thread ( () ->
        {
            // workbook object
            final XSSFWorkbook workbook = new XSSFWorkbook();

            // create bold and normal font and style objects
            final XSSFFont fontBold = workbook.createFont();
            fontBold.setFontHeightInPoints ( ( short ) 14 );
            fontBold.setFontName ( "Times New Roman" );
            fontBold.setColor ( IndexedColors.BLACK.getIndex() );
            fontBold.setBold ( true );
            fontBold.setItalic ( false );

            final XSSFCellStyle styleBold = workbook.createCellStyle();
            styleBold.setFillBackgroundColor ( IndexedColors.WHITE.getIndex() );
            styleBold.setAlignment ( HorizontalAlignment.CENTER );
            styleBold.setFont ( fontBold );

            final XSSFFont fontNormal = workbook.createFont();
            fontNormal.setFontHeightInPoints ( ( short ) 14 );
            fontNormal.setFontName ( "Times New Roman" );
            fontNormal.setColor ( IndexedColors.BLACK.getIndex() );
            fontNormal.setBold ( false );
            fontNormal.setItalic ( false );

            final XSSFCellStyle styleNormal = workbook.createCellStyle();
            styleNormal.setFillBackgroundColor ( IndexedColors.WHITE.getIndex() );
            styleNormal.setAlignment ( HorizontalAlignment.CENTER );
            styleNormal.setFont ( fontNormal );

            // spreadsheet object
            final XSSFSheet spreadsheet = workbook.createSheet ( "Network Switch Management" );

            int rowid = 0;

            // creating a row object
            final XSSFRow rowHeader = spreadsheet.createRow ( rowid++ );
            rowHeader.createCell ( 0 ).setCellValue ( "No" );
            rowHeader.createCell ( 1 ).setCellValue ( "Name" );
            rowHeader.createCell ( 2 ).setCellValue ( "IP Address" );
            rowHeader.createCell ( 3 ).setCellValue ( "MAC Address" );
            rowHeader.createCell ( 4 ).setCellValue ( "Status" );

            for ( int cellIndex = 0; cellIndex < 5; ++cellIndex )
            {
                rowHeader.getCell ( cellIndex ).setCellStyle ( styleBold );
            }

            // writing the data into the sheets...
            for ( final NetworkSwitch networkSwitch : tvObservableList )
            {
                // creating a row object
                final XSSFRow row = spreadsheet.createRow ( rowid++ );
                row.createCell ( 0 ).setCellValue ( networkSwitch.getNo() );
                row.createCell ( 1 ).setCellValue ( networkSwitch.getName() );
                row.createCell ( 2 ).setCellValue ( networkSwitch.getIpAddress() );
                row.createCell ( 3 ).setCellValue ( networkSwitch.getMacAddress() );
                row.createCell ( 4 ).setCellValue ( networkSwitch.getStatus() );

                for ( int cellIndex = 0; cellIndex < 5; ++cellIndex )
                {
                    row.getCell ( cellIndex ).setCellStyle ( styleNormal );
                }
            }

            for ( int cellIndex = 0; cellIndex < 5; ++cellIndex )
            {
                spreadsheet.autoSizeColumn ( cellIndex );
            }

            // .xlsx is the format for Excel Sheets...
            // writing the workbook into the file...
            final String excelFilename = "NetworkSwitchManagement.xlsx";

            try ( final FileOutputStream out = new FileOutputStream ( new File ( excelFilename ) ) )
            {
                workbook.write ( out );
            }
            catch ( final IOException e )
            {
                e.printStackTrace();
            }

            btnExportToExcel.setDisable ( false );

            Utils.RunCommand ( Data.excelPath + " " + excelFilename );
        } ).start();
    }

    @FXML
    private void btnBackAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "TechnicianMainDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Technician Main dialog!", "Unable to open Technician Main dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void lnkLogoutAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "LoginDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening login dialog!", "Unable to open login dialog: " + e.getMessage() ).showAndWait();
        }
    }
}
