package stock.exchange.cmd;

import stock.exchange.StockExchangeWorld;
import stock.exchange.common.CommonException;

public class ShellCommandParserImpl implements ShellCommandParser {

  private static final String ADD = "ADD";
  private static final String ERR = "ERR";
  private static final String ASK = "ASK";
  private static final String BID = "BID";
  private static final String BUY = "BUY";
  private static final String CANCEL = "CANCEL";
  private static final String SELL = "SELL";
  private static final String COMPOSITE = "COMPOSITE";
  private static final String SECURITY = "SECURITY";
  private static final String CREATE = "CREATE";
  private static final String TRADER = "TRADER";
  private static final String INSTRUMENT = "INSTRUMENT";
  private static final String DESCRIBE = "DESCRIBE";
  private static final String INSTRUMENTS = "INSTRUMENTS";
  private static final String EMPTY = "<EMPTY>";
  private static final String ORDERS = "ORDERS";
  private static final String LIST = "LIST";
  private static final String SHOW = "SHOW";
  private static final String BYE = "BYE";
  private static final String QUIT = "QUIT";

  private final StockExchangeWorld stockExchangeWorld;

  public ShellCommandParserImpl(StockExchangeWorld stockExchangeWorld) {
    this.stockExchangeWorld = stockExchangeWorld;
  }

