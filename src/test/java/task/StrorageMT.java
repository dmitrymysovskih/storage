package task;

import org.junit.Before;
import org.junit.Test;
import task.storage.PriceStorage;
import task.storage.PriceStorageImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Mysovskih on 20.01.15.
 */
public class StrorageMT {
   @Test
   public void testAllInFixedPool(){
       ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
       executeAll(executorService);
   }

    @Test
    public void testAllInCachedPool(){
        ExecutorService executorService = Executors.newCachedThreadPool();
        executeAll(executorService);
    }

    private static void executeAll(ExecutorService executorService){
        int numberOfRunnable = 2000;
        int window = 1000*60;
        PriceStorageImpl storage = new PriceStorageImpl(window, numberOfRunnable);
        for (int i = 0; i < numberOfRunnable; i++){
            executorService.execute(getRandomRunnable(i, storage));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private static Runnable getRandomRunnable(final int i, final PriceStorageImpl storage){
        final String quoteId = String.valueOf(i % 5);
        final double value = Math.random();
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long) (10*Math.random()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                if (i % 2 == 0){
                    System.out.println(String.format("put %f in %s", value, quoteId));
                    storage.updatePrice(quoteId, value);
                } else {
                    if (value < 0.3){
                        double val = storage.getAveragePrice(quoteId);
                        System.out.println(String.format("avg in quote %s is %f", quoteId, val));
                    } else if (value >= 0.3 && value < 0.6){
                        double val = storage.getMaxPrice(quoteId);
                        System.out.println(String.format("max in quote %s is %f", quoteId, val));
                    } else {
                        double val = storage.getPrice(quoteId);
                        System.out.println(String.format("last in quote %s is %f", quoteId, val));
                    }
                }
            }
        };
    }

    @Test
    public void testOperationsOneThread(){
        int numberOfRunnable = 2000;
        int window = 1000*60;
        PriceStorageImpl storage = new PriceStorageImpl(window, numberOfRunnable);
        double max = 0;
        double sum = 0;
        double previous = -1;
        String quote = "q1";
        for (int i = 0; i < 200; i++){
            if (previous != -1)
                assertTrue(previous == storage.getPrice(quote));
            double value = new BigDecimal(Math.random()).setScale(3, RoundingMode.HALF_UP).doubleValue();
            storage.updatePrice(quote, value);
            if (value > max) max = value;
            sum += value;
            previous = value;
        }
        assertTrue(max == storage.getMaxPrice(quote));
        double avg = new BigDecimal(sum/200).setScale(3, RoundingMode.HALF_UP).doubleValue();;
        double avgBuf = new BigDecimal(storage.getAveragePrice(quote)).setScale(3, RoundingMode.HALF_UP).doubleValue();
        assertTrue(avg == avgBuf);
    }
}
