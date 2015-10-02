package edu.illinois.adsc.resa.network_monitor;


import edu.illinois.adsc.resa.network_monitor.interfaces.ICpuUtilizationReader;
import edu.illinois.adsc.resa.network_monitor.interfaces.INetworkThroughputReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by robert on 9/29/15.
 */
public class LinuxNetworkReader implements INetworkThroughputReader, ICpuUtilizationReader {

    private long recBytes = 0;
    private long sendBytes = 0;

    private float recRate = 0;
    private float sendRate = 0;

    private float cpuUtilization = 0;

    private float period = 1000;

    private Thread thread;

    public LinuxNetworkReader() throws IOException {
        initialize();
        thread = new Thread(new Fresher());
        thread.start();
    }

    public void finalize(){
        thread.interrupt();
    }

    public double getThroughput() {
        return recRate + sendRate;
    }

    private void initialize() throws IOException{
        updateValues();
        recRate = 0;
        sendRate = 0;
    }

    private void updateValues() throws IOException {
        updateNetWokrValues();
        updateCPUValues();
    }
    private void updateNetWokrValues() throws IOException{
        ProcessBuilder builder = new ProcessBuilder();
        String defaultEthnetName = "eth0";
        String command = "ifconfig";
        builder.command(command, defaultEthnetName);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if(line == null) break;
            Pattern p = Pattern.compile( "RX bytes:([0-9]+).* TX bytes:([0-9]+)" );
            Matcher m = p.matcher(line);

            if ( m.find() ) {
                try{
                Long recbytes = Long.parseLong(m.group(1));
                Long sendbytes = Long.parseLong(m.group(2));

                recRate = (recbytes - recBytes)/period*1000/1024/1024;
                sendRate = (sendbytes - sendBytes)/period*1000/1024/1024;

                recBytes = recbytes;
                sendBytes = sendbytes;
                }
                catch (Exception e){
                    System.out.println("Some bad happens! Reason: ");
                    e.printStackTrace();
                }
            }
        }
        r.close();
        process.destroy();
    }

    private void updateCPUValues() throws IOException{
        ProcessBuilder builder = new ProcessBuilder();

        String[] command = {"top","-n", "2", "-b", "-d", "0.5"};//"-n 2 -b -d 1|grep Cpu";
        builder.command(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        r.readLine();
        r.readLine();
        r.readLine();
        r.readLine();
        r.readLine();
        while (true) {
            line = r.readLine();
            if(line == null) break;

            String head = line.substring(0,Math.min(8,line.length()));

            if(!head.equals("%Cpu(s):"))
                continue;

            Pattern p = Pattern.compile( "([0-9|.]+) us" );
            Matcher m = p.matcher(line);

            if ( m.find() ) {
                try{
                    cpuUtilization = Float.parseFloat(m.group(1));
                }
                catch (Exception e){
                    System.out.println("Some bad happens! Reason: ");
                    e.printStackTrace();
                }
                break;
            }
        }
        r.close();
        process.destroy();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        LinuxNetworkReader throughputReader = new LinuxNetworkReader();
        while(true) {
            Thread.sleep(1000);
            System.out.println("Network bandwidth: " + throughputReader.getThroughput());
            System.out.println("CPU utilization:" + throughputReader.getCpuUtilization());
        }

    }

    @Override
    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public class Fresher implements Runnable {

        @Override
        public void run() {

            while(true) {
                try{
                    Thread.sleep((long)period);
                    updateValues();
                }
                catch (Exception e) {

                }

            }
        }
    }
}
