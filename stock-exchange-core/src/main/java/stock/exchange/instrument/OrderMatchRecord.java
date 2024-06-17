package stock.exchange.instrument;

public record OrderMatchRecord(long buyersOrderId, long sellersOrderId, int quantity, double buyerPrice,
    double sellerPrice) {

}
