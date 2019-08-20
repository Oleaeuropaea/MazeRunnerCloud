package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud;

import com.amazonaws.regions.Regions;

import java.util.Arrays;
import java.util.List;

public class SystemUtil {


    public static final String FLAG_MAZE_FILE = "m";
    public static final String FLAG_X_INITIAL = "x0";
    public static final String FLAG_Y_INITIAL = "y0";
    public static final String FLAG_X_FINAL = "x1";
    public static final String FLAG_Y_FINAL = "y1";
    public static final String FLAG_VELOCITY = "v";
    public static final String FLAG_STRATEGY = "s";
    public static final List<String> VALID_STRATEGIES =Arrays.asList("bfs", "dfs","astar");
    public static final String CONTEXT_MAZERUNNER ="/mzrun.html";
    public static final String CONTEXT_ISALIVE ="/IsAlive.html";
    public static final String PROTOCOL_CONTACT_REPLICAS ="http";
    /**
     * In Milliseconds
     */
    public static final int WORKER_INSTANCE_PORT=8000;
    public static final int HealthCheckRetriesThreshold = 3;
    public static final int FowardRequestRetriesThreshold = 3;


    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    public static final int MAX_THREADS = 4 * Runtime.getRuntime().availableProcessors();

    public static final String AMI = "ami-21eb9459";
    public static final String TYPE = "t2.micro";
    public static final int MINIMUM_RUNNING_INSTANCES = 1;
    public static final String MY_KEY = "proj";
    public static final String GROUP_NAME = "cnv-ssh+http";
    public static final int CONNECTION_TIMEOUT_CONTACT_REPLICAS_PING = 10000;
    public static final long PERIOD_PING_RETRY = 5000;
    public static Regions AVAILABILITY_ZONE = Regions.US_WEST_2;

    public static final int CONNECTION_TIMEOUT_CONTACT_REPLICAS=3000000;
    public static final int GRACE_PERIODO_BEFORE_STARTING_HEALTHCHECKS = 40000;
    public static final int AUTO_SCALING_PERIOD = 90000;
    public static final double MAXIMUM_INSTANCES_TO_TERMINATE = 0.3;

    public static final double BRANCHES_PER_SECOND= 1.1 * Math.pow(10,7);

    /**
     * Two minutes
     */
    public static final long GRACE_PERIOD_BEFORE_RUNNIG_SCALLING = 2 * 60000;
    public static final int MAXIMUM_TIME_MAZESOLVER =   (2 * ((60 * 60) * 1000));;
    public static final int PORT_LOADBALANCER = 80;
    /**
     * TWO minutes
     */
    public static final long GRACE_PERIOD_BEFORE_RUNNIG_HEALTH_CHECKS = 2*60*1000;

    public static final int BUFFER_SIZE = 2048;

    public static final int DELAY_FOR_NEW_RETRY_FOWARDING = 10*1000;
}
