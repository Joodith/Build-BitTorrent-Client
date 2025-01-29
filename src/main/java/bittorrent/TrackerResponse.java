package bittorrent;

import java.net.Inet4Address;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class Peer {
    String peerId;
    Inet4Address ipAddr;
    long port;

    public Peer(String peerId, Inet4Address ipAddr, long port) {
        this.peerId = peerId;
        this.ipAddr = ipAddr;
        this.port = port;
    }

    public Inet4Address getIpAddr() {
        return ipAddr;
    }

    public long getPort() {
        return port;
    }

    public String getPeerId() {
        return peerId;
    }
}

public class TrackerResponse {
    int interval;
    String peerString = "";
    List<Object> peersList = new ArrayList<>();
    List<Peer> peers=new ArrayList<>();

    public TrackerResponse(int interval, String peersRep) throws UnknownHostException {
        this.interval = interval;
        this.peerString = peersRep;
        decodeBinaryPeers();
    }

    public TrackerResponse(int interval, List<Object> peersList) throws UnknownHostException {
        this.interval = interval;
        this.peersList = peersList;
        decodeDictPeers();
    }

    public int getInterval() {
        return interval;
    }

    public List<Object> getPeersList() {
        return peersList;
    }

    public String getPeerString() {
        return peerString;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    @SuppressWarnings("unchecked")
    void decodeDictPeers() throws UnknownHostException {
        for (Object obj : peersList) {
            Map<Object, Object> peer = (Map<Object, Object>) obj;
            Inet4Address ipAddr = (Inet4Address) Inet4Address.getByName(peer.get("ip").toString());
            String peerId = generatePeerId();
            System.out.println(peer.get("peer id"));
            System.out.println(peer.get("ip"));
            System.out.println(peer.get("port"));
            peers.add(new Peer(peerId, ipAddr,Long.parseLong(String.valueOf(peer.get("port")))));
        }
    }

    void decodeBinaryPeers() throws UnknownHostException {
        byte[] peerBytes = peerString.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(peerBytes);
        int peerLengthInBytes = 6;
        int total = peerBytes.length;
        int pos = 0;
        while (pos < total) {
            ByteBuffer peerBuffer = buffer.slice(pos, peerLengthInBytes);
            String ip = String.valueOf(peerBuffer.slice(0, 4));
            String port = String.valueOf(peerBuffer.slice(4, 2));
            String uuidAsString = generatePeerId();
            Peer peer = new Peer(uuidAsString, (Inet4Address) Inet4Address.getByName(ip), Long.parseLong(port));
            peers.add(peer);
            pos += peerLengthInBytes;

        }

    }

    private static String generatePeerId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
