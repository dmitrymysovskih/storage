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
   public void TestAll(){
       PriceStorageImpl storage = new PriceStorageImpl();
       ExecutorService executorService = Executors.newFixedThreadPool(4);
       for (int i = 0; i < 2000; i++){
            executorService.execute(getRunnableWriter(i, storage));
       }
       executorService.shutdown();
       try {
           executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
   }

    private static Runnable getRunnableWriter(final int i, final PriceStorageImpl storage){

        final String quoteId = String.valueOf(i % 5);
        if (i % 2 == 0){
            final double value = Math.random();
            return new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep((long) (10*Math.random()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(String.format("put %f in %s", value, quoteId));
                    storage.updatePrice(quoteId, value);
                }
            };
        } else {
            final double value = Math.random();
            if (value < 0.3){
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep((long) (10*Math.random()));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        double val = storage.getAveragePrice(quoteId);
                        System.out.println(String.format("avg in quote %s is %f", quoteId, val));
                    }
                };
            }
            if (value >= 0.3 && value < 0.6){
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep((long) (10*Math.random()));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        double val = storage.getMaxPrice(quoteId);
                        System.out.println(String.format("max in quote %s is %f", quoteId, val));
                    }
                };
            } else return new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep((long) (10*Math.random()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    double val = storage.getPrice(quoteId);
                    System.out.println(String.format("last in quote %s is %f", quoteId, val));
                }
            };

        }
    }
    @Test
    public void test(){
        PriceStorageImpl storage = new PriceStorageImpl();
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
