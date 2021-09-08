package com.goquant.quantplatform.gateway.data;

import com.goquant.quantplatform.common.GoquantConfig;
import com.goquant.quantplatform.common.Util;
import com.goquant.quantplatform.entity.BarData;
import com.goquant.quantplatform.entity.RowData;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.time.Instant;
import java.util.*;

// examples:
// https://github.com/apache/flink/blob/master/flink-examples/flink-examples-batch/src/main/java/org/apache/flink/examples/java/relational/EmptyFieldsCountAccumulator.java
// https://ci.apache.org/projects/flink/flink-docs-release-1.2/dev/batch/index.html

public class CsvDataClient extends BaseDataClient {
    private static final Logger LOG = LoggerFactory.getLogger(CsvDataClient.class);

    public CsvDataClient(GoquantConfig cfg) {
        super(cfg);
    }

    @Override
    protected Map<Instant, BarData> loadHistoricalDataSnapshot(Instant tsStart, Instant tsEnd) {
        Map<Instant, BarData> ret = new HashMap<>();

        String dataPrefixPath = cfg.getDataPath()+"/type=ticker/freq=day/";

        // type=ticker/freq=day/dt=20210722/20210731183446.228075.csv

//        String csvFile = cfg.getDataPath();

        File file = new File(dataPrefixPath);
        String[] dtList = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        for (String dt : dtList) {
            BarData curData = null;
            String csvFile = dataPrefixPath+dt+"/snapshot.dat";
            try {
                String dtStr = dt.split("=")[1];
                Instant curDayTs = Util.stringDayToInstant(dtStr);
                if (curDayTs.isBefore( tsStart) || curDayTs.isAfter(tsEnd)) {
                    continue;
                }
                curData = loadCSVFile(csvFile);
                if (curData != null) {
                    ret.put(curDayTs, curData);
                }
            } catch (Exception e) {
                LOG.error("skip csvFile: "+csvFile);
                e.printStackTrace();
            }
        }


//        LOG.info("folders: {}", snapshots[1]);


//        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
//
//        // make parameters available in the web interface
////        env.getConfig().setGlobalJobParameters(params);
//
//        // get the data set
////        final DataSet<StringTriple> file = getDataSet(env, csvFile);
//        //        file.print();
//
//        DataSet<Tuple2<String, Double>> csvInput = env.readCsvFile(csvFile)
//                .ignoreFirstLine()
//                .includeFields("10010")  // take the first and the fourth field
//                .types(String.class, Double.class);
//
//        try {
//            csvInput.print();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return ret;
    }

    private BarData loadCSVFile(String filePath) throws Exception{
        BarData ret = new BarData();
        CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath)).withSkipLines(1).build();

        List<String[]> r = csvReader.readAll();
        r.forEach(x -> {
            RowData rowData = new RowData(
                    Util.stringDayToInstantHMS(x[0]), // date time
                    x[1], // symbol
                    Double.parseDouble(x[2]), // open
                    Double.parseDouble(x[3]), // high
                    Double.parseDouble(x[4]), // low
                    Double.parseDouble(x[5]), // close
                    Double.parseDouble(x[6]) // vol
            );
            ret.addRow(rowData);
        });
        return ret;
    }

    @Override
    public void startStreamData() {

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
