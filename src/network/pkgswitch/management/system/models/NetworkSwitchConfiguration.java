package network.pkgswitch.management.system.models;

public class NetworkSwitchConfiguration
{
    private long id;
    private long no;
    private String name;
    private String ipAddress;
    private String macAddress;
    private String softwareVersion;
    private String modelNumber;
    private String serialNumber;

    public NetworkSwitchConfiguration ( final long id, final long no, final String name, final String ipAddress,
        final String macAddress, final String softwareVersion, final String modelNumber, final String serialNumber )
    {
        this.id = id;
        this.no = no;
        this.name = name;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.softwareVersion = softwareVersion;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
    }

    public long getId()                { return id;              }
    public long getNo()                { return no;              }
    public String getName()            { return name;            }
    public String getIpAddress()       { return ipAddress;       }
    public String getMacAddress()      { return macAddress;      }
    public String getSoftwareVersion() { return softwareVersion; }
    public String getModelNumber()     { return modelNumber;     }
    public String getSerialNumber()    { return serialNumber;    }

    public void setId              ( final long id                ) { this.id              = id;              }
    public void setNo              ( final long no                ) { this.no              = no;              }
    public void setName            ( final String name            ) { this.name            = name;            }
    public void setIpAddress       ( final String ipAddress       ) { this.ipAddress       = ipAddress;       }
    public void setMacAddress      ( final String macAddress      ) { this.macAddress      = macAddress;      }
    public void setSoftwareVersion ( final String softwareVersion ) { this.softwareVersion = softwareVersion; }
    public void setModelNumber     ( final String modelNumber     ) { this.modelNumber     = modelNumber;     }
    public void setSerialNumber    ( final String serialNumber    ) { this.serialNumber    = serialNumber;    }

    @Override
    public String toString()
    {
        return "ID: " + id + ", No: " + no + ", Name: " + name + ", IP Address: " + ipAddress + ", MAC Address: " + macAddress + ", Software Version: " + softwareVersion + ", Model #: " + modelNumber + ", Serial #: " + serialNumber;
    }
}
