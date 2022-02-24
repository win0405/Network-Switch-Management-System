package network.pkgswitch.management.system.models;

public class ConfigurationChangeHistory
{
    private long no;
    private String date;
    private String time;
    private String switchName;
    private String ipAddress;
    private String macAddress;

    public ConfigurationChangeHistory ( final long no, final String date, final String time,
        final String switchName, final String ipAddress, final String macAddress )
    {
        this.no = no;
        this.date = date;
        this.time = time;
        this.switchName = switchName;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }

    public long   getNo()         { return no;         }
    public String getDate()       { return date;       }
    public String getTime()       { return time;       }
    public String getSwitchName() { return switchName; }
    public String getIpAddress()  { return ipAddress;  }
    public String getMacAddress() { return macAddress; }

    public void setNo         ( final long   no         ) { this.no         = no;         }
    public void setDate       ( final String date       ) { this.date       = date;       }
    public void setTime       ( final String time       ) { this.time       = time;       }
    public void setSwitchName ( final String switchName ) { this.switchName = switchName; }
    public void setIpAddress  ( final String ipAddress  ) { this.ipAddress  = ipAddress;  }
    public void setMacAddress ( final String macAddress ) { this.macAddress = macAddress; }

    @Override
    public String toString()
    {
        return "No: " + no + ", Date: " + date + ", Time: " + time + ", Switch Name: " + switchName + ", IP Address: " + ipAddress + ", MAC Address: " + macAddress;
    }

    
}
