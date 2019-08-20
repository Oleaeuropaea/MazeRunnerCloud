package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MazeInstance implements Comparable<Object>{


    private String ip;

    private String id;

    private ConcurrentLinkedQueue<MazeToSolve> listOfRequests = new ConcurrentLinkedQueue<MazeToSolve>();

    private double delay;

    private boolean running = false;

    private boolean terminate = false;

    public MazeInstance(String id) {
        this.id = id;
        this.ip =null;

    }
    public MazeInstance(String id, String ip) {
        this.id = id;
        this.ip = ip;

    }

    public double getDelay() {
        return this.delay;
    }

    private void computeDelay() {
        double delay = 0;
        long shared = Long.MAX_VALUE;
        for (MazeToSolve maze: listOfRequests) if(shared > maze.getExpected()) shared = maze.getExpected();

        int nRequests = listOfRequests.size();
        for (MazeToSolve maze : listOfRequests) {
            long real = maze.getRealBranchesTaken() + (nRequests - 1) * shared;
            maze.setRealBranchesTaken(real);

            switch(maze.getClassification()) {
                case "fast":
                    if (maze.getRatio() <= 1.5) {
                        delay += 0;
                    } else if (maze.getRatio() <= 2) {
                        delay += 0.25;
                    } else if (maze.getRatio() <= 3) {
                        delay += 0.5;
                    } else {
                        delay += 2;
                    }
                    break;
                case "medium":
                    if (maze.getRatio() <= 1.5) {
                        delay += 0;
                    } else if (maze.getRatio() <= 2) {
                        delay += 0.5;
                    } else {
                        delay += 2;
                    }
                    break;
                case "slow":
                    if (maze.getRatio() <= 2) {
                        delay += 0;
                    } else {
                        delay += 2;
                    }
                    break;
            }
        }
        this.delay = delay;
    }

    private void setTerminatedStatus(boolean status) {
        this.terminate = status;
    }

    public boolean isReadyToBeTerminated() {
        return this.terminate;
    }

    public synchronized void addRequest(MazeToSolve request) {
        listOfRequests.add(request);
        if (isReadyToBeTerminated()) setTerminatedStatus(false);
        computeDelay();
    }

    public synchronized void removeRequest(MazeToSolve request) {
        listOfRequests.remove(request);
        computeDelay();
        if (listOfRequests.isEmpty()) {
            /*
             * If requests wasnt processed yet what is supposed to do ?
             * Add some kind of delay ?
             */
            setTerminatedStatus(true);
        }
    }


    public String getId() {return id;}

    public String getIp() {
        return ip;
    }

    public void setIp(String ip){this.ip=ip;}

    @Override
    public int compareTo(Object obj) {
        if(obj instanceof MazeInstance){
            MazeInstance mazeInstance = (MazeInstance) obj;
            if(this.calculateLoadUsage() > mazeInstance.calculateLoadUsage()){
                return 1;
            } else if(this.calculateLoadUsage() == mazeInstance.calculateLoadUsage()){
                return 0;
            } else {
                return -1;
            }
        } else return -100;
    }


    private int calculateLoadUsage() {
        return 0;
    }

    private static void monitorInstance(AWSCredentials credential, String instanceId) {

        // look it
        // https://stackoverflow.com/questions/10999392/how-to-get-cpu-ram-and-network-usage-of-a-java7-app

        // CPU Usage for how long
        // https://forums.aws.amazon.com/thread.jspa?threadID=80928
        //https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-cloudwatch-get-metrics.html
        final AmazonCloudWatch cw =
                AmazonCloudWatchClientBuilder.defaultClient();

        ListMetricsRequest request = new ListMetricsRequest()
                .withMetricName("CPUUtilization")
                .withNamespace("AWS/EC2");

        boolean done = false;

        while(!done) {
            ListMetricsResult response = cw.listMetrics(request);

            for(Metric metric : response.getMetrics()) {
                System.out.printf(
                        "Retrieved metric %s", metric.getMetricName());
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }

    @Override
    public String toString() {
        return "WorkerID:"+id + " IP:"+ip
                + " List of Requests:"+listOfRequests;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
