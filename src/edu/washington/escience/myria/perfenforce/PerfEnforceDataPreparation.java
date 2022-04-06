/**
 *
 */
package edu.washington.escience.myria.perfenforce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import edu.washington.escience.myria.CsvTupleReader;
import edu.washington.escience.myria.DbException;
import edu.washington.escience.myria.MyriaConstants;
import edu.washington.escience.myria.RelationKey;
import edu.washington.escience.myria.Schema;
import edu.washington.escience.myria.api.encoding.PerfEnforceStatisticsEncoding;
import edu.washington.escience.myria.api.encoding.PerfEnforceTableEncoding;
import edu.washington.escience.myria.operator.DbInsert;
import edu.washington.escience.myria.operator.DbQueryScan;
import edu.washington.escience.myria.operator.EOSSource;
import edu.washington.escience.myria.operator.EmptySink;
import edu.washington.escience.myria.operator.RootOperator;
import edu.washington.escience.myria.operator.TupleSource;
import edu.washington.escience.myria.operator.network.Consumer;
import edu.washington.escience.myria.operator.network.GenericShuffleProducer;
import edu.washington.escience.myria.operator.network.distribute.BroadcastDistributeFunction;
import edu.washington.escience.myria.operator.network.distribute.RoundRobinDistributeFunction;
import edu.washington.escience.myria.parallel.ExchangePairID;
import edu.washington.escience.myria.parallel.Server;

/**
 * Methods to help prepare the data for PSLA generation
 */
public class PerfEnforceDataPreparation {

  private final Server server;
  private HashMap<Integer, RelationKey> factTableRelationMapper;
  private PerfEnforceTableEncoding factTableDescription;

  /** Logger. */
  protected static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(PerfEnforceDataPreparation.class);

  /**
   * Constructs the PerfEnforceDataPreparation class
   * @param server the current instance of Server
   */
  public PerfEnforceDataPreparation(final Server server) {
    this.server = server;
  }

