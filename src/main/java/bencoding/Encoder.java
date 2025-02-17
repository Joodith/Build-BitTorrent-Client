package bencoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static bencoding.Tokens.*;

@SuppressWarnings("unchecked")
public class Encoder {
    static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    public static ByteBuffer encode(Object object) throws Exception {
        encodeObject(object);
        return ByteBuffer.wrap(byteStream.toByteArray());
    }

    private static void encodeObject(Object object) throws Exception {
        switch (object) {
            case byte[] bytes -> encodeBytes(bytes);
            case String s -> encodeString(String.valueOf(object));
            case Integer i -> encodeInteger((int) object);
            case List<?> list -> encodeList((List<Object>) object);
            case Map<?, ?> map -> encodeDict((Map<String, Object>) object);
            case null, default -> {
                assert object != null;
                throw new Exception("Unable to encode type " + object.getClass().getName());
            }
        }
    }

    private static void encodeDict(Map<String, Object> dictObject) throws Exception {
        byteStream.write(TOKEN_DICT.getIdentifier());
        for (Map.Entry<String, Object> map : dictObject.entrySet()) {
            encodeObject(map.getKey());
            encodeObject(map.getValue());
        }
        byteStream.write(TOKEN_END.getIdentifier());
    }

    private static void encodeList(List<Object> objects) throws Exception {
        byteStream.write(TOKEN_LIST.getIdentifier());
        for (Object obj : objects) {
            encodeObject(obj);
        }
        byteStream.write(TOKEN_END.getIdentifier());

    }

    private static void encodeInteger(int object) throws IOException {
        byteStream.write(TOKEN_INTEGER.getIdentifier());
        String numStr = String.valueOf(object);
        byteStream.write(numStr.getBytes(StandardCharsets.UTF_8));
        byteStream.write(TOKEN_END.getIdentifier());

    }

    private static void encodeBytes(byte[] body) throws IOException {
        String strLen = String.valueOf(body.length);
        byteStream.write(strLen.getBytes(StandardCharsets.UTF_8));
        byteStream.write(TOKEN_STRING_SEPARATOR.getIdentifier());
        byteStream.write(body);


    }

    private static void encodeString(String s) throws IOException {
        byte[] strBytes = s.getBytes(StandardCharsets.UTF_8);
        String strLen = String.valueOf(strBytes.length);
        byteStream.write(strLen.getBytes(StandardCharsets.UTF_8));
        byteStream.write(TOKEN_STRING_SEPARATOR.getIdentifier());
        byteStream.write(strBytes);
//        encodeBytes(strBytes);
    }


}
