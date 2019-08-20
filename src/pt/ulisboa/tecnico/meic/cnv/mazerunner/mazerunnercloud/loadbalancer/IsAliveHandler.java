package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;

import java.io.IOException;
import java.io.OutputStream;


public class IsAliveHandler implements HttpHandler {

        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("NEW REQUEST");
            OutputStream httpResponse = httpExchange.getResponseBody();
            String response = "Response to IsAlive, LoadBalancer is Running";
            httpExchange.sendResponseHeaders(SystemUtil.HTTP_OK, response.length());
            httpResponse.write(response.getBytes());
            httpResponse.close();
        }

}
