package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.autoscaler.ThreadHealthCheck;

import java.util.LinkedList;
import java.util.List;


public class InstanceManager {

    public List<MazeInstance> listRunningInstances = new LinkedList<>();


    private static AmazonEC2 ec2;

    public InstanceManager() {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(SystemUtil.AVAILABILITY_ZONE)
                .build();
    }


    /**
     * Check if Aws Instance already has an IP, if not it gives Its ip
     *
     * @param instance
     * @return
     */
    public boolean hasIpDefined(MazeInstance instance) {

        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.withInstanceIds(instance.getId());
        DescribeInstancesResult res = ec2.describeInstances(describeInstancesRequest);
        InstanceState state = res.getReservations().get(0).getInstances().get(0).getState();
        if (instance.getIp() == null) {
            instance.setIp(res.getReservations().get(0).getInstances().get(0).getPublicIpAddress());
        }
        return (instance.getIp() != null);
    }

    public void shutdownInstance(String instance_id) {
        StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(instance_id);
        ec2.stopInstances(request);
    }

    public void shutdownInstances(List<MazeInstance> instances) {
        for (MazeInstance instance : instances) {
            shutdownInstance(instance.getId());
        }
    }


    /**
     * Create one EC2 instance on AWS running the Webserver
     *
     * @return new Instance created
     * @throws AmazonClientException
     */
    private MazeInstance launchInstance() throws AmazonClientException {
        RunInstancesRequest runRequest = new RunInstancesRequest();
        runRequest.withImageId(SystemUtil.AMI)
                .withInstanceType(SystemUtil.TYPE)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(SystemUtil.MY_KEY)
                .withSecurityGroups(SystemUtil.GROUP_NAME);
        RunInstancesResult runInstancesResult = ec2.runInstances(runRequest);
        List<Instance> instances = runInstancesResult.getReservation().getInstances();
        MazeInstance instance = new MazeInstance(instances.get(0).getInstanceId());
        return instance;
    }

    /***
     * Create n EC2 instances in AWS running the webserver
     * @param numberMachines
     * @throws AmazonClientException
     */
    public void launchNInstances(int numberMachines) throws AmazonClientException {
        RunInstancesRequest runRequest = new RunInstancesRequest();
        runRequest.withImageId(SystemUtil.AMI)
                .withInstanceType(SystemUtil.TYPE)
                .withMinCount(numberMachines)
                .withMaxCount(numberMachines)
                .withKeyName(SystemUtil.MY_KEY)
                .withSecurityGroups(SystemUtil.GROUP_NAME);
        RunInstancesResult runInstancesResult = ec2.runInstances(runRequest);
        List<Instance> instances = runInstancesResult.getReservation().getInstances();
        synchronized (listRunningInstances) {
            for (Instance x : instances) {
                MazeInstance instanceNew = new MazeInstance(x.getInstanceId());
                listRunningInstances.add(instanceNew);
            }
        }
    }

    /**
     * Creates AWS Instance, waits for it to respond to Ping
     *
     * @return
     */
    public MazeInstance launchInstanceReadyForSolvingMazes() {

        MazeInstance createdInstance = null;
        while (createdInstance==null) {

            createdInstance = launchInstance();
            while (!hasIpDefined(createdInstance)){

            }

            try {
                Thread.sleep(SystemUtil.GRACE_PERIODO_BEFORE_STARTING_HEALTHCHECKS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ThreadHealthCheck threadHealthCheck = new ThreadHealthCheck(createdInstance, this);
            Thread thread = new Thread(threadHealthCheck);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (createdInstance.isRunning()) {
                synchronized (listRunningInstances) {
                    this.listRunningInstances.add(createdInstance);
                }
                return createdInstance;
            }
        }

        return createdInstance;
    }

    /**
     *
     * @param maze
     * @return
     */
    private InstanceDelay computeDelay(MazeToSolve maze) {
        double minDelay = Long.MAX_VALUE;
        MazeInstance bestInstance = null;
        for (MazeInstance instance : this.listRunningInstances) {
            instance.addRequest(maze);
            double delay = instance.getDelay();
            if (delay < minDelay) {
                minDelay = delay;
                bestInstance = instance;
            }
            instance.removeRequest(maze);
        }
        return new InstanceDelay(bestInstance, minDelay);
    }

    /**
     * Calculates cost
     *
     * @return
     */
    public InstanceDelay instancesToLaunch(MazeToSolve maze) {
        int nInstances = listRunningInstances.size();
        InstanceDelay bestInstance = computeDelay(maze);
        double cost = nInstances + bestInstance.getDelay();
        System.out.println("COST "+cost);


        // in case the lenght of the new request is not worth launching a new instance
        long expectedBranchesTaken = maze.getExpected();
        double runtime;
        if(expectedBranchesTaken==0){
            runtime=10*60;
        }
        else
            runtime = SystemUtil.BRANCHES_PER_SECOND/expectedBranchesTaken;
        double init = 0;
//        if (runtime < 5*60) {
//            init = 2;
//        } else if (runtime < 10*60) {
//            init = 1;
//        } else {
//            init = 0;
//        }

        double new_cost = ++nInstances + bestInstance.getInstance().getDelay() + init;
        System.out.println("Init: "+ init);
        System.out.println("NEW COST "+new_cost);
        if (new_cost < cost) {
            bestInstance.setInstance();
            maze.setRealBranchesTaken((long) (expectedBranchesTaken + 60 * SystemUtil.BRANCHES_PER_SECOND));
        }

        return bestInstance;
    }



}
