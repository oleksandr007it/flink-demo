/*
 * Copyright 2015 Fabian Hueske / Vasia Kalavri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htecoin.flinkdemo.domain;


import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

/**
 * This example shows how to use a CoProcessFunction and Timers.
 */
public class BetSlipProcess {

    public static void main(String[] args) throws Exception {

        Properties debeziumProperties = new Properties();
        // todo incremental.snapshot.chunk.size
        // snapshot.fetch.size
        // min.row.count.to.stream.results
//        debeziumProperties.put("debezium.snapshot.locking.mode", "none");
//        debeziumProperties.put("debezium.snapshot.locking.mode", "none");
//
        debeziumProperties.setProperty("debezium.snapshot.locking.mode", "none");
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
        // conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true);
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(8, conf);

        // enable checkpoint
        env.enableCheckpointing(3000);



//        env
//                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
//                // set 4 parallel source tasks
//                .setParallelism(4)
//
//                .print(); // use parallelism 1 for sink to keep message ordering

        DataStream<BetSlip> forwardedReadings = env
                .fromSource(mySqlSource,WatermarkStrategy.noWatermarks(), "MySQL Source");

        forwardedReadings.print();

        DataStream<String> alarms = forwardedReadings
                .keyBy(r -> r.getPlayerId() + "_" + r.getGameCode())
                .process(new BetSlipChecking()).uid("checkRtp");
//
//        DataStream<String> alarms2 = forwardedReadings
//                .keyBy(r -> r.getPlayerId() + "_" + r.getGameCode())
//                .process(new BetSlipChecking());


        alarms.print();


        env.execute("Print MySQL Snapshot + Binlog");
    }

}
