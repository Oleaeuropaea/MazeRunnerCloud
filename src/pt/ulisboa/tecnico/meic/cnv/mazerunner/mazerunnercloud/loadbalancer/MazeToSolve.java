package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;


import com.sun.net.httpserver.HttpExchange;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.SystemUtil;

import java.util.HashMap;
import java.util.Map;

public class MazeToSolve {

    String mazeFile;
    String xInitial;
    String yInitial;
    String xFinal;
    String yFinal;
    String velocity;
    String strategy;

    long expectedBranchesTaken;
    long realBranchesTaken;
    String classification;

    public MazeToSolve(String request){

        Map<String, String> elementsRequest = parseQuery(request);
        mazeFile = elementsRequest.get(SystemUtil.FLAG_MAZE_FILE);
        xInitial = elementsRequest.get(SystemUtil.FLAG_X_INITIAL);
        yInitial = elementsRequest.get(SystemUtil.FLAG_Y_INITIAL);
        xFinal = elementsRequest.get(SystemUtil.FLAG_X_FINAL);
        yFinal = elementsRequest.get(SystemUtil.FLAG_Y_FINAL);
        velocity = elementsRequest.get(SystemUtil.FLAG_VELOCITY);
        strategy = elementsRequest.get(SystemUtil.FLAG_STRATEGY);

        expectedBranchesTaken = predictedLoadProcessing();
        realBranchesTaken = expectedBranchesTaken;
        if (expectedBranchesTaken < 300 * SystemUtil.BRANCHES_PER_SECOND) {
            classification = "fast";
        } else if (expectedBranchesTaken < 1800 * SystemUtil.BRANCHES_PER_SECOND) {
            classification = "medium";
        } else {
            classification = "slow";
        }
    }

    /**
     * Base on the Maze request inputs we should predict a working load of the request
     * @return
     */
    public long predictedLoadProcessing(){
        AlgorithmLoadBalancer algo = new AlgorithmLoadBalancer();
        return algo.estimate(mazeFile, xInitial,
                             yInitial, xFinal, yFinal,
                             velocity, strategy);
    }

    private  Map<String,String> parseQuery(String request) {
        Map<String, String> map = new HashMap<String, String>();
        for (String s : request.split("&")) {
            String[] par = s.split("=");
            map.put(par[0], par[1]);
        }
        return map;
    }

    public long getExpected() {
        return this.expectedBranchesTaken;
    }

    public long getRealBranchesTaken() {
        return this.realBranchesTaken;
    }

    public void setRealBranchesTaken(long realBranchesTaken) {
        this.realBranchesTaken = realBranchesTaken;
    }

    public float getRatio() {
        if(this.expectedBranchesTaken==0)
            return 4;
        return (this.realBranchesTaken /this.expectedBranchesTaken);
    }

    public String getClassification() {
        return this.classification;
    }

    @Override
    public String toString() {
        return "MazeToSolve: MazeFile:"+mazeFile+ " x_Initial"+xInitial + " yinitial"+ yInitial
                +" xFinal"+ xFinal + " yFinal"+yFinal+ " velocity"+ velocity + " strategy "+strategy +
                " expectedBranches" +expectedBranchesTaken;
    }
}
