package edu.illinois.adsc.resa.network_monitor.daemon;

import edu.illinois.adsc.resa.network_monitor.generated.NetworkMonitor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Created by robert on 9/30/15.
 */
public class Client {
    public static void main(String[] args) {
        try {
            TTransport transport;

            transport = new TSocket("localhost", 9090);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            NetworkMonitor.Client client = new NetworkMonitor.Client(protocol);

            perform(client);

            transport.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static void perform(NetworkMonitor.Client client) throws TException, InterruptedException {
        while(true) {
            Thread.sleep(1000);
            double rate = client.getThroughput();
            System.out.println("Throughput:"+rate);
        }
    }
}
