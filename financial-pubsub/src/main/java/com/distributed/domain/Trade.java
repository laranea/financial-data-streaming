package com.distributed.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Trade implements Serializable {
    public String time_exchange;
    public String time_coinapi;
    public String uuid;
    public String price;
    public String size;
    public String taker_side;
    public String symbol_id;
    public String sequence;
    public String trade;

    public Trade(){

    }

    @Override
    public String toString() {
        return "Trade {" +
                "time_exchange=" + time_exchange +
                ", time_coinapi=" + time_coinapi +
                ", uuid=" + uuid +
                ", price=" + price +
                ", size=" + size +
                ", taker_side='" + taker_side + '\'' +
                ", symbol='" + symbol_id + '\'' +
                ", sequence=" + sequence +
                ", trade='" + trade + '\'' +
                '}';
    }
}
