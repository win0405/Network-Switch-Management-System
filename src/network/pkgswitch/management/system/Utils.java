package network.pkgswitch.management.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import javafx.scene.control.Alert;
import network.pkgswitch.management.system.models.NetworkSwitchConfiguration;

public class Utils
{
    private final static String RESOURCE_PATH = "/network/pkgswitch/management/system/resources/";

    private final static String SOFTWARE_VERSION_REPLY = "Software Version............................... ";
    private final static String MACHINE_MODEL_REPLY    = "Machine Model.................................. ";
    private final static String SERIAL_NUMBER_REPLY    = "Serial Number.................................. ";
    private final static String MAC_ADDRESS_REPLY      = "Burned In MAC Address.......................... ";
                
    public static URL GetResourceURL ( final String resourceFilename )
    {
        return NetworkSwitchManagementSystem.class.getResource ( RESOURCE_PATH + resourceFilename );
    }

    public static String GetResourcePath ( final String resourceFilename )
    {
        final String resourcePath = GetResourceURL ( resourceFilename ).getFile();
        final int pos = resourcePath.indexOf ( "/" ) + 1;

        return resourcePath.substring ( pos );
    }

    public static int[] Unbox ( final Integer[] array )
    {
        final int[] output = new int [ array.length ];
        
        for ( int index = 0; index < array.length; ++index )
            output [ index ] = array [ index ];
        
        return output;
    }

    public static ArrayList<String> RunCommand ( final String command )
    {
        final ArrayList<String> alResult = new ArrayList<>();

        try
        {
            final Process p = Runtime.getRuntime().exec ( command );

            final BufferedReader stdInput = new BufferedReader ( new InputStreamReader ( p.getInputStream() ) );

            // read the output from the command
            String line;

            while ( ( line = stdInput.readLine() ) != null )
            {
                if ( !line.isEmpty() )
                    alResult.add ( line );
            }
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
        }
        
        return alResult;
    }

    public static boolean ContainsIgnoreCase ( final ArrayList<String> alHaystack, final String needle )
    {
        final String needleLower = needle.toLowerCase();

        boolean isContains = false;

        for ( final String haystack : alHaystack )
        {
            if ( needleLower.equals ( haystack.toLowerCase() ) )
            {
                isContains = true;
                break;
            }
        }

        return isContains;
    }

    public static ArrayList<String> GetFilenames ( final String directory, final String prefix, final String suffix ) throws IOException
    {
        final ArrayList<String> alFilenames = new ArrayList<>();

        final Path folder = Paths.get ( directory );
        
        try ( final DirectoryStream<Path> stream = Files.newDirectoryStream ( folder ) )
        {
            for ( final Path path : stream )
            {
                if ( path.toFile().isFile() )
                {
                    final String filename = path.getFileName().toString();

                    if ( ( prefix.isEmpty() || filename.startsWith ( prefix ) ) && ( suffix.isEmpty() || filename.endsWith ( suffix ) ) )
                    {
                        alFilenames.add ( filename.substring ( prefix.length(), filename.length() - suffix.length() - 1 ) );
                    }
                }
            }
        }
        catch ( final IOException e )
        {
            throw e;
        }

        return alFilenames;
    }

