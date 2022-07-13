package us.stad.entity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A data sheet containing resulting informaiton from the analysis. The data
 * sheet increments count values.
 */
public class DataSheet {

    Map<LocalDateTime, Map<String, AtomicInteger>> data = new HashMap<>();
    SortedSet<String> metricSet = new TreeSet<>();
    SortedSet<LocalDateTime> dateTimeSet = new TreeSet<>();

    public DataSheet() {
        super();
    }

    public int incrementAndGet(LocalDateTime dateTime, String columnName) {
        if (dateTime == null || columnName == null) {
            return 0;
        }
        metricSet.add(columnName);
        dateTimeSet.add(dateTime);
        if (!data.containsKey(dateTime)) {
            data.put(dateTime, new HashMap<String, AtomicInteger>());
        }
        if (!data.get(dateTime).containsKey(columnName)) {
            data.get(dateTime).put(columnName, new AtomicInteger(0));
        }
        return data.get(dateTime).get(columnName).incrementAndGet();
    }

    public void dumpCSV(Writer writer) throws IOException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        // write the header row first (which is metric)

        StringBuffer buffer = new StringBuffer();
        buffer.append("time");
        for (String metric : metricSet) {
            buffer.append("," + metric);
        }
        buffer.append(System.getProperty("line.separator"));
        writer.write(buffer.toString());

        // iterate over the timestamps writing each to a line

        for (LocalDateTime dateTime : data.keySet()) {
            buffer = new StringBuffer();
            buffer.append(dateTime.format(dateTimeFormatter));
            for (String metric : metricSet) {
                int value = 0;
                if (data.get(dateTime).containsKey(metric)) {
                    value = data.get(dateTime).get(metric).get();
                }
                buffer.append(',');
                buffer.append(Integer.toString(value));
            }
            buffer.append(System.getProperty("line.separator"));
            writer.write(buffer.toString());
        }

    }

    public void dumpReversedCSV(Writer writer) throws IOException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        // write the header row first (which is time)

        StringBuffer buffer = new StringBuffer();
        buffer.append("metric");
        for (LocalDateTime dateTime : dateTimeSet) {
            buffer.append("," + dateTime.format(dateTimeFormatter));
        }
        buffer.append(System.getProperty("line.separator"));
        writer.write(buffer.toString());

        // iterate over metric writing each to a row

        for (String metric : metricSet) {
            buffer = new StringBuffer();
            buffer.append(metric);

            // iterate over timestamps and write each metric

            for (LocalDateTime dateTime : dateTimeSet) {
                int value = 0;
                if (data.get(dateTime).containsKey(metric)) {
                    value = data.get(dateTime).get(metric).get();
                }
                buffer.append(',');
                buffer.append(Integer.toString(value));
            }
            buffer.append(System.getProperty("line.separator"));
            writer.write(buffer.toString());
        }

    }

}
