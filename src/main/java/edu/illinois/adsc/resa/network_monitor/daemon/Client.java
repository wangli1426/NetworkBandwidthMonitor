package edu.illinois.adsc.resa.network_monitor.daemon;

import edu.illinois.adsc.resa.network_monitor.generated.NetworkMonitor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.sql.Struct;
import java.util.Vector;

/**
 * Created by robert on 9/30/15.
 */
public class Client {

    @Option(name = "--config_file", aliases = {"-c"}, usage = "set the config file")
    static private String config_file = "../conf/nodes.conf";

    @Option(name = "--help", aliases = {"-h"}, usage = "help")
    static private boolean _help;

    private Vector<Connection> connections = new Vector<Connection>();

    private class Connection {
        String nodeName;
        TTransport transport;
        NetworkMonitor.Client client;
    }

    public static void main(String[] args) {

        Client client = new Client();
        CmdLineParser parser = new CmdLineParser(client);

        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            _help = true;
        }

        if (_help) {
            parser.printUsage(System.err);
            System.err.println();
            return;
        }


        try {
            client.getConnections();
            client.perform();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void perform() throws TException, InterruptedException {
        while(true) {
            Thread.sleep(1000);
            System.out.println("====================");
            for(Connection c: connections) {
                double network = c.client.getThroughput();
                double cpu = c.client.getCpuUtilization();
                String nodeName = c.nodeName;
                System.out.format("%.20s\t%6.4f MBytes/s\t%2.2f\n",nodeName.substring(0,Math.min(20,nodeName.length())), network, cpu );
            }
        }
    }

    private boolean getConnections() throws IOException {
        try{
            FileInputStream fis = new FileInputStream(config_file);

            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                try {
                    Connection newConnection = new Connection();

                    newConnection.nodeName = line;

                    newConnection.transport = new TSocket(line, 9090);
                    newConnection.transport.open();

                    TProtocol protocol = new TBinaryProtocol(newConnection.transport);
                    newConnection.client = new NetworkMonitor.Client(protocol);

                    connections.add(newConnection);

                    System.out.println("connected to node " + newConnection.nodeName);

                }
                catch (TTransportException e) {
                    e.printStackTrace();
                }
            }

            br.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("Config file [" + config_file + "] not exists.");
        }

        return !connections.isEmpty();
    }

}
