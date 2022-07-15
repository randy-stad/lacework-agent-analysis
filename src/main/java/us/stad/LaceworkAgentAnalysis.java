package us.stad;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import us.stad.entity.DataSheet;

/**
 * Lacework agent analysis tool.
 */
public class LaceworkAgentAnalysis {

    private static final Log LOG = LogFactory.getLog(LaceworkAgentAnalysis.class);

    public static void main(String[] args) {

        CommandLine line = null;
        final Options options = buildOptions();
        try {
            CommandLineParser parser = new DefaultParser();
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            LOG.error("option parsing failed", exp);
            System.exit(1);
        }

        if (line.hasOption("h")) {
            new HelpFormatter().printHelp("lacework-agent-analysis", options);
            System.exit(0);
        }

        if (line.hasOption("s") && !line.hasOption("o")) {
            new HelpFormatter().printHelp("lacework-agent-analysis", options);
            System.exit(0);
        }

        if (line.hasOption("s")) {
            processDirectory(line.getOptionValue("s"), line.hasOption("c"), line.hasOption("m"));
        }

        if (line.hasOption("o")) {
            try (FileWriter writer = new FileWriter(line.getOptionValue("o"))) {
                if (line.hasOption("r")) {
                    sheet.dumpReversedCSV(writer);
                } else {
                    sheet.dumpCSV(writer);
                }
            } catch (IOException e) {
                LOG.error("write failed", e);
            }
        }

    }

    private static DataSheet sheet = new DataSheet();

    private static void processDirectory(String directory, boolean clusterCounts, boolean modeCounts) {
        File dir = new File(directory);
        if (!dir.isDirectory()) {
            LOG.error(directory + ": is not a directory");
            return;
        }
        File[] files = dir.listFiles((d, s) -> {
            return s.toLowerCase().endsWith("csv");
        });
        for (int i = 0; i < files.length; i++) {
            LOG.info("processing " + files[i].getName());
            processInputFile(files[i], clusterCounts, modeCounts);
        }
    }

    private static void processInputFile(File file, boolean clusterCounts, boolean modeCounts) {

        LocalDateTime dateTime = parseDateTimeFromFilename(file.getName());

        try (Reader in = new FileReader(file)) {
            Iterable<CSVRecord> records = buildFormat().parse(in);
            for (CSVRecord record : records) {
                String status = record.get("Agent Status");
                String name = record.get("Name");
                String mode = record.get("Agent Mode");
                if (status.equalsIgnoreCase("active")) {
                    if (clusterCounts) sheet.incrementAndGet(dateTime, nameToCluster(name));
                    if (modeCounts) sheet.incrementAndGet(dateTime, mode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String nameToCluster(String name) {
        String split[] = name.split("-Node");
        if (split[0] != null && split[0].startsWith("eks")) {
            return split[0];
        }
        return null;
    }

    // Jul 12 2022_11_50 (MDT)
    public static LocalDateTime parseDateTimeFromFilename(String filename) {
        String value = filename.replace("agents_agent_monitor_", "");
        value = value.replace(".csv", "");
        value = value.substring(0, value.length() - 6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy_HH_mm", Locale.getDefault());
        return LocalDateTime.parse(value, formatter);
    }

    private static CSVFormat buildFormat() {
        CSVFormat format = CSVFormat.RFC4180.builder().setHeader(new String[0]).build();
        return format;
    }

    private static Options buildOptions() {

        Options options = new Options();
        options.addOption("m", "mode", false, "calculate agent mode metrics");
        options.addOption("c", "cluster", false, "calculate cluster metrics");
        options.addOption("o", "output", true, "output file (required if source specified)");
        options.addOption("s", "source", true, "source directory of agent resource CSV files to parse");
        options.addOption("r", "reverse", false, "reverse rows and columns in the resulting CSV file");
        options.addOption("h", "help", false, "print this message");

        return options;

    }
}
