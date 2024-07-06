package stock.exchange;

import java.time.Duration;

import stock.exchange.book.OrderBook;
import stock.exchange.book.OrderBookManager;
import stock.exchange.domain.CompositeRecord;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TraderRecord;
import stock.exchange.engine.OrderBookRunner;
import stock.exchange.instrument.InstrumentManager;
import stock.exchange.trader.TraderManager;

public class StockExchangeFacadeImpl implements StockExchangeFacade {

  private final InstrumentManager instrumentManager;
  private final TraderManager traderManager;
  private final OrderBookRunner orderBookRunner;
  private final OrderBookManager orderBookManager;

  public StockExchangeFacadeImpl(
      InstrumentManager instrumentManager,
      TraderManager traderManager,
      OrderBookRunner orderBookRunner,
      OrderBookManager orderBookManager) {
    this.instrumentManager = instrumentManager;
    this.traderManager = traderManager;
    this.orderBookRunner = orderBookRunner;
    this.orderBookManager = orderBookManager;
  }

  @Override
  public CompositeRecord createComposite(int instrumentId, String symbol, String[] componentSymbols) {
    return instrumentManager.createComposite(instrumentId, symbol, componentSymbols);
  }

  @Override
  public SecurityRecord createSecurity(int instrumentId, String symbol, double initialPrice) {
    var instrument = instrumentManager.createSecurity(instrumentId, symbol, initialPrice);
    OrderBook book = orderBookManager.createOrderBook(instrument);
    orderBookRunner.runOrderBook(book, Duration.ofMillis(1000));
    return instrument;
  }

  @Override
  public InstrumentRecord getInstrument(String symbol) {
    return instrumentManager.findInstrumentBySymbol(symbol);
  }

  @Override
  public Iterable<? extends InstrumentRecord> listInstruments() {
    return instrumentManager.getAllInstruments();
  }

  @Override
  public TraderRecord getTrader(int traderId) {
    return traderManager.findTraderById(traderId);
  }

  @Override
  public TraderRecord createTrader(int traderId, String name) {
    return traderManager.createTrader(traderId, name);
  }

  @Override
  public Iterable<? extends OrderRecord> listOrders(String symbol) {
    var instrument = instrumentManager.findInstrumentBySymbol(symbol);
    var book = orderBookManager.findBookByInstrument(instrument);
    return book.getActiveOrders();
  }

  @Override
  public OrderRecord cancelOrder(String symbol, long orderId) {
    var instrument = instrumentManager.findInstrumentBySymbol(symbol);
    var book = orderBookManager.findBookByInstrument(instrument);
    return book.removeOrder(orderId);
  }

  @Override
  public OrderRecord sell(int traderId, String symbol, int quantity) {
    var trader = traderManager.findTraderById(traderId);
    var instrument = instrumentManager.findInstrumentBySymbol(symbol);
    var book = orderBookManager.findBookByInstrument(instrument);
    return book.addSell(trader, quantity);
  }

  @Override
  public OrderRecord buy(int traderId, String symbol, int quantity) {
    var trader = traderManager.findTraderById(traderId);
    var instrument = instrumentManager.findInstrumentBySymbol(symbol);
    var book = orderBookManager.findBookByInstrument(instrument);
    return book.addBuy(trader, quantity);
  }

  @Override
  public OrderRecord bid(int traderId, String symbol, int quantity, double price) {
    var trader = traderManager.findTraderById(traderId);
    var instrument = instrumentManager.findInstrumentBySymbol(symbol);
    var book = orderBookManager.findBookByInstrument(instrument);
    return book.addBid(trader, quantity, price);
  }

  @Override
  public OrderRecord ask(int traderId, String symbol, int quantity, double price) {
    var trader = traderManager.findTraderById(traderId);
    var instrument = instrumentManager.findInstrumentBySymbol(symbol);
    var book = orderBookManager.findBookByInstrument(instrument);
    return book.addAsk(trader, quantity, price);
  }
}
