package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.w3c.dom.Attr;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.metricstoragesystem.AWSDynamoDatabase;

import java.util.*;

public class AlgorithmLoadBalancer {


    AWSDynamoDatabase db;

    public AlgorithmLoadBalancer(){
        db = new AWSDynamoDatabase();
    }


    public Long estimate(String maze, String x0, String y0, String x1, String y1, String velocity, String strategy){
        return Long.parseLong(estimateBasic(maze, Double.parseDouble(x0), Double.parseDouble(y0),
                                                  Double.parseDouble(x1), Double.parseDouble(y1),
                                                  Double.parseDouble(velocity), strategy));
    }



    @Deprecated
    private void estimateSmart(String maze) {
        HashSet<Double> doubleHashSet = new HashSet<>();
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        LinkedList<Double> dataset = new LinkedList<>();
        ScanResult result = db.getForMaze(maze);
        List<Map<String, AttributeValue>> items =  result.getItems();
        for(Map<String, AttributeValue> sample : items){
            double y = Double.parseDouble(sample.get("countBasicBlocks").getS());
            if(!doubleHashSet.contains(y)){
                if(getDoubleFromStrategy(sample.get("strategy").getS()) != -1){
                    dataset.add(y);
                    doubleHashSet.add(y);
                    dataset.add(Double.parseDouble(sample.get("xStart").getN()));
                    dataset.add(Double.parseDouble(sample.get("yStart").getN()));
                    dataset.add(Double.parseDouble(sample.get("xFinal").getN()));
                    dataset.add(Double.parseDouble(sample.get("yFinal").getN()));
                    dataset.add(Double.parseDouble(sample.get("velocity").getN()));
                    dataset.add(getDoubleFromStrategy(sample.get("strategy").getS()));
                }

            }
        }
        int obs = dataset.size() / 7;
        int vars = 6;
        double[] dataAsDouble = new double[dataset.size()];
        for(int i = 0; i < dataset.size(); i++) dataAsDouble[i] = dataset.get(i);
        regression.newSampleData(dataAsDouble, obs, vars);
        double[] coe = regression.estimateRegressionParameters();
        System.out.println(Arrays.toString(coe));
    }

    private String estimateBasic(String maze, double x0, double y0, double x1, double y1, double velocity, String strategy) {
        ScanResult result = db.getForMaze(maze);
        List<Map<String, AttributeValue>> items = result.getItems();
        EuclideanDistance dist = new EuclideanDistance();
        double min = Double.MAX_VALUE;
        double minXS = Double.MAX_VALUE;
        double minYS = Double.MAX_VALUE;
        double minXF = Double.MAX_VALUE;
        double minYF = Double.MAX_VALUE;
        ArrayList<Map<String, AttributeValue>> closest = new ArrayList<>();
        List<Map<String, AttributeValue>> closestStart = new ArrayList<>();
        int epos = 0;

        for (int i = 0; i < items.size(); i++) {
            Map<String, AttributeValue> sample = items.get(i);
            double xs = Double.parseDouble(sample.get("xStart").getN());
            double ys = Double.parseDouble(sample.get("yStart").getN());
            double eucldist = dist.compute(new double[]{xs, ys}, new double[]{x0, y0});
            if(eucldist < min){
                min = eucldist;
                minXS = xs;
                minYS = ys;
                closestStart.add(sample);
            }
        }
        min = Double.MAX_VALUE;
        for (int i = 0; i < closestStart.size(); i++) {
            Map<String, AttributeValue> sample = closestStart.get(i);
            double xs = Double.parseDouble(sample.get("xStart").getN());
            double ys = Double.parseDouble(sample.get("yStart").getN());
            double xf = Double.parseDouble(sample.get("xFinal").getN());
            double yf = Double.parseDouble(sample.get("yFinal").getN());
            if(xs == minXS && ys == minYS){
                double eucldist = dist.compute(new double[]{xf, yf}, new double[]{x1, y1});
                if(eucldist < min){
                    min = eucldist;
                    minXF = xf;
                    minYF = yf;
                    epos = i;
                    closest.add(sample);
                }
            }
        }
        double closestVel = Double.MAX_VALUE;
        for (int i = 0; i < closest.size(); i++) {
            Map<String, AttributeValue> sample = closest.get(i);
            String strat = sample.get("strategy").getS();
            double vel = Double.parseDouble(sample.get("velocity").getN());
            if(closestStart.equals(strategy) && vel == velocity){
                epos = i;
                break;
            } else if(closestStart.equals(strategy) && Math.abs(velocity - vel) < closestVel){
                closestVel = Math.abs(velocity - vel);
                epos = i;
            } else if (Math.abs(velocity - vel) < closestVel){
                closestVel = Math.abs(velocity - vel);
                epos = i;
            }
        }
        if(items.isEmpty())
            return "0";
        String value = items.get(epos).get(AWSDynamoDatabase.BRANCH_COUNT).getS();
        System.out.printf("Closest point is (%.0f, %.0f) (%.0f, %.0f) %f %s -> %s", minXS, minYS, minXF, minYF, velocity, strategy,  value);
        return value;
    }

    private double getDoubleFromStrategy(String strategy){
        if(strategy.equals("dfs")) return 1.0;
        else if(strategy.equals("afs")) return 2.0;
        else if(strategy.equals("astar")) return 3.0;
        else return -1;
    }



    public static void main(String[] args) {
       new AlgorithmLoadBalancer();
    }








}
