package com.ramchandar.stockanalysis

import groovy.sql.Sql

import java.time.LocalDate

class Runner {

    def static LOAD = 1
    def static SHOW_DB = 2
    def static LIST_FEW = 3
    def static TRUNCATE = 4
    def static ANALYSE = 5
    def static ADDLOTS = 6
    def static MODIFY_LOT = 7
    def static DELETE_LOT = 8
    def static EXIT = 9
    def static sql = Sql.newInstance("jdbc:postgresql://localhost:5432/NSEFUTURES", "postgres", "abc123", "org.postgresql.Driver")

    public static void main(String[] args) {

        def Integer choice

        while (choice != EXIT) {
                print """
Choose operation
1. Load a new file
2. Show size of DB
3. List first few rows from DB
4. Truncate entries in DB
5. Analyse
6. ADD LOT
7. MODIFY LOT
8. DELETE LOT
9. Exit
>>
"""
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            choice = bufferRead.readLine() as Integer

            switch (choice) {
                case LOAD:
                    println "Which file do you want to load?"
                    def fileName = bufferRead.readLine()
                    new DataProcessor().processFile(fileName)
                    break
                case SHOW_DB:
                    def m_name = 'ACC_F1'
                    def query = "select  lot from nselots where name =  $m_name"

                    def s_lot
                    sql.eachRow(query) {
                        s_lot = it.lot
                    }

                    println s_lot
                    new DataProcessor().size()
                    break
                case LIST_FEW:
                    new DataProcessor().list()
                    break
                case TRUNCATE:
                    new DataProcessor().truncate()
                    break
                case ANALYSE:
                    print "Please enter the stock: "
                    def stock = bufferRead.readLine()
                    print "Please enter the date (yyyy-mm-dd): "
                    def date = bufferRead.readLine()
                    new Analyser().process(stock, LocalDate.parse(date))
                    break
                case ADDLOTS:
                    println "Enter Script Name :"
                    def m_name = bufferRead.readLine()
                    println "Enter Lot size :"
                    def  m_lot  = bufferRead.readLine()
                    int n_lot = m_lot.toInteger()
                    println n_lot
                    sql.execute("insert into nselots (name, lot) values (?, ?)", m_name, n_lot)
                    break
                case MODIFY_LOT:
                    println "Enter Script Name"
                    def n_name = bufferRead.readLine()
                    println "Enter Lot size :"
                    def  m_lot  = bufferRead.readLine()
                    int n_lot = m_lot.toInteger()
                    sql.execute("UPDATE nselots SET lot = ? where name = ?", [n_lot, n_name])
                    break
                case DELETE_LOT:
                    println "Enter Script Name"
                    def n_name = bufferRead.readLine()
                    sql.execute("DELETE from nselots where name = ?", [n_name])
                    break
                case EXIT:
                    println "Exiting"
                    break
                default:
                    println "Invalid Option"
                    break
            }
        }
    }
}