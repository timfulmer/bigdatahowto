package info.bigdatahowto.core;

/**
 * Encapsulates a behavior function, executed by the system according to type.
 *
 * @author timfulmer
 */
public class Behavior {

    private BehaviorType behaviorType;
    private String function;

    public Behavior() {

        super();
    }

    public Behavior(BehaviorType behaviorType, String function) {

        this();

        this.behaviorType = behaviorType;
        this.function = function;
    }

    public BehaviorType getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(BehaviorType behaviorType) {
        this.behaviorType = behaviorType;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
