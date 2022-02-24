package network.pkgswitch.management.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import network.pkgswitch.management.system.models.ConfigurationChangeHistory;
import network.pkgswitch.management.system.models.NetworkSwitchConfiguration;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ConfigurationChangeHistoryDocumentController implements Initializable
{
    private final ObservableList<ConfigurationChangeHistory> tvObservableList = FXCollections.observableArrayList();

    private final ArrayList<String> alLogFilenames = new ArrayList<>();

    @FXML
    private Label lblWelcome;

    @FXML
    private DatePicker pickerStartDate, pickerEndDate;

    @FXML
    private TableView tblReport;

    @FXML
    private Button btnGenerateReport, btnExportToExcel;

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );
        tblReport.getColumns().clear();
    }

    private void updateConfigurationChangeHistoryTable ( final ObservableList<ConfigurationChangeHistory> tvObservableList )
    {
        final TableColumn<ConfigurationChangeHistory, Long> colNo = new TableColumn<> ( "No" );
        colNo.setStyle ( "-fx-alignment: CENTER;" );
        colNo.setCellValueFactory ( new PropertyValueFactory<> ( "no" ) );

        final TableColumn<ConfigurationChangeHistory, Long> colDate = new TableColumn<> ( "Date" );
        colDate.setStyle ( "-fx-alignment: CENTER;" );
        colDate.setCellValueFactory ( new PropertyValueFactory<> ( "date" ) );

        final TableColumn<ConfigurationChangeHistory, Long> colTime = new TableColumn<> ( "Time" );
        colTime.setStyle ( "-fx-alignment: CENTER;" );
        colTime.setCellValueFactory ( new PropertyValueFactory<> ( "time" ) );

        final TableColumn<ConfigurationChangeHistory, Long> colSwitchName = new TableColumn<> ( "Switch Name" );
        colSwitchName.setStyle ( "-fx-alignment: CENTER;" );
        colSwitchName.setCellValueFactory ( new PropertyValueFactory<> ( "switchName" ) );

        final TableColumn<ConfigurationChangeHistory, Long> colIPAddress = new TableColumn<> ( "IP Address" );
        colIPAddress.setStyle ( "-fx-alignment: CENTER;" );
        colIPAddress.setCellValueFactory ( new PropertyValueFactory<> ( "ipAddress" ) );

        final TableColumn<ConfigurationChangeHistory, Long> colMACAddress = new TableColumn<> ( "MAC Address" );
        colMACAddress.setStyle ( "-fx-alignment: CENTER;" );
        colMACAddress.setCellValueFactory ( new PropertyValueFactory<> ( "macAddress" ) );

        tblReport.getColumns().clear();
        tblReport.getColumns().addAll ( colNo, colDate, colTime, colSwitchName, colIPAddress, colMACAddress );

        tblReport.setItems ( tvObservableList );

        final TableColumn<ConfigurationChangeHistory, Void> colAction = new TableColumn ( "Action" );

        final Callback<TableColumn<ConfigurationChangeHistory, Void>, TableCell<ConfigurationChangeHistory, Void>> cellFactory =
                new Callback<TableColumn<ConfigurationChangeHistory, Void>, TableCell<ConfigurationChangeHistory, Void>>()
        {
            @Override
            public TableCell<ConfigurationChangeHistory, Void> call ( final TableColumn<ConfigurationChangeHistory, Void> param )
            {
                final TableCell<ConfigurationChangeHistory, Void> cell = new TableCell<ConfigurationChangeHistory, Void>()
                {
                    private final Button btnViewHistory = new Button ( "View History" );
                    {
                        btnViewHistory.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final ConfigurationChangeHistory configurationChangeHistory = getTableView().getItems().get ( getIndex() );
                            final String logFilename = alLogFilenames.get ( ( int ) configurationChangeHistory.getNo() - 1 );

                            //try
                            //{
                            //    Files.copy ( Paths.get ( logFilename ), Paths.get ( Data.networkAccessHistoryFilePath + File.separator + "current.log" ), StandardCopyOption.REPLACE_EXISTING );
                            //    Runtime.getRuntime().exec ( "notepad \"" + Data.networkAccessHistoryFilePath + File.separator + "current.log" );
                            //}
                            //catch ( final IOException e )
                            //{
                            //    e.printStackTrace();
                            //}

                            //Runtime.getRuntime().exec ( "notepad \"" + logFilename + "\"" );
                            Utils.RunCommand ( "notepad \"" + logFilename + "\"" );
                        } );
                    }

                    private final HBox pane = new HBox ( btnViewHistory );
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
        tblReport.getColumns().add ( colAction );
    }

    @FXML
    private void btnGenerateReportAction ( final ActionEvent event )
    {
        final LocalDate startDate = pickerStartDate.getValue();

        if ( startDate == null )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No start date selected!", "Please select start date for login history report generation!" ).showAndWait();
            return;
        }

        final LocalDate endDate = pickerEndDate.getValue();

        if ( endDate == null )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No end date selected!", "Please select end date for login history report generation!" ).showAndWait();
            return;
        }

        btnGenerateReport.setDisable ( true );

        // use another thread to prevent blocking GUI
        new Thread ( () ->
        {
            tvObservableList.clear();

            try
            {
                int no = 1;

                alLogFilenames.clear();

                for ( final String filename : Utils.GetFilenames ( Data.networkAccessHistoryFilePath, "", "log" ) )
                {
                    try
                    {
                        final int[] startEndPosIP = Utils.findStartEndPos ( filename, "[", "]" );

                        if ( startEndPosIP == null )
                            continue;

                        final String remainder = filename.substring ( startEndPosIP [ 1 ] + 2 );

                        final int[] startEndPosTimestamp = Utils.findStartEndPos ( remainder, "(", ")" );

                        if ( startEndPosTimestamp == null )
                            continue;

                        final String[] ipAddressFields = filename.substring ( startEndPosIP [ 0 ] + 1, startEndPosIP [ 1 ] ).split ( " " );
                        final String ipAddress = ipAddressFields [ 1 ];

                        final ArrayList<NetworkSwitchConfiguration> alNetworkSwitches = Utils.readNetworkSwitchesByIPAddress ( ipAddress );

                        if ( alNetworkSwitches.isEmpty() )
                            continue;

                        final String[] timestampFields = remainder.substring ( startEndPosTimestamp [ 0 ] + 1, startEndPosTimestamp [ 1 ] ).split ( "_" );

                        final String date = timestampFields [ 0 ];

                        final LocalDate currDate = LocalDate.parse ( date );

                        if ( currDate.isBefore ( startDate ) || currDate.isAfter ( endDate ) )
                            continue;
                        
                        final String strHour = timestampFields [ 1 ].substring ( 0, 2 );
                        final String strMinute = timestampFields [ 1 ].substring ( 2, 4 );
                        final String strSecond = timestampFields [ 1 ].substring ( 4, 6 );
                        final String time = strHour + ":" + strMinute + ":" + strSecond;

                        final NetworkSwitchConfiguration networkSwitchConfiguration = alNetworkSwitches.get ( 0 );
                        final String switchName = networkSwitchConfiguration.getName();
                        final String macAddress = networkSwitchConfiguration.getMacAddress();

                        alLogFilenames.add ( Data.networkAccessHistoryFilePath + File.separatorChar + filename + ".log" );
                        tvObservableList.add ( new ConfigurationChangeHistory ( no++, date, time, switchName, ipAddress, macAddress ) );
                    }
                    catch ( final Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch ( final IOException e )
            {
                e.printStackTrace();
            }


            // update GUI using fx thread
            Platform.runLater ( () ->
            {
                updateConfigurationChangeHistoryTable ( tvObservableList );

                btnGenerateReport.setDisable ( false );
            } );
        } ).start();
    }

    @FXML
    private void btnExportToExcelAction ( final ActionEvent event )
    {
        if ( tvObservableList.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No configuration change histroy available!",
                "No configuration change history available for exporting to Excel!" ).showAndWait();
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
            final XSSFSheet spreadsheet = workbook.createSheet ( "Configuration Change History Report" );

            int rowid = 0;

            // creating a row object
            final XSSFRow rowHeader = spreadsheet.createRow ( rowid++ );
            rowHeader.createCell ( 0 ).setCellValue ( "No" );
            rowHeader.createCell ( 1 ).setCellValue ( "Date" );
            rowHeader.createCell ( 2 ).setCellValue ( "Time" );
            rowHeader.createCell ( 3 ).setCellValue ( "Switch Name" );
            rowHeader.createCell ( 4 ).setCellValue ( "IP Address" );
            rowHeader.createCell ( 5 ).setCellValue ( "MAC Address" );

            for ( int cellIndex = 0; cellIndex < 6; ++cellIndex )
            {
                rowHeader.getCell ( cellIndex ).setCellStyle ( styleBold );
            }

            // writing the data into the sheets...
            for ( final ConfigurationChangeHistory configurationChangeHistory : tvObservableList )
            {
                // creating a row object
                final XSSFRow row = spreadsheet.createRow ( rowid++ );
                row.createCell ( 0 ).setCellValue ( configurationChangeHistory.getNo() );
                row.createCell ( 1 ).setCellValue ( configurationChangeHistory.getDate() );
                row.createCell ( 2 ).setCellValue ( configurationChangeHistory.getTime() );
                row.createCell ( 3 ).setCellValue ( configurationChangeHistory.getSwitchName() );
                row.createCell ( 4 ).setCellValue ( configurationChangeHistory.getIpAddress() );
                row.createCell ( 5 ).setCellValue ( configurationChangeHistory.getMacAddress() );

                for ( int cellIndex = 0; cellIndex < 6; ++cellIndex )
                {
                    row.getCell ( cellIndex ).setCellStyle ( styleNormal );
                }
            }

            for ( int cellIndex = 0; cellIndex < 6; ++cellIndex )
            {
                spreadsheet.autoSizeColumn ( cellIndex );
            }

            // .xlsx is the format for Excel Sheets...
            // writing the workbook into the file...
            final String excelFilename = "ConfigurationChangeHistoryReport.xlsx";

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
            final Parent root = FXMLLoader.load ( getClass().getResource ( "ManagerMainDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Manager Main dialog!", "Unable to open Manager Main dialog: " + e.getMessage() ).showAndWait();
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
