# IndexCompression

This is an experiment to compress stored fields in Lucene using a new CompressionCodec that supports block based compression. We can specify chunkSize and blockSize trading performance over memory by extending *CompressingStoredFieldsFormat*. 


> Inspired from https://issues.apache.org/jira/browse/LUCENE-4226

##Build Instructions
```sh
  mvn clean package
  
  ## To create Index arg1-indexStoragePath  arg2-Zipped file for index
  mvn exec:java -Dexec.mainClass="com.mycompany.indexcompression.JsonIndexWriter" -Dexec.args="../index/ ../RC_2007-10.bz2"
  
  ## To search within Index
   mvn exec:java -Dexec.mainClass="com.mycompany.indexcompression.Searcher" -Dexec.args="../index/"
```


