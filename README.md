# bankingTransactions

## POSTMAN Collection 
bankingTransactions.json

## USED 
- Language : **Kotlin**
- Framework :**Micronaut**
- Testing : **Mockk**
- Distributed Locking : **Redisson And Rediss**
- Database - **H2 inmemory**
- Build Tool : **Gradle**
- For Unit test and Api test Micronaut Test  

## How To Run Application
 Run application using following command 
 - gradle clean build
 - gradle run
 - Use Postman collection present in git root folder
 
 
## Troubleshoot
 if you get error as  Exception in thread "main" java.lang.RuntimeException: Can't start redis server. Check logs for details.
 - lsof -i:6379
 - kill -9 PID 

## HLD

![Image of Yaktocat](
diagram.jpg)

## Assumptions
    
    1 Account of users in different contries in same Bank, no fees are charged for tansfer and exchange
    2.Exchange Rate are in application yaml , right now INR USD AND EURO are supported
    
    
## Features
    1 Account has a base currency
    2 Transfer can be done From INR account -> USD Account OR vice versa 
    3 Transfer Request can be in any Currency, amount will be deducted or deposited based on base currency of account and exchange Rate
    4.Transfer Request with duplicate requestId for same account will be discarded ir rejected
    5.Transfer request status
    6.Distributed Lock managemnt using Rediss and Redisson for synchronized Account activity Across Multiple Instance of server
    7.Suports Two type of transfer Inter and Intra 
    8.Inter bank transfer is not implemented  
    
## API Documentation

Transaction Api

    Api :http://localhost:8080/transaction/1/transaction-request/INTRABANK
   
     Request body 
              curl -X POST \
                http://localhost:8080/transaction/1/transaction-request/INTRABANK \
                -H 'Accept: */*' \
                -H 'Accept-Encoding: gzip, deflate' \
                -H 'Cache-Control: no-cache' \
                -H 'Connection: keep-alive' \
                -H 'Content-Length: 185' \
                -H 'Content-Type: application/json' \
                -H 'Host: localhost:8080' \
                -H 'cache-control: no-cache' \
                -d '{
                  "requestId": "23",
                  "description": "anuj money ask",
                  "creditor": {
                      "accountNumber": 2
                  },
                  "value": {
                      "amount": 100,
                      "currency": "INR"
                  }
              }'
      
      Response:
      
       {
           "id": "dae4ed02-b07b-4e14-b6ed-7a909de07d7b",
           "value": {
               "amount": 1.0,
               "currency": "EUR"
           },
           "status": "COMPLETED",
           "message": "anuj money ask"
       }
                