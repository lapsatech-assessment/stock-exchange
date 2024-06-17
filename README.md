# Stock Exchange sample app

To run the sample app execute:

    mvn install
    mvn -f stock-exchange-app/ -Prun

The app will build and start and you will get the promt 

    >

You type the command and the system executes the in the stock exchange system
Additional  terminal sessions could created in parallel using telnet:

    telnet localhost 7070

# Commands reference

## Create entries

#### Create trader

    CREATE TRADER <id> <traderName>

#### Create security instrument and the order book

    CREATE SECURITY <id> <symbol> <initialPrice>

#### Create composite instrument and link it to the components

    CREATE COMPOSITE <id> <symbol> <securitySymbol1...> <securitySymbolN>

### Placing/canceling orders in the book

#### Place buy market order

    BUY <traderId> <symbol> <quantity>

#### Place sell market order

    SELL <traderId> <symbol> <quantity>

#### Place buy limit order

    BID <traderId> <symbol> <quantity> <price>

#### Place sell limit order

    ASK <traderId> <symbol> <quantity> <price>

#### Attempt to cancel the order

    CANCEL <symbol> <orderId>

## Describe entries

#### Describe instrument status

    DESCRIBE INSTRUMENT <symbol>

#### Describe trader

    DESCRIBE TRADER <traderId>

## List entries

#### List orders in the book

    LIST ORDERS <securitySymbol>

#### List all instruments registered to the system

    LIST INSTRUMENTS

## Misc

#### Quit session/end program

      BYE

Note: Issuing that command in the main session will terminate the application

# Example scenario

      > create trader 1 trader1
      CREATE: TraderRecord[id=1, name=trader1]
      > create trader 2 trader2
      CREATE: TraderRecord[id=2, name=trader2]
      > create security 1 usdeur 1.2
      CREATE: SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2]
      > 2024-06-17 11:04:18 INFO  [pool-3-thread-1] USDEUR - Started
      create security 2 usdgbp 1.3
      CREATE: SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3]
      > 2024-06-17 11:04:23 INFO  [pool-3-thread-2] USDGBP - Started
      create composite 3 comp1 usdeur usdgbp
      CREATE: CompositeRecord[id=3, symbol=COMP1, marketPrice=1.25, componenents=[SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2], SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3]]]
      > describe instrument usdeur
      DESCRIBE: SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2]
      > describe instrument usdgbp
      DESCRIBE: SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3]
      > describe instrument comp1
      DESCRIBE: CompositeRecord[id=3, symbol=COMP1, marketPrice=1.25, componenents=[SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2], SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3]]]
      > list orders usdgbp
      LIST: <EMPTY>
      > list orders usdeur
      LIST: <EMPTY>
      > list instruments
      LIST:
      SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3]
      SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2]
      CompositeRecord[id=3, symbol=COMP1, marketPrice=1.25, componenents=[SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2], SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3]]]
      > buy 1 usdeur 100
      BUY: OrderRecord[id=2443005805398148662, instrument=SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2], type=BUY, trader=TraderRecord[id=1, name=trader1], quantity=100, price=-1.0]
      > sell 2 usdeur 100
      SELL: OrderRecord[id=5298743569042784410, instrument=SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2], type=SELL, trader=TraderRecord[id=2, name=trader2], quantity=100, price=-1.0]
      > 2024-06-17 11:05:43 INFO  [pool-3-thread-1] Trades - TRADE 9088938537078047375 USDEUR buyer:trader2 seller:trader1 100 1.2
      2024-06-17 11:05:43 INFO  [pool-3-thread-1] Orders - ORDER EXECUTED 2443005805398148662 USDEUR BUY trader1 100 -1.0
      2024-06-17 11:05:43 INFO  [pool-3-thread-1] Orders - ORDER EXECUTED 5298743569042784410 USDEUR SELL trader2 100 -1.0
      bid 2 usdgbp 100 1.35
      BID: OrderRecord[id=8300903126829088184, instrument=SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3], type=BID, trader=TraderRecord[id=2, name=trader2], quantity=100, price=1.35]
      > ask 1 usdgbp 100 1.32
      ASK: OrderRecord[id=6174372926990103407, instrument=SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.3], type=ASK, trader=TraderRecord[id=1, name=trader1], quantity=100, price=1.32]
      > 2024-06-17 11:06:47 INFO  [pool-3-thread-2] Trades - TRADE 604520294237487628 USDGBP buyer:trader1 seller:trader2 100 1.335
      2024-06-17 11:06:47 INFO  [pool-3-thread-2] Orders - ORDER EXECUTED 8300903126829088184 USDGBP BID trader2 100 1.35
      2024-06-17 11:06:47 INFO  [pool-3-thread-2] Orders - ORDER EXECUTED 6174372926990103407 USDGBP ASK trader1 100 1.32
      list instruments
      LIST:
      SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.335]
      SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2]
      CompositeRecord[id=3, symbol=COMP1, marketPrice=1.2675, componenents=[SecurityRecord[id=1, symbol=USDEUR, marketPrice=1.2], SecurityRecord[id=2, symbol=USDGBP, marketPrice=1.335]]]
      > bye
      See ya!
      2024-06-17 11:08:52 INFO  [pool-3-thread-1] USDEUR - Stopping
      2024-06-17 11:08:52 INFO  [pool-3-thread-1] USDEUR - Persisting unfilled orders before shutdown
      2024-06-17 11:08:52 INFO  [pool-3-thread-2] USDGBP - Stopping
      2024-06-17 11:08:52 INFO  [pool-3-thread-2] USDGBP - Persisting unfilled orders before shutdown
