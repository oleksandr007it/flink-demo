package com.htecoin.flinkdemo.domain;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BetSlipChecking extends KeyedProcessFunction<String, BetSlip, String> {

    // switch to enable forwarding


    private ValueState<BigDecimal> totalReturnAmount;

    private ValueState<BigDecimal> totalWagerAmount;


    @Override
    public void open(Configuration parameters) throws Exception {
        totalReturnAmount = getRuntimeContext().getState(
                new ValueStateDescriptor<>("totalReturnAmount", Types.BIG_DEC));
        totalWagerAmount = getRuntimeContext().getState(
                new ValueStateDescriptor<>("totalWagerAmount", Types.BIG_DEC));
    }


    @Override
    public void processElement(BetSlip betSlip, KeyedProcessFunction<String, BetSlip, String>.Context context, Collector<String> collector) throws Exception {
        BigDecimal rtp;
        if (totalWagerAmount.value() != null && totalReturnAmount.value() != null) {
            totalReturnAmount.update(totalReturnAmount.value().add(betSlip.getReturnAmount()));
            totalWagerAmount.update(totalWagerAmount.value().add(betSlip.getInitWagerAmount()));
            rtp = totalReturnAmount.value().divide(totalWagerAmount.value(), 6, RoundingMode.HALF_UP);
            System.out.println("totalReturnAmount=" + totalReturnAmount.value());
            System.out.println("totalWagerAmount=" + totalWagerAmount.value());
            System.out.println("RTP=" + rtp);


        } else {
            totalReturnAmount.update(betSlip.getReturnAmount());
            totalWagerAmount.update(betSlip.getInitWagerAmount());
            rtp = totalReturnAmount.value().divide(totalWagerAmount.value(), 6, RoundingMode.HALF_UP);
        }

        if (rtp.compareTo(new BigDecimal("2")) > 0) {
            collector.collect("BlockedUser By Id=" + betSlip.getPlayerId());

        } else {
            collector.collect("RTP=" + rtp);

        }
    }
}


//    @Override
//    public void processElement(BetSlip betSlip, KeyedProcessFunction<String, BetSlip, String>.Context context, Collector<String> collector) throws Exception {
//        BigDecimal rtp = BigDecimal.ZERO;
//        if (totalWagerAmount.value() != null && totalReturnAmount.value() != null && betSlip.getReturnAmount() != null) {
//            context.timerService().registerEventTimeTimer(betSlip.getLastModifiedDate().getTime() + 86400000);
//
//            totalReturnAmount.update(totalReturnAmount.value().add(betSlip.getReturnAmount()));
//            totalWagerAmount.update(totalWagerAmount.value().add(betSlip.getInitWagerAmount()));
//            rtp = totalReturnAmount.value().divide(totalWagerAmount.value(), 6, RoundingMode.HALF_UP);
//            System.out.println("totalReturnAmount=" + totalReturnAmount.value());
//            System.out.println("totalWagerAmount=" + totalWagerAmount.value());
//            System.out.println("RTP=" + rtp);
//
//
//        } else if (betSlip.getReturnAmount() != null) {
//            totalReturnAmount.update(betSlip.getReturnAmount());
//            totalWagerAmount.update(betSlip.getInitWagerAmount());
//            rtp = totalReturnAmount.value().divide(totalWagerAmount.value(), 6, RoundingMode.HALF_UP);
//        }
//
//        if (rtp.compareTo(new BigDecimal("2")) > 0) {
//            collector.collect("BlockedUser By Id=" + betSlip.getPlayerId());
//
//        } else {
//            collector.collect("RTP=" + rtp);
//
//        }
//
//    }
//}
