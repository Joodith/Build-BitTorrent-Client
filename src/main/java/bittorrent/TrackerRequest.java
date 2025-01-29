package bittorrent;

public class TrackerRequest {
    String infoHash;
    String peerId;
    long port;
    int uploaded=0;
    int downloaded;
    int left;
    int compact;

    public TrackerRequest(String infoHash, String peerId, long port, int uploaded, int downloaded, int left, int compact) {
        this.infoHash = infoHash;
        this.peerId = peerId;
        this.port = port;
        this.uploaded = uploaded;
        this.downloaded = downloaded;
        this.left = left;
        this.compact = compact;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public String getPeerId() {
        return peerId;
    }

    public long getPort() {
        return port;
    }

    public int getUploaded() {
        return uploaded;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public int getLeft() {
        return left;
    }

    public int getCompact() {
        return compact;
    }
}
