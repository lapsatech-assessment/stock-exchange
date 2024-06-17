package stock.exchange.domain;

public record OrderRecord(long id, SecurityRecord instrument, OrderType type, TraderRecord trader, int quantity, double price) {

}