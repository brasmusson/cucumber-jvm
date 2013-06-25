package cucumber.runtime;

import gherkin.formatter.Mappable;

@SuppressWarnings("unused")
public class Summary extends Mappable {
    private static final long serialVersionUID = 1L;

    private final String scenarios;
    private final String steps;
    private final String duration;

    public Summary(String scenarios, String steps, String duration) {
        this.scenarios = scenarios;
        this.steps = steps;
        this.duration = duration;
    }
}
