package network.pkgswitch.management.system.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import network.pkgswitch.management.system.Data;
import network.pkgswitch.management.system.models.NetworkSwitch;
import network.pkgswitch.management.system.models.UserManagement;

public class UserManagementServices
{
    public static boolean isDuplicateUsername ( final String username ) throws Exception
    {
        Data.dbLocal.OpenDatabase();

        final String sql = "SELECT COUNT(*) FROM \"User Profile\" WHERE \"Username\"=?";

        boolean isDuplicate = true;

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, username );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                rs.next();
                isDuplicate = ( rs.getLong ( 1 ) != 0 );
            }
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}

        return isDuplicate;
    }

    public static ArrayList<UserManagement> readUsers ( final String status ) throws Exception
    {
        final ArrayList<UserManagement> alUsers = new ArrayList<>();

        Data.dbLocal.OpenDatabase();

        final String sql = "SELECT \"Username\",\"Name\",\"E-mail\",\"Role\" FROM \"User Profile\" WHERE \"Status\"=? ORDER BY \"Username\" ASC";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, status );

            try ( final ResultSet rs = ps.executeQuery() )
            {
                long id = 1;

                while ( rs.next() )
                {
                    final String username = rs.getString ( 1 );
                    final String name = rs.getString ( 2 );
                    final String email = rs.getString ( 3 );
                    final String role = rs.getString ( 4 );

                    alUsers.add ( new UserManagement ( id++, username, name, email, role ) );
                }
            }
        }

        try
        {
            Data.dbLocal.CloseDatabase();
        }
        catch ( final Exception e )
        {}

        return alUsers;
    }

    public static void updateUser ( final UserManagement userManagement ) throws Exception
    {
        if ( userManagement == null )
            throw new Exception ( "User object cannot be null!" );

        //if ( isDuplicateUsername ( userManagement.getUsername() ) )
        //    throw new Exception ( "Username already exist in database!" );

        Data.dbLocal.OpenDatabase();

        final String sql = "UPDATE \"User Profile\" SET \"Name\"=?,\"E-mail\"=?,\"Role\"=? WHERE \"Username\"=?";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, userManagement.getName() );
            ps.setString ( 2, userManagement.getEmail() );
            ps.setString ( 3, userManagement.getRole() );
            ps.setString ( 4, userManagement.getUsername() );

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

    public static void setUserStatus ( final UserManagement userManagement, final String status ) throws Exception
    {
        if ( userManagement == null )
            throw new Exception ( "User object cannot be null!" );

        Data.dbLocal.OpenDatabase();

        final String sql = "UPDATE \"User Profile\" SET \"Status\"=? WHERE \"Username\"=?";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, status );
            ps.setString ( 2, userManagement.getUsername() );

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

    public static void deleteUser ( final String username ) throws Exception
    {
        Data.dbLocal.OpenDatabase();

        final String sql = "DELETE FROM \"User Profile\" WHERE \"Username\"=?";

        try ( final PreparedStatement ps = Data.dbLocal.GetPreparedStatement ( sql ) )
        {
            ps.setString ( 1, username );

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
}
