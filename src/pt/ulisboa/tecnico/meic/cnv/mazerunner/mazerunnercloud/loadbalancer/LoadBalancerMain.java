package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;


import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.autoscaler.AutoScaler;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.autoscaler.ThreadHealthCheck;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.metricstoragesystem.AWSDynamoDatabase;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoadBalancerMain {




    public static final int MAX_THREADS = 16 * Runtime.getRuntime().availableProcessors();

    private static AWSDynamoDatabase awsDynamoDatabase;
    private static InstanceManager instanceManager;
    private static AutoScaler autoScaler;


    public static void main(String[] args) throws IOException {
        startInstanceManager();
        startAutoScaler();// Launches n Instances, and assign a IP, Check if they are responding, Begins Autoscaling
        startLoadBalancer();
        startDynamoDB();

    }


    private static void startAutoScaler() {


        autoScaler = new AutoScaler(instanceManager);
        Thread thread = new Thread(autoScaler);
        thread.start();

    }

    private static void startInstanceManager() {
         instanceManager = new InstanceManager();
    }

    private static void startDynamoDB() {

            awsDynamoDatabase = new AWSDynamoDatabase();

    }

    private static void startLoadBalancer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(SystemUtil.PORT_LOADBALANCER), 0);
        server.createContext(SystemUtil.CONTEXT_MAZERUNNER, new LoadBalancerHandler(instanceManager));
        server.createContext(SystemUtil.CONTEXT_ISALIVE, new IsAliveHandler());

        ExecutorService e = Executors.newFixedThreadPool(MAX_THREADS);
        server.setExecutor(e);

        server.start();
        System.out.println("MazeRunner LoadBalancer running on Port: " + SystemUtil.PORT_LOADBALANCER);
    }







}