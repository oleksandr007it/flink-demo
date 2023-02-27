package com.htecoin.flinkdemo.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.ConverterType;

import java.util.HashMap;

public class BetSlipDebeziumDeserializationSchema implements DebeziumDeserializationSchema<BetSlip> {
    private static final long serialVersionUID = 1L;
    private transient JsonConverter jsonConverter;
    private final Boolean includeSchema;

    public BetSlipDebeziumDeserializationSchema() {
        this(false);
    }

    public BetSlipDebeziumDeserializationSchema(Boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    public void deserialize(SourceRecord record, Collector<BetSlip> out) throws Exception {
        if (this.jsonConverter == null) {
            this.jsonConverter = new JsonConverter();
            HashMap<String, Object> configs = new HashMap(2);
            configs.put("converter.type", ConverterType.VALUE.getName());
            configs.put("schemas.enable", this.includeSchema);
            this.jsonConverter.configure(configs);
        }

        byte[] bytes = this.jsonConverter.fromConnectData(record.topic(), record.valueSchema(), record.value());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = new String(bytes);
        JsonNode actualObj = objectMapper.readTree(json);
        JsonNode jsonNode = actualObj.get("after");
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        BetSlip betSlip = objectMapper.treeToValue(jsonNode, BetSlip.class);
        if (betSlip==null)
        {
            betSlip=new BetSlip();
        }

        out.collect(betSlip);
    }

    public TypeInformation<BetSlip> getProducedType() {
        return BasicTypeInfo.of(BetSlip.class);
    }
}
