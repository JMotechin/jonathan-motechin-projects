package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir = new File(System.getProperty("user.dir"));
    public DocumentPersistenceManager(){}
    public DocumentPersistenceManager(File baseDir){
        if(baseDir != null) {
            this.baseDir = baseDir;
            if(!baseDir.exists()){
                baseDir.mkdirs();
            }
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, (JsonSerializer<Document>) (document, type, jsonSerializationContext) -> {
            JsonObject json = new JsonObject();
            json.addProperty("Document Text", val.getDocumentAsTxt());
            json.addProperty("URI", uri.toString());
            json.addProperty("Document HashCode", val.getDocumentTextHashCode());
            json.addProperty("Document HashMap", val.getWordMap().toString());
            return json;
        }).create();
        String scheme = uri.getScheme();
        String[] uriSections = uri.toString().split("/");
        File directory = new File(baseDir.toString() + uri.toString().replace(scheme + ":/", "").replace(uriSections[uriSections.length - 1], ""));
        if(!directory.exists()){
            directory.mkdirs();
        }
        File file = new File(baseDir.toString() + uri.toString().replace(scheme + ":/", "") + ".json");
        FileWriter fileWriter = new FileWriter(file);
        gson.toJson(val, Document.class, fileWriter);
        fileWriter.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        String scheme = uri.getScheme();
        String[] uriSections = uri.toString().split("/");
        File directory = new File(baseDir.toString() + uri.toString().replace(scheme + ":/", "").replace(uriSections[uriSections.length - 1], ""));
        if(!directory.exists()){
            directory.mkdirs();
        }
        File file = new File(baseDir.toString() + uri.toString().replace(scheme + ":/", "") + ".json");
        if(!file.exists()){
            return null;
        }
        Gson gson = getGsonForDeserialize(uri);
        FileReader fileReader = new FileReader(file);
        Document returnedDoc = gson.fromJson(fileReader, Document.class);
        fileReader.close();
        file.delete();
        deleteDirectory(directory);
        return returnedDoc;
    }

    private Gson getGsonForDeserialize(URI uri){
        return new GsonBuilder().registerTypeAdapter(Document.class, (JsonDeserializer<Document>) (jsonElement, type, jsonDeserializationContext) -> {
            if(jsonElement == null){
                return null;
            }
            String text = jsonElement.getAsJsonObject().get("Document Text").getAsString();
            URI docUri = null;
            try {
                docUri = new URI(jsonElement.getAsJsonObject().get("URI").getAsString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            int hashCode = jsonElement.getAsJsonObject().get("Document HashCode").getAsInt();
            HashMap<String, Integer> map = new HashMap<>();
            String[] keyPlusValue = jsonElement.getAsJsonObject().get("Document HashMap").getAsString().split(", ");
            for(String pair : keyPlusValue){
                String[] keyValue = pair.split("=");
                map.put(keyValue[0].replaceAll("[^A-Z0-9]", ""), Integer.valueOf(keyValue[1].replaceAll("[^A-Z0-9]", "")));
            }
            return new DocumentImpl(docUri, text, hashCode, map);
        }).create();
    }

    private void deleteDirectory(File directory){
        directory.delete();
        if(directory.getParentFile() != null && directory != baseDir){
            deleteDirectory(directory.getParentFile());
        }
    }
}
