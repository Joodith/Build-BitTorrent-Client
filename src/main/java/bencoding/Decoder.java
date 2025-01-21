package bencoding;

import java.nio.ByteBuffer;
import java.util.*;


import static bencoding.Tokens.*;

public class Decoder {
    static ByteBuffer data;
    static int currentIndex;
    static int size;

    public static Object decode(ByteBuffer data, int size) {
        Decoder.data = data;
        currentIndex = 0;
        Decoder.size = size;
        return decodeObject();
    }

    public static Object decode(ByteBuffer data) {
        Decoder.data = data;
        currentIndex = 0;
        Decoder.size = data.capacity();
        return decodeObject();
    }

    public static Object decodeObject() {
        char c;
        try {
            c = peek();
        } catch (Exception ex) {
            throw new ClassCastException();
        }
//        System.out.println("Peek char is "+c);
        if (c == TOKEN_INTEGER.getIdentifier()) {
            moveNext();
            return decodeInt();
        } else if (c == TOKEN_LIST.getIdentifier()) {
            moveNext();
            return decodeList();
        } else if (c == TOKEN_DICT.getIdentifier()) {
            moveNext();
            return decodeDict();
        } else if ("0123456789".contains(Character.toString(c))) {
            return decodeString();
        } else if (c == TOKEN_END.getIdentifier()) {
            return null;
        } else throw new RuntimeException();
    }

    private static Map<String, Object> decodeDict() {
        Map<String, Object> map = new LinkedHashMap<>();
        while (hasNext() && peek() != TOKEN_END.getIdentifier()) {
            Object key = decodeObject();
            Object value = decodeObject();
            map.put(String.valueOf(key), value);
        }
        moveNext();
        return map;
    }


    private static List<Object> decodeList() {
        List<Object> objects = new ArrayList<>();
        while (hasNext() && peek() != TOKEN_END.getIdentifier()) {
            objects.add(decodeObject());
        }
        moveNext();
        return objects;
    }

    private static String decodeString() {
        StringBuilder numStr = new StringBuilder();
        StringBuilder word = new StringBuilder();
        while (hasNext() && peek() != TOKEN_STRING_SEPARATOR.getIdentifier()) {
            numStr.append(peek());
            moveNext();
        }
        moveNext();
        int strLen = Integer.parseInt(numStr.toString());
        int index = 0;
        while (hasNext() && index < strLen) {
            word.append(peek());
            moveNext();
            index++;
        }
        return word.toString();
    }

    private static Integer decodeInt() {
        StringBuilder numStr = new StringBuilder();
        while (hasNext() && peek() != TOKEN_END.getIdentifier()) {
            numStr.append(peek());
            moveNext();
        }
        moveNext();
        return Integer.parseInt(numStr.toString());

    }

    private static void moveNext() {
        currentIndex += 1;
    }

    private static boolean hasNext() {
        return currentIndex < size;
    }


    private static char peek() {
        if (!hasNext()) return '\0';
        return (char) data.get(currentIndex);
    }
}
