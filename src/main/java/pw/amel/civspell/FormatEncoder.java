package pw.amel.civspell;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.Charset;

/**
 * Allows encoding strings and other data types into minecraft color codes, in such a way that it is invisable to
 * the client.
 */
public class FormatEncoder {
    public static boolean isStringEncoded(String maybeEncoded) {
        return maybeEncoded.matches("§0(§[0-9a-f])*§r.*");
    }

    public FormatEncoder(String str, boolean isEncoded) {
        if (isEncoded && !str.matches("§0(§[0-9a-f])*§r.*")) {
            throw new IllegalArgumentException("String '" + str + "' is not a valid encoded string.");
        } else if (isEncoded) {
            this.encoded = str;
        } else {
            this.encoded = encodeString(str);
        }
    }

    private String encoded;

    public String getEncoded() {
        return encoded;
    }

    public String getDecoded() {
        StringBuilder hex = new StringBuilder();
        String data = encoded.substring(2).substring(0, encoded.indexOf("§r") - 1);
        for (char character : data.toCharArray()) {
            if (character != '§') {
                hex.append(character);
            }
        }
        byte[] bytes;
        try {
            bytes = Hex.decodeHex(hex.toString().toCharArray());
        } catch (DecoderException e) {
            throw new AssertionError("FormatEncoder contained non-hex bytes: " + encoded, e);
        }
        return new String(bytes, Charset.forName("UTF-8"));
    }

    /**
     * @return The text that is displayed to the client, if any. If there is no such text, a empty string is returned.
     */
    public String getDesplayText() {
        return encoded.replaceFirst("§0[0-9a-f]*§r", "");
    }

    /**
     * Encodes the given string into a string of minecraft format codes that are completely invisible to the client.
     */
    private static String encodeString(String str) {
        /*
         * A format code encoded string begins with a §0 and ends with a §r. It can contain characters matching the
         * regex [§r0-9a-f]. Any other characters are illegal, unless they come after the ending §r.
         * While the codes §k, §l, §m, §n, and §o could be used to save space, they are not to reduce complexity.
         *
         * The data portion of the format code string consists of the characters of hexidecimal-encoded UTF-8 string, and
         * the chatacter §, alternating so that before every digit there is a § character.
         */

        byte[] asBytes = str.getBytes(Charset.forName("UTF-8"));
        String asHex = Hex.encodeHexString(asBytes);
        StringBuilder dataPortionBuilder = new StringBuilder();
        for (int i = 0; i < asHex.length(); i++) {
            dataPortionBuilder.append("§");
            dataPortionBuilder.append(asHex.charAt(i));
        }
        String dataPortion = dataPortionBuilder.toString();
        return "§0" + dataPortion + "§r";
    }
}
