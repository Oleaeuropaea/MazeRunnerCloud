package pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.loadbalancer;

/**
 * Class to decide whether or not we should create another instance to put the request
 */
public class InstanceDelay {
    private MazeInstance instance;
    private double delay;
    private boolean needToLauchNewInstance;

    public InstanceDelay(MazeInstance instance, double delay) {
        this.instance = instance;
        this.delay = delay;
        this.needToLauchNewInstance = false;
    }

    public MazeInstance getInstance() {
        return this.instance;
    }

    public double getDelay() {
        return this.delay;
    }

    public boolean isNewInstanceNeeded() {
        return this.needToLauchNewInstance;
    }

    public void setInstance() {
        this.needToLauchNewInstance = true;
    }
}
