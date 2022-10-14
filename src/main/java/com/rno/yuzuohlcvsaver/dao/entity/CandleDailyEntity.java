package com.rno.yuzuohlcvsaver.dao.entity;

import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "candle_d")
@ToString
public class CandleDailyEntity extends BaseCandle {

}
