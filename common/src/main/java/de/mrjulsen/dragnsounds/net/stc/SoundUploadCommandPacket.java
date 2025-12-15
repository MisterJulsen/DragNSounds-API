package de.mrjulsen.dragnsounds.net.stc;

import java.util.concurrent.atomic.AtomicReference;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.client.UploadScreen;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.util.SoundUtils;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import ws.schild.jave.EncoderException;

public class SoundUploadCommandPacket extends NetworkPacketData {

    private static final String NBT_BUILDER = "Builder";
    private static final String NBT_SETTINGS = "Settings";
    private static final String NBT_SHOW_PROGRESS = "ShowProgress";

    private CompoundTag builderNbt;
    private AudioSettings settings;
    private boolean showProgress;

    public SoundUploadCommandPacket(DLStatus status) { super(status); }
    public SoundUploadCommandPacket(SoundFile.Builder builder, AudioSettings settings, boolean showProgress) {
        super(DLStatus.OK);
        this.builderNbt = builder.serializeNbt();
        this.settings = settings;
        this.showProgress = showProgress;
    }

    @Override protected void write(CompoundTag tag) {
        tag.putBoolean(NBT_SHOW_PROGRESS, showProgress);
        tag.put(NBT_BUILDER, builderNbt);
        if (settings != null) tag.put(NBT_SETTINGS, settings.toNbt());
    }

    @Override protected void read(CompoundTag tag) {
        this.showProgress = tag.getBoolean(NBT_SHOW_PROGRESS);
        this.builderNbt = tag.getCompound(NBT_BUILDER);
        this.settings = tag.contains(NBT_SETTINGS) ? AudioSettings.fromNbt(tag.getCompound(NBT_SETTINGS)) : null;
    }

    public static void handle(SoundUploadCommandPacket packet, NetworkPacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            SoundUtils.showUploadDialog(false, (files) -> {
                if (!files.isPresent()) return;
                try {
                    AtomicReference<UploadScreen> screen = new AtomicReference<>(null);
                    long uploadId = ClientSoundManager.uploadSound(
                        files.get()[0].toString(),
                        SoundFile.Builder.fromNbt(packet.builderNbt, context.getPlayer().level()),
                        packet.settings != null ? packet.settings : AudioSettings.getByFile(files.get()[0].toString()),
                        (file) -> {
                            if (Minecraft.getInstance().screen instanceof DLScreen sc) {
                                sc.getWindowManager().close();
                            }
                        }, (client, server) -> {
                            if (screen.get() != null) {
                                screen.get().setCurrentState(server.state());
                                screen.get().setProgress(server.progress());
                            }
                        }, (e) -> {
                            context.getPlayer().sendSystemMessage(TextUtils.translate("gui." + DragNSounds.MOD_ID + ".upload.failed").withStyle(ChatFormatting.RED));
                            if (Minecraft.getInstance().screen instanceof DLScreen sc) {
                                sc.getWindowManager().close();
                            }
                        }
                    );
                    if (packet.showProgress) {
                        DLWindow.openWindow(mgr -> {
                            UploadScreen uploadScreen = new UploadScreen(mgr, uploadId);
                            screen.set(uploadScreen);
                            return uploadScreen;
                        });
                    }
                } catch (EncoderException e) {
                    DragNSounds.LOGGER.error("Unable to upload sound.", e);
                }
            });
        });
    }
}
