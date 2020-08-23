package edu.yu.cs.com1320.project;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {
    /*/**
     * @param input
     * @return byte[]
     */
    /*public static byte[] toByteArray(InputStream input) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for(int data = input.read(); data != -1; data = input.read()){
                outputStream.write(data);
            }
            byte[] byteArray = outputStream.toByteArray();
            outputStream.close();
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    /**
     * converts byte[] of pdf data to text
     */
    public static String pdfDataToText(byte[] pdfBytes) {
        try {
            PDFTextStripper textStripper = new PDFTextStripper();
            PDDocument doc = PDDocument.load(pdfBytes);
            String returnString = textStripper.getText(doc).trim();
            doc.close();
            return returnString;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * converts a string to a PDF doc written out to a byte[]
     */
    public static byte[] textToPdfData(String text) throws IOException {
        //setup document and page
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PDPageContentStream content = new PDPageContentStream(document, page);
        document.addPage(page);
        content.beginText();
        PDFont font = PDType1Font.HELVETICA_BOLD;
        content.setFont(font, 10);
        content.newLineAtOffset(20, 20);
        //add text
        content.showText(text);
        content.endText();
        content.close();
        //save to ByteArrayOutputStream
        document.save(outputStream);
        document.close();
        byte[] returnValue = outputStream.toByteArray();
        outputStream.close();
        return returnValue;
    }
}