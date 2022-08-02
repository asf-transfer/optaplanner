package org.optaplanner.operator.impl.solver.model;

public final class OptaPlannerSolverSpec {
    private String solverImage;
    private AmqBroker amqBroker;

    private Scaling scaling;

    public OptaPlannerSolverSpec() {
        // required by Jackson
    }

    public OptaPlannerSolverSpec(String solverImage, AmqBroker amqBroker) {
        this.solverImage = solverImage;
        this.amqBroker = amqBroker;
    }

    public String getSolverImage() {
        return solverImage;
    }

    public void setSolverImage(String solverImage) {
        this.solverImage = solverImage;
    }

    public Scaling getScaling() {
        return scaling;
    }

    public void setScaling(Scaling scaling) {
        this.scaling = scaling;
    }

    public AmqBroker getAmqBroker() {
        return amqBroker;
    }

    public void setAmqBroker(AmqBroker amqBroker) {
        this.amqBroker = amqBroker;
    }
}
