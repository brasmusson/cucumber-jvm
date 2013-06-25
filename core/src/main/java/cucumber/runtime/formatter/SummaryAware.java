package cucumber.runtime.formatter;

import cucumber.runtime.SummaryCounter;

public interface SummaryAware {
    void setSummaryCounter(SummaryCounter counter);
}
