package de.mrjulsen.dragnsounds.net.stc;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.client.UploadScreen;
import de.mrjulsen.dragnsounds.core.ClientSoundManager;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.util.SoundUtils;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import ws.schild.jave.EncoderException;

public class SoundUploadCommandPacket implements IPacketBase<SoundUploadCommandPacket> {

    private SoundFile.Builder builder;
    private AudioSettings settings;
    private boolean showProgress;

    private CompoundTag nbt;

    public SoundUploadCommandPacket() {}

    public SoundUploadCommandPacket(SoundFile.Builder builder, AudioSettings settings, boolean showProgress) {
        this.builder = builder;
        this.settings = settings;
        this.showProgress = showProgress;
    }

    public SoundUploadCommandPacket(CompoundTag nbt, AudioSettings settings, boolean showProgress) {
        this.nbt = nbt;
        this.settings = settings;
        this.showProgress = showProgress;
    }

    @Override
    public void encode(SoundUploadCommandPacket packet, FriendlyByteBuf buf) {        
        buf.writeBoolean(packet.settings != null);
        buf.writeBoolean(packet.showProgress);
        buf.writeNbt(packet.builder.serializeNbt());
        if (packet.settings != null) {
            buf.writeNbt(packet.settings.toNbt());
        }
    }

    @Override
    public SoundUploadCommandPacket decode(FriendlyByteBuf buf) {
        boolean hasSettings = buf.readBoolean();
        boolean showProgress = buf.readBoolean();
        CompoundTag nbt = buf.readNbt();
        AudioSettings settings = null;
        if (hasSettings) {
            settings = AudioSettings.fromNbt(buf.readNbt());
        }
        return new SoundUploadCommandPacket(nbt, settings, showProgress);
    }

    @SuppressWarnings("resource")
    @Override
    public void handle(SoundUploadCommandPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                SoundUtils.showUploadDialog(false, (files) -> {
                    if (!files.isPresent()) {
                        return;
                    }
                    
                    try {
                        
                        AtomicReference<UploadScreen> screen = new AtomicReference<>(null);
                        long uploadId = ClientSoundManager.uploadSound(
                            files.get()[0].toString(),
                            SoundFile.Builder.fromNbt(packet.nbt, contextSupplier.get().getPlayer().getLevel()),
                            packet.settings != null ? packet.settings : AudioSettings.getByFile(files.get()[0].toString()),
                            (file) -> {
                                contextSupplier.get().getPlayer().sendMessage(TextUtils.translate("gui." + DragNSounds.MOD_ID + ".upload.completed"), null);
                                if (Minecraft.getInstance().screen == screen.get()) {
                                    DLScreen.setScreen(null);
                                }
                            }, (client, server) -> {
                                if (screen.get() != null) {
                                    screen.get().setCurrentState(server.state());
                                    screen.get().setProgress(server.progress());
                                    screen.get().setBuffer(client.progress());
                                }
                            }, (e) -> {
                                contextSupplier.get().getPlayer().sendMessage(TextUtils.translate("gui." + DragNSounds.MOD_ID + ".upload.failed"), null);
                                if (Minecraft.getInstance().screen == screen.get()) {
                                    DLScreen.setScreen(null);
                                }
                            }
                        );
                        
                        if (packet.showProgress) {                        
                            screen.set(new UploadScreen(uploadId));
                            DLScreen.setScreen(screen.get());
                        }
                    } catch (EncoderException e) {
                        DragNSounds.LOGGER.error("Unable to upload sound.", e);
                    }
                });
            });
        });
    }
    
}
