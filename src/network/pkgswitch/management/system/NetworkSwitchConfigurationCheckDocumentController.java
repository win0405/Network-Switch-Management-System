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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import network.pkgswitch.management.system.models.NetworkSwitch;
import network.pkgswitch.management.system.models.NetworkSwitchConfiguration;
import network.pkgswitch.management.system.services.NetworkSwitchConfigurationServices;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class NetworkSwitchConfigurationCheckDocumentController implements Initializable
{
    private ArrayList<NetworkSwitchConfiguration> alNetworkSwitches;

    @FXML
    private Label lblWelcome;

    @FXML
    private TableView tblNetworkSwitches;

    @FXML
    private Button btnRefreshAll, btnExportToExcel;

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );

        final ObservableList<NetworkSwitchConfiguration> tvObservableList = FXCollections.observableArrayList();

        try
        {
            alNetworkSwitches = NetworkSwitchConfigurationServices.readNetworkSwitches();
            tvObservableList.addAll ( alNetworkSwitches.toArray ( new NetworkSwitchConfiguration [ alNetworkSwitches.size() ] ) );
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }

        updateNetworkSwitchesTable ( tvObservableList );
    }

    private void updateNetworkSwitchesTable ( final ObservableList<NetworkSwitchConfiguration> tvObservableList )
    {
        final TableColumn<NetworkSwitch, Long> colNo = new TableColumn<> ( "No" );
        colNo.setStyle ( "-fx-alignment: CENTER;" );
        colNo.setCellValueFactory ( new PropertyValueFactory<> ( "no" ) );

        final TableColumn<NetworkSwitch, String> colName = new TableColumn<> ( "Name" );
        colName.setStyle ( "-fx-alignment: CENTER;" );
        colName.setCellValueFactory ( new PropertyValueFactory<> ( "name" ) );

        final TableColumn<NetworkSwitch, String> colIpAddress = new TableColumn<> ( "IP Address" );
        colIpAddress.setStyle ( "-fx-alignment: CENTER;" );
        colIpAddress.setCellValueFactory ( new PropertyValueFactory<> ( "ipAddress" ) );

        final TableColumn<NetworkSwitch, String> colMacAddress = new TableColumn<> ( "MAC Address" );
        colMacAddress.setStyle ( "-fx-alignment: CENTER;" );
        colMacAddress.setCellValueFactory ( new PropertyValueFactory<> ( "macAddress" ) );

        final TableColumn<NetworkSwitch, String> colSoftwareVersion = new TableColumn<> ( "Software Version" );
        colSoftwareVersion.setStyle ( "-fx-alignment: CENTER;" );
        colSoftwareVersion.setCellValueFactory ( new PropertyValueFactory<> ( "softwareVersion" ) );

        final TableColumn<NetworkSwitch, String> colModelNumber = new TableColumn<> ( "Model #" );
        colModelNumber.setStyle ( "-fx-alignment: CENTER;" );
        colModelNumber.setCellValueFactory ( new PropertyValueFactory<> ( "modelNumber" ) );

        final TableColumn<NetworkSwitch, String> colSerialNumber = new TableColumn<> ( "Serial #" );
        colSerialNumber.setStyle ( "-fx-alignment: CENTER;" );
        colSerialNumber.setCellValueFactory ( new PropertyValueFactory<> ( "serialNumber" ) );

        tblNetworkSwitches.getColumns().clear();
        tblNetworkSwitches.getColumns().addAll ( colNo, colName, colIpAddress, colMacAddress, colSoftwareVersion,
            colModelNumber, colSerialNumber );

        tblNetworkSwitches.setItems ( tvObservableList );

        final TableColumn<NetworkSwitchConfiguration, Void> colAction = new TableColumn ( "Action" );

        final Callback<TableColumn<NetworkSwitchConfiguration, Void>, TableCell<NetworkSwitchConfiguration, Void>> cellFactory =
                new Callback<TableColumn<NetworkSwitchConfiguration, Void>, TableCell<NetworkSwitchConfiguration, Void>>()
        {
            @Override
            public TableCell<NetworkSwitchConfiguration, Void> call ( final TableColumn<NetworkSwitchConfiguration, Void> param )
            {
                final TableCell<NetworkSwitchConfiguration, Void> cell = new TableCell<NetworkSwitchConfiguration, Void>()
                {
                    private final Button btnRefresh = new Button ( "Refresh" );
                    {
                        btnRefresh.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final NetworkSwitchConfiguration networkSwitchConfiguration = getTableView().getItems().get ( getIndex() );
                            final String ipAddress = networkSwitchConfiguration.getIpAddress();

                            btnRefresh.setDisable ( true );

                            // use another thread to prevent blocking GUI
                            new Thread ( () ->
                            {
                                networkSwitchConfiguration.setSoftwareVersion ( "---" );
                                networkSwitchConfiguration.setModelNumber ( "---" );
                                networkSwitchConfiguration.setSerialNumber ( "---" );

                                final String[] switchInformation = Utils.retrieveSwitchInformation ( ipAddress, Data.NETWORK_SWITCH_USERNAME, Data.NETWORK_SWITCH_PASSWORD );

                                if ( switchInformation != null && switchInformation.length == 4 )
                                {
                                    networkSwitchConfiguration.setSoftwareVersion ( switchInformation [ 0 ] );
                                    networkSwitchConfiguration.setModelNumber ( switchInformation [ 1 ] );
                                    networkSwitchConfiguration.setSerialNumber ( switchInformation [ 2 ] );

                                    final String currMACAddress = networkSwitchConfiguration.getMacAddress();

                                    if ( currMACAddress.isEmpty() || currMACAddress.equals ( "---" ) )
                                        networkSwitchConfiguration.setMacAddress ( switchInformation [ 3 ] );
                                }

                                getTableView().getItems().set ( getIndex(), networkSwitchConfiguration );

                                // update GUI using fx thread
                                Platform.runLater ( () -> btnRefresh.setDisable ( false ) );
                            } ).start();
                        } );
                    }

                    private final HBox pane = new HBox ( btnRefresh );
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

    @FXML
    private void btnRefreshAllAction ( final ActionEvent event )
    {
        btnRefreshAll.setDisable ( true );

        for ( final NetworkSwitchConfiguration networkSwitchConfiguration : alNetworkSwitches )
        {
            networkSwitchConfiguration.setSoftwareVersion ( "---" );
            networkSwitchConfiguration.setModelNumber ( "---" );
            networkSwitchConfiguration.setSerialNumber ( "---" );
        }

        final ObservableList<NetworkSwitchConfiguration> tvObservableList = FXCollections.observableArrayList();
        tvObservableList.addAll ( alNetworkSwitches.toArray ( new NetworkSwitchConfiguration [ alNetworkSwitches.size() ] ) );
        updateNetworkSwitchesTable ( tvObservableList );

        new Thread ( () ->
        {
            final ArrayList<Thread> alThreads = new ArrayList<>();

            for ( final NetworkSwitchConfiguration networkSwitchConfiguration : alNetworkSwitches )
            {
                // use another thread to prevent blocking GUI
                final Thread thread = new Thread ( () ->
                {
                    final String[] switchInformation = Utils.retrieveSwitchInformation (
                        networkSwitchConfiguration.getIpAddress(), Data.NETWORK_SWITCH_USERNAME, Data.NETWORK_SWITCH_PASSWORD );

                    if ( switchInformation != null && switchInformation.length == 4 )
                    {
                        networkSwitchConfiguration.setSoftwareVersion ( switchInformation [ 0 ] );
                        networkSwitchConfiguration.setModelNumber ( switchInformation [ 1 ] );
                        networkSwitchConfiguration.setSerialNumber ( switchInformation [ 2 ] );

                        final String currMACAddress = networkSwitchConfiguration.getMacAddress();

                        if ( currMACAddress.isEmpty() || currMACAddress.equals ( "---" ) )
                            networkSwitchConfiguration.setMacAddress ( switchInformation [ 3 ] );
                    }

                    // update GUI using fx thread
                    Platform.runLater ( () ->
                    {
                        final ObservableList<NetworkSwitchConfiguration> tvObservableListCurrent = FXCollections.observableArrayList();
                        tvObservableListCurrent.addAll ( alNetworkSwitches.toArray ( new NetworkSwitchConfiguration [ alNetworkSwitches.size() ] ) );
                        updateNetworkSwitchesTable ( tvObservableListCurrent );
                    } );
                } );

                alThreads.add ( thread );

                thread.start();
            }

            for ( final Thread thread : alThreads )
                try { thread.join(); } catch ( final InterruptedException e ) {}

            btnRefreshAll.setDisable ( false );
        } ).start();
    }

    @FXML
    private void btnExportToExcelAction ( final ActionEvent event )
    {
        final ObservableList<NetworkSwitchConfiguration> tvObservableList = tblNetworkSwitches.getItems();

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
            final XSSFSheet spreadsheet = workbook.createSheet ( "Network Switch Configuration Check" );

            int rowid = 0;

            // creating a row object
            final XSSFRow rowHeader = spreadsheet.createRow ( rowid++ );
            rowHeader.createCell ( 0 ).setCellValue ( "No" );
            rowHeader.createCell ( 1 ).setCellValue ( "Name" );
            rowHeader.createCell ( 2 ).setCellValue ( "IP Address" );
            rowHeader.createCell ( 3 ).setCellValue ( "MAC Address" );
            rowHeader.createCell ( 4 ).setCellValue ( "Software Version" );
            rowHeader.createCell ( 5 ).setCellValue ( "Model #" );
            rowHeader.createCell ( 6 ).setCellValue ( "Serial #" );

            for ( int cellIndex = 0; cellIndex < 7; ++cellIndex )
            {
                rowHeader.getCell ( cellIndex ).setCellStyle ( styleBold );
            }

            // writing the data into the sheets...
            for ( final NetworkSwitchConfiguration networkSwitchConfiguration : tvObservableList )
            {
                // creating a row object
                final XSSFRow row = spreadsheet.createRow ( rowid++ );
                row.createCell ( 0 ).setCellValue ( networkSwitchConfiguration.getNo() );
                row.createCell ( 1 ).setCellValue ( networkSwitchConfiguration.getName() );
                row.createCell ( 2 ).setCellValue ( networkSwitchConfiguration.getIpAddress() );
                row.createCell ( 3 ).setCellValue ( networkSwitchConfiguration.getMacAddress() );
                row.createCell ( 4 ).setCellValue ( networkSwitchConfiguration.getSoftwareVersion() );
                row.createCell ( 5 ).setCellValue ( networkSwitchConfiguration.getModelNumber() );
                row.createCell ( 6 ).setCellValue ( networkSwitchConfiguration.getSerialNumber() );

                for ( int cellIndex = 0; cellIndex < 7; ++cellIndex )
                {
                    row.getCell ( cellIndex ).setCellStyle ( styleNormal );
                }
            }

            for ( int cellIndex = 0; cellIndex < 7; ++cellIndex )
            {
                spreadsheet.autoSizeColumn ( cellIndex );
            }

            // .xlsx is the format for Excel Sheets...
            // writing the workbook into the file...
            final String excelFilename = "NetworkSwitchConfigurationCheck.xlsx";

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
}
