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
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * This example shows how to use a CoProcessFunction and Timers.
 */
public class BetSlipProcessGenerated {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        // enable checkpoint
        env.enableCheckpointing(3000);


        DataStream<BetSlip> forwardedReadings = env
                .addSource(new BetSlipGenerator());


        forwardedReadings.print();

        DataStream<String> alarms = forwardedReadings
                .keyBy(r -> r.getPlayerId() + "_" + r.getGameCode())
                .process(new BetSlipChecking());

        alarms.print();


        env.execute("Print MySQL Snapshot + Binlog");
    }

}