  /**
   * Ingesting the fact table in a parallel sequence
   *
   * @param factTableDesc contains information about the fact table to ingest
   * @return a hashmap with the relationkeys for all versions of the fact table
   * @throws PerfEnforceException if there is an error ingesting the fact table
   */
  public HashMap<Integer, RelationKey> ingestFact(final PerfEnforceTableEncoding factTableDesc)
      throws PerfEnforceException {
    factTableDescription = factTableDesc;
    factTableRelationMapper = new HashMap<Integer, RelationKey>();

    ArrayList<RelationKey> relationKeysToUnion = new ArrayList<RelationKey>();
    Collections.sort(PerfEnforceDriver.configurations, Collections.reverseOrder());

    // Create a sequence for the largest configuration size
    int maxConfig = PerfEnforceDriver.configurations.get(0);
    Set<Integer> maxWorkerRange = PerfEnforceUtils.getWorkerRangeSet(maxConfig);

    /*
     * First, ingest the fact table under the relationKey with the union ("_U"). Then, create a materialized view with
     * the original relationKey name and add it to the catalog. This is what the user will be using on the MyriaL front
     * end.
     */
    try {
      RelationKey relationKeyWithUnion =
          new RelationKey(
              factTableDesc.relationKey.getUserName(),
              factTableDesc.relationKey.getProgramName(),
              factTableDesc.relationKey.getRelationName() + maxConfig + "_U");

      server.parallelIngestDataset(
          relationKeyWithUnion,
          factTableDesc.schema,
          factTableDesc.delimiter,
          null,
          null,
          null,
          factTableDesc.source,
          maxWorkerRange,
          null);

      relationKeysToUnion.add(relationKeyWithUnion);

      RelationKey relationKeyOriginal =
          new RelationKey(
              factTableDesc.relationKey.getUserName(),
              factTableDesc.relationKey.getProgramName(),
              factTableDesc.relationKey.getRelationName() + maxConfig);
      server.createMaterializedView(
          relationKeyOriginal.toString(MyriaConstants.STORAGE_SYSTEM_POSTGRESQL),
          PerfEnforceUtils.createUnionQuery(relationKeysToUnion),
          maxWorkerRange);
      server.addDatasetToCatalog(
          relationKeyOriginal, factTableDesc.schema, new ArrayList<Integer>(maxWorkerRange));
      factTableRelationMapper.put(maxConfig, relationKeyOriginal);

      /*
       * Iterate and run this for the rest of the workers
       */
      Set<Integer> previousWorkerRange = maxWorkerRange;
      RelationKey previousRelationKey = relationKeyWithUnion;
      for (int c = 1; c < PerfEnforceDriver.configurations.size(); c++) {
        // Get the next sequence of workers
        int currentSize = PerfEnforceDriver.configurations.get(c);
        Set<Integer> currentWorkerRange = PerfEnforceUtils.getWorkerRangeSet(currentSize);
        Set<Integer> diff = Sets.difference(previousWorkerRange, currentWorkerRange);

        RelationKey currentRelationKeyToUnion =
            new RelationKey(
                factTableDesc.relationKey.getUserName(),
                factTableDesc.relationKey.getProgramName(),
                factTableDesc.relationKey.getRelationName() + currentSize + "_U");

        DbQueryScan scan = new DbQueryScan(previousRelationKey, factTableDesc.schema);

        int[] producingWorkers =
            PerfEnforceUtils.getRangeInclusiveArray(Collections.min(diff), Collections.max(diff));
        int[] receivingWorkers =
            PerfEnforceUtils.getRangeInclusiveArray(1, Collections.max(currentWorkerRange));
        final ExchangePairID shuffleId = ExchangePairID.newID();

        RoundRobinDistributeFunction df = new RoundRobinDistributeFunction();
        df.setDestinations(receivingWorkers.length, 1);
        GenericShuffleProducer producer =
            new GenericShuffleProducer(
                scan, new ExchangePairID[] {shuffleId}, receivingWorkers, df);
        Consumer consumer = new Consumer(factTableDesc.schema, shuffleId, producingWorkers);
        DbInsert insert = new DbInsert(consumer, currentRelationKeyToUnion, true);

        Map<Integer, RootOperator[]> workerPlans = new HashMap<>(currentSize);
        for (Integer workerID : producingWorkers) {
          workerPlans.put(workerID, new RootOperator[] {producer});
        }
        for (Integer workerID : receivingWorkers) {
          workerPlans.put(workerID, new RootOperator[] {insert});
        }

        server.submitQueryPlan(new EmptySink(new EOSSource()), workerPlans).get();
        relationKeysToUnion.add(currentRelationKeyToUnion);

        RelationKey currentConfigRelationKey =
            new RelationKey(
                factTableDesc.relationKey.getUserName(),
                factTableDesc.relationKey.getProgramName(),
                factTableDesc.relationKey.getRelationName() + currentSize);
        server.createMaterializedView(
            currentConfigRelationKey.toString(MyriaConstants.STORAGE_SYSTEM_POSTGRESQL),
            PerfEnforceUtils.createUnionQuery(relationKeysToUnion),
            currentWorkerRange);
        server.addDatasetToCatalog(
            currentConfigRelationKey,
            factTableDesc.schema,
            new ArrayList<Integer>(currentWorkerRange));
        factTableRelationMapper.put(currentSize, currentConfigRelationKey);
        previousWorkerRange = currentWorkerRange;
        previousRelationKey = currentConfigRelationKey;
      }
      Collections.sort(PerfEnforceDriver.configurations);
      return factTableRelationMapper;
    } catch (Exception e) {
      throw new PerfEnforceException("Error while ingesting fact table");
    }
  }

  /**
   * Ingesting dimension tables for broadcasting
   *
   * @param dimTableDesc holds information about the dimension table to ingest
   * @throws PerfEnforceException if there is an error ingesting dimension tables
   */
  public void ingestDimension(final PerfEnforceTableEncoding dimTableDesc)
      throws PerfEnforceException {
    Set<Integer> totalWorkers =
        PerfEnforceUtils.getWorkerRangeSet(Collections.max(PerfEnforceDriver.configurations));

    try {

      TupleSource source =
          new TupleSource(
              new CsvTupleReader(dimTableDesc.schema, dimTableDesc.delimiter), dimTableDesc.source);

      server.ingestDataset(
          dimTableDesc.relationKey,
          new ArrayList<Integer>(totalWorkers),
          null,
          source,
          new BroadcastDistributeFunction());

    } catch (Exception e) {
      throw new PerfEnforceException("Error ingesting dimension tables");
    }
  }

