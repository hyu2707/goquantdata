package com.goquant.quantplatform.gateway.data;

import com.goquant.quantplatform.entity.BarData;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple8;

import java.time.Instant;
import java.util.Set;

// examples:
// https://github.com/apache/flink/blob/master/flink-examples/flink-examples-batch/src/main/java/org/apache/flink/examples/java/relational/EmptyFieldsCountAccumulator.java
// https://ci.apache.org/projects/flink/flink-docs-release-1.2/dev/batch/index.html

public class CsvDataClient extends BaseDataClient {

    public CsvDataClient() {

    }

    @Override
    public BarData getBarData(Instant ts, Set<String> symbols) {
        return null;
    }

    @Override
    public void loadHistoricalDataSnapshot(Instant tsStart, Instant tsEnd) {
        String csvFile = "~/goquantdata/raw_data_dev/type=ticker/freq=day/dt=20210722/20210731183446.228075.csv";
        csvFile = csvFile.replaceFirst("^~", System.getProperty("user.home"));

        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // make parameters available in the web interface
//        env.getConfig().setGlobalJobParameters(params);

        // get the data set
//        final DataSet<StringTriple> file = getDataSet(env, csvFile);
        //        file.print();

        DataSet<Tuple2<String, Double>> csvInput = env.readCsvFile(csvFile)
                .ignoreFirstLine()
                .includeFields("10010")  // take the first and the fourth field
                .types(String.class, Double.class);
//        csvInput.print();

    }



    private static DataSet<StringTriple> getDataSet(
            ExecutionEnvironment env, String filePath) {
        return env.readCsvFile(filePath)
                .fieldDelimiter(",")
                .lineDelimiter("\n")
                .pojoType(StringTriple.class, "Date Time", "Symbol", "Open", "High", "Low", "Close", "Volume", "Adj Close");
    }

    private static class StringTriple extends Tuple8<String, String, String, String, String, String, String, String> {

        public StringTriple() {}

        public StringTriple(String f0, String f1, String f2, String f3, String f4, String f5, String f6, String f7) {
            super(f0, f1, f2, f3, f4, f5, f6, f7);
        }
    }
}
