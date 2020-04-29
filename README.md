   # bankingTransactions

## POSTMAN Collection 
bankingTransactions.json

## USED 
- Language : **Kotlin**
- Framework :**Micronaut**
- Testing : **Mockk**
- Distributed Locking : **Redisson And Rediss in-memory**
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
    4.Transfer Request with duplicate requestId for same account will be discarded and rejected
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
    
    URL : localhost:8080/account/{accountNumber}
    
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
            
Account Transaction
    
    URL: localhost:8080/account/transact
    
    
    Request Body :
            curl -X POST \
              http://localhost:8080/account/transact \
              -H 'Accept: */*' \
              -H 'Accept-Encoding: gzip, deflate' \
              -H 'Cache-Control: no-cache' \
              -H 'Connection: keep-alive' \
              -H 'Content-Length: 180' \
              -H 'Content-Type: application/json' \
              -H 'Host: localhost:8080' \
              -H 'Postman-Token: da621a9c-2239-46e5-bde4-1a4895ce4f75,7e8b9e28-b2cd-4b95-86d3-0e6c4ffbd210' \
              -H 'User-Agent: PostmanRuntime/7.15.2' \
              -H 'cache-control: no-cache' \
              -d '{
                "accountNumber": 1,
                "transactionAmount": {
                	"value":3,
                	"currency" :"EUR"
                },
                "activityRemark":"deposit 3 Euro in account",
                "activityType" :"DEPOSIT"
            }'
            
    Response :
            
            {
                "accountNumber": 1,
                "updatedAccountBalance": 336.79,
                "status": "COMPLETED",
                "message": "deposit 3 Euro in account"
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
                -H 'Content-Length: 184' \
                -H 'Content-Type: application/json' \
                -H 'Host: localhost:8080' \
                -H 'Postman-Token: e9158873-7138-4fe9-99be-403e4afc5747,6c2f9f85-7f45-43f9-904d-6f348c997754' \
                -H 'User-Agent: PostmanRuntime/7.15.2' \
                -H 'cache-control: no-cache' \
                -d '{
                  "requestId": "223",
                  "description": "anuj money ask",
                  "creditor": {
                      "accountNumber": 2
                  },
                  "value": {
                      "amount": 1,
                      "currency": "EUR"
                  }
              }'
      
      Response:
      
       {
           "id": "e241c307-f05c-465c-8dc6-1255dc4fa460",
           "value": {
               "amount": 1.0,
               "currency": "EUR"
           },
           "status": "COMPLETED",
           "message": "anuj money ask"
       }
       
       
     Request :
            curl -X POST \
              http://localhost:8080/transaction/1/transaction-request/INTRABANK \
              -H 'Accept: */*' \
              -H 'Accept-Encoding: gzip, deflate' \
              -H 'Cache-Control: no-cache' \
              -H 'Connection: keep-alive' \
              -H 'Content-Length: 187' \
              -H 'Content-Type: application/json' \
              -H 'Host: localhost:8080' \
              -H 'Postman-Token: 38fc73e6-14a2-43dd-904f-07bbbe9af64d,6ee1af4e-032e-48dd-a8a9-6f9b6e1686ed' \
              -H 'User-Agent: PostmanRuntime/7.15.2' \
              -H 'cache-control: no-cache' \
              -d '{
                "requestId": "2232",
                "description": "anuj money ask",
                "creditor": {
                    "accountNumber": 222
                },
                "value": {
                    "amount": 1,
                    "currency": "EUR"
                }
            }'
            
      Response :
            
            {
                "id": "30fe05ad-28d0-46ce-9ca4-0cedcf5398e2",
                "value": {
                    "amount": 1.0,
                    "currency": "EUR"
                },
                "status": "FAILED",
                "message": "Creditor : Account Not Found : Reverse Transaction"
            }        
 
 
 Transaction Status
    
     URL : localhost:8080/transaction/{transactionNumber}/status  
     
     Request :
       curl -X GET \
         http://localhost:8080/transaction/30fe05ad-28d0-46ce-9ca4-0cedcf5398e2/status \
         -H 'Accept: */*' \
         -H 'Accept-Encoding: gzip, deflate' \
         -H 'Cache-Control: no-cache' \
         -H 'Connection: keep-alive' \
         -H 'Content-Type: application/json' \
         -H 'Host: localhost:8080' \
         -H 'Postman-Token: aac8b06f-b18d-4b7b-865b-a8eb2ef7795e,5cda0e10-604e-4636-ac8a-4345665e40a3' \
         -H 'User-Agent: PostmanRuntime/7.15.2' \
         -H 'cache-control: no-cache'
         
     Response:
        
        {
            "id": "30fe05ad-28d0-46ce-9ca4-0cedcf5398e2",
            "value": {
                "amount": 1.0,
                "currency": "EUR"
            },
            "status": "FAILED",
            "message": "Creditor : Account Not Found : Reverse Transaction"
        }    
                       
