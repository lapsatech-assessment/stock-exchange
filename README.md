# Stock Exchange sample app

To run the sample app execute:

    mvn install
    mvn -f stock-exchange-app/ -Prun

The app will build and start, and you will get the console prompt afterward

    >

You type the command, and the system executes it in the stock exchange system.
Additional terminal sessions can be created in parallel using Telnet

    telnet localhost 7070

# Commands reference

## Create entries

#### Create trader

    CREATE TRADER <id> <traderName>

#### Create a security instrument and the order book

    CREATE SECURITY <id> <symbol> <initialPrice>

#### Create a composite instrument and link it to the nested components

    CREATE COMPOSITE <id> <symbol> <securitySymbol1...> <securitySymbolN>

### Placing/canceling orders in the order book

#### Place a buy market order

    BUY <traderId> <symbol> <quantity>

#### Place a sell market order

    SELL <traderId> <symbol> <quantity>

#### Place a buy limit order

    BID <traderId> <symbol> <quantity> <price>

#### Place a sell limit order

    ASK <traderId> <symbol> <quantity> <price>

#### Attempt to cancel the order

    CANCEL <symbol> <orderId>

## Describe entries

#### Describe the instrument status

    DESCRIBE INSTRUMENT <symbol>

#### Describe the trader

    DESCRIBE TRADER <traderId>

## List the entries

#### List orders in the order book

    LIST ORDERS <securitySymbol>

#### List all instruments registered in the system

    LIST INSTRUMENTS

## Misc

#### Terminate the session/program

      BYE

Note: Issuing that command in the main session will terminate the application

