package dev.benndorf.minitestframework;

import com.google.common.base.Stopwatch;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.MultipleTestTracker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// kinda copied from vanilla I guess?
public class TestReporter implements net.minecraft.gametest.framework.TestReporter {

    private final Document document;
    private final Element testSuite;
    private final Stopwatch stopwatch;
    private final File destination;
    private int failures;
    private int skips;
    private int successes;
    private final MultipleTestTracker tests;

    public TestReporter(final File dest, final MultipleTestTracker tests) throws ParserConfigurationException {
        this.destination = dest;
        this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        this.testSuite = this.document.createElement("testsuite");
        this.tests = tests;

        final Element testSuites = this.document.createElement("testsuites");
        testSuites.setAttribute("name", "MiniTestFramework Tests");
        testSuites.appendChild(this.testSuite);
        this.document.appendChild(testSuites);

        this.testSuite.setAttribute("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        this.stopwatch = Stopwatch.createStarted();
    }

    private Element createTestCase(final GameTestInfo testInfo, final String string) {
        final Element testCase = this.document.createElement("testcase");
        testCase.setAttribute("name", string);
        testCase.setAttribute("classname", testInfo.getStructureName());
        testCase.setAttribute("time", String.valueOf(testInfo.getRunTime() / 1000d));
        this.testSuite.appendChild(testCase);
        return testCase;
    }

    @Override
    public void onTestFailed(final GameTestInfo testInfo) {
        final String name = testInfo.getTestName();
        final String errorMsg = Objects.requireNonNull(testInfo.getError()).getMessage();

        final Element failure; // "I'm a failure :("
        if (testInfo.isRequired()) {
            this.failures++;
            failure = this.document.createElement("failure");
        } else {
            this.skips++;
            failure = this.document.createElement("skipped");
        }
        failure.setAttribute("message", errorMsg);

        final Element testCase = this.createTestCase(testInfo, name);
        testCase.appendChild(failure);
        this.checkDone();
    }

    @Override
    public void onTestSuccess(final GameTestInfo testInfo) {
        this.successes++;
        final String testName = testInfo.getTestName();
        this.createTestCase(testInfo, testName);
        this.checkDone();
    }

    private void checkDone() {
        if (this.tests.isDone()) {
            GlobalTestReporter.finish();
            System.out.println("GameTest done! " + this.tests.getTotalCount() + " tests were run");
            int exitCode = 0;
            if (this.tests.hasFailedRequired()) {
                System.err.println(this.tests.getFailedRequiredCount() + " required tests failed :(");
                exitCode = this.tests.getFailedRequiredCount();
            } else {
                System.out.println("All required tests passed :)");
            }

            if (this.tests.hasFailedOptional()) {
                System.err.println(this.tests.getFailedOptionalCount() + " optional tests failed");
                exitCode = exitCode > 0 ? exitCode : this.tests.getFailedOptionalCount() * -1;
            }
            Runtime.getRuntime().halt(exitCode);
        }
    }

    @Override
    public void finish() {
        this.stopwatch.stop();

        this.testSuite.setAttribute("tests", "" + (this.failures + this.skips + this.successes));
        this.testSuite.setAttribute("name", "root");
        this.testSuite.setAttribute("failures", "" + this.failures);
        this.testSuite.setAttribute("skipped", "" + this.skips);
        this.testSuite.setAttribute("time", String.valueOf(this.stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d));

        try {
            this.save(this.destination);
        } catch (final TransformerException exc) {
            throw new Error("Couldn't save test report", exc);
        }
    }

    public void save(final File file) throws TransformerException {
        final TransformerFactory factory = TransformerFactory.newInstance();

        final Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        final DOMSource source = new DOMSource(this.document);
        final StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
}
