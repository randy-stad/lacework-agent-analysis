package us.stad;

import java.io.File;
import java.io.FileReader;
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

        if (line.hasOption("s")) {
            processDirectory(line.getOptionValue("s"));
        }

    }

    private static DataSheet sheet = new DataSheet();

    private static void processDirectory(String directory) {
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
            processInputFile(files[i]);
        }
        sheet.dumpCSV();
    }


    private static void processInputFile(File file) {

        LocalDateTime dateTime = parseDateTimeFromFilename(file.getName());
    
        try (Reader in = new FileReader(file)) {
            Iterable<CSVRecord> records = buildFormat().parse(in);
            for (CSVRecord record : records) {
                String status = record.get("Agent Status");
                String name = record.get("Name");
                if (status.equalsIgnoreCase("active")) {
                    sheet.incrementAndGet(dateTime, nameToCluster(name));
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
    //Jul 12 2022_11_50 (MDT)
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
        options.addOption("o", "output", true, "output file (required if source specified)");
        options.addOption("s", "source", true, "source directory of CSV files to parse downloaded from Lacework");
        options.addOption("h", "help", false, "print this message");

        return options;

    }
}
