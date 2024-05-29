package de.mrjulsen.dragnsounds.core.ext;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.Sound.Type;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class CustomSoundInstance extends AbstractSoundInstance {

    public static final String CUSTOM_SOUND_FILENAME_ROOT = "custom";
    /** @see Sound */ public static final String SOUND_LOCATION_ROOT = "sounds";
    
    public static final float PITCH_MIN = 0.5F;
    public static final float PITCH_MAX = 2.0F;
    public static final float PITCH_DEFAULT = 1.0F;
    public static final float VOLUME_MIN = 0.0F;
    public static final float VOLUME_MAX = 1.0F;
    public static final float VOLUME_DEFAULT = 1.0F;
    public static final int ATTENUATION_DISTANCE_DEFAULT = 16;

    private final long soundId;
    private final SoundFile file;

    private int attenuationDistance;

    public CustomSoundInstance(long soundId, SoundFile file, SoundSource source, float volume, float pitch, boolean looping, int delay, SoundInstance.Attenuation attenuation, double x, double y, double z, boolean relative, int attenuationDistance) {
        super(new ResourceLocation(DragNSounds.MOD_ID, CUSTOM_SOUND_FILENAME_ROOT + "/" + file.getId()), source);
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.looping = looping;
        this.delay = delay;
        this.attenuation = attenuation;
        this.relative = relative;
        this.attenuationDistance = attenuationDistance;

        this.file = file;
        this.soundId = soundId;
    }

    public int getAttenuationDistance() {
        return attenuationDistance;
    }

    public static CustomSoundInstance ui(long soundId, SoundFile file, SoundSource source, float pitch, float volume) {
        return new CustomSoundInstance(soundId, file, source, volume, pitch, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true, ATTENUATION_DISTANCE_DEFAULT);
    }

    public static CustomSoundInstance world(long soundId, SoundFile file, SoundSource source, float pitch, float volume, Vec3 position, boolean relative, int attenuationDistance) {
        return new CustomSoundInstance(soundId, file, source, volume, pitch, false, 0, SoundInstance.Attenuation.LINEAR, position.x(), position.y(), position.z(), relative, attenuationDistance);
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {   
        WeighedSoundEvents event = new WeighedSoundEvents(new ResourceLocation(DragNSounds.MOD_ID, CUSTOM_SOUND_FILENAME_ROOT), file.getDisplayName());
        Sound sound = new Sound(DragNSounds.MOD_ID + ":" + CUSTOM_SOUND_FILENAME_ROOT + "/" + String.valueOf(soundId), volume, pitch, 1, Type.FILE, true, false, getAttenuationDistance());
        event.addSound(sound);
        this.sound = sound;
        return event;
    }

}
