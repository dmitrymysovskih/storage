package task.storage;

/**
 * Created by Mysovskih on 20.01.15.
 */
public interface PriceStorage {

    // Update the latest quote price
    public void updatePrice(String quoteId, double price);

    // Get the latest price for the quote
    public double getPrice(String quoteId);

    // Get the average price for the quote for the last 1 minute
    public double getAveragePrice(String quoteId);

    // Get them maximum price for the quote for the past 1 minute
    public double getMaxPrice(String quoteId);

}
