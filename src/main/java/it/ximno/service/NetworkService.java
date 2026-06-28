package it.ximno.service;

import oshi.SystemInfo;
import oshi.software.os.NetworkParams;
import oshi.software.os.OperatingSystem;

public class NetworkService {

    private final OperatingSystem os;

    public NetworkService() {
        SystemInfo systemInfo = new SystemInfo();
        this.os = systemInfo.getOperatingSystem();
    }

    /**
     * Returns the hostname of the current machine.
     */
    public String getHostname() {
        NetworkParams params = os.getNetworkParams();
        return params.getHostName();
    }

    /**
     * Returns the IPv4 default gateway, or an empty string if not defined.
     */
    public String getIpv4DefaultGateway() {
        NetworkParams params = os.getNetworkParams();
        return params.getIpv4DefaultGateway();
    }

    /**
     * Returns the list of configured DNS server addresses.
     */
    public String[] getDnsServers() {
        NetworkParams params = os.getNetworkParams();
        return params.getDnsServers();
    }
}