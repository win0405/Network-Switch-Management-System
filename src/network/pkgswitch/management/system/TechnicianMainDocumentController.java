package network.pkgswitch.management.system;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class TechnicianMainDocumentController implements Initializable
{
    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );
    }

    @FXML
    private Label lblWelcome;

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

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Login dialog!", "Unable to open Login dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnNetworkSwitchManagementAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "NetworkSwitchManagementDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.setX ( -10 );
            NetworkSwitchManagementSystem.primaryStage.setY ( 0 );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Network Switch Management dialog!", "Unable to open Network Switch Management dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnPingTestAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "PingTestDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Ping Test dialog!", "Unable to open Ping Test dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnNetworkSwitchDiscoveryAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "NetworkSwitchDiscoveryDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Network Switch Discovery dialog!", "Unable to open Network Switch Discovery dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnNetworkSwitchConfigurationCheckAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "NetworkSwitchConfigurationCheckDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.setX ( -10 );
            NetworkSwitchManagementSystem.primaryStage.setY ( 0 );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Network Switch Configuration Check dialog!", "Unable to open Network Switch Configuration Check dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnNetworkSwitchConfigurationSettingAction ( final ActionEvent event )
    {
        new Thread ( () -> Utils.RunCommand ( Data.networkSwitchConfigurationProgramPath ) ).start();
    }
}
