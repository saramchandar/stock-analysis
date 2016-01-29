package com.ramchandar.stockanalysis

class Runner {
    public static void main(String[] args) {

        def Integer choice

        while (choice != 5) {
                print """
Choose operation
1. Load a new file
2. Show size of DB
3. List first few rows from DB
4. Delete entries in DB
5. Exit
>>
"""
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            choice = bufferRead.readLine() as Integer

            switch (choice) {
                case 1:
                    println "Which file do you want to load?"
                    def fileName = bufferRead.readLine()
                    new DataProcessor().processFile(fileName)
                    break
                case 2:
                    new DataProcessor().size()
                    break
                case 3:
                    new DataProcessor().list()
                    break
                case 4:
                    new DataProcessor().truncate()
                    break
                case 5:
                    println "Exiting"
                    break
                default:
                    println "Invalid Option"
                    break
            }
        }
    }
}