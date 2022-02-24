package network.pkgswitch.management.system.models;

public class UserManagement
{
    private long no;
    private String username;
    private String name;
    private String email;
    private String role;

    public UserManagement ( final long no, final String username, final String name, final String email, final String role )
    {
        this.no = no;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public long getNo() { return no; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public void setNo ( final long no ) { this.no = no; }
    public void setUsername ( final String username ) { this.username = username; }
    public void setName ( final String name ) { this.name = name; }
    public void setEmail ( final String email ) { this.email = email; }
    public void setRole ( final String role ) { this.role = role; }

    @Override
    public String toString()
    {
        return "" + no + ": " + username + ", " + name + ", " + email + ", " + role;
    }
}
