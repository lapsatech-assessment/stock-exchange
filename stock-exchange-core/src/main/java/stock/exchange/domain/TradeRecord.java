package stock.exchange.domain;

public record TradeRecord(
    long id,
    SecurityRecord instrument,
    TraderRecord seller,
    TraderRecord buyer,
    int quantity,
    double price) {
}
