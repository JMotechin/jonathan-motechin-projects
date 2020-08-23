package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.IOException;
import java.util.Arrays;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    private static final int MAX = 4;
    private Node root;
    private int height;
    private int n;
    private PersistenceManager<Key, Value> persistenceManager;

    private static final class Node{
        private int entryCount;
        private Entry[] entries = new Entry[BTreeImpl.MAX];
        private Node next;
        private Node previous;

        private Node(int k){
            this.entryCount = k;
        }

        private void setNext(Node next){
            this.next = next;
        }
        private Node getNext(){
            return this.next;
        }
        private void setPrevious(Node previous){
            this.previous = previous;
        }
        private Node getPrevious(){
            return this.previous;
        }

        private Entry[] getEntries(){
            return Arrays.copyOf(this.entries, this.entryCount);
        }

    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
    private static class Entry{
        private Comparable<?> key;
        private Object val;
        private Node child;

        protected Entry(Comparable<?> key, Object val, Node child){
            this.key = key;
            this.val = val;
            this.child = child;
        }
        private Object getValue(){
            return this.val;
        }
        private Comparable<?> getKey(){
            return this.key;
        }
    }
    public  BTreeImpl(){
        this.root = new Node(0);
    }

    @Override
    public Value get(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry entry = this.get(this.root, key, this.height);
        if(entry != null && entry.val != null) {
            return (Value) entry.val;
        }
        try{
            if(persistenceManager != null){
                Value value = persistenceManager.deserialize(key);
                put(key, value);
                return value;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Entry get(Node currentNode, Key key, int height)
    {
        Entry[] entries = currentNode.entries;
        if(height == 0){
            for (int j = 0; j < currentNode.entryCount; j++) {
                if(isEqual(key, entries[j].key)) {
                    return entries[j];
                }
            }
            return null;
        }
        else{
            for (int j = 0; j < currentNode.entryCount; j++) {
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
                {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            return null;
        }
    }

    @Override
    public Value put(Key key, Value val) {
        if (key == null){
            throw new IllegalArgumentException("argument key to put() is null");
        }
        Entry alreadyThere = this.get(this.root, key, this.height);
        if(alreadyThere != null) {
            Value oldVal = (Value) alreadyThere.val;
            alreadyThere.val = val;
            return oldVal;
        }
        Node newNode = this.put(this.root, key, val, this.height);
        this.n++;
        if (newNode == null){
            return null;
        }
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        this.height++;
        return null;
    }

    private Node put(Node currentNode, Key key, Value val, int height) {
        int j;
        Entry newEntry = new Entry(key, val, null);
        if (height == 0) {
            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++) {
                if (less(key, currentNode.entries[j].key)) {
                    break;
                }
            }
        }
        else {
            for (j = 0; j < currentNode.entryCount; j++) {
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeImpl.MAX) {
            return null;
        }
        else {
            return this.split(currentNode, height);
        }
    }

    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        currentNode.entryCount = BTreeImpl.MAX / 2;
        for (int j = 0; j < BTreeImpl.MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        if (height == 0){
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    @Override
    public void moveToDisk(Key key) throws Exception {
        Value value = get(key);
        persistenceManager.serialize(key, value);
        put(key, null);
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.persistenceManager = pm;
    }
    private boolean less(Comparable k1, Comparable k2){
        return k1.compareTo(k2) < 0;
    }

    private boolean isEqual(Comparable k1, Comparable k2){
        return k1.compareTo(k2) == 0;
    }
}
