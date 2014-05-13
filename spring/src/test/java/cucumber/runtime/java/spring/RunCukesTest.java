package cucumber.runtime.java.spring;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(glue = {"cucumber.runtime.java.spring", "cucumber.api.spring"})
public class RunCukesTest {
}
