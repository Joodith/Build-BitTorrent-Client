package bittorrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bencoding.Decoder;
import bencoding.Encoder;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;


public class Torrent {
    String announce;
    Info info;
    String infoHash;

    public String getAnnounce() {
        return announce;
    }

    public Info getInfo() {
        return info;
    }

    public String getInfoHash() {
        return infoHash;
    }

    @SuppressWarnings("unchecked")
    public Torrent(String fileName) throws Exception {
        ByteBuffer byteBuffer = readFileIntoBuffer(fileName);
        Map<String, Object> metaInfo = (Map<String, Object>) Decoder.decode(byteBuffer, (int) new File(fileName).length());
        if (metaInfo.containsKey("announce")) {
            announce = (String) metaInfo.get("announce");
        } else {
            throw new Exception("Missing announce url");
        }
        if (metaInfo.get("info") instanceof Map<?, ?>) {
            Map<Object, Object> infoDict = (Map<Object, Object>) (metaInfo.get("info"));
            calcInfoHash(infoDict);
            info = new Info(infoDict);
        } else {
            throw new Exception("Invalid meta info in torrent file");
        }


    }

    private ByteBuffer readFileIntoBuffer(String fileName) throws IOException {
        File file = new File(fileName);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
            byte[] bytes = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                byteBuffer.put(bytes, 0, bytesRead);
            }
            byteBuffer.flip();
            return byteBuffer;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }


    private void calcInfoHash(Map<Object, Object> infoDict) throws Exception {
        ByteBuffer encodedBuffer = Encoder.encode(infoDict);
        byte[] encodedBytes = new byte[encodedBuffer.remaining()];
        encodedBuffer.get(encodedBytes);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(encodedBytes);
        System.out.println("Byte size of info hash : " + digest.length);
        StringBuilder str = new StringBuilder();
        for (byte b : digest) {
            str.append(String.format("%%%02X", b));
        }
        infoHash = str.toString();
    }

    public void requestPeers() throws URISyntaxException, IOException {
        String url = getTrackerRequestUrl();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();

        String respStr = response.body().string();
        System.out.println("Before : " + respStr);
        respStr = cleanResponse(respStr);
        System.out.println("After : " + respStr);
        TrackerResponse trackerResponse = parseTrackerResponse(respStr);
//        System.out.println(trackerResponse.getInterval());
        System.out.println("---Peers--");
        for (Peer peer : trackerResponse.getPeers()) {
            System.out.println(peer.getIpAddr() + ":" + peer.getPort());
        }


    }

    private @NotNull String getTrackerRequestUrl() {
        String peerId = "00112233445566778899";
        int len = 0;
        if (!getInfo().isMultiFileMode()) len = getInfo().getLength();
        String encodedInfoHash = getInfoHash();
        System.out.println("Info hex : " + encodedInfoHash);


        TrackerRequest trackerRequest = new TrackerRequest(encodedInfoHash,
                peerId, 6881, 0, 0,
                len, 1);
        String announceUrl = getAnnounce();

        HttpUrl.Builder urlBuilder
                = Objects.requireNonNull(HttpUrl.parse(announceUrl)).newBuilder();
        urlBuilder.addEncodedQueryParameter("info_hash", trackerRequest.getInfoHash());
        urlBuilder.addQueryParameter("peer_id", trackerRequest.getPeerId());
        urlBuilder.addQueryParameter("port", String.valueOf(trackerRequest.getPort()));
        urlBuilder.addQueryParameter("uploaded", String.valueOf(trackerRequest.getUploaded()));
        urlBuilder.addQueryParameter("downloaded", String.valueOf(trackerRequest.getDownloaded()));
        urlBuilder.addQueryParameter("left", String.valueOf(trackerRequest.getLeft()));
        urlBuilder.addQueryParameter("compact", String.valueOf(trackerRequest.getCompact()));
        return urlBuilder.build().toString();
    }

    @SuppressWarnings("unchecked")
    private static @NotNull TrackerResponse parseTrackerResponse(String respStr) throws UnknownHostException {
        byte[] bencodedData = respStr.getBytes(StandardCharsets.ISO_8859_1);
        Object object = Decoder.decode(ByteBuffer.wrap(bencodedData));
        Map<String, Object> responseJson = (Map<String, Object>) object;
        TrackerResponse trackerResponse = null;
        int interval = (int) responseJson.get("interval");
        Object peersObj = responseJson.get("peers");
        if (peersObj instanceof String peersRep) {
            trackerResponse = new TrackerResponse(interval, peersRep);
        } else {
            List<Object> peersList = (List<Object>) peersObj;
            trackerResponse = new TrackerResponse(interval, peersList);
        }
        return trackerResponse;
    }

    private static String cleanResponse(String respStr) {
        return respStr.replace("\\u0000", "\u0000");
    }


}
