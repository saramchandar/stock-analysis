package com.ramchandar.stockanalysis1


/**
 * Created by S. A. Ramchandar on 2/23/2016.
 */

import com.ramchandar.stockanalysis.domain.DAO
import com.ramchandar.stockanalysis.domain.Price
import groovy.sql.Sql
import org.apache.log4j.Logger

import java.time.LocalDate
import java.time.LocalTime

class Ram {

    def static LOG = Logger.getLogger(Ram.class)

    def DAO dao

    Ram(){
        dao = new DAO()
    }
    def static sql = Sql.newInstance("jdbc:postgresql://localhost:5432/NSEFUTURES", "postgres", "abc123", "org.postgresql.Driver")

    public static void main(String[] args) {
        def Ram Ram = new Ram()
        def M_high = 0
        def M_low = 0
        def M_volume = 0
        def M_time = LocalTime.parse('00:00')

        // sql.execute("truncate table nsetradebook")

        sql.eachRow("select  name,date,time,open,high,low,close  from nsetable ") {
            LOG.info("Processing $it.name and $it.date $it.time")
           // Ram.process(it.name as String, LocalDate.parse(it.date as String))
           // Ram.process(it.name as String, LocalDate.parse(it.date as String))



            for (int i = 0; i < 75; i++) {
               // def singlePrice = allPrices[i]

                if (it.time == LocalTime.parse("09:15")) {
                    M_high = it.high
                    M_low = it.low
                    M_volume = it.volume


                }

            }
            println "M_high : $M_high, M_low : $M_low, M_volume : $M_volume"
        }
    }
}