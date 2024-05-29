package de.mrjulsen.dragnsounds.test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.api.ServerApi;
import de.mrjulsen.dragnsounds.client.UploadScreen;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.util.SoundUtils;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ws.schild.jave.EncoderException;

public class TestItem extends Item {

    public TestItem() {
        super(new Properties());
    }

    private void upload(String path, Level level, Player player) {
        try {
            AtomicReference<UploadScreen> screen = new AtomicReference<>();
            long uploadId = ClientApi.uploadSound(path, new SoundFile.Builder(new SoundLocation(level, "pain", "path"), "Pain and Suffering", Map.of()), AudioSettings.getByFile(path),
            (file) -> {
                DLScreen.setScreen(null);
                System.out.println("FILE: " + file.get().getDisplayName());
            }, (client, server) -> {
                if (screen.get() != null) {
                    screen.get().setProgress(server.progress());
                    screen.get().setBuffer(client.progress());
                }
            }, (e) -> {
                System.out.println(e.message());
                DLScreen.setScreen(null);
            });
            screen.set(new UploadScreen(uploadId));
            DLScreen.setScreen(screen.get());
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                System.out.println("UPLOAD FILE...");
                SoundUtils.showUploadDialog(true, path -> upload(path.get()[0].toString(), level, player));                
                
                ClientApi.getAllSoundFiles(
                    List.of(new FileInfoFilter(FileInfoFilter.KEY_ID, "77", ECompareOperation.CONTAINS)),
                    (files) -> {
                        System.out.println("SOUND FILES (" + files.length + "):");
                        for (SoundFile file : files) {
                            System.out.println(" - " + file.getDisplayName() + ", " + file.getInfo().getOriginalTitle() + ", " + file.getInfo().getArtist() + ", " + file.getInfo().getAlbum() + ", " + file.getInfo().getDate());
                        }
                    }
                );
            }

        } else {
            if (!level.isClientSide) {                
                System.out.println("ALL FILES");
                SoundFile[] files = ServerApi.getAllSoundFiles(level, List.of()).get();
                for (SoundFile file : files) {
                    System.out.println(" - " + file.getDisplayName());
                }
                System.out.println("FILTERED FILES");
                files = ServerApi.getAllSoundFiles(level, List.of(new FileInfoFilter(FileInfoFilter.KEY_LOCATION, "oil:oi", ECompareOperation.EQUALS))).get();
                for (SoundFile file : files) {
                    System.out.println(" - " + file.getDisplayName());
                }
            }
        }
        

        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
