package de.mrjulsen.dragnsounds.core.data.filter;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import net.minecraft.resources.ResourceLocation;

public class FileMetadataFilter extends AbstractFilter<SoundFile> {

    public FileMetadataFilter(String key, String value, ECompareOperation operation) {
        super(key, value, operation);
    }

    @Override
    public ResourceLocation getFilterId() {
        return new ResourceLocation(DragNSounds.MOD_ID, "soundfile_metadata");
    }

    @Override
    public boolean isValid(SoundFile file) {
        boolean b = false;
        b = compareOperation() == ECompareOperation.NOT ?
            file.getMetadata().entrySet().stream().allMatch(e -> compareOperation().compare(e.getKey(), key()) || (!value().isBlank() && compareOperation().compare(e.getValue(), value())))
        :
            file.getMetadata().entrySet().stream().anyMatch(e -> compareOperation().compare(e.getKey(), key()) && (value().isBlank() || compareOperation().compare(e.getValue(), value())));

        return b;
    }
}
