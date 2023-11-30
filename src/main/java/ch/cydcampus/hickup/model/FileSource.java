package ch.cydcampus.hickup.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/* TODO: adjust to support multiple readers. */
public class FileSource implements DataSource {

    BufferedReader reader;
    TokenPool tokenPool = TokenPool.getPool();

    public FileSource(String filename) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filename));
    }

    @Override
    public Token consume() throws InterruptedException {
        String row = null;
        Token token = null;
        try {
            row = reader.readLine();
            token = tokenPool.allocateFromString(row);
        } catch (Exception e) {            
            if(row != null && token == null) {
                // invalid line in file, try again with next one.
                return consume();
            }
        }

        return token;
    }

    @Override
    public void stopProducer() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Problem closing file");
            e.printStackTrace();
        }
    }

    @Override
    public void registerReader() {
    }
    
}
