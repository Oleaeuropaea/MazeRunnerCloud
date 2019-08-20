package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Main;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.CantGenerateOutputFileException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.CantReadMazeInputFileException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidCoordinatesException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.InvalidMazeRunningStrategyException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.instrumentation.InstrumentMaze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.metricstoragesystem.AWSDynamoDatabase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;



public class WebServerMain {


    public static AWSDynamoDatabase awsDynamoDatabase;

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(SystemUtil.WORKER_INSTANCE_PORT), 0);
        server.createContext(SystemUtil.CONTEXT_MAZERUNNER, new MazeHandler());
        server.createContext(SystemUtil.CONTEXT_ISALIVE, new IsAliveHandler());
        awsDynamoDatabase = new AWSDynamoDatabase();

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("MazeRunner webserver running on Port: " + SystemUtil.WORKER_INSTANCE_PORT);


    }


    static class MazeHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            long threadId = Thread.currentThread().getId();
            String request = httpExchange.getRequestURI().getQuery();
            System.out.println("Received request: " + request + " Handled by Thread:"+threadId);
            OutputStream httpResponse = httpExchange.getResponseBody();
            Map<String, String> map = parseQuery(request);
            String s = Paths.get(".").toAbsolutePath().normalize().toString();


            if(isValidRequest(map)){

                String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
                String MazeFileSolved=threadId+timestamp+"_ouput.html";

                InstrumentMaze.insertInputByThread(map.get(SystemUtil.FLAG_MAZE_FILE)+"," + map.get(SystemUtil.FLAG_X_INITIAL)+"," +
                        map.get(SystemUtil.FLAG_Y_INITIAL)+"," +map.get(SystemUtil.FLAG_X_FINAL)+"," +map.get(SystemUtil.FLAG_Y_FINAL)+"," +
                        map.get(SystemUtil.FLAG_VELOCITY)+"," +map.get(SystemUtil.FLAG_STRATEGY));

                String[] args = {map.get(SystemUtil.FLAG_X_INITIAL),map.get(SystemUtil.FLAG_Y_INITIAL), map.get(SystemUtil.FLAG_X_FINAL), map.get(SystemUtil.FLAG_Y_FINAL),
                        map.get(SystemUtil.FLAG_VELOCITY),map.get(SystemUtil.FLAG_STRATEGY), s+"/"+map.get(SystemUtil.FLAG_MAZE_FILE),MazeFileSolved};
                try {

                    Main.main(args);
                    File image = new File(MazeFileSolved);

                    httpExchange.sendResponseHeaders(SystemUtil.HTTP_OK, image.length());
                    Files.copy(image.toPath(), httpResponse);
                    httpResponse.close();
                    Files.delete(Paths.get(image.getCanonicalPath()));

                } catch(IOException e) {
                    String responseText = "Bad request: " + request + "  - Cannot Find Solved Mazed";
                    respondBadRequest(httpExchange, responseText, SystemUtil.HTTP_INTERNAL_SERVER_ERROR, httpResponse);
                    e.printStackTrace();

                } catch (InvalidMazeRunningStrategyException e) {
                    String responseText = "Bad request: " + request + "  - The Strategy should be one of the followings {bfs, dfs, astar}";
                    respondBadRequest(httpExchange, responseText, SystemUtil.HTTP_BAD_REQUEST, httpResponse);
                    e.printStackTrace();

                } catch (InvalidCoordinatesException e) {
                    String responseText = "Bad request: " + request + "  - Error for given coordinates, please review them";
                    respondBadRequest(httpExchange, responseText, SystemUtil.HTTP_BAD_REQUEST, httpResponse);
                    e.printStackTrace();

                } catch (CantGenerateOutputFileException e) {
                    String responseText = "Bad request: " + request + "  - Error Saving Solved Maze";
                    respondBadRequest(httpExchange, responseText, SystemUtil.HTTP_INTERNAL_SERVER_ERROR, httpResponse);
                    e.printStackTrace();

                } catch (CantReadMazeInputFileException e) {
                    String responseText = "Not Found: " + request + "  - Given Maze does not exist";
                    respondBadRequest(httpExchange, responseText, SystemUtil.HTTP_NOT_FOUND, httpResponse);
                    e.printStackTrace();

                }

            }

            else {
                    String responseText = "Bad request: " + request + "  - It should contain:the entry coordinates, the exit coordinates," +
                            " the speed, and the strategy to employ for maze solving. Speed within [1-100], strategy ={bfs, dfs, astar}";
                    httpExchange.sendResponseHeaders(SystemUtil.HTTP_BAD_REQUEST, responseText.length());
                    httpResponse.write(responseText.getBytes());
                    httpResponse.close();

                }

            }

        private void respondBadRequest(HttpExchange httpExchange, String responseText, int code, OutputStream httpResponse) {
            try {
                httpExchange.sendResponseHeaders(code, responseText.length());
                httpResponse.write(responseText.getBytes());
                httpResponse.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isValidRequest(Map<String, String> map) {

            return map.containsKey(SystemUtil.FLAG_MAZE_FILE) && map.containsKey(SystemUtil.FLAG_X_INITIAL) && map.containsKey(SystemUtil.FLAG_Y_INITIAL) &&
                    map.containsKey(SystemUtil.FLAG_X_FINAL) && map.containsKey(SystemUtil.FLAG_Y_FINAL) && map.containsKey(SystemUtil.FLAG_VELOCITY) &&
                    map.containsKey(SystemUtil.FLAG_STRATEGY) &&
                    (Integer.valueOf(map.get(SystemUtil.FLAG_VELOCITY)) >= 1 && Integer.valueOf(map.get(SystemUtil.FLAG_VELOCITY)) <= 100) &&
                    SystemUtil.VALID_STRATEGIES.contains(map.get(SystemUtil.FLAG_STRATEGY));

        }

        private Map<String,String> parseQuery(String request) {
            Map<String, String> map = new HashMap<String, String>();
            for (String s : request.split("&")) {
                String[] par = s.split("=");
                map.put(par[0], par[1]);
            }
            return map;
        }
    }


    private static class IsAliveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream httpResponse = httpExchange.getResponseBody();
            String response = "Response to IsAlive, Webserver is Running";
            httpExchange.sendResponseHeaders(SystemUtil.HTTP_OK, response.length());
            httpResponse.write(response.getBytes());
            httpResponse.close();
        }
    }
}


