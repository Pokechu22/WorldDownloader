package wdl.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDLPluginChannels;
import wdl.WDLPluginChannels.ChunkRange;

/**
 * A GUI that Lists... well, will list, the current chunk overrides.  Currently
 * a work in progress.
 * 
 * Also, expect a possible minimap integration in the future.
 */
public class GuiWDLChunkOverrides extends GuiScreen {
	private static final int TOP_MARGIN = 61, BOTTOM_MARGIN = 32;
	
	private TextList list;
	/**
	 * Parent GUI screen; displayed when this GUI is closed.
	 */
	private final GuiScreen parent;
	
	/**
	 * Text fields for creating a new range.
	 */
	private GuiNumericTextField x1Field, z1Field, x2Field, z2Field;
	
	public GuiWDLChunkOverrides(GuiScreen parent) {
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		this.list = new TextList(mc, width, height, TOP_MARGIN, BOTTOM_MARGIN);
		list.addLine("§c§lThis is a work in progress.");
		list.addLine("You can download in overriden chunks even if you are " +
				"not allowed to download elsewhere on the server.");
		list.addLine("Here is a list of the current chunk overrides; in the " +
				"future, a map will appear here.");
		list.addLine("Maybe also there will be a minimap mod integration.");
		list.addBlankLine();
		list.addLine(WDLPluginChannels.getChunkOverrides().toString());
		list.addBlankLine();
		list.addLine("You are requesting the following ranges " +
				"(to submit your request, go to the permission request page): ");
		for (ChunkRange range : WDLPluginChannels.getChunkOverrideRequests()) {
			list.addLine(range.toString());
		}
		
		this.x1Field = new GuiNumericTextField(0, fontRendererObj,
				this.width / 2 - 138, 18, 33, 20);
		this.z1Field = new GuiNumericTextField(1, fontRendererObj,
				this.width / 2 - 87, 18, 33, 20);
		this.x2Field = new GuiNumericTextField(2, fontRendererObj,
				this.width / 2 - 36, 18, 33, 20);
		this.z2Field = new GuiNumericTextField(3, fontRendererObj,
				this.width / 2 + 15, 18, 33, 20);
		
		this.buttonList.add(new GuiButton(5, width / 2 + 55, 18, 100, 20,
				"Add range request"));
		
		this.buttonList.add(new GuiButton(100, width / 2 - 100, height - 29,
				I18n.format("gui.done")));
		
		this.buttonList.add(new GuiButton(200, this.width / 2 - 155, 39, 100, 20,
				"Current perms"));
		this.buttonList.add(new GuiButton(201, this.width / 2 - 50, 39, 100, 20,
				"Request perms"));
		this.buttonList.add(new GuiButton(202, this.width / 2 + 55, 39, 100, 20,
				"Chunk Overrides"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 5) {
			ChunkRange range = new ChunkRange("",
					x1Field.getValue(), z1Field.getValue(), 
					x2Field.getValue(), z2Field.getValue());
			WDLPluginChannels.addChunkOverrideRequest(range);
			list.addLine(range.toString());
			x1Field.setText("");
			z1Field.setText("");
			x2Field.setText("");
			z2Field.setText("");
		}
		
		if (button.id == 100) {
			this.mc.displayGuiScreen(this.parent);
		}
		
		if (button.id == 200) {
			this.mc.displayGuiScreen(new GuiWDLPermissions(this.parent));
		}
		if (button.id == 201) {
			this.mc.displayGuiScreen(new GuiWDLPermissionRequest(this.parent));
		}
		if (button.id == 202) {
			// Would open this GUI; do nothing.
		}
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		x1Field.mouseClicked(mouseX, mouseY, mouseButton);
		z1Field.mouseClicked(mouseX, mouseY, mouseButton);
		x2Field.mouseClicked(mouseX, mouseY, mouseButton);
		z2Field.mouseClicked(mouseX, mouseY, mouseButton);
		list.func_148179_a(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.func_178039_p();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		x1Field.textboxKeyTyped(typedChar, keyCode);
		z1Field.textboxKeyTyped(typedChar, keyCode);
		x2Field.textboxKeyTyped(typedChar, keyCode);
		z2Field.textboxKeyTyped(typedChar, keyCode);
		
		super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void updateScreen() {
		x1Field.updateCursorCounter();
		z1Field.updateCursorCounter();
		x2Field.updateCursorCounter();
		z2Field.updateCursorCounter();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.list == null) {
			return;
		}
		
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, "Chunk overrides",
				this.width / 2, 8, 0xFFFFFF);
		
		x1Field.drawTextBox();
		z1Field.drawTextBox();
		x2Field.drawTextBox();
		z2Field.drawTextBox();
		
		this.drawString(fontRendererObj, "X1:", x1Field.xPosition - 16, 24, 0xFFFFFF);
		this.drawString(fontRendererObj, "Z1:", z1Field.xPosition - 16, 24, 0xFFFFFF);
		this.drawString(fontRendererObj, "X2:", x2Field.xPosition - 16, 24, 0xFFFFFF);
		this.drawString(fontRendererObj, "Z2:", z2Field.xPosition - 16, 24, 0xFFFFFF);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
