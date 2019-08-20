package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.autoscaler;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer.InstanceManager;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer.MazeInstance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

public class AutoScaler implements Runnable {

    private  InstanceManager instanceManager;
    private AmazonEC2 amazonEC2Client;
    private HashSet<String> instancePool;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public AutoScaler(){
        amazonEC2Client = AmazonEC2ClientBuilder.standard().withRegion(SystemUtil.AVAILABILITY_ZONE).build();
        instancePool = new HashSet<>();
    }

    public AutoScaler(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }


    @Override
    public void run() {

        setupInitialNumberOfInstances();


        try {
            Thread.sleep(SystemUtil.GRACE_PERIOD_BEFORE_RUNNIG_SCALLING);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        while (true) {
            //try {

                /*healthChecksForIndividualInstances(executorService);
                long timeoutFaultDetector =  3 * SystemUtil.CONNECTION_TIMEOUT_CONTACT_REPLICAS_PING * SystemUtil.PERIOD_PING_RETRY;
                executorService.awaitTermination(timeoutFaultDetector, TimeUnit.MILLISECONDS);
                */
                System.out.println("Doing SCALLING...");
                scallingAlgorithm();

                try {
                    Thread.sleep(SystemUtil.AUTO_SCALING_PERIOD);
                } catch (InterruptedException e) {
                    continue;
                }

            /**}catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }

    /**
     * Setup Initial Instances according to the setup. Does not check if there are Instances already running
     */
    private void setupInitialNumberOfInstances() {
        System.out.println("[AutoScaler] launching:"+ SystemUtil.MINIMUM_RUNNING_INSTANCES + " Instances");
        instanceManager.launchNInstances(SystemUtil.MINIMUM_RUNNING_INSTANCES);
        System.out.println("[AutoScaler] finished Launching " +SystemUtil.MINIMUM_RUNNING_INSTANCES + " Instances");
        System.out.println("[AutoScaler]"+instanceManager.listRunningInstances);

        //Wait until all Instances have an IP
        synchronized (instanceManager.listRunningInstances) {
            for (MazeInstance worker : instanceManager.listRunningInstances) {
                while (!instanceManager.hasIpDefined(worker)) {
                    System.out.println("[AutoScaler] NO IP for worker" + worker.getId());
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("[AutoScaler] Worker:" + worker.getId() + " has now an IP:" + worker.getIp());
            }
        }


    }


    private void scallingAlgorithm() {

        List<MazeInstance> listRunningInstances = instanceManager.listRunningInstances;
        List<MazeInstance> toTerminateInstances = new ArrayList<>();

        int runningInstances = listRunningInstances.size();
        /*
         * [TODO] Math.FLOOR more correct than round
         * Math.floor(runningInstances * SystemUtil.MAXIMUM_INSTANCES_TO_TERMINATE);
         */
        long maximumTerminatedInstances = Math.round(runningInstances *
                                                    SystemUtil.MAXIMUM_INSTANCES_TO_TERMINATE);

        synchronized(listRunningInstances) {
            for (MazeInstance instance : listRunningInstances) {
                /*
                * toTerminateInstances.size() = 0 >= maximumTerminatedInstances 0 (true)
                * runningInstances *2* - toTerminateInstances.size() *0* <= MINIMUM_RUNNING_INSTANCES *1*
                *
                * [TODO] We have to change the order and add new condition.
                * if ((runningInstances - toTerminateInstances.size()) <= SystemUtil.MINIMUM_RUNNING_INSTANCES
                * && instance.isReadyToBeTerminated()) {
                    toTerminateInstances.add(instance);
                }
                * [TODO] Remove the break condition. We just need one IF block.
                *
                */

                if (toTerminateInstances.size() >= maximumTerminatedInstances ||
                    (runningInstances - toTerminateInstances.size()) <= SystemUtil.MINIMUM_RUNNING_INSTANCES) {
                    break;
                } else {
                    if (instance.isReadyToBeTerminated()) {
                        toTerminateInstances.add(instance);
                    }
                }
                /*
                 * [TODO] Update that value always.
                 * runningInstances = listRunningInstances.size();
                 *
                 */
            }
            for(MazeInstance instance : toTerminateInstances) {
                listRunningInstances.remove(instance);
            }
        }
        if(!toTerminateInstances.isEmpty()) {
            instanceManager.shutdownInstances(toTerminateInstances);
        }


    }

    private void healthChecksForIndividualInstances(ExecutorService executer) {
        System.out.println("[AutoScaler]-Launching Fault Detector");

        synchronized (instanceManager.listRunningInstances) {
            for (MazeInstance instance : instanceManager.listRunningInstances) {
                executer.execute( new ThreadHealthCheck(instance, instanceManager));

            }
        }
    }
}
