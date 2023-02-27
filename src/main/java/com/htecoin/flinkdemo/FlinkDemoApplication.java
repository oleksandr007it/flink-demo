package com.htecoin.flinkdemo;

import com.htecoin.flinkdemo.domain.BetSlip;
import com.htecoin.flinkdemo.domain.BetSlipChecking;
import com.htecoin.flinkdemo.domain.BetSlipDebeziumDeserializationSchema;
import com.htecoin.flinkdemo.domain.BetSlipProcess;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class FlinkDemoApplication {

	public static void main(String[] args) {
		//SpringApplication.run(BetSlipProcess.class, args);

		SpringApplication application = new SpringApplication(FlinkDemoApplication.class);

		application.run(args);

		Properties debeziumProperties = new Properties();
		// todo incremental.snapshot.chunk.size
		// snapshot.fetch.size
		// min.row.count.to.stream.results
//        debeziumProperties.put("debezium.snapshot.locking.mode", "none");
		debeziumProperties.setProperty("decimal.handling.mode", "string");
		MySqlSource<BetSlip> mySqlSource = MySqlSource.<BetSlip>builder()
				.hostname("127.0.0.1")
				.port(3306)
				.databaseList("flink_demo") // set captured database
				.tableList("flink_demo.bet_slip") // set captured table
				.username("root")
				.password("root")
				.debeziumProperties(debeziumProperties)
				.deserializer(new BetSlipDebeziumDeserializationSchema())
				.build();

		//StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
// set up the execution environment
		Configuration conf = new Configuration();
		conf.setInteger(RestOptions.PORT, 8082);
		 conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true);
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(8, conf);

		// enable checkpoint
		env.enableCheckpointing(30000);


//        env
//                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
//                // set 4 parallel source tasks
//                .setParallelism(4)
//
//                .print(); // use parallelism 1 for sink to keep message ordering

		DataStream<BetSlip> forwardedReadings =   env
				.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");

		forwardedReadings.print();

		DataStream<String> alarms = forwardedReadings
				.keyBy(r -> r.getPlayerId() + "_" + r.getGameCode())
				.process(new BetSlipChecking()).uid("checkRtp");

		DataStream<String> alarms2 = forwardedReadings
				.keyBy(r -> r.getPlayerId() + "_" + r.getGameCode())
				.process(new BetSlipChecking());


		alarms.print();


		try {
			env.execute("Print MySQL Snapshot + Binlog");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
