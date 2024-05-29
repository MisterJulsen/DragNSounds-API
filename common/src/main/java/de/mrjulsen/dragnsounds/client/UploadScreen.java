package de.mrjulsen.dragnsounds.client;

import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.api.ClientApi.UploadState;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.CommonComponents;

public class UploadScreen extends DLScreen {

    protected ProgressBarWidget progressBar;
    protected final long uploadId;
    protected DLButton cancelButton;

    protected UploadState currentState;

    protected final String keyConvert = "gui." + DragNSounds.MOD_ID + ".upload.convert";
    protected final String keyUpload = "gui." + DragNSounds.MOD_ID + ".upload.upload";

    public UploadScreen(long uploadId) {
        super(TextUtils.translate("gui." + DragNSounds.MOD_ID + ".upload.title"));
        this.uploadId = uploadId;
    }

    @Override
    protected void init() {
        super.init();
        this.progressBar = addRenderableOnly(new ProgressBarWidget(width / 2 - 80, height / 2, 160, 0, 100, 0));
        this.progressBar.setBackColor(0xFF000000);
        this.progressBar.setBorderColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
        this.progressBar.setBarColor(0xFFA4EB34);
        this.progressBar.setBufferBarColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);

        cancelButton = addButton(width / 2 - 50, height / 2 + 20, 100, 20, CommonComponents.GUI_CANCEL, (btn) -> {
            ClientApi.cancelUpload(uploadId);            
        }, null);
    }

    @Override
    public void tick() {
        super.tick();
        cancelButton.active = ClientApi.canCancelUpload(uploadId);
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderScreenBackground(graphics);
        DynamicGuiRenderer.renderWindow(graphics, new GuiAreaDefinition(width / 2 - 100, height / 2 - 50, 200, 100));
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
        GuiUtils.drawString(graphics, font, width / 2, height / 2 - 40, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.CENTER, false);
        GuiUtils.drawString(graphics, font, width / 2, height / 2 - 20, currentState == UploadState.CONVERT ? TextUtils.translate(keyConvert, (int)progressBar.getValue()) : TextUtils.translate(keyUpload, (int)progressBar.getValue()), DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.CENTER, false);
    }
    
    public void setProgress(double d) {
        this.progressBar.setValue(d);
    }

    public void setBuffer(double d) {
        this.progressBar.setBufferValue(d);
    }

    public void setCurrentState(UploadState state) {
        this.currentState = state;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
}
