package com.Lilith.Curtain.features.player.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.Lilith.Curtain.features.player.menu.ContainerFakePlayerInventory;

public class GuiFakePlayerInventory extends GuiContainer {

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(
        "textures/gui/container/generic_54.png");
    private final ContainerFakePlayerInventory container;

    public GuiFakePlayerInventory(EntityPlayer player) {
        super(new ContainerFakePlayerInventory(player));
        this.container = (ContainerFakePlayerInventory) this.inventorySlots;
        this.ySize = 222;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(this.container.inventory.getInventoryName(), 8, 6, 4210752);
        this.fontRendererObj
            .drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
            .bindTexture(CHEST_GUI_TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, 6 * 18 + 17);
        this.drawTexturedModalRect(x, y + 6 * 18 + 17, 0, 126, this.xSize, 96);
    }
}
