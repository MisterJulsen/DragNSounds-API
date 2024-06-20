package de.mrjulsen.dragnsounds.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        Map<String, String> metadata = new LinkedHashMap<>();
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("vorbis")) {
                    String[] fields = line.split("\0");
                    for (String field : fields) {
                        if (field.contains("=")) {
                            String[] keyValue = field.split("=", 2);
                            metadata.put(keyValue[0], keyValue[1].substring(0, keyValue[1].length() - 1).split("\1")[0].replaceAll("\\p{C}", ""));
                        }
                    }
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
            callback.accept(Optional.of(Arrays.stream(s.split("|")).map(x -> Paths.get(s)).toArray(Path[]::new)));
        } else {
            callback.accept(Optional.empty());
        }
    }
}
