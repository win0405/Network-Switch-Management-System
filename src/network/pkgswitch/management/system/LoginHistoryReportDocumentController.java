package network.pkgswitch.management.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import network.pkgswitch.management.system.models.LoginHistory;
import network.pkgswitch.management.system.models.NetworkSwitch;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LoginHistoryReportDocumentController implements Initializable
{
    private final ObservableList<LoginHistory> tvObservableList = FXCollections.observableArrayList();

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

    private void updateLoginHistoryTable ( final ObservableList<LoginHistory> tvObservableList )
    {
        final TableColumn<NetworkSwitch, Long> colNo = new TableColumn<> ( "No" );
        colNo.setStyle ( "-fx-alignment: CENTER;" );
        colNo.setCellValueFactory ( new PropertyValueFactory<> ( "no" ) );

        final TableColumn<NetworkSwitch, Long> colDate = new TableColumn<> ( "Date" );
        colDate.setStyle ( "-fx-alignment: CENTER;" );
        colDate.setCellValueFactory ( new PropertyValueFactory<> ( "date" ) );

        final TableColumn<NetworkSwitch, Long> colTime = new TableColumn<> ( "Time" );
        colTime.setStyle ( "-fx-alignment: CENTER;" );
        colTime.setCellValueFactory ( new PropertyValueFactory<> ( "time" ) );

        final TableColumn<NetworkSwitch, Long> colUsername = new TableColumn<> ( "Username" );
        colUsername.setStyle ( "-fx-alignment: CENTER;" );
        colUsername.setCellValueFactory ( new PropertyValueFactory<> ( "username" ) );

        final TableColumn<NetworkSwitch, Long> colName = new TableColumn<> ( "Name" );
        colName.setStyle ( "-fx-alignment: CENTER;" );
        colName.setCellValueFactory ( new PropertyValueFactory<> ( "name" ) );

        final TableColumn<NetworkSwitch, Long> colEmail = new TableColumn<> ( "E-mail" );
        colEmail.setStyle ( "-fx-alignment: CENTER;" );
        colEmail.setCellValueFactory ( new PropertyValueFactory<> ( "email" ) );

        tblReport.getColumns().clear();
        tblReport.getColumns().addAll ( colNo, colDate, colTime, colUsername, colName, colEmail );

        tblReport.setItems ( tvObservableList );
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
                Data.dbLocal.OpenDatabase();
            }
            catch ( final SQLException e )
            {
                Utils.getAlert ( Alert.AlertType.ERROR, "Error generating login history report!", "Unable to generate login history report: " + e.getMessage() ).showAndWait();
                return;
            }

            final String sql = "SELECT to_char(\"Timestamp\",'FMDDth FMMonth YYYY'),to_char(\"Timestamp\",'FMHH12:MI:SS PM'),\"Login History\".\"Username\",\"Name\",\"E-mail\" FROM \"Login History\" INNER JOIN \"User Profile\" ON \"Login History\".\"Username\"=\"User Profile\".\"Username\" WHERE \"Timestamp\" BETWEEN ? AND ? ORDER BY \"Timestamp\" DESC,\"Username\" ASC";

            try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
            {
                final Timestamp timestampStartDate = Timestamp.valueOf ( startDate.atStartOfDay() );
                final Timestamp timestampEndDate = Timestamp.valueOf ( endDate.atStartOfDay() );

                ps.setTimestamp ( 1, timestampStartDate );
                ps.setTimestamp ( 2, timestampEndDate );

                try ( final ResultSet rs = ps.executeQuery() )
                {
                    int no = 1;

                    while ( rs.next() )
                    {
                        final String date = rs.getString ( 1 );
                        final String time = rs.getString ( 2 );
                        final String username = rs.getString ( 3 );
                        final String name = rs.getString ( 4 );
                        final String email = rs.getString ( 5 );

                        tvObservableList.add ( new LoginHistory ( no++, date, time, username, name, email ) );
                    }
                }
            }
            catch ( final SQLException e )
            {
                Utils.getAlert ( Alert.AlertType.ERROR, "Error generating login history report!", "Unable to generate login history report: " + e.getMessage() ).showAndWait();
            }

            try
            {
                Data.dbLocal.CloseDatabase();
            }
            catch ( final Exception e )
            {}

            // update GUI using fx thread
            Platform.runLater ( () ->
            {
                updateLoginHistoryTable ( tvObservableList );

                btnGenerateReport.setDisable ( false );
            } );
        } ).start();
    }

    @FXML
    private void btnExportToExcelAction ( final ActionEvent event )
    {
        if ( tvObservableList.isEmpty() )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "No login histroy available!",
                "No login history available for exporting to Excel!" ).showAndWait();
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
            final XSSFSheet spreadsheet = workbook.createSheet ( "Login History Report" );

            int rowid = 0;

            // creating a row object
            final XSSFRow rowHeader = spreadsheet.createRow ( rowid++ );
            rowHeader.createCell ( 0 ).setCellValue ( "No" );
            rowHeader.createCell ( 1 ).setCellValue ( "Date" );
            rowHeader.createCell ( 2 ).setCellValue ( "Time" );
            rowHeader.createCell ( 3 ).setCellValue ( "Username" );
            rowHeader.createCell ( 4 ).setCellValue ( "Name" );
            rowHeader.createCell ( 5 ).setCellValue ( "E-mail" );

            for ( int cellIndex = 0; cellIndex < 6; ++cellIndex )
            {
                rowHeader.getCell ( cellIndex ).setCellStyle ( styleBold );
            }

            // writing the data into the sheets...
            for ( final LoginHistory loginHistory : tvObservableList )
            {
                // creating a row object
                final XSSFRow row = spreadsheet.createRow ( rowid++ );
                row.createCell ( 0 ).setCellValue ( loginHistory.getNo() );
                row.createCell ( 1 ).setCellValue ( loginHistory.getDate() );
                row.createCell ( 2 ).setCellValue ( loginHistory.getTime() );
                row.createCell ( 3 ).setCellValue ( loginHistory.getUsername() );
                row.createCell ( 4 ).setCellValue ( loginHistory.getName() );
                row.createCell ( 5 ).setCellValue ( loginHistory.getEmail() );

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
            final String excelFilename = "LoginHistoryReport.xlsx";

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
