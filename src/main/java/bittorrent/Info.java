package bittorrent;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class FileItem {
    int length;
    List<String> path;

    FileItem(int length, List<String> path) {
        this.length = length;
        this.path = path;
    }

}

public class Info {


    String name;
    int pieceLength;
    List<String> pieceHashes = new ArrayList<>();
    int length = -1;
    List<FileItem> files = new ArrayList<>();
    boolean multiFileMode = true;

    public String getName() {
        return name;
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public List<String> getPieceHashes() {
        return pieceHashes;
    }

    public int getLength() {
        return length;
    }

    public List<FileItem> getFiles() {
        return files;
    }

    public boolean isMultiFileMode() {
        return multiFileMode;
    }

    @SuppressWarnings("unchecked")
    public Info(Map<Object, Object> infoDict) {
        try {
            name = (String) infoDict.get("name");
            pieceLength = (int) infoDict.get("piece length");
            updatePieceHashes((byte[]) infoDict.get("pieces"));
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


    private void updatePieceHashes(byte[] bytePieces) throws Exception {
        ByteBuffer byteBufferPieces = ByteBuffer.wrap(bytePieces);
        int bytesLength = bytePieces.length;
        if (bytesLength % 20 == 0) {
            int len = 0;
            while (len < bytesLength) {
                pieceHashes.add(String.valueOf(byteBufferPieces.slice(len, 20)));
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