  @Override
  public String execute(String line) {
    String[] tokens = line.split("\\s+");
    if (tokens.length == 0) {
      return null;
    }
    try {
      switch (tokens[0].toUpperCase()) {
        case ADD:
        case CREATE: {
          try {
            switch (tokens[1].toUpperCase()) {
              case TRADER: {
                int traderId;
                String name;
                try {
                  traderId = Integer.parseInt(tokens[2]);
                  name = tokens[3];
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                  throw new InvalidInput(CREATE + " " + TRADER + " <id> <traderName>");
                }
                var trader = stockExchangeWorld.createTrader(traderId, name);
                return CREATE + ": " + trader;
              }

              case SECURITY: {
                int securityId;
                String symbol;
                double initialPrice;
                try {
                  securityId = Integer.parseInt(tokens[2]);
                  symbol = tokens[3].toUpperCase();
                  initialPrice = Double.parseDouble(tokens[4]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                  throw new InvalidInput(CREATE + " " + SECURITY + " <id> <symbol> <initialPrice>");
                }
                var security = stockExchangeWorld.createSecurity(securityId, symbol, initialPrice);
                return CREATE + ": " + security;
              }

              case COMPOSITE: {
                int instrumentId;
                String symbol;
                String[] compositeSymbols;
                try {
                  instrumentId = Integer.parseInt(tokens[2]);
                  symbol = tokens[3].toUpperCase();
                  compositeSymbols = new String[tokens.length - 4];
                  for (int i = 4; i < tokens.length; i++) {
                    compositeSymbols[i - 4] = tokens[i].toUpperCase();
                  }

                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                  throw new InvalidInput(CREATE + " " + COMPOSITE + " <id> <symbol> <securitySymbol1...> <securitySymbolN>");
                }
                var instrument = stockExchangeWorld.createComposite(instrumentId, symbol, compositeSymbols);
                return CREATE + ": " + instrument;
              }

            }
          } catch (ArrayIndexOutOfBoundsException e) {
          }
          return CREATE + " " + TRADER + "|" + SECURITY + "|" + COMPOSITE;

        }

        case SHOW:
        case DESCRIBE: {
          try {
            switch (tokens[1].toUpperCase()) {
              case INSTRUMENT: {
                String symbol;
                try {
                  symbol = tokens[2].toUpperCase();
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                  throw new InvalidInput(DESCRIBE + " " + INSTRUMENT + " symbol");
                }
                var instrument = stockExchangeWorld.getInstrument(symbol);
                return DESCRIBE + ": " + instrument;
              }

              case TRADER: {
                int traderId;
                try {
                  traderId = Integer.parseInt(tokens[2]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                  throw new InvalidInput(DESCRIBE + " " + TRADER + " <traderId>");
                }
                var instrument = stockExchangeWorld.getTrader(traderId);
                return DESCRIBE + ": " + instrument;
              }
            }
          } catch (ArrayIndexOutOfBoundsException e) {
          }
          return DESCRIBE + " " + INSTRUMENT + "|" + TRADER;

        }

        case LIST: {
          try {
            switch (tokens[1].toUpperCase()) {
              case ORDERS: {
                String symbol;
                try {
                  symbol = tokens[2].toUpperCase();
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                  throw new InvalidInput(LIST + " " + ORDERS + " <securitySymbol>");
                }
                var orders = stockExchangeWorld.listOrders(symbol);
                StringBuilder sb = new StringBuilder();
                sb.append(LIST + ": ");
                boolean empty = true;
                for (var oderer : orders) {
                  sb.append(System.lineSeparator());
                  sb.append(oderer);
                  empty = false;
                }
                if (empty) {
                  sb.append(EMPTY);
                }
                return sb.toString();
              }

              case INSTRUMENTS: {
                var instruments = stockExchangeWorld.listInstruments();
                StringBuilder sb = new StringBuilder();
                sb.append(LIST + ": ");
                boolean empty = true;
                for (var instrument : instruments) {
                  sb.append(System.lineSeparator());
                  sb.append(instrument);
                  empty = false;
                }
                if (empty) {
                  sb.append(EMPTY);
                }
                return sb.toString();
              }

            }
          } catch (ArrayIndexOutOfBoundsException e) {
          }
          return LIST + " " + ORDERS + "|" + INSTRUMENTS;
        }

        case SELL: {
          int traderId;
          String symbol;
          int quantity;
          try {
            traderId = Integer.parseInt(tokens[1]);
            symbol = tokens[2].toUpperCase();
            quantity = Integer.parseInt(tokens[3]);
          } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new InvalidInput(SELL + " <traderId> <symbol> <quantity>");
          }
          var order = stockExchangeWorld.sell(traderId, symbol, quantity);
          return SELL + ": " + order;
        }

        case CANCEL: {
          String symbol;
          long orderId;
          try {
            symbol = tokens[1].toUpperCase();
            orderId = Long.parseLong(tokens[2]);
          } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new InvalidInput(CANCEL + " <symbol> <orderId>");
          }
          var order = stockExchangeWorld.cancelOrder(symbol, orderId);
          return CANCEL + ": " + order;
        }

        case BUY: {
          int traderId;
          String symbol;
          int quantity;
          try {
            traderId = Integer.parseInt(tokens[1]);
            symbol = tokens[2].toUpperCase();
            quantity = Integer.parseInt(tokens[3]);
          } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new InvalidInput(BUY + " <traderId> <symbol> <quantity>");
          }
          var order = stockExchangeWorld.buy(traderId, symbol, quantity);
          return BUY + ": " + order;
        }

        case BID: {
          int traderId;
          String symbol;
          int quantity;
          double price;
          try {
            traderId = Integer.parseInt(tokens[1]);
            symbol = tokens[2].toUpperCase();
            quantity = Integer.parseInt(tokens[3]);
            price = Double.parseDouble(tokens[4]);
          } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new InvalidInput(BID + " <traderId> <symbol> <quantity> <price>");
          }
          var order = stockExchangeWorld.bid(traderId, symbol, quantity, price);
          return BID + ": " + order;
        }

        case ASK: {
          int traderId;
          String symbol;
          int quantity;
          double price;
          try {
            traderId = Integer.parseInt(tokens[1]);
            symbol = tokens[2].toUpperCase();
            quantity = Integer.parseInt(tokens[3]);
            price = Double.parseDouble(tokens[4]);
          } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new InvalidInput(ASK + " <traderId> <symbol> <quantity> <price>");
          }
          var order = stockExchangeWorld.ask(traderId, symbol, quantity, price);
          return ASK + ": " + order;
        }

        case QUIT:
        case BYE: {
          return BYE_STRING;
        }
      }

    } catch (CommonException e) {
      if (e.getMessage() != null) {
        return ERR + ": " + e.getClass().getSimpleName() + " " + e.getMessage();
      } else {
        return ERR + ": " + e.getClass().getSimpleName();
      }
    }
    return "Unrecoginized input";
  }

  @SuppressWarnings("serial")
  private static class InvalidInput extends CommonException {

    public InvalidInput(String string) {
      super(string);
    }

  }

}
