package de.mrjulsen.dragnsounds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.MutableComponent;;

public class SoundUtils {
    
    public static final String[] ACCEPTED_INPUT_AUDIO_FILE_EXTENSIONS = {
        "3g2", "3ga", "aac", "ac3", "aif", "amr", "ape", "au", "caf", "dts", "flac",
        "m4a", "m4b", "m4p", "mka", "mp2", "mp3", "oga", "ogg", "oma", "opus", "ra",
        "ram", "sln", "tta", "voc", "wav", "wma", "wv"
    };

    public static Map<String, String> getAudioMetadata(File file) {
        try {
            return getAudioMetadata(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            DragNSounds.LOGGER.error("Unable to read metadata from audio file.", e);
        }
        return new LinkedHashMap<>();
    }

    public static Map<String, String> getAudioMetadata(InputStream file) {
        Map<String, String> metadata = new LinkedHashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\0");
                for (String field : fields) {
                    if (field.contains("=")) {
                        String[] keyValue = field.split("=", 2);
                        if (keyValue.length < 2) continue;
                        String key = keyValue[0];
                        String value = keyValue[1].substring(0, keyValue[1].length() - 1).split("\1")[0].replaceAll("\\p{C}", "");
                        metadata.put(key, value);
                    }
                }
                if (line.contains("vorbis)")) {
                    break;
                }
            }
            br.close();
        } catch (IOException e) {
            DragNSounds.LOGGER.error("Unable to read metadata from audio file.", e);
        }
        return metadata;
    }

    public static String getMetaSafe(Map<String, String> meta, String key) {
        return meta.containsKey(key) ? meta.get(key) : "";
    }

    public static void showUploadDialog(boolean multiselect, Consumer<Optional<Path[]>> callback) {        
        MutableComponent title = TextUtils.translate("gui." + DragNSounds.MOD_ID + ".file_dialog.title");
        MutableComponent filter = TextUtils.translate("gui." + DragNSounds.MOD_ID + ".file_dialog.filter");
        PointerBuffer filterPatterns = MemoryUtil.memAllocPointer(ACCEPTED_INPUT_AUDIO_FILE_EXTENSIONS.length);
        for (String s : ACCEPTED_INPUT_AUDIO_FILE_EXTENSIONS) {
            filterPatterns.put(MemoryUtil.memUTF8("*." + s));
        }
        filterPatterns.flip();

        String s = TinyFileDialogs.tinyfd_openFileDialog(title.getString(), (CharSequence)null, filterPatterns, filter.getString(), multiselect);
        if (s != null) {
            callback.accept(Optional.ofNullable(Arrays.stream(s.split("|")).map(x -> Paths.get(s)).toArray(Path[]::new)));
        } else {
            callback.accept(Optional.empty());
        }
    }

    /**
     * Calculates the duration of the OGG audio file.
     * @param data The OGG audio file data
     * @return The playback duration in milliseconds
     */
    public static long calculateOggDuration(final byte[] data) {
        int rate = -1;
        int length = -1;

        for (int i = data.length - 1 - 8 - 2 - 4; i >= 0 && length < 0; i--) {
            if (isMatch(data, i, "OggS")) {
                byte[] byteArray = extractByteArray(data, i + 6, 8);
                length = extractIntLittleEndian(byteArray);
            }
        }

        for (int i = 0; i < data.length - 8 - 2 - 4 && rate < 0; i++) {
            if (isMatch(data, i, "vorbis")) {
                byte[] byteArray = extractByteArray(data, i + 11, 4);
                rate = extractIntLittleEndian(byteArray);
            }
        }

        double duration = (double) length / (double) rate;
        return (long)(duration * 1000);
    }

    private static boolean isMatch(byte[] array, int startIndex, String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            if (array[startIndex + i] != pattern.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static byte[] extractByteArray(byte[] array, int startIndex, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, startIndex, result, 0, length);
        return result;
    }

    private static int extractIntLittleEndian(byte[] byteArray) {
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }
}
