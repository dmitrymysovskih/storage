package task.storage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mysovskih on 20.01.15.
 */

public class Buffer {
    private java.nio.ByteBuffer buffer;
    public AtomicInteger size;

    private final static int ID_OFFSET = 0;
    private final static int ID_SIZE = Integer.SIZE;
    private final static int TIME_OFFSET = ID_OFFSET + ID_SIZE;
    private final static int TIME_SIZE = Long.SIZE;
    private final static int VALUE_OFFSET = TIME_OFFSET + TIME_SIZE;
    private final static int VALUE_SIZE = Double.SIZE;
    private final static int ITEM_SIZE = ID_SIZE + TIME_SIZE + VALUE_SIZE;

    private final static int CAPACITY = ITEM_SIZE * 300000;

    public Buffer(){
        buffer = java.nio.ByteBuffer.allocate(CAPACITY);
        size = new AtomicInteger(0);
        //size = 0;
    }

    public boolean push(int id, long time, double value) {
        int offset = offsetTransform(positionToOffset(size.getAndIncrement()));
        buffer.putInt(offset + ID_OFFSET, id);
        buffer.putLong(offset + TIME_OFFSET, time);
        buffer.putDouble(offset + VALUE_OFFSET, value);

        return true;
    }

    public void clear() {
        buffer.clear();
        size = new AtomicInteger(0);
    }

    public static int positionToOffset(int position){
        return position*ITEM_SIZE;
    }

    public int getId(int offset){
        return buffer.getInt(offsetTransform(offset + ID_OFFSET));
    }

    public long getTime(int offset){
        return buffer.getLong(offsetTransform(offset + TIME_OFFSET));
    }

    public double getValue(int offset){
        return buffer.getDouble(offsetTransform(offset + VALUE_OFFSET));
    }

    public int offsetTransform(int offset){
        if (offset + ITEM_SIZE < CAPACITY){
            return offset;
        } else {
            return offset % CAPACITY;
        }
    }
}
