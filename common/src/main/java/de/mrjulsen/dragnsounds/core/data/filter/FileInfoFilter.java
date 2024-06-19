package de.mrjulsen.dragnsounds.core.data.filter;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import net.minecraft.resources.ResourceLocation;

public class FileInfoFilter extends AbstractFilter<SoundFile> {

    public static final String KEY_DURATION = "Duration";
    public static final String KEY_OWNER_UUID = "Owner";
    public static final String KEY_UPLOAD_TIMESTAMP = "UploadTime";
    public static final String KEY_SIZE = "Size";
    public static final String KEY_ID = "Id";
    public static final String KEY_CHANNELS = "Channels";
    public static final String KEY_LOCATION = "Location";

    public FileInfoFilter(String key, String value, ECompareOperation operation) {
        super(key, value, operation);
    }
    
    @Override
    public ResourceLocation getFilterId() {
        return new ResourceLocation(DragNSounds.MOD_ID, "soundfile_info");
    }

    @Override
    public boolean isValid(SoundFile file) {
        try {
            switch (key()) {                
                case KEY_LOCATION:
                    return compareOperation().compare(file.getLocation().toString(), value());
                case KEY_DURATION:
                    return compareOperation().compare(file.getInfo().getDuration(), Long.parseLong(value()));
                case KEY_OWNER_UUID:
                    return compareOperation().compare(file.getInfo().getOwnerId().toString(), value());
                case KEY_UPLOAD_TIMESTAMP:
                    return compareOperation().compare(file.getInfo().getUploadTimeMillis(), Long.parseLong(value()));
                case KEY_SIZE:
                    return compareOperation().compare(file.getInfo().getSize(), Long.parseLong(value()));
                case KEY_ID:
                    return compareOperation().compare(file.getId(), value());
                case KEY_CHANNELS:
                    return compareOperation().compare(file.getInfo().getChannels(), Integer.parseInt(value()));
                default:
                    return false;
            }
        } catch (Throwable e) {
            return false;
        }
    }
}
