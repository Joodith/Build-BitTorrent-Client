package bittorrent;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bencoding.Decoder;

class FileItem {
    int length;
    List<String> path;

    FileItem(int length, List<String> path) {
        this.length = length;
        this.path = path;
    }

}

class Info {


    String name;
    int pieceLength;
    List<String> pieces=new ArrayList<>();
    int length = -1;
    List<FileItem> files=new ArrayList<>();
    boolean multiFileMode = true;

    @SuppressWarnings("unchecked")
    public Info(Map<Object, Object> infoDict) {
        try {
            name = (String) infoDict.get("name");
            pieceLength = (int) infoDict.get("piece length");
            updatePieces((String) infoDict.get("pieces"));
            if (infoDict.containsKey("length")) {
                length = (int) infoDict.get("length");
                multiFileMode = false;
            } else {
                List<Object> filesList = (List<Object>) infoDict.get("files");
                for (Object file : filesList) {
                    Map<Object, Object> fileMap = (Map<Object, Object>) file;
                    int len = (int) fileMap.get("length");
                    List<String> path = (List<String>) fileMap.get("path");
                    FileItem fileItem = new FileItem(len, path);
                    files.add(fileItem);

                }

            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("Error in info mapping");
        }


    }


    private void updatePieces(String strPieces) throws Exception {
        ByteBuffer bytePieces = ByteBuffer.wrap(strPieces.getBytes(StandardCharsets.UTF_8));
        int bytesLength = strPieces.length();
        if (bytesLength % 20 == 0) {
            int len = 0;
            while (len < bytesLength) {
                pieces.add(String.valueOf(bytePieces.slice(len, 20)));
                len += 20;
            }
        }


    }

    @Override
    public String toString() {
        return "Info{" +
                "name='" + name + '\'' +
                ", pieceLength=" + pieceLength +
                ", length=" + length +
                ", files=" + files +
                ", multiFileMode=" + multiFileMode +
                '}';
    }
}

public class Torrent {
    String announce;
    Info info;

    public String getAnnounce() {
        return announce;
    }

    public Info getInfo() {
        return info;
    }

    @SuppressWarnings("unchecked")
    public Torrent(String fileName) throws Exception {
        File file = new File(fileName);
        FileInputStream inputStream = new FileInputStream(file);
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
        byte[] bytes = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(bytes)) != -1) {
            byteBuffer.put(bytes, 0, bytesRead);
        }
        byteBuffer.flip();
        Map<String, Object> metaInfo = (Map<String, Object>) Decoder.decode(byteBuffer, (int) file.length());
        if (metaInfo.containsKey("announce")) {
            announce = (String) metaInfo.get("announce");
        } else {
            throw new Exception("Missing announce url");
        }
        if (metaInfo.get("info") instanceof Map<?, ?>) {
            Map<Object, Object> infoDict = (Map<Object, Object>) (metaInfo.get("info"));
            info = new Info(infoDict);
        } else {
            throw new Exception("Invalid meta info in torrent file");
        }


    }


}
