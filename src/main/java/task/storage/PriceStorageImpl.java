package task.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mysovskih on 20.01.15.
 */
public class PriceStorageImpl implements PriceStorage {
    private long WINDOW = 1000*60;
    private AtomicInteger quotesSize = new AtomicInteger(0);
    private Map<String, Integer> quotes = new HashMap<String, Integer>();
    private Buffer index = new Buffer();

    @Override
    public void updatePrice(String quoteId, double price) {
        if (!quotes.containsKey(quoteId)){
            quotes.put(quoteId, quotesSize.getAndIncrement());
        }
        int quote = quotes.get(quoteId);
        index.push(quote, System.currentTimeMillis(), price);
    }

    @Override
    public double getPrice(String quoteId) {
        if (!quotes.containsKey(quoteId)){
            return -1;
        }
        int quote = quotes.get(quoteId);
        int offset = 0;
        int sizeOfBuffer = index.size.get();
        if (sizeOfBuffer == 0) return 0;
        sizeOfBuffer--;
        for (int i = sizeOfBuffer; i >= 0; i--){
            offset = Buffer.positionToOffset(i);
            if(index.getId(offset) == quote) return index.getValue(offset);
        }
        return -1;
    }

    @Override
    public double getAveragePrice(String quoteId) {
        if (!quotes.containsKey(quoteId)){
            return -1;
        }
        int quote = quotes.get(quoteId);
        double sum = 0;
        int count = 0;
        int offset = 0;
        long lastTime = System.currentTimeMillis() - WINDOW;
        int sizeOfBuffer = index.size.get();
        if (sizeOfBuffer == 0) return 0;
        sizeOfBuffer--;
        for (int i = sizeOfBuffer; i >= 0; i--){
            offset = Buffer.positionToOffset(i);
            if (index.getTime(offset) < lastTime){
                return count > 0 ? sum/count : 0;
            }
            if (index.getId(offset) == quote){
                sum += index.getValue(offset);
                count++;
            }
        }
        return count > 0 ? sum/count : 0;
    }

    @Override
    public double getMaxPrice(String quoteId) {
        if (!quotes.containsKey(quoteId)){
            return -1;
        }
        int quote = quotes.get(quoteId);
        double max = 0;
        int offset = 0;
        long lastTime = System.currentTimeMillis() - WINDOW;
        int sizeOfBuffer = index.size.get();
        if (sizeOfBuffer == 0) return 0;
        sizeOfBuffer--;
        for (int i = sizeOfBuffer; i >= 0; i--){
            offset = Buffer.positionToOffset(i);
            if (index.getTime(offset) < lastTime){
                return max;
            }
            if (index.getId(offset) == quote){
                double val = index.getValue(offset);
                if (val > max) max = val;
            }
        }
        return max;
    }
}
