package org.observertc.webrtc.observer.compressors.jszip;

import org.observertc.webrtc.observer.compressors.Decompressor;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JsZipDecompressor implements Decompressor {

    private ByteArrayOutputStream byteStream;
    private ZipOutputStream zipStream;
    String fileName = "Unknown";

    @Override
    public byte[] apply(byte[] input) throws Throwable {
        ZipEntry entry = new ZipEntry(this.fileName);
        entry.setSize(input.length);
        this.zipStream.putNextEntry(entry);
        this.zipStream.write(input);
        this.zipStream.closeEntry();
        return this.byteStream.toByteArray();
    }

    JsZipDecompressor() {
        this.byteStream = new ByteArrayOutputStream();
        this.zipStream = new ZipOutputStream(this.byteStream);
    }

    @Override
    public void close() throws Exception{
        this.zipStream.close();
    }
}
