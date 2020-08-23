package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import org.junit.Before;
import org.junit.Test;
import edu.yu.cs.com1320.project.impl.Utils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BTreeImplTest {

    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;
    private byte[] pdfData1;
    private String pdfTxt1;

    //variables to hold possible values for doc2
    private URI uri2;
    private String txt2;
    private byte[] pdfData2;
    private String pdfTxt2;

    private List<DocumentImpl> emptyList = new ArrayList<>();
    private List<byte[]> emptyPDFList = new ArrayList<>();
    private List<String> emptyStringList = new ArrayList<>();

    @Before
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "This is the text of doc1, in plain text. No fancy file format - just plain old String too tool";
        this.pdfTxt1 = "This is some PDF text for doc1, hat tip to Adobe. too tool tools";
        this.pdfData1 = Utils.textToPdfData(this.pdfTxt1);

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text for doc2. A plain old String.";
        this.pdfTxt2 = "PDF content for doc2: PDF format was opened in 2008.";
        this.pdfData2 = Utils.textToPdfData(this.pdfTxt2);
    }

    @Test
    public void get() {
        BTreeImpl<String, Integer> bTree = new BTreeImpl<>();
        bTree.put("one", 1);
        bTree.put("two", 2);
        bTree.put("three", 3);
        bTree.put("four", 4);
        assertEquals((int)3, (int)bTree.get("three"));
        assertEquals((int)2, (int)bTree.get("two"));
        assertNull(bTree.get("seven"));
    }

    @Test
    public void put() {
    }

    @Test
    public void moveToDisk() {
    }

    @Test
    public void setPersistenceManager() {
    }
}