package de.mrjulsen.dragnsounds.client;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadState;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLProgressBar;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class UploadScreen extends DLWindow {

    protected DLProgressBar progressBar;
    protected final long uploadId;
    protected DLButton cancelButton;

    protected UploadState currentState = UploadState.CONVERT;

    protected final Component title = TextUtils.translate("gui." + DragNSounds.MOD_ID + ".upload.title");
    protected final String keyConvert = "gui." + DragNSounds.MOD_ID + ".upload.convert";
    protected final String keyUpload = "gui." + DragNSounds.MOD_ID + ".upload.upload";

    public UploadScreen(DLWindowManager manager, long uploadId) {
        super(manager);
        this.uploadId = uploadId;

        setSize(200, 100);
        windowSpawnPosition.set(WindowPosition.CENTER);
        getWindowManager().setCloseOnEscape(false);

        progressBar = addComponent(new DLProgressBar(width() / 2 - 80, height() / 2, 160, 10));
        progressBar.color.set(DLColor.fromInt(0xFFA4EB34));
        progressBar.backgroundColor.set(DLColor.fromInt(0xFF000000));
        progressBar.borderColor.set(DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR);

        cancelButton = addComponent(new DLButton(width() / 2 - 50, height() / 2 + 20, 100, 20));
        cancelButton.text.set(CommonComponents.GUI_CANCEL);
        cancelButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            ClientApi.cancelUpload(uploadId);
            return false;
        });
    }

    @Override
    public void close() throws Exception {        
        getWindowManager().setCloseOnEscape(true);
    }

    @Override
    public void tick() {
        super.tick();
        cancelButton.enabled.set(ClientApi.canCancelUpload(uploadId));
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, height() / 2 - 40, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, height() / 2 - 20, currentState == UploadState.CONVERT ? TextUtils.translate(keyConvert) : TextUtils.translate(keyUpload, (int)(progressBar.value.get().doubleValue() * 100)), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
    }
    
    public void setProgress(double d) {
        DLUtils.doIfNotNull(progressBar, x -> x.value.set(d));
    }

    public void setCurrentState(UploadState state) {
        this.currentState = state;
    }    
}
