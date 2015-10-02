package edu.illinois.adsc.resa.network_monitor.daemon;

import edu.illinois.adsc.resa.network_monitor.LinuxNetworkReader;
import edu.illinois.adsc.resa.network_monitor.interfaces.INetworkThroughputReader;
import edu.illinois.adsc.resa.network_monitor.generated.NetworkMonitor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

/**
 * Created by robert on 9/30/15.
 */
public class Monitor {

    public static INetworkThroughputReader reader;

    public static NetworkMonitor.Processor processor;

    public static void main(String[] args) {

        try {
            reader = new LinuxNetworkReader();
            processor = new NetworkMonitor.Processor(reader);

            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the monitoring daemon...");
            server.serve();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
