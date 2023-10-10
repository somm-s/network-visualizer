package com.lockedshields;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
public class PcapDecompressor {

    // method to decompress pcap.gz file and store it in a temporary file. Returns the temporary file name.
    public static void decompress(String filename, String tempname) throws Exception {
        // Open the compressed file
        FileInputStream fis = new FileInputStream(filename);
        GZIPInputStream gzis = new GZIPInputStream(fis);

        // Create a temporary file to store the decompressed data
        FileOutputStream fos = new FileOutputStream(tempname);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }

        // Close the streams
        gzis.close();
        fis.close();
        fos.close();
    }
}
