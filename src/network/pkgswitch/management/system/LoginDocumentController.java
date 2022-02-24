package network.pkgswitch.management.system;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

public class LoginDocumentController implements Initializable
{
    private static String status = "";

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
    }

    @FXML
    private TextField txtUsername, txtPassword;

    private static boolean performLogin ( final String username, final String password )
    {
        try
        {
            Data.dbLocal.OpenDatabase();
        }
        catch ( final SQLException e )
        {
            e.printStackTrace();
        }

        String name = null;
        String role = null;

        final String sql = "SELECT \"Name\",\"Role\",\"Status\" FROM \"User Profile\" WHERE \"Username\"=? AND \"Password\"=?";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, username );
            ps.setString ( 2, password );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                if ( rs.next() )
                {
                    name = rs.getString ( 1 );
                    role = rs.getString ( 2 );
                    status = rs.getString ( 3 );

                    Data.username = username;
                    Data.name = name;
                    Data.role = role;
                }
            }
        }
        catch ( final SQLException e )
        {
            e.printStackTrace();
        }

        boolean isUserFound = ( name != null && role != null && status != null );

        if ( isUserFound )
        {
            if ( status.equals ( "Approve" ) )
            {
                final String sqlHistory = "INSERT INTO \"Login History\" (\"Username\",\"Timestamp\") VALUES (?,NOW())";

                try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sqlHistory ) )
                {
                    ps.setString ( 1, username );

                    ps.executeUpdate();

                    Data.dbLocal.Commit();
                }
                catch ( final SQLException e )
                {
                    e.printStackTrace();
                }
            }
            else if ( status.equals ( "Reject" ) )
            {
                Utils.getAlert ( AlertType.WARNING, "Account rejected!", "Your account has been rejected... please contact the administrator for further clarification!" ).showAndWait();
            }
            else if ( status.equals ( "New" ) )
            {
                Utils.getAlert ( AlertType.INFORMATION, "Account pending approval!", "Your account is still pending approval.\n\nPlease give some time for the administrator to approve your account!" ).showAndWait();
            }
            else if ( status.equals ( "Banned" ) )
            {
                Utils.getAlert ( AlertType.ERROR, "Account banned!", "Your account has been banned... please contact the administrator for further clarification!" ).showAndWait();
            }
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final SQLException e )
        {}

        return isUserFound;
    }

    @FXML
    private void btnLoginAction ( final ActionEvent event )
    {
        final String username = txtUsername.getText().trim();

        if ( username.isEmpty() )
        {
            Utils.getAlert ( AlertType.ERROR, "No username entered!", "Please enter username!" ).showAndWait();
            return;
        }

        final String password = txtPassword.getText().trim();

        if ( password.isEmpty() )
        {
            Utils.getAlert ( AlertType.ERROR, "No password entered!", "Please enter password!" ).showAndWait();
            return;
        }

        if ( !performLogin ( username, password ) )
        {
            Utils.getAlert ( AlertType.ERROR, "Login failed!", "Invalid username and/or password!" ).showAndWait();
            return;
        }

        if ( !status.equals ( "Approve" ) )
            return;

        Utils.getAlert ( AlertType.INFORMATION, "Login successful!", "Logged in as " + Data.name + " with role " + Data.role + "!" ).showAndWait();

        switch ( Data.role )
        {
            case "Technician":
                try
                {
                    final Parent root = FXMLLoader.load ( getClass().getResource ( "TechnicianMainDocument.fxml" ) );
                    NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
                    NetworkSwitchManagementSystem.primaryStage.show();
                }
                catch ( final IOException e )
                {
                    e.printStackTrace();

                    Utils.getAlert ( AlertType.ERROR, "Error opening Technician Main dialog!", "Unable to open Technician Main dialog: " + e.getMessage() ).showAndWait();
                }
                break;

            case "Manager":
                try
                {
                    final Parent root = FXMLLoader.load ( getClass().getResource ( "ManagerMainDocument.fxml" ) );
                    NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
                    NetworkSwitchManagementSystem.primaryStage.show();
                }
                catch ( final IOException e )
                {
                    e.printStackTrace();

                    Utils.getAlert ( AlertType.ERROR, "Error opening Manager Main dialog!", "Unable to open Manager Main dialog: " + e.getMessage() ).showAndWait();
                }
                break;
        }
    }

    @FXML
    private void btnClearAction ( final ActionEvent event )
    {
        txtUsername.setText ( "" );
        txtPassword.setText ( "" );
    }

    @FXML
    private void lnkRegisterNewUserAction ( final ActionEvent event )
    {
        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "RegisterNewUserDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( AlertType.ERROR, "Error opening register new user dialog!", "Unable to open register new user dialog: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnQuitAction ( final ActionEvent event )
    {
        Runtime.getRuntime().exit ( 0 );
    }
}
