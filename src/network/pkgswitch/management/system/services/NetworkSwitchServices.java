package network.pkgswitch.management.system.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import network.pkgswitch.management.system.Data;
import network.pkgswitch.management.system.models.NetworkSwitch;

public class NetworkSwitchServices
{
    public enum Duplicate_t { NO_DUPLICATE, IP_ADDRESS_DUPLICATE, MAC_ADDRESS_DUPLICATE, BOTH_DUPLICATE }

    public static Duplicate_t checkDuplicateNetworkSwitch ( final String technician, final String ipAddress, final String macAddress ) throws Exception
    {
        Duplicate_t duplicateStatus = Duplicate_t.NO_DUPLICATE;

        Data.dbLocal.OpenDatabase();

        final String sql = "SELECT \"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" WHERE \"Technician\"=? AND \"IP Address\"=? OR \"MAC Address\"=?";

        Boolean isDuplicate = null;

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, technician );
            ps.setString ( 2, ipAddress );
            ps.setString ( 3, macAddress );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                if ( rs.next() )
                {
                    final String dbIPAddress = rs.getString ( 1 );
                    final String dbMACAddress = rs.getString ( 2 );

                    final boolean isDuplicateIPAddress = dbIPAddress.equals ( ipAddress );
                    final boolean isDuplicateMACAddress = dbMACAddress.equals ( macAddress );

                    if ( isDuplicateIPAddress )
                        duplicateStatus = isDuplicateMACAddress ? Duplicate_t.BOTH_DUPLICATE : Duplicate_t.IP_ADDRESS_DUPLICATE;
                    else
                        duplicateStatus = Duplicate_t.MAC_ADDRESS_DUPLICATE;
                }
            }
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}

        return duplicateStatus;
    }

    public static ArrayList<NetworkSwitch> readNetworkSwitches ( final long id ) throws Exception
    {
        final ArrayList<NetworkSwitch> alNetworkSwitches = new ArrayList<>();

        Data.dbLocal.OpenDatabase();

        final String sql;

        if ( id != Long.MIN_VALUE )
            sql = "SELECT \"Network Switch ID\",\"Technician\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" WHERE \"Network Switch ID\"=? ORDER BY \"Name\" ASC";
        else
            sql = "SELECT \"Network Switch ID\",\"Technician\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" ORDER BY \"Name\" ASC";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            if ( id != Long.MIN_VALUE )
                ps.setLong ( 1, id );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                long no = 1;

                while ( rs.next() )
                {
                    final long networkSwitchID = rs.getLong ( 1 );
                    final String technician = rs.getString ( 2 );
                    final String name = rs.getString ( 3 );
                    final String ipAddress = rs.getString ( 4 );
                    final String macAddress = rs.getString ( 5 );

                    alNetworkSwitches.add ( new NetworkSwitch ( networkSwitchID, no++, name, ipAddress, macAddress, technician, "---" ) );
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

    public static ArrayList<NetworkSwitch> readNetworkSwitches() throws Exception
    {
        return readNetworkSwitches ( Long.MIN_VALUE );
    }

    public static void updateNetworkSwitch ( final NetworkSwitch networkSwitch ) throws Exception
    {
        if ( networkSwitch == null )
            throw new Exception ( "Network Switch object cannot be null!" );

        Data.dbLocal.OpenDatabase();

        final String sql = "UPDATE \"Registered Network Switches\" SET \"Name\"=?,\"IP Address\"=?,\"MAC Address\"=? WHERE \"Network Switch ID\"=?";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, networkSwitch.getName() );
            ps.setString ( 2, networkSwitch.getIpAddress() );
            ps.setString ( 3, networkSwitch.getMacAddress() );
            ps.setLong ( 4, networkSwitch.getId() );

            ps.executeUpdate();
            Data.dbLocal.Commit();
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}
    }

    public static void deleteNetworkSwitch ( final long id ) throws Exception
    {
        Data.dbLocal.OpenDatabase();

        final String sql = "DELETE FROM \"Registered Network Switches\" WHERE \"Network Switch ID\"=?";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setLong ( 1, id );

            ps.executeUpdate();
            Data.dbLocal.Commit();
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}
    }

    public static boolean addNetworkSwitch ( final String name, final String ipAddress, final String macAddress, final String technician ) throws Exception
    {
        Data.dbLocal.OpenDatabase();

        String sql = "SELECT COUNT(*) FROM \"Registered Network Switches\" WHERE \"Name\"=? OR \"IP Address\"=? OR \"MAC Address\"=?";

        boolean isDuplicate = false;
        
        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, name );
            ps.setString ( 2, ipAddress );
            ps.setString ( 3, macAddress );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                rs.next();
                isDuplicate = ( rs.getLong ( 1 ) > 0 );
            }
        }

        if ( isDuplicate )
        {
            try
            {
                Data.dbLocal.CloseDatabase();
            }
            catch ( final Exception e )
            {}

            return false;
        }

        sql = "INSERT INTO \"Registered Network Switches\" (\"Name\",\"IP Address\",\"MAC Address\",\"Technician\") VALUES (?,?,?,?)";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, name );
            ps.setString ( 2, ipAddress );
            ps.setString ( 3, macAddress );
            ps.setString ( 4, technician );

            ps.executeUpdate();
            Data.dbLocal.Commit();
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}

        return true;
    }

    public static boolean addNetworkSwitch ( final NetworkSwitch networkSwitch ) throws Exception
    {
        return addNetworkSwitch ( networkSwitch.getName(), networkSwitch.getIpAddress(),
                networkSwitch.getMacAddress(), networkSwitch.getTechnician() );
    }
}
