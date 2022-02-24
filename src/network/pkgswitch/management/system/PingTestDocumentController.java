package network.pkgswitch.management.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import network.pkgswitch.management.system.services.NetworkSwitchServices;

public class PingTestDocumentController implements Initializable
{
    private String ipAddress = null, macAddress = null;

    private class Console extends OutputStream
    {
        private TextArea console;
 
        public Console ( final TextArea console )
        {
            this.console = console;
        }
 
        public void appendText ( final String valueOf )
        {
            Platform.runLater ( () -> console.appendText ( valueOf ) );
        }
 
        public void write ( final int b ) throws IOException
        {
            appendText ( String.valueOf ( ( char ) b ) );
        }
    }

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );

        final PrintStream ps = new PrintStream ( new Console ( txtPingStatus ) );
        System.setOut ( ps );
        System.setErr ( ps );
    }

    @FXML
    private Label lblWelcome;

    @FXML
    private TextField txtIPAddress;

    @FXML
    private Label lblStatus;

    @FXML
    private TextArea txtPingStatus;

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

            Utils.getAlert ( AlertType.ERROR, "Error opening Login dialog!", "Unable to open Login dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnPerformPingTestAction ( final ActionEvent event )
    {
        final String ipAddress = txtIPAddress.getText().trim();

        if ( ipAddress.isEmpty() )
        {
            Utils.getAlert ( AlertType.ERROR, "No IP address entered!", "Please enter IP address!" ).showAndWait();
            return;
        }

        txtPingStatus.setText ( "" );

        try
        {
            final Process pingProcess = Runtime.getRuntime().exec ( "ping " + ipAddress );

            try ( final InputStream inputStream = pingProcess.getInputStream();
                  final BufferedReader br = new BufferedReader ( new InputStreamReader ( inputStream,"gb2312" ) ) )
            {
                this.ipAddress = this.macAddress = null;
                lblStatus.setText ( "---" ); lblStatus.setTextFill ( Color.BLACK );

                String line;

                while ( ( line = br.readLine() ) != null )
                {
                    if ( line.startsWith ( "Reply from " ) && line.contains ( "bytes" ) && line.contains ( "time" ) && line.contains ( "TTL" ) )
                    {
                        this.ipAddress = ipAddress;
                        lblStatus.setText ( "Device Found" ); lblStatus.setTextFill ( Color.GREEN );

                        try
                        {
                            macAddress = Utils.getMACAddress ( ipAddress );

                            if ( macAddress != null )
                                System.out.println ( "MAC address: " + macAddress );
                            else
                                System.out.println ( "MAC address not found!" );
                        }
                        catch ( final IOException e )
                        {
                            e.printStackTrace();
                        }
                        break;
                    }
                    else if ( line.equals ( "Request timed out." ) || line.contains ( "Destination host unreachable." ) )
                    {
                        System.out.println ( line );
                        lblStatus.setText ( "Device Not Found" ); lblStatus.setTextFill ( Color.RED );
                        break;
                    }
                }
            }
            catch ( final IOException e )
            {
                e.printStackTrace();
            }
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
            Utils.getAlert ( AlertType.ERROR, "Error executing ping command!", "Unable to perform ping: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnAddToDatabaseAction ( final ActionEvent event )
    {
        if ( ipAddress == null )
        {
            Utils.getAlert ( AlertType.ERROR, "No found IP address to add!", "Please perform a ping test to search for a device first!" );
            return;
        }

        final String macAddress = ( this.macAddress != null ) ? this.macAddress : "N/A";

        final TextInputDialog td = new TextInputDialog();
        td.setHeaderText ( "Enter name for the new network switch" );
        td.showAndWait();

        final String name = td.getResult();

        if ( name == null )
        {
            Utils.getAlert ( AlertType.INFORMATION, "Add to Database command cancelled!", "The Add to Database command has been cancelled at your request!" ).showAndWait();
            return;
        }

        try
        {
            switch ( NetworkSwitchServices.checkDuplicateNetworkSwitch ( Data.username, ipAddress, macAddress ) )
            {
                case IP_ADDRESS_DUPLICATE:
                    Utils.getAlert ( AlertType.ERROR, "Duplicate IP Address Detected!", "This IP address is already registered in the database!" ).showAndWait();
                    return;

                case MAC_ADDRESS_DUPLICATE:
                    Utils.getAlert ( AlertType.ERROR, "Duplicate MAC Address Detected!", "This MAC address is already registered in the database!" ).showAndWait();
                    return;

                case BOTH_DUPLICATE:
                    Utils.getAlert ( AlertType.ERROR, "Duplicate IP and MAC Address Detected!", "This IP address and MAC address is already registered in the database!" ).showAndWait();
                    return;
            }
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
            Utils.getAlert ( AlertType.ERROR, "Error connecting to database!", "Error opening connection to database: " + e.getMessage() ).showAndWait();
            return;
        }

        try
        {
            Data.dbLocal.OpenDatabase();
        }
        catch ( final SQLException e )
        {
            e.printStackTrace();
            Utils.getAlert ( AlertType.ERROR, "Error connecting to database!", "Error opening connection to database: " + e.getMessage() ).showAndWait();
            return;
        }

        final String sql = "INSERT INTO \"Registered Network Switches\" (\"Technician\",\"Name\",\"IP Address\",\"MAC Address\") " +
                           "VALUES (?,?,?,?)";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, Data.username );
            ps.setString ( 2, name );
            ps.setString ( 3, ipAddress );
            ps.setString ( 4, macAddress );

            ps.executeUpdate();
            Data.dbLocal.Commit();

            Utils.getAlert ( AlertType.INFORMATION, "Network switch added to database!", "Network switch added to database successfully!" ).showAndWait();
        }
        catch ( final SQLException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                Data.dbLocal.CloseDatabase();
            }
            catch ( final SQLException e )
            {}
        }
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
