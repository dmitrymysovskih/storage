package task.storage;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mysovskih on 20.01.15.
 */

public class Buffer {
    private java.nio.ByteBuffer buffer;

    private AtomicInteger size;

    private final static int ID_OFFSET = 0;
    private final static int ID_SIZE = Integer.SIZE;
    private final static int TIME_OFFSET = ID_OFFSET + ID_SIZE;
    private final static int TIME_SIZE = Long.SIZE;
    private final static int VALUE_OFFSET = TIME_OFFSET + TIME_SIZE;
    private final static int VALUE_SIZE = Double.SIZE;

    public final static int  ITEM_SIZE = ID_SIZE + TIME_SIZE + VALUE_SIZE;

    private int capacity;

    public Buffer(int itemsLength){
        capacity = itemsLength * ITEM_SIZE;
        buffer = java.nio.ByteBuffer.allocate(capacity);
        size = new AtomicInteger(0);
    }

    public boolean push(int id, long time, double value) {
        int offset = getCurrentOffset(size.getAndIncrement());
        buffer.putInt(offset + ID_OFFSET, id);
        buffer.putLong(offset + TIME_OFFSET, time);
        buffer.putDouble(offset + VALUE_OFFSET, value);

        return true;
    }

    public void clear() {
        buffer.clear();
        size = new AtomicInteger(0);
    }

    private int offsetTransform(int offset){
        if (offset + ITEM_SIZE < capacity){
            return offset;
        } else {
            return offset % capacity;
        }
    }

    public int getCurrentOffset(){
        return getCurrentOffset(size.get() - 1);
    }

    private int getCurrentOffset(int position){
        return offsetTransform(positionToOffset(position));
    }

    public static int positionToOffset(int position){
        return position*ITEM_SIZE;
    }

    public int getId(int offset){
        return buffer.getInt(offset + ID_OFFSET);
    }

    public long getTime(int offset){
        return buffer.getLong(offset + TIME_OFFSET);
    }

    public double getValue(int offset){
        return buffer.getDouble(offset + VALUE_OFFSET);
    }
}
