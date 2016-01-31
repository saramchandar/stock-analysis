package com.ramchandar.stockanalysis

import groovy.sql.Sql


def Sql sql = Sql.newInstance("jdbc:postgresql://localhost:5432/sar", "postgres", "abc123", "org.postgresql.Driver")

def query = "select min(low) as min, max(high) as max " +
        "from nsefutures " +
        "where time >= '09:15' and time <= '09:44' " +
        "and date = '2015-04-06' and name = 'BANKNIFTY_F1'"

def Mmin, Mmax
sql.eachRow(query) {
   Mmin = it.min
    Mmax = it.max
}
println "Min/Max is -> $Mmin,$Mmax"

def Mdayp

query ="select open as dayp from nsefutures" +
        " where date='2015-04-06' AND time='09:45' "
sql.eachRow(query){
    Mdayp= it.dayp
}
println Mdayp

def tradetype

switch (Mdayp) {
    case (Mdayp > Mmax):
        tradetype = "Above high"
        break
    case (Mdayp < Mmin):
        tradetype = "Below low"
        break
    default:
        tradetype = "Between high and low"
}

println "TradeType is $tradetype"

query="select time, high from nsefutures where date ='2015-04-06' and high > $Mmax AND time > '09:44' order by time limit 1"

def mtime, mhigh
mhigh=0
sql.eachRow(query){
    mtime = it.time
    mhigh = it.high
}

// println "After 9.44 higher than high -> $mtime, $mhigh"
def buytime, buyprice
if (mhigh == 0) {
    println "No long buy oppurtunity"
}
if (mhigh >  0) {
    if (Mmax + 1 < mhigh) {
        buytime = mtime
        buyprice = (Mmax + 1)
    }

    println "Buy time: $buytime Buy Price: $buyprice"
    def target
    target = buyprice + 40
    println target
    query = "select  time, high from nsefutures where date = '2015-04-06' and high >= $target AND time > $mtime order by time limit 1"

    def stime, shigh
    sql.eachRow(query) {
        stime = it.time
        shigh = it.high
    }


    println "Sell Time $stime Sale Price: $shigh"
}
// this will for price crossing low.
query="select time, low from nsefutures where date ='2015-04-06' and low < $Mmin AND time > '09:44' order by time limit 1"

def mintime, mlow
sql.eachRow(query){
    mintime = it.time
    mlow = it.low
}
if (mlow == 0) {
    println "No sort trade possible"
}
if (mlow > 0) {
    println "Minimum after 9:44: $mintime mlow: $mlow"
    def sortsaletime, sortsaleprice, sorttarget
    sql.eachRow(query) {
        sortsaletime = it.time
        sortsaleprice = it.low}
        sorttarget = sortsaleprice-40
        println "sortsaletime : $sortsaletime, sortsaleprice : $sortsaleprice"
        println "sort target : $sorttarget"
        query="select time, low from nsefutures where date ='2015-04-06' and low <= $sorttarget AND time > $sortsaletime order by time limit 1"
        def sortbuytime,sortbuyprice
        sql.eachRow(query){
            sortbuytime = it.time
            sortbuyprice = it.low
            println "sortbuy time : $sortbuytime, Sort buy Price : $sortbuyprice"


        }
}