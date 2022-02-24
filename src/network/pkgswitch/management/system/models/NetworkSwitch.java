package network.pkgswitch.management.system.models;

public class NetworkSwitch
{
    private long id;
    private long no;
    private String name;
    private String ipAddress;
    private String macAddress;
    private String technician;
    private String status;

    public NetworkSwitch ( final long id, final long no, final String name, final String ipAddress, final String macAddress, final String technician, final String status )
    {
        this.id = id;
        this.no = no;
        this.name = name;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.technician = technician;
        this.status = status;
    }        

    public long getId() { return id; }
    public long getNo() { return no; }
    public String getName() { return name; }
    public String getIpAddress() { return ipAddress; }
    public String getMacAddress() { return macAddress; }
    public String getTechnician() { return technician; }
    public String getStatus() { return status; }

    public void setId ( final long id ) { this.id = id; }
    public void setNo ( final long no ) { this.no = no; }
    public void setName ( final String name ) { this.name = name; }
    public void setIpAddress ( final String ipAddress ) { this.ipAddress = ipAddress; }
    public void setMacAddress ( final String macAddress ) { this.macAddress = macAddress; }
    public void setTechnician ( final String technician ) { this.technician = technician; }
    public void setStatus ( final String status ) { this.status = status; }

    @Override
    public String toString()
    {
        return "ID: " + id + ", Name: " + name + ", IP Address: " + ipAddress + ", MAC AAddress: " + macAddress + ", Technician: " + technician + ", Status: " + status;
    }
}
