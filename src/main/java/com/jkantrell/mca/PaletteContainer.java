package com.jkantrell.mca;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;

public class PaletteContainer<T> implements List<T> {

    //STATIC
    private static IllegalStateException cannotAddException() {
        return new IllegalStateException("Cannot add elements to a PaletteContainer.");
    }
    private static IllegalStateException cannotRemoveException() {
        return new IllegalStateException("Cannot remove elements to a PaletteContainer.");
    }


    //FIELDS
    private final List<T> palette_;
    private final int minimumBitSize_;
    private BinaryMap binaryMap_;
    private int size_;


    //CONSTRUCTORS
    public PaletteContainer(List<T> palette, int size, int minimumBitSize, long[] data) {
        //Checking list is not empty
        if (palette.isEmpty()) {
            throw new IllegalArgumentException("Palette cannot be empty");
        }
        this.palette_ = new ArrayList<>(palette);

        //Setting size
        this.minimumBitSize_ = minimumBitSize;
        this.size_ = size;

        //If single element
        if (palette.size() < 2) {
            this.binaryMap_ = null;
            return;
        }

        //Generating binary map
        int bitsPerEntry = this.minimumBitsFor(palette.size() - 1);
        this.binaryMap_ = new BinaryMap(bitsPerEntry, this.size_, data);
    }
    public PaletteContainer(List<T> palette, int size, long[] data) {
        this(palette, size, 1, data);
    }
    public PaletteContainer(List<T> palette, int size, int minimumBitSize) {
        this(palette, size, minimumBitSize, null);
    }
    public PaletteContainer(List<T> palette, int size) {
        this(palette, size, 1);
    }
    public PaletteContainer(T singleElement, int size, int minimumBitSize) {
        this(new ArrayList<>(List.of(singleElement)), size, minimumBitSize);
    }
    public PaletteContainer(T singleElement, int size) {
        this(singleElement, size, 1);
    }


    //GETTER
    public List<T> getPalette() {
        return List.copyOf(this.palette_);
    }
    public long[] getByteMap() {
        if (this.binaryMap_ == null) { return null; }
        return this.binaryMap_.getData();
    }


    //LIST OVERWRITES
    @Override
    public int size() {
        return this.size_;
    }

    @Override
    public boolean isEmpty() {
        return this.palette_.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.palette_.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new PaletteIterator();
    }

    @Override
    public Object[] toArray() {
        return IntStream.range(0, this.size_).boxed().map(this::get).toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A[] toArray(A[] a) {
        A[] r = (a.length >= this.size_)
                ? a
                : (A[]) Array.newInstance(a.getClass().getComponentType(), this.size_);
        for (int i = 0; i < this.size(); i++) {
            r[i] = (A) this.get(i);
        }
        return r;
    }

    @Override
    public boolean add(T t) {
        int index = this.intoPalette(t);
        this.size_++;
        this.calculateBinaryMap();
        if (this.binaryMap_ != null) {
            this.binaryMap_.set(this.size_ - 1, index);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        if (index < 0) { return false; }
        while (index < this.size_ - 1) {
            this.binaryMap_.set(index, this.binaryMap_.get(index + 1));
            index++;
        }
        if (this.indexOf(o) < 0) { this.palette_.remove(o); }
        this.size_--;
        this.calculateBinaryMap();
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.palette_.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw cannotRemoveException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw cannotRemoveException();
    }

    @Override
    public void clear() {
        throw cannotRemoveException();
    }

    @Override
    public T get(int index) {
        if (this.binaryMap_ != null) {
            return this.palette_.get(this.binaryMap_.get(index));
        }
        if (index < 0 || index > this.size_ - 1) {
            throw new IndexOutOfBoundsException();
        }
        return this.palette_.get(0);
    }

    @Override
    public T set(int index, T element) {
        if (index < 0 || index > this.size_ - 1) {
            throw new IndexOutOfBoundsException();
        }
        T old = this.get(index);
        int i = this.intoPalette(element);
        this.calculateBinaryMap();
        if (this.binaryMap_ != null) {
            this.binaryMap_.set(index, i);
        }
        return old;
    }

    @Override
    public void add(int index, T element) {
        throw cannotAddException();
    }

    @Override
    public T remove(int index) {
        throw cannotRemoveException();
    }

    @Override
    public int indexOf(Object o) {
        if (!this.palette_.contains(o)) { return -1; }
        int index = this.palette_.indexOf(o);
        for (int i = 0; i < this.size_; i++) {
            if (this.binaryMap_.get(i) == index) { return i; }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }


    //PRIVATE UTIL
    private int intoPalette(T value) {
        int index = this.palette_.indexOf(value);
        if (index < 0) {
            index = this.palette_.size();
            this.palette_.add(value);
        }
        return index;
    }
    private void calculateBinaryMap() {
        if (this.palette_.size() < 2) { this.binaryMap_ = null; }

        int bitsPerEntry = this.minimumBitsFor(this.palette_.size());
        if (bitsPerEntry == this.binaryMap_.getBitsPerEntry() && this.size_ == this.binaryMap_.getSize()) {
            return;
        }

        BinaryMap newMap = new BinaryMap(bitsPerEntry, this.size_);
        if (this.binaryMap_ != null) {
            for (int i = 0; i < this.size_; i++) { newMap.set(i, this.binaryMap_.get(i)); }
        }

        this.binaryMap_ = newMap;
        this.size_ = newMap.getSize();
    }
    private int minimumBitsFor(int integer) {
        return Math.max(Integer.SIZE - Integer.numberOfLeadingZeros(integer), this.minimumBitSize_);
    }


    //CLASSES
    private class PaletteIterator implements Iterator<T> {

        private int current = 0;

        @Override
        public boolean hasNext() {
            return this.current < PaletteContainer.this.size();
        }

        @Override
        public T next() {
            T next = PaletteContainer.this.get(this.current);
            this.current++;
            return next;
        }
    }
}
