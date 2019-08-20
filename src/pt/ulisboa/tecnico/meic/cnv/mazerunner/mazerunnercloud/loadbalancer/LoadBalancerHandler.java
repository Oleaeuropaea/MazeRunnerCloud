package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class LoadBalancerHandler implements HttpHandler {

    private final InstanceManager instanceManager;
    private int i = 0;


    public LoadBalancerHandler(InstanceManager manager) {
        this.instanceManager = manager;

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        String request = httpExchange.getRequestURI().getQuery();
        Map<String, String> map = parseQuery(request);


        if (isValidRequest(map)) {
            System.out.println("[LoadBAlancer Handler] Handling valid Request");
            forwardRequest(httpExchange);
        }

    }

    private void forwardRequest(HttpExchange httpExchange) {

        System.out.println("FORWARDING REQUEST");
        String request = httpExchange.getRequestURI().getQuery();

        int tries = 1;
        while (tries <= SystemUtil.FowardRequestRetriesThreshold) {
            MazeToSolve toSolve = new MazeToSolve(request);
            MazeInstance mazeInstance = selectInstance(toSolve);

            try {
                sendRequest(mazeInstance, httpExchange);
                mazeInstance.removeRequest(toSolve);
                break;
            } catch (WorkerCrasherException e) {
                tries++;
                mazeInstance.removeRequest(toSolve);
                try {
                    Thread.sleep(SystemUtil.DELAY_FOR_NEW_RETRY_FOWARDING);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

        }


    }
    private void sendRequest(MazeInstance mazeInstance, HttpExchange httpExchange) throws WorkerCrasherException {
            InputStream in = null;
            final OutputStream out = httpExchange.getResponseBody();

        try {


            int bytesToRead;
            String request = httpExchange.getRequestURI().getQuery();
            URL url = new URL(SystemUtil.PROTOCOL_CONTACT_REPLICAS, mazeInstance.getIp(), SystemUtil.WORKER_INSTANCE_PORT, "/mzrun.html?"+request);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(SystemUtil.CONNECTION_TIMEOUT_CONTACT_REPLICAS);
            connection.setReadTimeout(SystemUtil.MAXIMUM_TIME_MAZESOLVER);
            connection.connect();

            in = connection.getInputStream();
            httpExchange.sendResponseHeaders(SystemUtil.HTTP_OK, 0);
            byte[] buffer = new byte[SystemUtil.BUFFER_SIZE];
            while ((bytesToRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesToRead);
            }
            out.close();
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new WorkerCrasherException();
        } catch (IOException e) {
                System.err.println("InputStream/OutputStream error, close connection: " + e.getMessage());
                System.err.println("LoadBalancer Handler " + e.getStackTrace());
                e.printStackTrace();
                throw new WorkerCrasherException();
        }
    }

    private MazeInstance selectInstance(MazeToSolve toSolve) {

        InstanceDelay instance = instanceManager.instancesToLaunch(toSolve);
        MazeInstance mazeToSendRequest;
        if (instance.isNewInstanceNeeded()) {
            mazeToSendRequest= instanceManager.launchInstanceReadyForSolvingMazes();
        }
        else {
            mazeToSendRequest = instance.getInstance();
        }
        mazeToSendRequest.addRequest(toSolve);

        return mazeToSendRequest;


    }
    private static boolean isValidRequest(Map<String, String> map) {
        return map.containsKey(SystemUtil.FLAG_MAZE_FILE) && map.containsKey(SystemUtil.FLAG_X_INITIAL) && map.containsKey(SystemUtil.FLAG_Y_INITIAL) &&
                map.containsKey(SystemUtil.FLAG_X_FINAL) && map.containsKey(SystemUtil.FLAG_Y_FINAL) && map.containsKey(SystemUtil.FLAG_VELOCITY) &&
                map.containsKey(SystemUtil.FLAG_STRATEGY) &&
                (Integer.valueOf(map.get(SystemUtil.FLAG_VELOCITY)) >= 1 && Integer.valueOf(map.get(SystemUtil.FLAG_VELOCITY)) <= 100) &&
                SystemUtil.VALID_STRATEGIES.contains(map.get(SystemUtil.FLAG_STRATEGY));

    }

    private static Map<String,String> parseQuery(String request) {
        Map<String, String> map = new HashMap<String, String>();
        for (String s : request.split("&")) {
            String[] par = s.split("=");
            map.put(par[0], par[1]);
        }
        return map;
    }
}
