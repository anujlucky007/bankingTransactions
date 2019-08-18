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

Account Create Api

    url : http://localhost:8080/account/create
    Request Body :
            curl -X POST \
              http://localhost:8080/account/create \
              -H 'Accept: */*' \
              -H 'Accept-Encoding: gzip, deflate' \
              -H 'Cache-Control: no-cache' \
              -H 'Connection: keep-alive' \
              -H 'Content-Length: 115' \
              -H 'Content-Type: application/json' \
              -H 'Host: localhost:8080' \
              -H 'Postman-Token: c63b2ea1-38ea-4c44-9bb3-62360a4fe959,1c82ea03-4bae-4d44-a4b4-79ffd860460c' \
              -H 'User-Agent: PostmanRuntime/7.15.2' \
              -H 'cache-control: no-cache' \
              -d '{
                "accountBalance": 100,
                "accountType": "SAVINGS",
                "status": "ACTIVE",
                "customerName": "ANUJ Rai"
            }'
     Response :
            
            {
                "accountNumber": 1,
                "accountBalance": 100.0,
                "customerName": "ANUJ Rai",
                "baseCurrency": "INR",
                "accountType": "SAVINGS",
                "status": "ACTIVE"
            }
            
Account Status
    
    URL : http://localhost:8080/account/create
    
    Request Body
            curl -X GET \
              http://localhost:8080/account/1 \
              -H 'Accept: */*' \
              -H 'Accept-Encoding: gzip, deflate' \
              -H 'Cache-Control: no-cache' \
              -H 'Connection: keep-alive' \
              -H 'Host: localhost:8080' \
              -H 'Postman-Token: 43d28510-6fae-4892-9356-6cbaf0f838dd,be05a245-d3a9-4756-9d25-e7bfdbeb6486' \
              -H 'User-Agent: PostmanRuntime/7.15.2' \
              -H 'cache-control: no-cache'
              
    Response :
            
            {
                "accountNumber": 1,
                "accountBalance": 100.0,
                "customerName": "ANUJ Rai",
                "baseCurrency": "INR",
                "accountType": "SAVINGS",
                "status": "ACTIVE"
            }
            
            
    Request :
            
            curl -X GET \
              http://localhost:8080/account/2 \
              -H 'Accept: */*' \
              -H 'Accept-Encoding: gzip, deflate' \
              -H 'Cache-Control: no-cache' \
              -H 'Connection: keep-alive' \
              -H 'Host: localhost:8080' \
              -H 'Postman-Token: 5681be1c-cd0a-4284-8553-95651b88a75a,78e9929d-bdeb-4675-a0c4-167ac243c714' \
              -H 'User-Agent: PostmanRuntime/7.15.2' \
              -H 'cache-control: no-cache'
              
    Response :
            
            {
                "errorMessage": "Account Not Found",
                "errorCode": "ACC.INVALID.001"
            }               
    

Transaction Api

     URL :http://localhost:8080/transaction/1/transaction-request/INTRABANK
   
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
                