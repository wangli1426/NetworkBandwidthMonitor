package edu.illinois.adsc.resa.network_monitor.interfaces;

import edu.illinois.adsc.resa.network_monitor.generated.NetworkMonitor;

/**
 * Created by robert on 9/29/15.
 */
public interface INetworkThroughputReader extends NetworkMonitor.Iface {
    public double getThroughput();
}
