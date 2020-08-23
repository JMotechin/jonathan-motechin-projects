package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable> extends MinHeap<E> {

    public MinHeapImpl(){
        //fix this
        elements = (E[]) new Comparable[5];
        elementsToArrayIndex = new HashMap<>();
    }

    public void reHeapify(E element) {
        //Do the overriden methods get called since upheap is in the superclass?
        int k = getArrayIndex(element);
        if(k / 2 > 0 && isGreater(k / 2, k)){
            upHeap(k);
        }
        if(2 * k < this.count && (isGreater(k, 2 * k) || isGreater(k, 2 * k + 1))){
            downHeap(k);
        }
    }

    protected int getArrayIndex(E element) {
        if(elementsToArrayIndex.get(element) == null){
            return -1;
        }
        return elementsToArrayIndex.get(element);
    }

    protected void doubleArraySize() {
        elements = Arrays.copyOf(elements, elements.length * 2);
    }

    protected E[] getElementsArray(){
        return this.elements;
    }

    protected  boolean isEmpty()
    {
        return this.count == 0;
    }

    @Override
    protected  void swap(int i, int j) {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
        elementsToArrayIndex.put(this.elements[i], i);
        elementsToArrayIndex.put(this.elements[j], j);
    }

    @Override
    public void insert(E x) {
        if (this.count >= this.elements.length - 1)
        {
            this.doubleArraySize();
        }
        this.elements[++this.count] = x;
        elementsToArrayIndex.put(x, this.count);
        this.upHeap(this.count);
    }

    @Override
    public E removeMin() {
        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        this.swap(1, this.count--);
        this.downHeap(1);
        elementsToArrayIndex.put(this.elements[this.count + 1], null);
        this.elements[this.count + 1] = null;
        return min;
    }
}