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

public class ManagerMainDocumentController implements Initializable
{
    @FXML
    private Label lblWelcome;

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );
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
    private void btnLoginHistoryReportAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "LoginHistoryReportDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Login History Report dialog!", "Unable to open Login History Report dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnConfigurationChangeHistoryReportAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "ConfigurationChangeHistoryDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Login History Report dialog!", "Unable to open Login History Report dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnUserManagementAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "UserManagementDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.setX ( -10 );
            NetworkSwitchManagementSystem.primaryStage.setY ( 0 );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening User Management dialog!", "Unable to open User Management dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnApproveRejectUsersAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "ApproveRejectUsersDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.setX ( -10 );
            NetworkSwitchManagementSystem.primaryStage.setY ( 0 );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Approve/Reject User dialog!", "Unable to open Approve/Reject User dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnUnbanUsersAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "UnbanUserDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.setX ( -10 );
            NetworkSwitchManagementSystem.primaryStage.setY ( 0 );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( Alert.AlertType.ERROR, "Error opening Unban Users dialog!", "Unable to open Unban Users dialog: " + e.getMessage() ).showAndWait();
        }
    }
}
