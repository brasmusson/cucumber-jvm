package cucumber.runtime.model;

import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberFeatureTest {
    @Test
    public void succeds_if_no_features_are_found() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        CucumberFeature.load(resourceLoader, asList("does/not/exist"), emptyList(), new PrintStream(new ByteArrayOutputStream()));
    }

    @Test
    public void logs_message_if_no_features_are_found() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources("does/not/exist", ".feature")).thenReturn(Collections.<Resource>emptyList());

        CucumberFeature.load(resourceLoader, asList("does/not/exist"), emptyList(), new PrintStream(baos));

        assertEquals(String.format("No features found at [does/not/exist]%n"), baos.toString());
    }

    @Test
    public void logs_message_if_features_are_found_but_filters_are_too_strict() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mockFeatureFileResource("features", "Feature: foo");

        CucumberFeature.load(resourceLoader, asList("features"), asList((Object) "@nowhere"), new PrintStream(baos));

        assertEquals(String.format("None of the features at [features] matched the filters: [@nowhere]%n"), baos.toString());
    }

    @Test
    public void logs_message_if_no_feature_paths_are_given() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceLoader resourceLoader = mock(ResourceLoader.class);

        CucumberFeature.load(resourceLoader, Collections.<String>emptyList(), emptyList(), new PrintStream(baos));

        assertEquals(String.format("Got no path to feature directory or feature file%n"), baos.toString());
    }

    @Test
    public void applies_line_filters_when_loading_a_feature() throws Exception {
        String featurePath = "path/foo.feature";
        String feature = "" +
                "Feature: foo\n" +
                "  Scenario: scenario 1\n" +
                "    * step\n" +
                "  Scenario: scenario 2\n" +
                "    * step\n";
        ResourceLoader resourceLoader = mockFeatureFileResource(featurePath, feature);

        List<CucumberFeature> features = CucumberFeature.load(
                resourceLoader,
                asList(featurePath + ":2"),
                new ArrayList<Object>(),
                new PrintStream(new ByteArrayOutputStream()));

        assertEquals(1, features.size());
        assertEquals(1, features.get(0).getFeatureElements().size());
        assertEquals("Scenario: scenario 1", features.get(0).getFeatureElements().get(0).getVisualName());
    }

    @Test
    public void loads_features_specified_in_rerun_file() throws Exception {
        String featurePath1 = "path/bar.feature";
        String feature1 = "" +
                "Feature: bar\n" +
                "  Scenario: scenario bar\n" +
                "    * step\n";
        String featurePath2 = "path/foo.feature";
        String feature2 = "" +
                "Feature: foo\n" +
                "  Scenario: scenario 1\n" +
                "    * step\n" +
                "  Scenario: scenario 2\n" +
                "    * step\n";
        String rerunPath = "path/rerun.txt";
        String rerunFile = featurePath1 + ":2 " + featurePath2 + ":4";
        ResourceLoader resourceLoader = mockFeatureFileResource(featurePath1, feature1);
        mockFeatureFileResource(resourceLoader, featurePath2, feature2);
        mockFileResource(resourceLoader, rerunPath, null, rerunFile);

        List<CucumberFeature> features = CucumberFeature.load(
                resourceLoader,
                asList("@" + rerunPath),
                new ArrayList<Object>(),
                new PrintStream(new ByteArrayOutputStream()));

        assertEquals(2, features.size());
        assertEquals(1, features.get(0).getFeatureElements().size());
        assertEquals("Scenario: scenario bar", features.get(0).getFeatureElements().get(0).getVisualName());
        assertEquals(1, features.get(1).getFeatureElements().size());
        assertEquals("Scenario: scenario 2", features.get(1).getFeatureElements().get(0).getVisualName());
    }

    @Test
    public void loads_features_specified_in_rerun_file_from_classpath_when_not_in_file_system() throws Exception {
        String featurePath = "path/bar.feature";
        String feature = "" +
                "Feature: bar\n" +
                "  Scenario: scenario bar\n" +
                "    * step\n";
        String rerunPath = "path/rerun.txt";
        String rerunFile = featurePath + ":2";
        ResourceLoader resourceLoader = mockFeatureFileResource("classpath:" + featurePath, feature);
        mockFeaturePathToNotExist(resourceLoader, featurePath);
        mockFileResource(resourceLoader, rerunPath, suffix(null), rerunFile);

        List<CucumberFeature> features = CucumberFeature.load(
                resourceLoader,
                asList("@" + rerunPath),
                new ArrayList<Object>(),
                new PrintStream(new ByteArrayOutputStream()));

        assertEquals(1, features.size());
        assertEquals(1, features.get(0).getFeatureElements().size());
        assertEquals("Scenario: scenario bar", features.get(0).getFeatureElements().get(0).getVisualName());
    }

    private ResourceLoader mockFeatureFileResource(String featurePath, String feature)
            throws IOException, UnsupportedEncodingException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        mockFeatureFileResource(resourceLoader, featurePath, feature);
        return resourceLoader;
    }

    private void mockFeatureFileResource(ResourceLoader resourceLoader, String featurePath, String feature)
            throws IOException, UnsupportedEncodingException {
        mockFileResource(resourceLoader, featurePath, ".feature", feature);
    }

    private void mockFileResource(ResourceLoader resourceLoader, String featurePath, String extension, String feature)
            throws IOException, UnsupportedEncodingException {
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(featurePath);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(feature.getBytes("UTF-8")));
        when(resourceLoader.resources(featurePath, extension)).thenReturn(asList(resource));
    }

    private void mockFeaturePathToNotExist(ResourceLoader resourceLoader, String featurePath) {
        when(resourceLoader.resources(featurePath, ".feature")).thenThrow(new IllegalArgumentException("Not a file or directory"));
    }

    private String suffix(String suffix) {
        return suffix;
    }
}
