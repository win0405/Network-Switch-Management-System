package network.pkgswitch.management.system;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

public class Database
{
    private PgConnection connection = null;

    private int dbPort = 5432;
    private String dbName;
    private String dbUsername = "postgres";
    private String dbPassword = "12345678";

    private String serverAddress;
    private String connStr = "jdbc:postgresql://" + serverAddress + ":" + dbPort + "/" + dbName + "?user=" + dbUsername + "&password=" + dbPassword;

    public Database ( final String dbName, final String serverAddress )
    {
        this.dbName = dbName;
        setServerAddress ( serverAddress );
    }

    public int getPort()
    {
        return dbPort;
    }

    public void setPort ( final int port )
    {
        dbPort = port;
    }

    public String getDatabaseName()
    {
        return dbName;
    }

    public void setDatabaseName ( final String databaseName )
    {
        dbName = databaseName;
    }

    public String getUsername()
    {
        return dbUsername;
    }

    public void setUsername ( final String username )
    {
        dbUsername = username;
    }

    public String getPassword()
    {
        return dbPassword;
    }

    public void setPassword ( final String password )
    {
        dbPassword = password;
    }

    public String getServerAddress()
    {
        return serverAddress;
    }

    public void setServerAddress ( final String serverAddress )
    {
        this.serverAddress = serverAddress;
        UpdateConnectionString ( serverAddress );
    }

    public void UpdateConnectionString ( final String serverAddress )
    {
        connStr = "jdbc:postgresql://" + serverAddress + ":" + dbPort + "/" + dbName + "?user=" + dbUsername + "&password=" + dbPassword;
    }

    public static void InitDatabase()
    {
        try
        {
            Class.forName ( "org.postgresql.Driver" );
        }
        catch ( final ClassNotFoundException e )
        {
            e.printStackTrace();
        }
    }

    public void OpenDatabase() throws SQLException
    {
        CloseDatabase();

        DriverManager.setLoginTimeout ( 100 );
        connection = ( PgConnection ) DriverManager.getConnection ( connStr );

        connection.setAutoCommit ( false );
    }

    public PreparedStatement GetPreparedStatement ( final String sql ) throws SQLException
    {
        return connection.prepareStatement ( sql );
    }

    public static long NonQueryDatabase ( final PreparedStatement ps ) throws SQLException
    {
        long newID = -1;

        ResultSet rs = null;

        SQLException sqlException = null;

        try
        {
            rs = ps.executeQuery();

            rs.next();

            newID = rs.getLong ( 1 );
        }
        catch ( final SQLException e )
        {
            sqlException = e;
        }
        finally
        {
            try
            {
                if ( rs != null ) rs.close();
            }
            catch ( final SQLException e )
            {}

            try
            {
                if ( ps != null ) ps.close();
            }
            catch ( final SQLException e )
            {}
        }

        if ( sqlException != null )
            throw sqlException;

        return newID;
    }

    public void Commit() throws SQLException
    {
        connection.commit();
    }

    public void UnlinkLargeObject ( final long oid ) throws SQLException, IOException
    {
        connection.getLargeObjectAPI().unlink ( oid );
    }

    public Array CreateArrayOf ( final String typeName, final Object elements ) throws SQLException
    {
        return connection.createArrayOf ( typeName, elements );
    }
    
    public long WriteLargeObject ( final InputStream inputStream ) throws SQLException, IOException
    {
        final LargeObjectManager lobj = connection.getLargeObjectAPI();
        final long oid = lobj.createLO ( LargeObjectManager.READ | LargeObjectManager.WRITE );
        final LargeObject obj = lobj.open ( oid, LargeObjectManager.WRITE );

        final byte buf[] = new byte [ 2048 ];
        int data;

        while ( ( data = inputStream.read ( buf, 0, 2048 ) ) > 0 )
            obj.write ( buf, 0, data );

        obj.close();

        return oid;
    }

    public BufferedImage LoadImage ( final long oid ) throws SQLException, IOException
    {
        final LargeObjectManager lobj = connection.getLargeObjectAPI();
        final LargeObject objPicture = lobj.open ( oid, LargeObjectManager.READ );
        
        final byte[] bufPicture = new byte [ objPicture.size() ];
        objPicture.read ( bufPicture, 0, objPicture.size() );
        objPicture.close();
        
        return ImageIO.read ( new ByteArrayInputStream ( bufPicture ) );
    }

    public void CloseDatabase() throws SQLException
    {
        if ( connection != null )
        {
            connection.close();
            connection = null;
        }
    }
}
