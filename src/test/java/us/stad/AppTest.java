package us.stad;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Test name to cluster.
     */
    public void testNameToCluster() {
        assertEquals("eks-prod-eu", LaceworkAgentAnalysis.nameToCluster("eks-prod-eu-Node-Default"));
    }

    /**
     * Test filename date parsing.
     */
    public void testFilenameDateParsing() {
        LocalDateTime dateTime = LaceworkAgentAnalysis.parseDateTimeFromFilename("agents_agent_monitor_Jul 12 2022_11_50 (MDT).csv");
        assertNotNull(dateTime);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        String stringDateTime = dateTime.format(dateTimeFormatter);
        assertEquals("07-12-2022 11:50:00", stringDateTime);
    }
}
