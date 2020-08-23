package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 91;
    private Node root;

    public TrieImpl(){}

    private class Node{
        private ArrayList<Value> val = new ArrayList<>();
        private Node[] links = new TrieImpl.Node[TrieImpl.alphabetSize];
    }

    //make sure the key is all uppercase and alphanumeric
    public void put(String key, Value val) {
        if (val != null || key != null){
            key = key.toUpperCase().replaceAll("[^A-Z0-9]", "");
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node put(Node x, String key, Value val, int d)
    {
        if (x == null)
        {
            x = new Node();
        }
        if (d == key.length())
        {
            if(!x.val.contains(val)) {
                x.val.add(val);
            }
            return x;
        }
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        ArrayList<Value> completeList = new ArrayList<>();
        if(key == null || comparator == null){
            return  completeList;
        }
        key = key.toUpperCase().replaceAll("[^A-Z0-9]", "");
        Node x = this.get(this.root, key, 0);
        if(x == null){
            return completeList;
        }
        completeList = x.val;
        if(!completeList.isEmpty()){
            completeList.sort(comparator);
        }
        return completeList;
    }

    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        ArrayList<Value> completeList = new ArrayList<>();
        if(prefix == null || comparator == null){
            return  completeList;
        }
        prefix = prefix.toUpperCase().replaceAll("[^A-Z0-9]", "");
        Node x = this.get(this.root, prefix, 0);
        if(x == null){
            return completeList;
        }
        ArrayList<Value> tempList = new ArrayList<>(getValueOfAllChildren(x));
        for(Value value : tempList){
            if(!completeList.contains(value)){
                completeList.add(value);
            }
        }
        completeList.sort(comparator);
        return completeList;
    }

    private Node get(Node x, String key, int d){
        if(x == null){
            return null;
        }
        if(d == key.length()){
            return x;
        }
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }

    private List<Value> getValueOfAllChildren(Node x){
        ArrayList<Value> completeList = new ArrayList<>();
        for(int i = 0; i < alphabetSize; i++){
            if(x.links[i] != null) {
                completeList.addAll(getValueOfAllChildren(x.links[i]));
            }
        }
        completeList.addAll(x.val);
        return completeList;
    }

    public Set<Value> deleteAllWithPrefix(String prefix) {
        if (prefix == null){
            return new HashSet<>();
        }
        prefix = prefix.toUpperCase().replaceAll("[^A-Z0-9]", "");
        Node x = get(this.root, prefix, 0);
        if(x == null){
            return new HashSet<>();
        }
        HashSet<Value> completeSet = new HashSet<>(getValueOfAllChildren(x));
        deleteReference(x, prefix);
        return completeSet;
    }

    public Set<Value> deleteAll(String key) {
        if (key == null){
            return new HashSet<>();
        }
        key = key.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return new HashSet<>(deleteAll(this.root, key, 0));
    }

    private ArrayList<Value> deleteAll(Node x, String key, int d){
        ArrayList<Value> list = new ArrayList<>();
        ArrayList<Value> emptyList = new ArrayList<>();
        if (x == null) {
            return list;
        }
        if (d == key.length()) {
            list = x.val;
            x.val = emptyList;
        } else {
            char c = key.charAt(d);
            list = this.deleteAll(x.links[c], key, d + 1);
        }
        if (x.val != emptyList) {
            return list;
        }
        for (int c = 0; c <TrieImpl.alphabetSize; c++) {
            if (x.links[c] != null)
            {
                return list;
            }
        }
        deleteReference(x, key);
        return list;
    }

    private void deleteReference(Node x, String key){
        Node thisX = this.root;
        int d = 0;
        char c = key.charAt(d);
        while(thisX.links[c] != x){
            thisX = thisX.links[c];
            d++;
            c = key.charAt(d);
        }
        thisX.links[c] = null;
    }

    public Value delete(String key, Value val) {
        if(key == null || val == null){
            return null;
        }
        key = key.toUpperCase().replaceAll("[^A-Z0-9]", "");
        Node x = get(this.root, key, 0);
        if(x != null && x.val.contains(val)){
            x.val.remove(val);
            if(x.val.isEmpty()){
                deleteAll(key);
            }
            return val;
        }
        return null;
    }
}