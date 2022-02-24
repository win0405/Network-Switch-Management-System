package network.pkgswitch.management.system;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import network.pkgswitch.management.system.models.UserManagement;
import network.pkgswitch.management.system.services.UserManagementServices;

public class UnbanUserDocumentController implements Initializable
{
    private ArrayList<UserManagement> alUsers;

    private TableColumn<UserManagement, String> createEditableTableColumns ( final String title, final String tableName )
    {
        final TableColumn<UserManagement, String> tableColumn = new TableColumn<> ( title );

        tableColumn.setStyle ( "-fx-alignment: CENTER;" );
        tableColumn.setCellValueFactory ( new PropertyValueFactory<> ( tableName ) );
        tableColumn.setCellFactory ( TextFieldTableCell.forTableColumn() );

        return tableColumn;
    }

    private void updateUsersTable ( final ObservableList<UserManagement> tvObservableList )
    {
        final TableColumn<UserManagement, Long> colNo = new TableColumn<> ( "No" );
        colNo.setStyle ( "-fx-alignment: CENTER;" );
        colNo.setCellValueFactory ( new PropertyValueFactory<> ( "no" ) );

        final TableColumn<UserManagement, Long> colUsername = new TableColumn<> ( "Username" );
        colUsername.setStyle ( "-fx-alignment: CENTER;" );
        colUsername.setCellValueFactory ( new PropertyValueFactory<> ( "username" ) );

        final TableColumn<UserManagement, String> colName = createEditableTableColumns ( "Name", "name" );
        colName.setOnEditCommit ( new EventHandler<TableColumn.CellEditEvent<UserManagement, String>>()
        {
            @Override
            public void handle ( TableColumn.CellEditEvent<UserManagement, String> event )
            {
                
                final UserManagement userManagement =
                    event.getTableView().getItems().get ( event.getTablePosition().getRow() );

                userManagement.setName ( event.getNewValue() );
            }
        } );

        final TableColumn<UserManagement, String> colEmail = createEditableTableColumns ( "E-mail", "email" );
        colEmail.setOnEditCommit ( new EventHandler<TableColumn.CellEditEvent<UserManagement, String>>()
        {
            @Override
            public void handle ( TableColumn.CellEditEvent<UserManagement, String> event )
            {
                
                final UserManagement userManagement =
                    event.getTableView().getItems().get ( event.getTablePosition().getRow() );

                userManagement.setEmail(event.getNewValue() );
            }
        } );

        final TableColumn<UserManagement, String> colRole = createEditableTableColumns ( "Role", "role" );
        colRole.setOnEditCommit ( new EventHandler<TableColumn.CellEditEvent<UserManagement, String>>()
        {
            @Override
            public void handle ( TableColumn.CellEditEvent<UserManagement, String> event )
            {
                
                final UserManagement userManagement =
                    event.getTableView().getItems().get ( event.getTablePosition().getRow() );

                userManagement.setRole(event.getNewValue() );
            }
        } );

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
                    private final Button btnUnban = new Button ( "Unban" );
                    {
                        btnUnban.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final UserManagement userManagement = getTableView().getItems().get ( getIndex() );

                            try
                            {
                                UserManagementServices.setUserStatus ( userManagement, "New" );

                                Utils.getAlert ( Alert.AlertType.INFORMATION, "User unbanned!",
                                    "User unbanned successfully in database!" ).showAndWait();

                                btnReloadFromDatabaseAction ( event );
                            }
                            catch ( final Exception e )
                            {
                                e.printStackTrace();

                                Utils.getAlert ( Alert.AlertType.ERROR, "Error unbanning user!",
                                    "Unable to unban user in database: " + e.getMessage() ).showAndWait();
                            }
                        } );
                    }

                    private final Button btnDelete = new Button ( "Delete" );
                    {
                        btnDelete.setOnAction ( ( final ActionEvent event ) ->
                        {
                            final UserManagement userManagement = getTableView().getItems().get ( getIndex() );

                            try
                            {
                                UserManagementServices.deleteUser ( userManagement.getUsername() );

                                Utils.getAlert ( Alert.AlertType.INFORMATION, "User deleted!",
                                    "User deleted successfully from database!" ).showAndWait();

                                btnReloadFromDatabaseAction ( event );
                            }
                            catch ( final Exception e )
                            {
                                e.printStackTrace();

                                Utils.getAlert ( Alert.AlertType.ERROR, "Error deleting user!",
                                    "Unable to delete user from database: " + e.getMessage() ).showAndWait();
                            }
                        } );
                    }

                    private final VBox pane = new VBox ( btnUnban, btnDelete );
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

        tblUsers.setEditable ( true );
    }

    @Override
    public void initialize ( final URL url, final ResourceBundle rb )
    {
        // TODO
        lblWelcome.setText ( "Welcome, " + Data.name + "!" );

        btnReloadFromDatabaseAction ( null );
    }

    @FXML
    private void btnReloadFromDatabaseAction ( final ActionEvent event )
    {
        final ObservableList<UserManagement> tvObservableList = FXCollections.observableArrayList();

        try
        {
            alUsers = UserManagementServices.readUsers ( "Banned" );
            tvObservableList.addAll ( alUsers.toArray ( new UserManagement [ alUsers.size() ] ) );
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }

        updateUsersTable ( tvObservableList );
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
