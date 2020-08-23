package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {

    private BTreeImpl<URI, Document> bTree;
    private StackImpl<Undoable> commandStack = new StackImpl<>();
    private TrieImpl<URI> trie = new TrieImpl<>();
    private MinHeap<Entry> minHeap = new MinHeapImpl<>();
    private HashMap<URI, Entry> entryMap = new HashMap<>();

    private int docCount = 0;
    private int maxDocCount = 0;
    private boolean maxDoc = false;
    private int totalBytes = 0;
    private int maxDocBytes = 0;
    private boolean maxBytes = false;
    private File baseDir = new File(System.getProperty("user.dir"));

    protected class Entry implements Comparable<Entry> {
        private URI uri;
        private Entry(URI uri){
            this.uri = uri;
            entryMap.put(uri, this);
        }
        @Override
        public int compareTo(Entry e2){
            return bTree.get(uri).compareTo(bTree.get(e2.uri));
        }
    }
    public DocumentStoreImpl(){
        bTree = new BTreeImpl<>();
        bTree.setPersistenceManager(new DocumentPersistenceManager());
        try {
            bTree.put(new URI("A"), null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public DocumentStoreImpl(File baseDir){
        bTree = new BTreeImpl<>();
        if(baseDir != null) {
            this.baseDir = baseDir;
            bTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        }
        try {
            bTree.put(new URI("A"), null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if(format == null || uri == null){
            throw new IllegalArgumentException();
        }
        if(input == null){
            if(getDocumentAsTxt(uri) != null) {
                int oldHash = getDocumentAsTxt(uri).hashCode();
                if (deleteDocument(uri)) {
                    return oldHash;
                }
            }
            return 0;
        }
        if (format == DocumentStore.DocumentFormat.TXT) {
            return putTextDocument(input, uri);
        }
        if(format == DocumentStore.DocumentFormat.PDF) {
            return putPDFDocument(input, uri);
        }
        return 0;
    }

    private int putTextDocument(InputStream input, URI uri){
        String txt = new String(getByteArrayOfInputStream(input)).trim();
        int txtHash = txt.hashCode();
        ensureDocInMinHeap(uri);
        if ((bTree.get(uri) == null) || (bTree.get(uri).getDocumentTextHashCode() != txtHash)) {
            DocumentImpl document = new DocumentImpl(uri, txt, txtHash);
            addDocument(uri, document);
            if (bTree.get(uri) != null) {
                return replaceDocument(uri, document);
            }
            this.bTree.put(uri, document);
            minHeap.insert(new Entry(document.getKey()));
        }else{
            //removed seemingly unnecessary pop
            //commandStack.pop();
            no_op(uri);

        }
        return 0;
    }

    private int putPDFDocument(InputStream input, URI uri){
        byte[] pdfBytes = getByteArrayOfInputStream(input);
        String txt = getPDFText(pdfBytes);
        ensureDocInMinHeap(uri);
        int txtHash = 0;
        if (txt != null) {
            txtHash = txt.hashCode();
        }
        if ((bTree.get(uri) == null) || bTree.get(uri).getDocumentTextHashCode() != txtHash) {
            DocumentImpl document = new DocumentImpl(uri, txt, txtHash, pdfBytes);
            addDocument(uri, document);
            if (bTree.get(uri) != null) {
                return replaceDocument(uri, document);
            }
            this.bTree.put(uri, document);
            minHeap.insert(new Entry(document.getKey()));
        }else{
            //removed seemingly unnecessary pop
            //commandStack.pop();
            no_op(uri);
        }
        return 0;
    }

    private void addDocument(URI uri, DocumentImpl document){
        addToTrie(document);
        ensureDocInMinHeap(uri);
        Document oldDocument = bTree.get(uri);
        GenericCommand<?> newCommand = new GenericCommand<>(uri, oldUri -> {
            document.setLastUseTime(0);
            minHeap.reHeapify(entryMap.get(uri));
            minHeap.removeMin();
            bTree.put(oldUri, oldDocument);
            deleteDocFromTrie(document);
            totalBytes -= (document.getDocumentAsPdf().length + document.getDocumentAsTxt().getBytes().length);
            if(oldDocument != null) {
                addToTrie(oldDocument);
                oldDocument.setLastUseTime(System.nanoTime());
                minHeap.insert(new Entry(oldUri));
                totalBytes += oldDocument.getDocumentAsPdf().length + oldDocument.getDocumentAsTxt().getBytes().length;
            }else{
                docCount--;
            }
            return true;
        });
        docCount++;
        totalBytes += document.getDocumentAsPdf().length + document.getDocumentAsTxt().getBytes().length;
        commandStack.push(newCommand);
        if((maxBytes && totalBytes > maxDocBytes) || (maxDoc && docCount > maxDocCount)){
            makeSpace();
        }
    }

    private int replaceDocument(URI uri, DocumentImpl document){
        int oldHashCode = bTree.get(uri).getDocumentTextHashCode();
        deleteDocument(uri);
        commandStack.pop();
        this.bTree.put(uri, document);
        minHeap.insert(new Entry(uri));
        return oldHashCode;
    }

    private void no_op(URI uri){
        GenericCommand<?> newCommand = new GenericCommand<>(uri, oldUri -> true);
        commandStack.push(newCommand);
    }

    private String getPDFText (byte[] pdfBytes) {
        try {
            PDDocument pdf = PDDocument.load(pdfBytes);
            PDFTextStripper pdfToText = new PDFTextStripper();
            String pdfText = pdfToText.getText(pdf).trim();
            pdf.close();
            return pdfText;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getByteArrayOfInputStream(InputStream inputStream){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int nextByte;
        try {
            nextByte = inputStream.read();
            while(nextByte != -1){
                byteArrayOutputStream.write(nextByte);
                nextByte = inputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] returnValue = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    private void addToTrie(Document document){
        String[] listOfWords = document.getDocumentAsTxt().split(" ");
        for (String word : listOfWords) {
            trie.put(word, document.getKey());
        }
    }

    private void deleteDocFromTrie(Document doc){
        if(doc == null){
            return;
        }
        String[] listOfWords = doc.getDocumentAsTxt().split(" ");
        for (String word : listOfWords) {
            trie.delete(word, doc.getKey());
        }
    }

    private void makeSpace(){
        while(maxDoc && docCount > maxDocCount || maxBytes && totalBytes > maxDocBytes){
            URI removedURI = minHeap.removeMin().uri;
            entryMap.put(removedURI, null);
            Document removedDoc = bTree.get(removedURI);
            //deleteDocFromTrie(removedDoc);
            try {
                bTree.moveToDisk(removedURI);
            } catch (Exception e) {
                e.printStackTrace();
            }
            totalBytes -= (removedDoc.getDocumentAsTxt().getBytes().length + removedDoc.getDocumentAsPdf().length);
            docCount--;
        }
    }

    public byte[] getDocumentAsPdf(URI uri) {
        ensureDocInMinHeap(uri);
        Document doc = bTree.get(uri);
        if(doc != null) {
            doc.setLastUseTime(System.nanoTime());
            minHeap.reHeapify(entryMap.get(uri));
            return bTree.get(uri).getDocumentAsPdf();
        }
        return null;
    }

    public String getDocumentAsTxt(URI uri) {
        ensureDocInMinHeap(uri);
        Document doc = bTree.get(uri);
        if(doc == null){
            return null;
        }
        doc.setLastUseTime(System.nanoTime());
        minHeap.reHeapify(entryMap.get(uri));
        return bTree.get(uri).getDocumentAsTxt();
    }

    public boolean deleteDocument(URI uri) {
        if(commandStack.size() == 0){
            no_op(uri);
            return false;
        }
        ensureDocInMinHeap(uri);
        if(bTree.get(uri) == null){
            return false;
        }
        Document oldDocument = bTree.get(uri);
        GenericCommand<?> newCommand = new GenericCommand<>(uri, oldUri -> {
            bTree.put(oldUri, oldDocument);
            if(oldDocument != null) {
                addToTrie(oldDocument);
                oldDocument.setLastUseTime(System.nanoTime());
                minHeap.insert(new Entry(oldUri));
                totalBytes += (oldDocument.getDocumentAsPdf().length + oldDocument.getDocumentAsTxt().getBytes().length);
                docCount++;
            }
            return true;
        });
        commandStack.push(newCommand);
        totalBytes -= (oldDocument.getDocumentAsPdf().length + oldDocument.getDocumentAsTxt().getBytes().length);
        docCount--;
        deleteDocFromTrie(oldDocument);
        oldDocument.setLastUseTime(0);
        minHeap.reHeapify(entryMap.get(uri));
        minHeap.removeMin();
        bTree.put(uri, null);
        return true;
    }

    public void undo() throws IllegalStateException {
        if(commandStack.size() == 0){
            throw new IllegalStateException();
        }
        commandStack.peek().undo();
        commandStack.pop();
        if(maxDoc || maxBytes) {
            makeSpace();
        }
    }

    public void undo(URI uri) throws IllegalStateException {
        if(commandStack.size() == 0){
            throw new IllegalStateException();
        }
        StackImpl<Undoable> tempStack = new StackImpl<>();
        while(true){
            if(commandStack.peek() instanceof GenericCommand && ((GenericCommand<?>) commandStack.peek()).getTarget() == uri){
                break;
            }
            if(commandStack.peek() instanceof CommandSet && ((CommandSet<URI>)commandStack.peek()).containsTarget(uri)){
                break;
            }
            tempStack.push(commandStack.peek());
            commandStack.pop();
        }
        if(commandStack.peek() instanceof GenericCommand) {
            commandStack.peek().undo();
            commandStack.pop();
        }
        if(commandStack.peek() instanceof CommandSet){
            ((CommandSet<URI>)commandStack.peek()).undo(uri);
            if(((CommandSet<?>)commandStack.peek()).size() == 0){
                commandStack.pop();
            }
        }
        while(tempStack.size() != 0){
            commandStack.push(tempStack.peek());
            tempStack.pop();
        }
        if(maxDoc || maxBytes) {
            makeSpace();
        }
    }
    public List<String> search(String keyword) {
        if(keyword == null){
            return new ArrayList<>();
        }
        Comparator<URI> comparator = Comparator.comparingInt((URI o) -> bTree.get(o).wordCount(keyword));
        List<URI> uriList = trie.getAllSorted(keyword, comparator.reversed());
        ArrayList<String> stringList = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri : uriList) {
            ensureDocInMinHeap(uri);
            Document doc = bTree.get(uri);
            stringList.add(doc.getDocumentAsTxt());
            doc.setLastUseTime(time);
            minHeap.reHeapify(entryMap.get(uri));
        }
        return stringList;
    }

    public List<byte[]> searchPDFs(String keyword) {
        if(keyword == null){
            return new ArrayList<>();
        }
        Comparator<URI> comparator = Comparator.comparingInt((URI o) -> bTree.get(o).wordCount(keyword));
        List<URI> uriList = trie.getAllSorted(keyword, comparator.reversed());
        ArrayList<byte[]> pdfList = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri : uriList) {
            ensureDocInMinHeap(uri);
            Document doc = bTree.get(uri);
            pdfList.add(doc.getDocumentAsPdf());
            doc.setLastUseTime(time);
            minHeap.reHeapify(entryMap.get(uri));
        }
        return pdfList;
    }

    public List<String> searchByPrefix(String keywordPrefix) {
        if(keywordPrefix == null){
            return new ArrayList<>();
        }
        List<URI> uriList = trie.getAllWithPrefixSorted(keywordPrefix, getPrefixComparator(keywordPrefix));
        ArrayList<String> stringList = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri : uriList) {
            ensureDocInMinHeap(uri);
            Document doc = bTree.get(uri);
            stringList.add(doc.getDocumentAsTxt());
            doc.setLastUseTime(time);
            minHeap.reHeapify(entryMap.get(uri));
        }
        return stringList;
    }

    public List<byte[]> searchPDFsByPrefix(String keywordPrefix) {
        if(keywordPrefix == null){
            return new ArrayList<>();
        }
        List<URI> uriList = trie.getAllWithPrefixSorted(keywordPrefix, getPrefixComparator(keywordPrefix));
        ArrayList<byte[]> pdfList = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri : uriList) {
            ensureDocInMinHeap(uri);
            Document doc = bTree.get(uri);
            pdfList.add(doc.getDocumentAsPdf());
            doc.setLastUseTime(time);
            minHeap.reHeapify(entryMap.get(uri));
        }
        return pdfList;
    }

    private Comparator<URI> getPrefixComparator(String keywordPrefix){
        return (o1, o2) -> {
            String[] firstListOfWords = bTree.get(o1).getDocumentAsTxt().split(" ");
            int d1Total = 0;
            for(String word : firstListOfWords ){
                if(word.startsWith(keywordPrefix)){
                    d1Total++;
                }
            }
            String[] secondListOfWords = bTree.get(o2).getDocumentAsTxt().split(" ");
            int d2Total = 0;
            for(String word : secondListOfWords ){
                if(word.startsWith(keywordPrefix)){
                    d2Total++;
                }
            }
            return d2Total - d1Total;
        };
    }

    public Set<URI> deleteAll(String keyword) {
        HashSet<URI> uriHashSet = new HashSet<>();
        if(keyword == null){
            return uriHashSet;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        Set<URI> uriSet = trie.deleteAll(keyword);
        for(URI uri : uriSet){
            ensureDocInMinHeap(uri);
            Document doc = bTree.get(uri);
            GenericCommand<URI> newCommand = new GenericCommand<>(uri, oldUri -> {
                bTree.put(oldUri, doc);
                addToTrie(doc);
                doc.setLastUseTime(System.nanoTime());
                minHeap.insert(new Entry(uri));
                return true;
            });
            uriHashSet.add(uri);
            deleteDocFromTrie(doc);
            deleteDocument(uri);
            commandStack.pop();
            commandSet.addCommand(newCommand);
        }
        commandStack.push(commandSet);
        return uriHashSet;
    }

    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        HashSet<URI> uriHashSet = new HashSet<>();
        if(keywordPrefix == null){
            return uriHashSet;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        Set<URI> uriSet = trie.deleteAllWithPrefix(keywordPrefix);
        for(URI uri : uriSet){
            ensureDocInMinHeap(uri);
            Document doc = bTree.get(uri);
            GenericCommand<URI> newCommand = new GenericCommand<>(uri, oldUri -> {
                bTree.put(oldUri, doc);
                addToTrie(doc);
                doc.setLastUseTime(System.nanoTime());
                minHeap.insert(new Entry(uri));
                return true;
            });
            uriHashSet.add(uri);
            deleteDocFromTrie(doc);
            deleteDocument(uri);
            commandStack.pop();
            commandSet.addCommand(newCommand);
        }
        commandStack.push(commandSet);
        return uriHashSet;
    }

    public void setMaxDocumentCount(int limit) {
        this.maxDocCount = limit;
        this.maxDoc = true;
        makeSpace();
    }

    public void setMaxDocumentBytes(int limit) {
        this.maxDocBytes = limit;
        this.maxBytes = true;
        makeSpace();
    }
    protected Document getDocument(URI uri){
        if(entryMap.get(uri) != null) {
            return bTree.get(uri);
        }
        return null;
    }

    protected void deleteFileFromDisk(URI uri){
        String scheme = uri.getScheme();
        String[] uriSections = uri.toString().split("/");
        File file = new File(baseDir.toString() + uri.toString().replace(scheme + ":/", "") + ".json");
        if(file.exists()){
            file.delete();
        }
        File directory = new File(baseDir.toString() + uri.toString().replace(scheme + ":/", "").replace(uriSections[uriSections.length - 1], ""));
        deleteDirectory(directory);
    }

    private void deleteDirectory(File directory){
        directory.delete();
        if(directory.getParentFile() != null && directory != baseDir){
            deleteDirectory(directory.getParentFile());
        }
    }

    private void ensureDocInMinHeap(URI uri){
        boolean alreadyInMinHeap = entryMap.get(uri) != null;
        if(bTree.get(uri) != null && !alreadyInMinHeap){
            this.docCount++;
            this.totalBytes += (bTree.get(uri).getDocumentAsPdf().length + bTree.get(uri).getDocumentAsTxt().getBytes().length);
            makeSpace();
            bTree.get(uri).setLastUseTime(System.nanoTime());
            minHeap.insert(new Entry(uri));
        } else if(bTree.get(uri) != null){
            bTree.get(uri).setLastUseTime(System.nanoTime());
            minHeap.reHeapify(entryMap.get(uri));
        }
    }
}
