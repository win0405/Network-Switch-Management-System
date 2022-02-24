package network.pkgswitch.management.system;

public class Data
{
    public final static Database dbLocal = new Database ( "Network Switch Management System", "localhost" );
    public final static String excelPath = "\"C:\\Program Files\\Microsoft Office\\root\\Office16\\EXCEL.EXE\"";
    public final static String networkSwitchConfigurationProgramPath = "\"C:\\Program Files (x86)\\Mobatek\\MobaXterm\\MobaXterm.exe\"";
    //public final static String networkSwitchConfigurationProgramPath = "\"C:\\Program Files (x86)\\teraterm\\ttermpro.exe\"";
    public final static String networkAccessHistoryFilePath = "C:\\Users\\wloo\\OneDrive - Extreme Networks, Inc\\Desktop\\All Session Logs";

    public final static String NETWORK_SWITCH_USERNAME = "admin";
    public final static String NETWORK_SWITCH_PASSWORD = "Symbol123";
    
    public static String username = null;
    public static String name = null;
    public static String role = null;
}
