package com.htecoin.flinkdemo.domain;

import lombok.Data;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class BetSlipGenerator implements SourceFunction<BetSlip> {

    public static final int SLEEP_MILLIS_PER_EVENT = 1000;
    private volatile boolean running = true;


    @Override
    public void run(SourceContext<BetSlip> ctx) throws Exception {
        long i = 0;
        while (running) {
            BetSlip betSlip = new BetSlip();
            betSlip.setId(i++);
            betSlip.setCreatedDate(new Date());
            betSlip.setCreatedBy("");
            betSlip.setLastModifiedDate(new Date());
            betSlip.setLastModifiedBy("");
            betSlip.setGameCode("HILO");
            betSlip.setOdds(new BigDecimal("1.44"));
            betSlip.setWagerAmount(new BigDecimal("10"));

            betSlip.setPayoffAmount(new BigDecimal("5"));

            if (i % 2 == 0) {
                betSlip.setPlayerId(13L);
                betSlip.setInitWagerAmount(new BigDecimal("10"));
                betSlip.setReturnAmount(new BigDecimal("15"));
            } else {
                betSlip.setPlayerId(2L);
                betSlip.setInitWagerAmount(new BigDecimal("5"));
                betSlip.setReturnAmount(new BigDecimal("20"));
            }

            ctx.collect(betSlip);

            // don't go too fast
            Thread.sleep(SLEEP_MILLIS_PER_EVENT);
        }

    }

    @Override
    public void cancel() {
        running = false;
    }
}
