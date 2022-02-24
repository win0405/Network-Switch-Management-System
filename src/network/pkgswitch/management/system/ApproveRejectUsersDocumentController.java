package network.pkgswitch.management.system;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
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
import network.pkgswitch.management.system.models.UserManagement;
import network.pkgswitch.management.system.services.UserManagementServices;

public class ApproveRejectUsersDocumentController implements Initializable
{
    private ArrayList<UserManagement> alUsers;

    private void updateUsersTable()
    {
        final ObservableList<UserManagement> tvObservableList = FXCollections.observableArrayList();

        try
        {
            alUsers = UserManagementServices.readUsers ( "New" );
            tvObservableList.addAll ( alUsers.toArray ( new UserManagement [ alUsers.size() ] ) );
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }

        final TableColumn<UserManagement, Long> colNo = new TableColumn<> ( "No" );
        colNo.setStyle ( "-fx-alignment: CENTER;" );
        colNo.setCellValueFactory ( new PropertyValueFactory<> ( "no" ) );

        final TableColumn<UserManagement, Long> colUsername = new TableColumn<> ( "Username" );
        colUsername.setStyle ( "-fx-alignment: CENTER;" );
        colUsername.setCellValueFactory ( new PropertyValueFactory<> ( "username" ) );

        final TableColumn<UserManagement, Long> colName = new TableColumn<> ( "Name" );
        colName.setStyle ( "-fx-alignment: CENTER;" );
        colName.setCellValueFactory ( new PropertyValueFactory<> ( "name" ) );

        final TableColumn<UserManagement, Long> colEmail = new TableColumn<> ( "E-mail" );
        colEmail.setStyle ( "-fx-alignment: CENTER;" );
        colEmail.setCellValueFactory ( new PropertyValueFactory<> ( "email" ) );

        final TableColumn<UserManagement, Long> colRole = new TableColumn<> ( "Role" );
        colRole.setStyle ( "-fx-alignment: CENTER;" );
        colRole.setCellValueFactory ( new PropertyValueFactory<> ( "role" ) );

        tblUsers.getColumns().clear();
        tblUsers.getColumns().addAll ( colNo, colUsername, colName, colEmail, colRole );

        tblUsers.setItems ( tvObservableList );

        final TableColumn<UserManagement, Void> colAction = new TableColumn ( "Action" );

        final Callback<TableColumn<UserManagement, Void>, TableCell<UserManagement, Void>> cellFactory =
                new Callback<TableColumn<UserManagement, Void>, TableCell<UserManagement, Void>>()
        {
            @Override
            public TableCell<UserManagement, Void> call ( final TableColumn<UserManagement, Void> param )
            {
                final TableCell<UserManagement, Void> cell = new TableCell<UserManagement, Void>()
                {
                    private final Button btnApprove = new Button ( "Approve" );
                    {
                        btnApprove.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final UserManagement userManagement = getTableView().getItems().get ( getIndex() );

                            try
                            {
                                UserManagementServices.setUserStatus ( userManagement, "Approve" );

                                Utils.getAlert ( Alert.AlertType.INFORMATION, "User approval successful!",
                                    "User approved successfully in database!" ).showAndWait();

                                updateUsersTable();
                            }
                            catch ( final Exception e )
                            {
                                e.printStackTrace();

                                Utils.getAlert ( Alert.AlertType.ERROR, "Error approving user!",
                                    "Unable to approve user in database: " + e.getMessage() ).showAndWait();
                            }
                        } );
                    }

                    private final Button btnReject = new Button ( "Reject" );
                    {
                        btnReject.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final UserManagement userManagement = getTableView().getItems().get ( getIndex() );

                            try
                            {
                                UserManagementServices.setUserStatus ( userManagement, "Reject" );

                                Utils.getAlert ( Alert.AlertType.INFORMATION, "User rejection successful!",
                                    "User rejected successfully in database!" ).showAndWait();

                                updateUsersTable();
                            }
                            catch ( final Exception e )
                            {
                                e.printStackTrace();

                                Utils.getAlert ( Alert.AlertType.ERROR, "Error rejecting user!",
                                    "Unable to reject user in database: " + e.getMessage() ).showAndWait();
                            }
                        } );
                    }

                    private final HBox pane = new HBox ( btnApprove, new Label ( " " ), btnReject );
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
        tblUsers.getColumns().add ( colAction );
    }

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );

        updateUsersTable();
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

    @FXML
    private Label lblWelcome;

    @FXML
    private TableView tblUsers;
}
