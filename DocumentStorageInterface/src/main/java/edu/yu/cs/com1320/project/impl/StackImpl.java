package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    Entry head = null;

    public StackImpl(){}

    public void push(T element) {
        if(element == null){
            throw new IllegalArgumentException();
        }
        Entry entry = new Entry(element);
        if(size() == 0){
            head = entry;
            entry.next = null;
        }else{
            entry.next = head;
            head = entry;
        }
    }

    public T pop() {
        if(size() == 0){
            return null;
        }
        T element = head.element;
        head = head.next;
        return element;
    }

    public T peek() {
        if(size() == 0){
            return null;
        }
        return head.element;
    }

    public int size() {
        Entry localHead = head;
        if(localHead == null){
            return 0;
        }
        int size = 1;
        while(localHead.next != null){
            size++;
            localHead = localHead.next;
        }
        return size;
    }

    private class Entry{
        T element;
        Entry next;
        private Entry(T element){
            this.element = element;
        }
    }
}