package network.pkgswitch.management.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import network.pkgswitch.management.system.models.UserManagement;

public class EnterNewPasswordDocumentController
{
    private UserManagement userManagement;

    public void setUser ( final UserManagement userManagement )
    {
        this.userManagement = userManagement;
        lblUsername.setText ( userManagement.getUsername() );
    }

    @FXML
    private void btnChangePasswordAction ( final ActionEvent event )
    {
        final String oldPassword = pwdOldPassword.getText();
        final String newPassword = pwdNewPassword.getText();

        if ( oldPassword.equals ( newPassword ) )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "New password equal to old password!", "New password must be different from old password!" ).showAndWait();
            return;
        }

        final String confirmPassword = pwdConfirmPassword.getText();

        if ( !confirmPassword.equals ( newPassword ) )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "New password not equal to confirm password!", "New password must be same as confirm password!" ).showAndWait();
            return;
        }

        try
        {
            Data.dbLocal.OpenDatabase();

            final String sqlCheck = "SELECT COUNT(*) FROM \"User Profile\" WHERE \"Username\"=? AND \"Password\"=?";

            boolean isValid = false;

            try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sqlCheck ) )
            {
                ps.setString ( 1, userManagement.getUsername() );
                ps.setString ( 2, oldPassword );

                try ( final ResultSet rs = ps.executeQuery() )
                {
                    rs.next();
                    isValid = ( rs.getLong ( 1 ) == 1 );
                }
            }

            if ( !isValid )
            {
                Utils.getAlert ( Alert.AlertType.ERROR, "Invalid password!", "Password was not correct!" ).showAndWait();
                return;
            }
            
            final String sqlUpdate = "UPDATE \"User Profile\" SET \"Password\"=? WHERE \"Username\"=?";

            boolean isDuplicate = true;

            try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sqlUpdate ) )
            {
                ps.setString ( 1, newPassword );
                ps.setString ( 2, userManagement.getUsername() );

                ps.executeUpdate();

                Data.dbLocal.Commit();
            }
            finally
            {
                try
                {
                    Data.dbLocal.CloseDatabase();
                }
                catch ( final Exception e )
                {}
            }

            Utils.getAlert ( Alert.AlertType.INFORMATION, "Password changed successfully!", "Password for user " + userManagement.getUsername() + " changed successfully!" ).showAndWait();
 
            btnCancelAction ( event );
        }
        catch ( final SQLException e )
        {
            Utils.getAlert ( Alert.AlertType.ERROR, "Change password failed!", "Unable to change password: " + e.getMessage() ).showAndWait();
        }
    }

    @FXML
    private void btnCancelAction ( final ActionEvent event )
    {
        final Node source = ( Node ) event.getSource();
        final Stage stage = ( Stage ) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    private Label lblUsername;

    @FXML
    private PasswordField pwdOldPassword, pwdNewPassword, pwdConfirmPassword;
}
