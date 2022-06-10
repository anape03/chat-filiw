package com.example.filiw.backend;

import android.media.MediaMetadataRetriever;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class MultimediaFile implements Serializable {
    String multimediaFileName;
    String dateCreated;
    String length;
    String frameRate;
    String frameWidth;
    String frameHeight;
    byte[] multimediaFileChunk;
    int chunkID;
    
    /**
     * Constructor for MultimediaFile
     * @param fileName File name
     * @param buffer Buffer array of data
     * @param chunkID Chunk number
     */
    public MultimediaFile(String fileName, byte[] buffer, int chunkID) {
        this.multimediaFileName = fileName;
        String extension = "";
        int index = fileName.lastIndexOf('.');
        if(index > 0) {
            extension = fileName.substring(index + 1);
        }

        if(!extension.equals("STRING")){ // Not Text Message
            File m_file = new File(multimediaFileName);
            Path m_path = Paths.get(multimediaFileName);
            MediaMetadataRetriever mMR = new MediaMetadataRetriever();
            mMR.setDataSource(m_file.getPath());
            try {
                BasicFileAttributes attr = Files.readAttributes(m_path, BasicFileAttributes.class);
                this.dateCreated = attr.creationTime().toString();
                this.length = Long.toString(attr.size()) ;
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (extension.equals("mp4")){ // video
                this.frameWidth = mMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                this.frameHeight = mMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                this.frameRate = mMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
            }
            if (extension.equals("jpg") || extension.equals("jpeg")){ // image
                this.frameWidth = mMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH);
                this.frameHeight = mMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT);
            }
        }

        this.multimediaFileChunk = buffer;
        this.chunkID = chunkID;
    }

    public int getChunkID(){ return this.chunkID; }

    public byte[] getChunkData(){ return this.multimediaFileChunk; }
   
}