  /**
   * This method analyzes the given table
   *
   * @param tableToAnalyze the table to analyze
   */
  public void analyzeTable(final PerfEnforceTableEncoding tableToAnalyze)
      throws DbException, InterruptedException {
    /*
     * If this table is Fact, we need to make sure we run "analyze" on all versions of the table
     */
    if (tableToAnalyze.type.equalsIgnoreCase("fact")) {
      for (Entry<Integer, RelationKey> entry : factTableRelationMapper.entrySet()) {
        PerfEnforceTableEncoding temp =
            new PerfEnforceTableEncoding(
                tableToAnalyze.relationKey,
                tableToAnalyze.type,
                tableToAnalyze.source,
                tableToAnalyze.schema,
                tableToAnalyze.delimiter,
                tableToAnalyze.keys,
                tableToAnalyze.corresponding_fact_key);
        temp.relationKey =
            new RelationKey(
                entry.getValue().getUserName(),
                entry.getValue().getProgramName(),
                entry.getValue().getRelationName());
        postgresStatsAnalyzeTable(temp, PerfEnforceUtils.getWorkerRangeSet(entry.getKey()));
      }
    } else {
      postgresStatsAnalyzeTable(
          tableToAnalyze,
          PerfEnforceUtils.getWorkerRangeSet(Collections.max(PerfEnforceDriver.configurations)));
    }
  }

  /**
   * Helper method that runs the ANALYZE command for a set of workers
   *
   * @param tableToAnalyze the table to analyze
   * @param workers the set of workers to run the ANALYZE command
   */
  public void postgresStatsAnalyzeTable(
      final PerfEnforceTableEncoding tableToAnalyze, Set<Integer> workers)
      throws DbException, InterruptedException {
    for (int i = 0; i < tableToAnalyze.schema.getColumnNames().size(); i++) {
      server.executeSQLStatement(
          String.format(
              "ALTER TABLE %s ALTER COLUMN %s SET STATISTICS 500;",
              tableToAnalyze.relationKey.toString(MyriaConstants.STORAGE_SYSTEM_POSTGRESQL),
              tableToAnalyze.schema.getColumnName(i)),
          workers);
    }
    server.executeSQLStatement(
        String.format(
            "ANALYZE %s;",
            tableToAnalyze.relationKey.toString(MyriaConstants.STORAGE_SYSTEM_POSTGRESQL)),
        workers);
  }

  /**
   * Collects statistical information about each table
   * @throws PerfEnforceException if there is an error collecting data statistics
   */
  public void collectSelectivities() throws PerfEnforceException {
    try {
      /* record the stats for each configuration */
      for (Integer currentConfig : PerfEnforceDriver.configurations) {

        Path statsWorkerPath =
            PerfEnforceDriver.configurationPath
                .resolve("PSLAGeneration")
                .resolve(currentConfig + "_Workers")
                .resolve("stats.json");
        List<PerfEnforceStatisticsEncoding> statsEncodingList =
            new ArrayList<PerfEnforceStatisticsEncoding>();

        RelationKey factRelationKey = factTableRelationMapper.get(currentConfig);
        long factTableTupleCount = server.getDatasetStatus(factRelationKey).getNumTuples();
        statsEncodingList.add(
            runTableRanking(
                factRelationKey,
                factTableTupleCount,
                currentConfig,
                factTableDescription.type,
                factTableDescription.keys,
                factTableDescription.schema));

        for (PerfEnforceTableEncoding t : PerfEnforceDriver.tableList) {
          if (t.type.equalsIgnoreCase("dimension")) {
            RelationKey dimensionTableKey = t.relationKey;
            long dimensionTableTupleCount =
                server.getDatasetStatus(dimensionTableKey).getNumTuples();
            statsEncodingList.add(
                runTableRanking(
                    dimensionTableKey,
                    dimensionTableTupleCount,
                    Collections.max(PerfEnforceDriver.configurations),
                    t.type,
                    t.keys,
                    t.schema));
          }
        }

        try (PrintWriter statsObjectWriter =
                new PrintWriter(new FileOutputStream(new File(statsWorkerPath.toString())))) {
          ObjectMapper mapper = new ObjectMapper();
          mapper.writeValue(statsObjectWriter, statsEncodingList);
        }
      }
    } catch (Exception e) {
      throw new PerfEnforceException("Error collecting table statistics");
    }
  }

