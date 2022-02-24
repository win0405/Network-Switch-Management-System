package network.pkgswitch.management.system.models;

public class LoginHistory
{
    private long no;
    private String date;
    private String time;
    private String username;
    private String name;
    private String email;

    public LoginHistory ( final long no, final String date, final String time, final String username, final String name, final String email )
    {
        this.no = no;
        this.date = date;
        this.time = time;
        this.username = username;
        this.name = name;
        this.email = email;
    }

    public long getNo() { return no; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void setNo ( final long no ) { this.no = no; }
    public void setDate ( final String date ) { this.date = date; }
    public void setTime ( final String time ) { this.time = time; }
    public void setUsername ( final String username ) { this.username = username; }
    public void setName ( final String name ) { this.name = name; }
    public void setEmail ( final String email ) { this.email = email; }

    public String toString()
    {
        return "" + no + ": " + date + ", " + time + ", " + username + ", " + name + ", " + email;
    }
}
