package com.mycompany.indexcompression;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.ArrayList;
import org.apache.lucene.search.NumericRangeQuery;



/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class Searcher {
  private static StandardAnalyzer analyzer = new StandardAnalyzer();

  private IndexWriter writer;
  private ArrayList<File> queue = new ArrayList<>();
  static int NDOCS=5;

  public static void main(String[] args) throws IOException {
    
    
    if(args.length<1){
        System.err.println("Index path required!");
        System.exit(-1);
    }
    if(args.length>=2){
        NDOCS=Integer.parseInt(args[1]);
    }
        
    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
    String indexLocation = args[0];
    String s = args[0];

 
    IndexReader reader;
      reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation).toPath()));
    IndexSearcher searcher = new IndexSearcher(reader);
    
    
    s = "";
    while (!s.equalsIgnoreCase("q")) {
      try {
        System.out.println("Enter the search query (q=quit):");
        s = br.readLine();
        if (s.equalsIgnoreCase("q")) {
          break;
        }
        Query q = new QueryParser( "body", analyzer).parse(s);
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(NDOCS);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId);
            System.out.println(d.get("id")+" "+d.get("body"));
        }
        
        try{
            Long id=Long.valueOf(s);
            Query q2 = NumericRangeQuery.newLongRange("id",  id, id, true, true);
            collector = TopScoreDocCollector.create(NDOCS);
            searcher.search(q2, collector);
            
            hits = collector.topDocs().scoreDocs;

            // 4. display results
            System.out.println("Found " + hits.length + " hits.");
            for(int i=0;i<hits.length;++i) {
              int docId = hits[i].doc;
              Document d = searcher.doc(docId);
                System.out.println(d.get("id")+" "+d.get("body"));
            }
            
        }
        catch(NumberFormatException ex){
            
        }
      } catch (Exception e) {
        System.out.println("Error searching " + s + " : " + e.getMessage());
      }
    }

  }

  
}