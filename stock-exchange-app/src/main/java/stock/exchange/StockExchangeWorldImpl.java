package stock.exchange;

import java.time.Duration;

import stock.exchange.book.OrderBook;
import stock.exchange.book.OrderBookManager;
import stock.exchange.book.OrderBookManagerImpl;
import stock.exchange.book.StockMatcherImpl;
import stock.exchange.domain.CompositeRecord;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TraderRecord;
import stock.exchange.instrument.InstrumentManager;
import stock.exchange.instrument.MarketDataWorld;
import stock.exchange.instrument.MarketDataWrites;
import stock.exchange.trade.TradeManager;
import stock.exchange.trade.TradeManagerImpl;
import stock.exchange.trader.TraderManager;
import stock.exchange.trader.TraderManagerImpl;

public class StockExchangeWorldImpl implements StockExchangeWorld {

  private final MarketDataWrites marketDataWrites;
  private final InstrumentManager instrumentManager;
  private final TradeManager tradeManager;
  private final TraderManager traderManager;
  private final StockMarketRunner stockMarketRunner;
  private final OrderBookManager orderBookManager;

  public StockExchangeWorldImpl() {
    MarketDataWorld mds = new MarketDataWorld();
    this.marketDataWrites = mds;
    this.instrumentManager = mds;
    this.tradeManager = new TradeManagerImpl(marketDataWrites);
    this.traderManager = new TraderManagerImpl();
    this.orderBookManager = new OrderBookManagerImpl(
        StockMatcherImpl::new,
        new OrderFulfilledLoggerDownstream(),
        new TradeHappenLoggerDownstream());
    this.stockMarketRunner = new StockMarketRunnerImpl();
  }

  @Override
  public CompositeRecord createComposite(int instrumentId, String symbol, String[] componentSymbols) {
    return instrumentManager.createComposite(instrumentId, symbol, componentSymbols);
  }

  @Override
  public SecurityRecord createSecurity(int instrumentId, String symbol, double initialPrice) {
    var instrument = instrumentManager.createSecurity(instrumentId, symbol, initialPrice);
    OrderBook book = orderBookManager.addOrderBook(instrument, tradeManager);
    stockMarketRunner.run(book, Duration.ofMillis(1000));
    return instrument;
  }

  @Override
  public InstrumentRecord getInstrument(String symbol) {
    return instrumentManager.findInstrumentBySymbol(symbol);
  }

  @Override
  public Iterable<InstrumentRecord> listInstruments() {
    return instrumentManager.getInstruments();
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
  public Iterable<OrderRecord> listOrders(String symbol) {
    var instrument = instrumentManager.findInstrumentBySymbol(symbol);
    var book = orderBookManager.findBookByInstrument(instrument);
    return book.getOrders();
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

  @Override
  public void shutdown() {
    stockMarketRunner.shutdown();
  }
}
