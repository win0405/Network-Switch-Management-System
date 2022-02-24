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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class RegisterNewUserDocumentController implements Initializable
{
    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        choiceRole.setStyle ( "-fx-font: 24px \"Times New Roman\";");

        choiceRole.getItems().removeAll ( choiceRole.getItems() );
        choiceRole.getItems().addAll ( "Technician", "Manager" );
        choiceRole.getSelectionModel().select ( "Technician" );
    }

    @FXML
    private TextField txtName, txtEmail, txtUsername, txtPassword, txtConfirmPassword;

    @FXML
    private ChoiceBox choiceRole;

    private static Boolean checkDuplicateUsername ( final String username )
    {
        try
        {
            Data.dbLocal.OpenDatabase();
        }
        catch ( final SQLException e )
        {
            e.printStackTrace();
        }

        Boolean isDuplicate = null;
        final String sql = "SELECT COUNT(*) FROM \"User Profile\" WHERE \"Username\"=?";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, username );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                rs.next();
                isDuplicate = ( rs.getLong ( 1 ) > 0 );
            }
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

        return isDuplicate;
    }

    @FXML
    private void btnRegisterUserAction ( final ActionEvent event )
    {
        final String name = txtName.getText().trim();

        if ( name.isEmpty() )
        {
            Utils.getAlert ( AlertType.ERROR, "No name entered!", "Please enter name!" ).showAndWait();
            return;
        }

        final String email = txtEmail.getText().trim();

        if ( email.isEmpty() )
        {
            Utils.getAlert ( AlertType.ERROR, "No e-mail entered!", "Please enter e-mail!" ).showAndWait();
            return;
        }

        final String username = txtUsername.getText().trim();

        if ( username.isEmpty() )
        {
            Utils.getAlert ( AlertType.ERROR, "No username entered!", "Please enter username!" ).showAndWait();
            return;
        }

        Boolean isDuplicateUsername = checkDuplicateUsername ( username );

        if ( isDuplicateUsername == null )
        {
            Utils.getAlert ( AlertType.ERROR, "Error checking username with database!", "Unable to check for duplicate username with database!" ).showAndWait();
            return;
        }
        else if ( isDuplicateUsername )
        {
            Utils.getAlert ( AlertType.ERROR, "Duplicate username detected!", "This username already exist in database!" ).showAndWait();
            return;
        }

        final String password = txtPassword.getText().trim();

        if ( password.isEmpty() )
        {
            Utils.getAlert ( AlertType.ERROR, "No password entered!", "Please enter password!" ).showAndWait();
            return;
        }

        final String confirmPassword = txtConfirmPassword.getText().trim();

        if ( !confirmPassword.equals ( password ) )
        {
            Utils.getAlert ( AlertType.ERROR, "Mismatched passwords!", "The Confirmed Password and Password values entered do not match!" ).showAndWait();
            return;
        }

        final String role = ( String ) choiceRole.getSelectionModel().getSelectedItem();

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

        final String sql = "INSERT INTO \"User Profile\" (\"Name\",\"E-mail\",\"Username\",\"Password\",\"Role\",\"Status\") " +
                           "VALUES (?,?,?,?,?,'New')";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, name );
            ps.setString ( 2, email );
            ps.setString ( 3, username );
            ps.setString ( 4, password );
            ps.setString ( 5, role );

            ps.executeUpdate();
            Data.dbLocal.Commit();

            Utils.getAlert ( AlertType.INFORMATION, "User registration successful!", "User registered successfully to database!\n\nPlease give some time for the administrator to approve your accout before logging in." ).showAndWait();

            btnBackAction ( event );
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
            final Parent root = FXMLLoader.load ( getClass().getResource ( "LoginDocument.fxml" ) );
            NetworkSwitchManagementSystem.primaryStage.setScene ( new Scene ( root ) );
            NetworkSwitchManagementSystem.primaryStage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();

            Utils.getAlert ( AlertType.ERROR, "Error opening login dialog!", "Unable to open login dialog: " + e.getMessage() ).showAndWait();
        }
    }
}
