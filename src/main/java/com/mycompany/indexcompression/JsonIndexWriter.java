package com.mycompany.indexcompression;



import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Created by Prudhvi
 */
public class JsonIndexWriter {

    String indexPath = "";

    String jsonFilePath = "";

    IndexWriter indexWriter = null;

    public JsonIndexWriter(String indexPath, String jsonFilePath) {
        this.indexPath = indexPath;
        this.jsonFilePath = jsonFilePath;
    }
    
     public boolean openIndex(){
        try {
            Directory dir = FSDirectory.open(new File(indexPath).toPath());
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig( analyzer);
            
            //Always overwrite the directory now changed to append
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            iwc.setCodec(new CompressingCodec());
            indexWriter = new IndexWriter(dir, iwc);

            return true;
        } catch (Exception e) {
            System.err.println("Error opening the index. " + e.getMessage());

        }
        return false;

    }
    
         public static BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
        return br2;
    }   
    public  JSONArray readJsonFile() {

        BufferedReader br = null;
        JSONParser parser = new JSONParser();
        JSONArray array=new JSONArray();
        try {

            String sCurrentLine;
            br=getBufferedReaderForCompressedFile(jsonFilePath);
//            br = new BufferedReader(new FileReader(jsonFilePath));

            while ((sCurrentLine = br.readLine()) != null) {

                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    array.add(jsonObject);

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompressorException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return array;
    }
    
   

    
    public void addDocument(JSONObject object){
            Document doc = new Document();
            String body=(String) object.get("body");
            String id=(String) object.get("id");
            long idLong=Long.valueOf(id, 36);
//            System.out.println(body);
            doc.add(new TextField("body", body, Field.Store.YES));
            doc.add(new LongField("id", idLong, Field.Store.YES));
//            doc.add(new StoredField("body_store",CompressionTools.compressString(body)));
            try {
                indexWriter.addDocument(doc);
            } catch (IOException ex) {
                System.err.println("Error adding documents to the index. " +  ex.getMessage());
            }
            catch(Exception ex){
                 System.err.println("Error adding documents to the index. " +  ex.getMessage());
            }
        
    }

    public void createIndexFly(){
        openIndex();
        BufferedReader br = null;
        JSONParser parser = new JSONParser();
        try {

            String sCurrentLine;
            br=getBufferedReaderForCompressedFile(jsonFilePath);
//            br = new BufferedReader(new FileReader(jsonFilePath));

            while ((sCurrentLine = br.readLine()) != null) {
//                System.out.println("Record:\t" + sCurrentLine);

                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    addDocument(jsonObject);

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompressorException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        finish();
    }

    
    
    /**
     * Write the document to the index and close it
     */
    public void finish(){
        try {
            indexWriter.commit();
            indexWriter.close();
        } catch (IOException ex) {
            System.err.println("We had a problem closing the index: " + ex.getMessage());
        }
    }
    
    
    
    public static void main(String[] args) {
        String indexPath=null,jsonPath=null;
        if(args.length<2){
            System.err.println("Index Path and Json Path needed");
            System.exit(0);
        }
        indexPath=args[0];
        jsonPath=args[1];
        JsonIndexWriter indexWriter=new  JsonIndexWriter(args[0], args[1]);
        indexWriter.createIndexFly();
        
    }
}


