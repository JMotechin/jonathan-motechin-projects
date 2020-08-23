package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private int txtHash;
    private byte[] pdfBytes;
    private DocumentStore.DocumentFormat format;
    private Map<String, Integer> hashMap = new HashMap<>();
    private long lastUseTime = 0;

    public DocumentImpl(URI uri, String txt, int txtHash){
        this.uri = uri;
        if(txt == null){
            throw new IllegalArgumentException();
        }
        this.txt = txt;
        this.txtHash = txtHash;
        this.format = DocumentStore.DocumentFormat.TXT;
        indexDocument();
        setLastUseTime(System.nanoTime());
    }
    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes){
        this.uri = uri;
        if(txt == null){
            throw new IllegalArgumentException();
        }
        this.txt = txt;
        this.txtHash = txtHash;
        this.pdfBytes = pdfBytes;
        this.format = DocumentStore.DocumentFormat.PDF;
        indexDocument();
        setLastUseTime(System.nanoTime());
    }
    protected DocumentImpl(URI uri, String txt, int txtHash, Map<String, Integer> index){
        this.uri = uri;
        if(txt == null){
            throw new IllegalArgumentException();
        }
        this.txt = txt;
        this.txtHash = txtHash;
        this.format = DocumentStore.DocumentFormat.TXT;
        setWordMap(index);
        setLastUseTime(System.nanoTime());
    }

    public byte[] getDocumentAsPdf() {
        if(format == DocumentStore.DocumentFormat.PDF){
            return this.pdfBytes;
        } else if (format == DocumentStore.DocumentFormat.TXT) {
            return makePDF();
        }
        return null;
    }
    public String getDocumentAsTxt() {
        return this.txt.trim();
    }

    public int getDocumentTextHashCode() {
        return this.txtHash;
    }

    public URI getKey() {
        return this.uri;
    }

    private byte[] makePDF(){
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);
        ByteArrayOutputStream outputStream;
        try {
            PDPageContentStream stream = new PDPageContentStream(pdf, page);
            stream.beginText();
            stream.setFont(PDType1Font.TIMES_BOLD_ITALIC, 14);
            stream.showText(this.txt.trim());
            stream.endText();
            stream.close();
            outputStream = new ByteArrayOutputStream();
            pdf.save(outputStream);
            pdf.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void indexDocument(){
        String[] listOfWords = this.getDocumentAsTxt().split(" ");
        for (String word : listOfWords) {
            word = word.toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (this.hashMap.get(word) == null) {
                this.hashMap.put(word, 1);
            } else {
                this.hashMap.put(word, this.hashMap.get(word) + 1);
            }
        }
    }

    public int wordCount(String word) {
        word = word.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if(this.hashMap.get(word) != null){
            return this.hashMap.get(word);
        }
        return 0;
    }

    public long getLastUseTime() {
        return this.lastUseTime;
    }

    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime = timeInNanoseconds;
    }

    @Override
    public int compareTo(Document o) {
        if(this.lastUseTime - o.getLastUseTime() < 0){
            return -1;
        } else if (this.lastUseTime - o.getLastUseTime() > 0){
            return 1;
        }
        return 0;
    }

    public Map<String, Integer> getWordMap() {
        return this.hashMap;
    }

    public void setWordMap(Map<String, Integer> wordMap) {
        this.hashMap = wordMap;
    }
}
