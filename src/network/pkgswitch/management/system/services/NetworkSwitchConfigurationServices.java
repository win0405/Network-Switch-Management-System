package network.pkgswitch.management.system.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import network.pkgswitch.management.system.Data;
import network.pkgswitch.management.system.models.NetworkSwitchConfiguration;

public class NetworkSwitchConfigurationServices
{
    public static ArrayList<NetworkSwitchConfiguration> readNetworkSwitches ( final long id ) throws Exception
    {
        final ArrayList<NetworkSwitchConfiguration> alNetworkSwitches = new ArrayList<>();

        Data.dbLocal.OpenDatabase();

        final String sql;

        if ( id != Long.MIN_VALUE )
            sql = "SELECT \"Network Switch ID\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" WHERE \"Network Switch ID\"=? ORDER BY \"Name\" ASC";
        else
            sql = "SELECT \"Network Switch ID\",\"Name\",\"IP Address\",\"MAC Address\" FROM \"Registered Network Switches\" ORDER BY \"Name\" ASC";

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
                    final String name = rs.getString ( 2 );
                    final String ipAddress = rs.getString ( 3 );
                    final String macAddress = rs.getString ( 4 );

                    alNetworkSwitches.add ( new NetworkSwitchConfiguration ( networkSwitchID, no++, name, ipAddress, macAddress, "---", "---", "---" ) );
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

    public static ArrayList<NetworkSwitchConfiguration> readNetworkSwitches() throws Exception
    {
        return readNetworkSwitches ( Long.MIN_VALUE );
    }
}
