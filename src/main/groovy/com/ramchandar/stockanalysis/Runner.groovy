package com.ramchandar.stockanalysis

import java.time.LocalDate

class Runner {

    def static LOAD = 1
    def static SHOW_DB = 2
    def static LIST_FEW = 3
    def static TRUNCATE = 4
    def static ANALYSE = 5
    def static EXIT = 6

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
6. Exit
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