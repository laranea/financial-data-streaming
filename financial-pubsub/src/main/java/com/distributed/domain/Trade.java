package com.distributed.domain;

import java.util.Date;
import java.util.UUID;

public class Trade {
    public String time_exchange;
    public String time_coinapi;
    public String uuid;
    public long price;
    public double size;
    public String taker_side;
    public String symbol;
    public double sequence;
    public String trade;

    public Trade(){

    }

    @Override
    public String toString() {
        return "Trade{" +
                "time_exchange=" + time_exchange +
                ", time_coinapi=" + time_coinapi +
                ", uuid=" + uuid +
                ", price=" + price +
                ", size=" + size +
                ", taker_side='" + taker_side + '\'' +
                ", symbol='" + symbol + '\'' +
                ", sequence=" + sequence +
                ", trade='" + trade + '\'' +
                '}';
    }
}
