package stock.exchange.instrument;

public interface MarketDataWrites {

  void acceptLastTradePrice(int securityId, double price, int quantity);
}