package network.pkgswitch.management.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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

public class NetworkSwitchDiscoveryDocumentController implements Initializable
{
    private final ObservableList<NetworkSwitch> tvObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );
        tblNetworkSwitches.getColumns().clear();
    }

    private void updateNetworkSwitchesTable ( final ObservableList<NetworkSwitch> tvObservableList )
    {
        final TableColumn<NetworkSwitch, Long> colNo = new TableColumn<> ( "No" );
        colNo.setStyle ( "-fx-alignment: CENTER;" );
        colNo.setCellValueFactory ( new PropertyValueFactory<> ( "no" ) );

        final TableColumn<NetworkSwitch, Long> colIpAddress = new TableColumn<> ( "IP Address" );
        colIpAddress.setStyle ( "-fx-alignment: CENTER;" );
        colIpAddress.setCellValueFactory ( new PropertyValueFactory<> ( "ipAddress" ) );

        final TableColumn<NetworkSwitch, Long> colMacAddress = new TableColumn<> ( "MAC Address" );
        colMacAddress.setStyle ( "-fx-alignment: CENTER;" );
        colMacAddress.setCellValueFactory ( new PropertyValueFactory<> ( "macAddress" ) );

        tblNetworkSwitches.getColumns().clear();
        tblNetworkSwitches.getColumns().addAll ( colNo, colIpAddress, colMacAddress );

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
                    private final Button btnAddToDatabase = new Button ( "Add to Database" );
                    {
                        btnAddToDatabase.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final NetworkSwitch networkSwitch = getTableView().getItems().get ( getIndex() );

                            try
                            {
                                if ( NetworkSwitchServices.addNetworkSwitch ( networkSwitch ) )
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
                                e.printStackTrace();

                                Utils.getAlert ( Alert.AlertType.ERROR, "Error adding network switch!",
                                    "Unable to add network switch to database: " + e.getMessage() ).showAndWait();
                            }
                        } );
                    }

                    private final HBox pane = new HBox ( btnAddToDatabase );
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
    }

    @FXML
    private void btnStartNetworkSwitchDiscoveryAction ( final ActionEvent event )
    {
        final String strStartIPAddress = txtStartIPAddress.getText().trim();

        if ( strStartIPAddress.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No start IP address entered!", "Please enter start IP address for network switch discovery!" ).showAndWait();
            return;
        }

        final int[] startIPAddress;

        try
        {
            startIPAddress = Utils.parseIpAddress ( strStartIPAddress );
        }
        catch ( final IllegalArgumentException exceptionIllegalArgument )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "Invalid start IP address detected!",
                "Illegal start IP address: " + exceptionIllegalArgument.getMessage() ).showAndWait();
            return;
        }

        final String strEndIPAddress = txtEndIPAddress.getText().trim();

        if ( strEndIPAddress.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No end IP address entered!", "Please enter end IP address for network switch discovery!" ).showAndWait();
            return;
        }

        final int[] endIPAddress;

        try
        {
            endIPAddress = Utils.parseIpAddress ( strEndIPAddress );
        }
        catch ( final IllegalArgumentException exceptionIllegalArgument )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "Invalid end IP address detected!",
                "Illegal end IP address: " + exceptionIllegalArgument.getMessage() ).showAndWait();
            return;
        }
 
        if ( startIPAddress [ 0 ] != endIPAddress [ 0 ] || startIPAddress [ 1 ] != endIPAddress [ 1 ] || startIPAddress [ 2 ] != endIPAddress [ 2 ] )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "Start and end IP address first three bytes do not match!",
                "The first three bytes of the start and end IP addresses must match to perform discovery!" ).showAndWait();
            return;
        }

        if ( startIPAddress [ 3 ] > endIPAddress [ 3 ] )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "End IP address larger than start IP address!",
                "The fourth byte of the end IP address must be greater than or equal to the fourth byte of the start IP address!" ).showAndWait();
            return;
        }

        final String ipAddressHeader = "" + startIPAddress [ 0 ] + "." + startIPAddress [ 1 ] + "." + startIPAddress [ 2 ] + ".";

        btnStartNetworkSwitchDiscovery.setDisable ( true );

        // use another thread to prevent blocking GUI
        new Thread ( () ->
        {
            tvObservableList.clear();

            long index = 1;

            for ( int ipLSB = startIPAddress [ 3 ]; ipLSB <= endIPAddress [ 3 ]; ++ipLSB )
            {
                final String ipAddress = ipAddressHeader + ipLSB;
                final String status = Utils.pingNetworkSwitch ( ipAddress );

                if ( status.equals ( "Up" ) )
                {
                    final long no = index++;

                    // update GUI using fx thread
                    Platform.runLater ( () ->
                    {
                        try
                        {
                            final InetAddress inetAddress = InetAddress.getByName ( ipAddress );
                            String macAddress = "N/A";

                            try
                            {
                                final String command = "arp -a " + ipAddress;
                                final ArrayList<String> alResult = Utils.RunCommand ( command );

                                if ( alResult.size() == 3 )
                                {
                                    final String[] fields = alResult.get ( 2 ).split ( " " );

                                    if ( fields.length == 19 && fields [ 2 ].equals ( ipAddress ) )
                                        macAddress = fields [ 13 ];
                                }
    
                                if ( macAddress.equals ( "N/A" ) )
                                {
                                    final NetworkInterface network = NetworkInterface.getByInetAddress ( inetAddress );

                                    if ( network != null )
                                    {
                                        final byte[] mac = network.getHardwareAddress();

                                        final StringBuilder sb = new StringBuilder();

                                        for ( int i = 0; i < mac.length; ++i )
                                        {
                                            sb.append ( String.format ( "%02X%s", mac [ i ], ( i < mac.length - 1 ) ? "-" : "" ) );		
                                        }

                                        macAddress = sb.toString();
                                    }
                                }
                            }
                            catch ( final SocketException e )
                            {}

                            tvObservableList.add ( new NetworkSwitch ( -1, no, ipAddress, ipAddress, macAddress, Data.username, status ) );
                            updateNetworkSwitchesTable ( tvObservableList );
                        }
                        catch ( final Exception e )
                        {
                            e.printStackTrace();
                        }
                    } );
                }
            }

            // update GUI using fx thread
            Platform.runLater ( () ->
            {
                btnStartNetworkSwitchDiscovery.setDisable ( false );
            } );
        } ).start();
    }

    @FXML
    private void btnExportToExcelAction ( final ActionEvent event )
    {
        if ( tvObservableList.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No network switches discovered!",
                "No network switches has been discovered yet.\n\nAt least one discovered network switches required to export to Excel!" ).showAndWait();
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
            final XSSFSheet spreadsheet = workbook.createSheet ( "Network Switch Discovery" );

            int rowid = 0;

            // creating a row object
            final XSSFRow rowHeader = spreadsheet.createRow ( rowid++ );
            rowHeader.createCell ( 0 ).setCellValue ( "IP Address" );
            rowHeader.createCell ( 1 ).setCellValue ( "MAC Address" );

            for ( int cellIndex = 0; cellIndex < 2; ++cellIndex )
            {
                rowHeader.getCell ( cellIndex ).setCellStyle ( styleBold );
            }

            // writing the data into the sheets...
            for ( final NetworkSwitch networkSwitch : tvObservableList )
            {
                // creating a row object
                final XSSFRow row = spreadsheet.createRow ( rowid++ );
                row.createCell ( 0 ).setCellValue ( networkSwitch.getIpAddress() );
                row.createCell ( 1 ).setCellValue ( networkSwitch.getMacAddress() );

                for ( int cellIndex = 0; cellIndex < 2; ++cellIndex )
                {
                    row.getCell ( cellIndex ).setCellStyle ( styleNormal );
                }
            }

            for ( int cellIndex = 0; cellIndex < 2; ++cellIndex )
            {
                spreadsheet.autoSizeColumn ( cellIndex );
            }

            // .xlsx is the format for Excel Sheets...
            // writing the workbook into the file...
            final String excelFilename = "NetworkSwitchDiscovery.xlsx";

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

    @FXML
    private Label lblWelcome;

    @FXML
    private TextField txtStartIPAddress, txtEndIPAddress;

    @FXML
    private TableView tblNetworkSwitches;

    @FXML
    private Button btnStartNetworkSwitchDiscovery, btnExportToExcel;
}
