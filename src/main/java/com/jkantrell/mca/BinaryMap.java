package com.jkantrell.mca;

import java.util.Arrays;

public class BinaryMap {

    //FIELDS
    private final long[] data_;
    private final long maxEntryValue_;
    private final int size_, bitsPerEntry_, entriesPerLong_;


    //CONSTRUCTORS
    public BinaryMap(int bitsPerEntry, int size) {
        this(bitsPerEntry, size, null);
    }

    public BinaryMap(int bitsPerEntry, int size,long[] data) {
        if(bitsPerEntry < 1 || bitsPerEntry > 64) {
            throw new IllegalArgumentException("BitsPerEntry cannot be outside of accepted range.");
        }

        this.size_ = size;
        this.bitsPerEntry_ = bitsPerEntry;
        this.entriesPerLong_ = 64 / bitsPerEntry;
        this.maxEntryValue_ = (1L << this.bitsPerEntry_) - 1;

        int requiredLongs = (int) Math.ceil((double) this.size_ / this.entriesPerLong_);
        if (data == null) {
            this.data_ = new long[requiredLongs];
            Arrays.fill(this.data_, 0L);
            return;
        }
        if (data.length < requiredLongs) {
            this.data_ = Arrays.copyOf(data, requiredLongs);
            return;
        }
        this.data_ = data;
    }


    //GETTERS
    public long[] getData() {
        return this.data_;
    }
    public int getBitsPerEntry() {
        return this.bitsPerEntry_;
    }
    public int getSize() {
        return this.size_;
    }
    public int get(int index) {
        if(index < 0 || index > this.size_ - 1) {
            throw new IndexOutOfBoundsException();
        }

        int longIndex = index / this.entriesPerLong_;
        int entryIndex = index % this.entriesPerLong_;
        return this.getFromLong(entryIndex, longIndex);
    }
    public int[] indexesOf(int val) {
        int[] r = new int[this.size_];
        int i = 0;
        for (int l = 0; l < this.data_.length; l++) {
            if (this.data_[l] < val) { continue; }
            for (int e = 0; e < this.entriesPerLong_; e++) {
                int v = this.getFromLong(e, l);
                if (v == val) {
                    r[i] = (this.entriesPerLong_ * l) + e;
                    i++;
                }
            }
        }
        r = Arrays.copyOf(r,i);
        return r;
    }


    //SETTERS
    public void set(int index, int value) {
        if(index < 0 || index > this.size_ - 1) {
            throw new IndexOutOfBoundsException();
        }

        if(value < 0 || value > this.maxEntryValue_) {
            throw new IllegalArgumentException("Value cannot be outside of accepted range.");
        }

        int longIndex = index / this.entriesPerLong_;
        int entryIndex = index % this.entriesPerLong_;
        int bitIndex = entryIndex * this.bitsPerEntry_;
        this.data_[longIndex] = this.data_[longIndex] & ~(this.maxEntryValue_ << bitIndex) | ((long) value & this.maxEntryValue_) << bitIndex;
    }


    //UTIL
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof BinaryMap && Arrays.equals(this.data_, ((BinaryMap) o).data_) && this.bitsPerEntry_ == ((BinaryMap) o).bitsPerEntry_ && this.size_ == ((BinaryMap) o).size_ && this.maxEntryValue_ == ((BinaryMap) o).maxEntryValue_);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.data_);
        result = 31 * result + this.bitsPerEntry_;
        result = 31 * result + this.size_;
        result = 31 * result + (int) this.maxEntryValue_;
        return result;
    }


    //PRIVATE UTIL
    private int getFromLong(int entryIndex, int longIndex) {
        int bitIndex = entryIndex * this.bitsPerEntry_;
        return (int) (this.data_[longIndex] >>> bitIndex & this.maxEntryValue_);
    }
    private static int roundToNearest(int value, int roundTo) {
        if(roundTo == 0) {
            return 0;
        } else if(value == 0) {
            return roundTo;
        } else {
            if(value < 0) {
                roundTo *= -1;
            }

            int remainder = value % roundTo;
            return remainder != 0 ? value + roundTo - remainder : value;
        }
    }

}
