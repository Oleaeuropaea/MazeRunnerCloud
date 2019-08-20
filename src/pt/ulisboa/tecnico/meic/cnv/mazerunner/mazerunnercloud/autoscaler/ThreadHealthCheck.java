package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.autoscaler;

import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer.InstanceManager;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer.MazeInstance;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;


public class ThreadHealthCheck implements Runnable{

    private final MazeInstance instanceToVerify;
    private final InstanceManager instanceManager;

    public ThreadHealthCheck( MazeInstance instance, InstanceManager instanceManager1) {
        this.instanceToVerify = instance;
        this.instanceManager = instanceManager1;
    }


    @Override
    public void run() {

        int tries = 1;
        boolean isAlive = false;
        while (!isAlive && tries <= SystemUtil.HealthCheckRetriesThreshold) {
            try {
                URL url = new URL(SystemUtil.PROTOCOL_CONTACT_REPLICAS, instanceToVerify.getIp(), SystemUtil.WORKER_INSTANCE_PORT, SystemUtil.CONTEXT_ISALIVE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(SystemUtil.CONNECTION_TIMEOUT_CONTACT_REPLICAS_PING);
                isAlive = connection.getResponseCode() == HttpURLConnection.HTTP_OK;
                System.out.println("[THREAD-HEALTH-CHECK] InstanceID=" + instanceToVerify.getId()+
                        " IP="+instanceToVerify.getIp()+ " responded, tryNumber="+tries);
                break;

            } catch (IOException e) {
                System.out.println("[THREAD-HEALTH-CHECK] InstanceID=" + instanceToVerify.getId()+
                        " IP="+instanceToVerify.getIp()+ " is not responding, tryNumber="+tries);
                tries++;
                try {
                    Thread.sleep(SystemUtil.PERIOD_PING_RETRY);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if(!isAlive){//fail 3 attempts, should be shutdown
            synchronized (instanceManager.listRunningInstances) {
                instanceManager.listRunningInstances.remove(instanceToVerify);
                instanceManager.shutdownInstance(instanceToVerify.getId());
            }

        }
        instanceToVerify.setRunning(isAlive);

    }



    }