# Example scenario

      > create trader 1 trader1
      CREATE: Trader[id=1, name=trader1]
      > create trader 2 trader2
      CREATE: Trader[id=2, name=trader2]
      > create security 1 usdeur 1.2
      CREATE: Security[id=1, symbol=USDEUR, marketPrice=1.2]
      2024-06-19 17:48:03 INFO  [pool-3-thread-1] USDEUR - Started
      > create security 2 usdgbp 1.3
      CREATE: Security[id=2, symbol=USDGBP, marketPrice=1.3]
      2024-06-19 17:48:10 INFO  [pool-3-thread-2] USDGBP - Started
      > create composite 3 comp1 usdeur usdgbp
      CREATE: Composite[id=3, symbol=COMP1, marketPrice=1.25, componenents=[Security[id=1, symbol=USDEUR, marketPrice=1.2], Security[id=2, symbol=USDGBP, marketPrice=1.3]]]
      > describe instrument usdeur
      DESCRIBE: Security[id=1, symbol=USDEUR, marketPrice=1.2]
      > describe instrument usdgbp
      DESCRIBE: Security[id=2, symbol=USDGBP, marketPrice=1.3]
      > describe instrument comp1
      DESCRIBE: Composite[id=3, symbol=COMP1, marketPrice=1.25, componenents=[Security[id=1, symbol=USDEUR, marketPrice=1.2], Security[id=2, symbol=USDGBP, marketPrice=1.3]]]
      > list orders usdgbp
      LIST: <EMPTY>
      > list orders usdeur
      LIST: <EMPTY>
      > list instruments
      LIST:
      Security[id=2, symbol=USDGBP, marketPrice=1.3]
      Security[id=1, symbol=USDEUR, marketPrice=1.2]
      Composite[id=3, symbol=COMP1, marketPrice=1.25, componenents=[Security[id=1, symbol=USDEUR, marketPrice=1.2], Security[id=2, symbol=USDGBP, marketPrice=1.3]]]
      > buy 1 usdeur 100
      BUY: Order[id=3538232062919553365, security=Security[id=1, symbol=USDEUR, marketPrice=1.2], type=BUY, trader=Trader[id=1, name=trader1], quantity=100, price=NaN]
      > sell 2 usdeur 100
      SELL: Order[id=2617944189576431928, security=Security[id=1, symbol=USDEUR, marketPrice=1.2], type=SELL, trader=Trader[id=2, name=trader2], quantity=100, price=NaN]
      2024-06-19 17:48:58 INFO  [pool-3-thread-1] StockExchangeApp - Trade executed Trade[id=6796790021360864211, security=Security[id=1, symbol=USDEUR, marketPrice=1.2], buyingOrder=Order[id=3538232062919553365, security=Security[id=1, symbol=USDEUR, marketPrice=1.2], type=BUY, trader=Trader[id=1, name=trader1], quantity=100, price=NaN], sellingOrder=Order[id=2617944189576431928, security=Security[id=1, symbol=USDEUR, marketPrice=1.2], type=SELL, trader=Trader[id=2, name=trader2], quantity=100, price=NaN], price=1.2, quantity=100]
      2024-06-19 17:48:58 INFO  [pool-3-thread-1] StockExchangeApp - Order fulfilled Order[id=3538232062919553365, security=Security[id=1, symbol=USDEUR, marketPrice=1.2], type=BUY, trader=Trader[id=1, name=trader1], quantity=100, price=NaN]
      2024-06-19 17:48:58 INFO  [pool-3-thread-1] StockExchangeApp - Order fulfilled Order[id=2617944189576431928, security=Security[id=1, symbol=USDEUR, marketPrice=1.2], type=SELL, trader=Trader[id=2, name=trader2], quantity=100, price=NaN]
      > bid 2 usdgbp 100 1.35
      BID: Order[id=219252769784576971, security=Security[id=2, symbol=USDGBP, marketPrice=1.3], type=BID, trader=Trader[id=2, name=trader2], quantity=100, price=1.35]
      > ask 1 usdgbp 100 1.32
      ASK: Order[id=2937330223162700471, security=Security[id=2, symbol=USDGBP, marketPrice=1.3], type=ASK, trader=Trader[id=1, name=trader1], quantity=100, price=1.32]
      2024-06-19 17:49:10 INFO  [pool-3-thread-2] StockExchangeApp - Trade executed Trade[id=8630444937854404164, security=Security[id=2, symbol=USDGBP, marketPrice=1.3], buyingOrder=Order[id=219252769784576971, security=Security[id=2, symbol=USDGBP, marketPrice=1.3], type=BID, trader=Trader[id=2, name=trader2], quantity=100, price=1.35], sellingOrder=Order[id=2937330223162700471, security=Security[id=2, symbol=USDGBP, marketPrice=1.3], type=ASK, trader=Trader[id=1, name=trader1], quantity=100, price=1.32], price=1.335, quantity=100]
      2024-06-19 17:49:10 INFO  [pool-3-thread-2] StockExchangeApp - Order fulfilled Order[id=219252769784576971, security=Security[id=2, symbol=USDGBP, marketPrice=1.335], type=BID, trader=Trader[id=2, name=trader2], quantity=100, price=1.35]
      2024-06-19 17:49:10 INFO  [pool-3-thread-2] StockExchangeApp - Order fulfilled Order[id=2937330223162700471, security=Security[id=2, symbol=USDGBP, marketPrice=1.335], type=ASK, trader=Trader[id=1, name=trader1], quantity=100, price=1.32]
      > list instruments
      LIST:
      Security[id=2, symbol=USDGBP, marketPrice=1.335]
      Security[id=1, symbol=USDEUR, marketPrice=1.2]
      Composite[id=3, symbol=COMP1, marketPrice=1.2675, componenents=[Security[id=1, symbol=USDEUR, marketPrice=1.2], Security[id=2, symbol=USDGBP, marketPrice=1.335]]]
      > bye
      See ya!
      2024-06-19 17:49:21 INFO  [pool-3-thread-2] USDGBP - Stopping
      2024-06-19 17:49:21 INFO  [pool-3-thread-1] USDEUR - Stopping
      2024-06-19 17:49:21 INFO  [pool-3-thread-2] ORDER_BOOK_USDGBP - Persisting unfilled orders before shutdown
      2024-06-19 17:49:21 INFO  [pool-3-thread-1] ORDER_BOOK_USDEUR - Persisting unfilled orders before shutdown
