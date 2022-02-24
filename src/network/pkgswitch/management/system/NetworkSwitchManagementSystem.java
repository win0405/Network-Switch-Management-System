package network.pkgswitch.management.system;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NetworkSwitchManagementSystem extends Application
{
    private final static SplashScreen splash = new SplashScreen();

    public static Stage primaryStage;

    @Override
    public void start ( final Stage stage )
    {
        primaryStage = stage;

        try
        {
            final Parent root = FXMLLoader.load ( getClass().getResource ( "LoginDocument.fxml" ) );
            stage.setTitle ( "Network Switch Management System" );
            stage.initStyle ( StageStyle.UTILITY );

            splash.hide();

            stage.setScene ( new Scene ( root ) );
            stage.show();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
            Runtime.getRuntime().exit ( -1 );
        }

        stage.setOnCloseRequest ( event -> event.consume() );
    }

    public static void main ( final String[] args )
    {
        // TODO code application logic here
        splash.show ( 3000 );

        launch ( args );
    }
}
