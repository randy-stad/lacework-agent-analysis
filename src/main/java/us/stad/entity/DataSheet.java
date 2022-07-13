package us.stad.entity;

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
    SortedSet<String> columns = new TreeSet<>();

    public DataSheet() {
        super();
    }

    public int incrementAndGet(LocalDateTime dateTime, String columnName) {
        if (dateTime == null || columnName == null) {
            return 0;
        }
        columns.add(columnName);
        if (!data.containsKey(dateTime)) {
            data.put(dateTime, new HashMap<String, AtomicInteger>());
        }
        if (!data.get(dateTime).containsKey(columnName)) {
            data.get(dateTime).put(columnName, new AtomicInteger(0));
        }
        return data.get(dateTime).get(columnName).incrementAndGet();
    }

    public void dumpCSV() {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        // write the header row first

        StringBuffer buffer = new StringBuffer();
        buffer.append("time");
        for (String columnName : columns) {
            buffer.append("," + columnName);
        }
        buffer.append(System.getProperty("line.separator"));
        System.out.print(buffer.toString());

        // iterate over the timestamps writing each to a line

        for (LocalDateTime dateTime : data.keySet()) {
            buffer = new StringBuffer();
            buffer.append(dateTime.format(dateTimeFormatter));
            for (String columnName : columns) {
                int value = 0;
                if (data.get(dateTime).containsKey(columnName)) {
                    value = data.get(dateTime).get(columnName).get();
                }
                buffer.append(',');
                buffer.append(Integer.toString(value));
            }
            buffer.append(System.getProperty("line.separator"));
            System.out.print(buffer.toString());
        }

    }

}