  /**
   * Given the primary key, this method determines which key values will return either .001%, .01% or 10% of the data.
   *
   * @param relationKey the relationkey of the table
   * @param tableSize the size of the table
   * @param config the cluster configuration
   * @param type the type of the table -- can be either "fact" or "dimension"
   * @param keys the primary keys of the relation
   * @param schema the schema of the relation
   * @return returns the statistics metadata for a relation
   * @throws PerfEnforceException if there is an error computing the statistics
   */
  public PerfEnforceStatisticsEncoding runTableRanking(
      final RelationKey relationKey,
      final long tableSize,
      final int config,
      final String type,
      final Set<Integer> keys,
      final Schema schema)
      throws PerfEnforceException {

    List<String> selectivityKeys = new ArrayList<String>();
    List<Double> selectivityList = Arrays.asList(new Double[] {.001, .01, .1});

    String attributeKeyString = PerfEnforceUtils.getAttributeKeyString(keys, schema);
    Schema attributeKeySchema = PerfEnforceUtils.getAttributeKeySchema(keys, schema);

    String tableName = relationKey.toString(MyriaConstants.STORAGE_SYSTEM_POSTGRESQL);
    try {
      for (int i = 0; i < selectivityList.size(); i++) {
        String rankingQuery =
            String.format(
                "select %s from (select %s, CAST(rank() over (order by %s asc) AS float)/%s as rank from %s) as r where r.rank >= %s LIMIT 1;",
                attributeKeyString,
                attributeKeyString,
                attributeKeyString,
                tableSize / config,
                tableName,
                selectivityList.get(i));
        String[] sqlResult =
            server.executeSQLStatement(
                rankingQuery, attributeKeySchema, new HashSet<Integer>(Arrays.asList(1)));

        selectivityKeys.add(sqlResult[0]);
      }

      return new PerfEnforceStatisticsEncoding(tableName, tableSize, selectivityKeys);
    } catch (Exception e) {
      throw new PerfEnforceException("error running table ranks");
    }
  }

  /**
   * This method collects features from each query generated by PSLAManager
   * @throws PerfEnforceException if there is an error collecting features from the generated queries
   */
  public void collectFeaturesFromGeneratedQueries() throws PerfEnforceException {
    for (Integer config : PerfEnforceDriver.configurations) {
      Path workerPath =
          PerfEnforceDriver.configurationPath
              .resolve("PSLAGeneration")
              .resolve(config + "_Workers");
      String currentLine = "";

      try {
        try (PrintWriter featureWriter =
                new PrintWriter(workerPath.resolve("TESTING.arff").toString(), "UTF-8")) {

          featureWriter.write("@relation testing \n");
          featureWriter.write("@attribute numberTables numeric \n");
          featureWriter.write("@attribute postgesEstCostMin numeric \n");
          featureWriter.write("@attribute postgesEstCostMax numeric \n");
          featureWriter.write("@attribute postgesEstNumRows numeric \n");
          featureWriter.write("@attribute postgesEstWidth numeric \n");
          featureWriter.write("@attribute numberOfWorkers numeric \n");
          featureWriter.write("@attribute realTime numeric \n");
          featureWriter.write("\n");
          featureWriter.write("@data \n");

          try (BufferedReader br =
                  new BufferedReader(
                      new FileReader(workerPath.resolve("SQLQueries-Generated.txt").toString()))) {
            while ((currentLine = br.readLine()) != null) {
              currentLine =
                  currentLine.replace(
                      factTableDescription.relationKey.getRelationName(),
                      factTableRelationMapper.get(config).getRelationName());

              String features = PerfEnforceUtils.getMaxFeature(server, currentLine, config);
              featureWriter.write(features + "\n");
            }
          }
        }

      } catch (Exception e) {
        throw new PerfEnforceException("Error creating table features");
      }
    }
  }
}