    public static int[] GetFileIndices ( final String directory, final String prefix, final String suffix ) throws IOException
    {
        final ArrayList<Integer> alFileIndices = new ArrayList<>();

        final Path folder = Paths.get ( directory );
        
        try ( final DirectoryStream<Path> stream = Files.newDirectoryStream ( folder ) )
        {
            for ( final Path path : stream )
            {
                if ( path.toFile().isFile() )
                {
                    final String filename = path.getFileName().toString();

                    if ( ( prefix.isEmpty() || filename.startsWith ( prefix ) ) && ( suffix.isEmpty() || filename.endsWith ( suffix ) ) )
                    {
                        try
                        {
                            final int index = Integer.parseInt ( filename.substring ( prefix.length(), filename.length() - suffix.length() ) );
                            alFileIndices.add ( index );
                        }
                        catch ( final NumberFormatException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch ( final IOException e )
        {
            throw e;
        }

        Collections.sort ( alFileIndices );

        final int[] arrFileIndices = new int [ alFileIndices.size() ];

        for ( int index = 0; index < alFileIndices.size(); ++index )
            arrFileIndices [ index ] = alFileIndices.get ( index );

        return arrFileIndices;
    }

    public static void addLibraryPath ( final String pathToAdd ) throws IllegalAccessException, NoSuchFieldException
    {
        final Field fieldUsrPaths = ClassLoader.class.getDeclaredField ( "usr_paths" );
        fieldUsrPaths.setAccessible ( true );

        // get array of paths
        final String[] paths = ( String[] ) fieldUsrPaths.get ( null );

        // check if the path to add is already present
        for ( final String path : paths )
        {
            if ( path.equals ( pathToAdd ) )
                return;
        }

        // add the new path
        final String[] newPaths = Arrays.copyOf ( paths, paths.length + 1 );
        newPaths [ newPaths.length - 1 ] = pathToAdd;
        fieldUsrPaths.set ( null, newPaths );
    }

    public static void addLibraryPaths ( final String[] pathsToAdd ) throws IllegalAccessException, NoSuchFieldException
    {
        for ( final String pathToAdd : pathsToAdd )
            addLibraryPath ( pathToAdd );
    }

    public static void addModule ( final String moduleName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        final ModuleLayer boot = ModuleLayer.boot();
        final Module module = boot.findModule ( "java.base" ).get();

        final Method addOpens = module.getClass().getDeclaredMethod ( "implAddOpens", String.class );

        addOpens.setAccessible ( true );
        addOpens.invoke ( module, "jdk.internal.loader" );

        final Optional<ModuleReference> ref = ModuleFinder.ofSystem().find( moduleName );
        final ClassLoader scl = ClassLoader.getSystemClassLoader();

        final Method loadModule = scl.getClass().getMethod ( "loadModule", ModuleReference.class );
        loadModule.invoke ( scl, ref.get() );
    }

    public static Alert getAlert ( final Alert.AlertType alertType, final String title, final String content )
    {
        final Alert alert = new Alert ( alertType, content );
        alert.setTitle( title );
        alert.setHeaderText ( "" );

        return alert;
    }

    public static String getMACAddress ( final String ipAddress ) throws UnsupportedEncodingException, IOException
    {
        String macAddress = null;

        final Process pingProcess = Runtime.getRuntime().exec ( "arp -a" );

        try ( final InputStream inputStream = pingProcess.getInputStream();
              final BufferedReader br = new BufferedReader ( new InputStreamReader ( inputStream,"gb2312" ) ) )
        {
            String line;

            while ( ( line = br.readLine() ) != null )
            {
                final String[] fields = line.trim().split ( "\\s+" );

                if ( fields.length == 3 && fields [ 0 ].equals ( ipAddress ) )
                {
                    macAddress = fields [ 1 ];
                }
            }
        }

        return macAddress;
    }

    public static String pingNetworkSwitch ( final String ipAddress )
    {
        String status = "Down";

        try
        {
            final Process pingProcess = Runtime.getRuntime().exec ( "ping " + ipAddress );

            try ( final InputStream inputStream = pingProcess.getInputStream();
                  final BufferedReader br = new BufferedReader ( new InputStreamReader ( inputStream,"gb2312" ) ) )
            {
                String line;

                while ( ( line = br.readLine() ) != null )
                {
                    if ( line.startsWith ( "Reply from " ) && line.contains ( "bytes" ) && line.contains ( "time" ) && line.contains ( "TTL" ) )
                    {
                        status = "Up";
                        break;
                    }
                    else if ( line.equals ( "Request timed out." ) || line.contains ( "Destination host unreachable." ) )
                    {
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
            Utils.getAlert ( Alert.AlertType.ERROR, "Error executing ping command!", "Unable to perform ping: " + e.getMessage() ).showAndWait();
        }

        return status;
    }

    public static int[] parseIpAddress ( final String ip ) throws IllegalArgumentException
    {
	final String[] fields = ip.split ( "\\." );

	if ( fields.length != 4 )
        {
            throw new IllegalArgumentException ( "IP address must be in the format 'xxx.xxx.xxx.xxx'" );
	}

        final int[] ipAddress = new int [ 4 ];

        for ( int index = 0; index < 4; ++index )
        {
            try
            {
                final int value = Integer.parseInt ( fields [ index ] );

                if ( value < 0 || value > 254 )
                    throw new IllegalArgumentException ( "Illegal value '" + fields [ index ] + "' at byte " + ( index + 1 ) + " in the IP address." );

                ipAddress [ index ] = value;
            }
            catch ( final NumberFormatException e )
            {
                throw new IllegalArgumentException ( "Illegal value '" + fields [ index ] + "' at byte " + ( index + 1 ) + " in the IP address." );
            }
        }

        return ipAddress;
    }

    public static String[] retrieveSwitchInformation ( final String ipAddress, final String username, final String password )
    {
        String[] switchInformation = null;
   
        try ( final Socket pingSocket = new Socket ( ipAddress, 23 );
              final PrintWriter out = new PrintWriter ( pingSocket.getOutputStream(), true );
              final BufferedReader in = new BufferedReader ( new InputStreamReader(pingSocket.getInputStream() ) ) )
        {
            while ( !in.ready() )
                continue;

            System.out.println ( in.readLine());
            out.println ( username );
            out.println ( password );

            while ( !in.ready() )
                continue;

            System.out.println ( in.readLine()); System.out.flush();
            System.out.println ( in.readLine()); System.out.flush();

            out.println ( "enable" );
            System.out.println ( in.readLine()); System.out.flush();

            out.println ( "show version" );

            in.readLine();
            System.out.println ( in.readLine() ); System.out.flush();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            //System.out.println ( "Model: " + in.readLine() ); System.out.flush();
            //System.out.println ( "Serial: " + in.readLine() ); System.out.flush();
            //System.out.println ( "MAC: " + in.readLine() ); System.out.flush();
            //System.out.println ( "SW Ver: " + in.readLine() ); System.out.flush();

            String softwareVersion = "---", modelNumber = "---", serialNumber = "---", macAddress = "---";

            String[] fields = in.readLine().split ( "\\.\\.\\.\\.\\. " );
            modelNumber = fields [ 1 ];

            fields = in.readLine().split ( "\\.\\.\\.\\.\\. " );
            serialNumber = fields [ 1 ];

            fields = in.readLine().split ( "\\.\\.\\.\\.\\. " );
            macAddress = fields [ 1 ];

            fields = in.readLine().split ( "\\.\\.\\.\\.\\. " );
            softwareVersion = fields [ 1 ];

            /*
            while ( in.ready() )
            {
                final String line = in.readLine();

                final int posSoftwareVersion = line.indexOf ( SOFTWARE_VERSION_REPLY );

                if ( posSoftwareVersion >= 0 )
                {
                    softwareVersion = line.substring ( posSoftwareVersion + SOFTWARE_VERSION_REPLY.length() );
                    continue;
                }

                final int posMachineModel = line.indexOf ( MACHINE_MODEL_REPLY );

                if ( posMachineModel >= 0 )
                {
                    modelNumber = line.substring ( posMachineModel + MACHINE_MODEL_REPLY.length() );
                    continue;
                }

                final int posSerialNumber = line.indexOf ( SERIAL_NUMBER_REPLY );

                if ( posSerialNumber >= 0 )
                {
                    serialNumber = line.substring ( posSerialNumber + SERIAL_NUMBER_REPLY.length() );
                    continue;
                }

                final int posMACAddress = line.indexOf ( MAC_ADDRESS_REPLY );

                if ( posMACAddress >= 0 )
                {
                    macAddress = line.substring ( posMACAddress + MAC_ADDRESS_REPLY.length() );
                    continue;
                }
            }
            */

            switchInformation = new String[] { softwareVersion, modelNumber, serialNumber, macAddress };
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
        }

        return switchInformation;
    }

    public static int[] findStartEndPos ( final String str, final String prefix, final String suffix )
    {
        final int startPos = str.indexOf ( prefix );

        if ( startPos < 0 ) return null;

        final int endPos = str.indexOf ( suffix );

        if ( endPos <= startPos ) return null;

        return new int[] { startPos, endPos };
    }

    public static ArrayList<NetworkSwitchConfiguration> readNetworkSwitchesByIPAddress ( final String ipAddress ) throws Exception
    {
        final ArrayList<NetworkSwitchConfiguration> alNetworkSwitches = new ArrayList<>();

        Data.dbLocal.OpenDatabase();

        final String sql;

        if ( ipAddress != null && !ipAddress.isEmpty() )
            sql = "SELECT \"Network Switch ID\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" WHERE \"IP Address\"=? ORDER BY \"Name\" ASC";
        else
            sql = "SELECT \"Network Switch ID\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" ORDER BY \"Name\" ASC";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            if ( ipAddress != null && !ipAddress.isEmpty() )
                ps.setString ( 1, ipAddress );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                long no = 1;

                while ( rs.next() )
                {
                    final long networkSwitchID = rs.getLong ( 1 );
                    final String name = rs.getString ( 2 );
                    final String retrievedIPAddress = rs.getString ( 3 );
                    final String macAddress = rs.getString ( 4 );

                    alNetworkSwitches.add ( new NetworkSwitchConfiguration ( networkSwitchID, no++, name, retrievedIPAddress, macAddress, "---", "---", "---" ) );
                }
            }
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}

        return alNetworkSwitches;
    }

    public static ArrayList<NetworkSwitchConfiguration> readNetworkSwitchesByMACAddress ( final String macAddress ) throws Exception
    {
        final ArrayList<NetworkSwitchConfiguration> alNetworkSwitches = new ArrayList<>();

        Data.dbLocal.OpenDatabase();

        final String sql;

        if ( macAddress != null && !macAddress.isEmpty() )
            sql = "SELECT \"Network Switch ID\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" WHERE \"MAC Address\"=? ORDER BY \"Name\" ASC";
        else
            sql = "SELECT \"Network Switch ID\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" ORDER BY \"Name\" ASC";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            if ( macAddress != null && !macAddress.isEmpty() )
                ps.setString ( 1, macAddress );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                long no = 1;

                while ( rs.next() )
                {
                    final long networkSwitchID = rs.getLong ( 1 );
                    final String name = rs.getString ( 2 );
                    final String ipAddress = rs.getString ( 3 );
                    final String retrievedMACAddress = rs.getString ( 4 );

                    alNetworkSwitches.add ( new NetworkSwitchConfiguration ( networkSwitchID, no++, name, ipAddress, retrievedMACAddress, "---", "---", "---" ) );
                }
            }
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}

        return alNetworkSwitches;
    }
}
