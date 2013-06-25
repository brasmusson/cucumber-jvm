package cucumber.runtime;

import gherkin.formatter.AnsiFormats;
import gherkin.formatter.Format;
import gherkin.formatter.Formats;
import gherkin.formatter.MonochromeFormats;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.model.Result;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SummaryCounter {
    public static final long ONE_SECOND = 1000000000;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    public static final String PENDING = "pending";
    private SubCounts scenarioSubCounts = new SubCounts();
    private SubCounts stepSubCounts = new SubCounts();
    private long totalDuration = 0;
    private Formats formats;
    private Locale locale;

    public SummaryCounter(boolean monochrome) {
        this(monochrome, Locale.getDefault());
    }

    public SummaryCounter(boolean monochrome, Locale locale) {
        this.locale = locale;
        if (monochrome) {
            formats = new MonochromeFormats();
        } else {
            formats = new AnsiFormats();
        }
    }

    public void printSummary(PrintStream out) {
        if (stepSubCounts.getTotal() == 0) {
            out.println("0 Scenarios");
            out.println("0 Steps");
        } else {
            printScenarioCounts(out);
            printStepCounts(out);
        }
        printDuration(out);
    }

    public Summary getSummary() {
        if (stepSubCounts.getTotal() == 0) {
            return new Summary("0 Scenarios", "0 Steps", "0m0.000s");
        } else {
            Formats formats = new MonochromeFormats();
            Appendable scenarios = new StringBuilder();
            Appendable steps = new StringBuilder();
            Appendable duration = new StringBuilder();
            appendScenarioCounts(new NiceAppendable(scenarios), formats);
            appendStepCounts(new NiceAppendable(steps), formats);
            appendDuration(new NiceAppendable(duration));
            return new Summary(scenarios.toString(), steps.toString(), duration.toString());
        }
    }

    private void printStepCounts(PrintStream out) {
        appendStepCounts(new NiceAppendable(out), formats);
        out.println("");
    }

    private void appendStepCounts(NiceAppendable out, Formats formats) {
        out.append(Integer.toString(stepSubCounts.getTotal()));
        out.append(" Steps (");
        appendSubCounts(out, stepSubCounts, formats);
        out.append(")");
    }

    private void printScenarioCounts(PrintStream out) {
        appendScenarioCounts(new NiceAppendable(out), formats);
        out.println("");
    }

    private void appendScenarioCounts(NiceAppendable out, Formats formats) {
        out.append(Integer.toString(scenarioSubCounts.getTotal()));
        out.append(" Scenarios (");
        appendSubCounts(out, scenarioSubCounts, formats);
        out.append(")");
    }

    private void appendSubCounts(NiceAppendable out, SubCounts subCounts, Formats formats) {
        boolean addComma = false;
        addComma = appendSubCount(out, subCounts.failed, Result.FAILED, addComma, formats);
        addComma = appendSubCount(out, subCounts.skipped, Result.SKIPPED.getStatus(), addComma, formats);
        addComma = appendSubCount(out, subCounts.pending, PENDING, addComma, formats);
        addComma = appendSubCount(out, subCounts.undefined, Result.UNDEFINED.getStatus(), addComma, formats);
        addComma = appendSubCount(out, subCounts.passed, Result.PASSED, addComma, formats);
    }

    private boolean appendSubCount(NiceAppendable out, int count, String type, boolean addComma, Formats formats) {
        if (count != 0) {
            if (addComma) {
                out.append(", ");
            }
            Format format = formats.get(type);
            out.append(format.text(count + " " + type));
            addComma = true;
        }
        return addComma;
    }

    private void printDuration(PrintStream out) {
        appendDuration(new NiceAppendable(out));
        out.println("");
    }

    private void appendDuration(NiceAppendable out) {
        out.append(String.format("%dm", (totalDuration/ONE_MINUTE)));
        DecimalFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(locale));
        out.append(format.format(((double)(totalDuration%ONE_MINUTE))/ONE_SECOND) + "s");
    }

    public void addStep(Result result) {
        addResultToSubCount(stepSubCounts, result.getStatus());
        addTime(result.getDuration());
    }

    public void addScenario(String resultStatus) {
        addResultToSubCount(scenarioSubCounts, resultStatus);
    }

    public void addHookTime(Long duration) {
        addTime(duration);
    }

    private void addTime(Long duration) {
        totalDuration += duration != null ? duration : 0;
    }

    private void addResultToSubCount(SubCounts subCounts, String resultStatus) {
        if (resultStatus.equals(Result.FAILED)) {
            subCounts.failed++;
        } else if (resultStatus.equals(PENDING)) {
            subCounts.pending++;
        } else if (resultStatus.equals(Result.UNDEFINED.getStatus())) {
            subCounts.undefined++;
        } else if (resultStatus.equals(Result.SKIPPED.getStatus())) {
            subCounts.skipped++;
        } else if (resultStatus.equals(Result.PASSED)) {
            subCounts.passed++;
        }
    }
}

class SubCounts {
    public int passed = 0;
    public int failed = 0;
    public int skipped = 0;
    public int pending = 0;
    public int undefined = 0;

    public int getTotal() {
        return passed + failed + skipped + pending + undefined;
    }
}
