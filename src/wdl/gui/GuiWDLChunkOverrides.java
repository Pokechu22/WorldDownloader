package wdl.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import wdl.WDL;
import wdl.WDLPluginChannels;
import wdl.WDLPluginChannels.ChunkRange;

import com.google.common.collect.Multimap;

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

	private GuiButton startDownloadButton;
	
	/**
	 * The current position.
	 */
	private float scrollX, scrollZ;
	/**
	 * How large each chunk is on-screen.
	 */
	private static final int SCALE = 8;
	
	/**
	 * Is the background being dragged?
	 */
	private boolean dragging;
	/**
	 * The position of the mouse on the last tick, for dragging.
	 */
	private int lastTickX, lastTickY;
	
	public GuiWDLChunkOverrides(GuiScreen parent) {
		this.parent = parent;
		
		if (WDL.thePlayer != null) {
			this.scrollX = WDL.thePlayer.chunkCoordX;
			this.scrollZ = WDL.thePlayer.chunkCoordZ;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		this.list = new TextList(mc, width, height, TOP_MARGIN, BOTTOM_MARGIN);
		list.addLine("\u00A7c\u00A7lThis is a work in progress.");
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
		
		this.buttonList.add(new GuiButton(5, width / 2 - 155, 18, 150, 20,
				"Add range request"));
		startDownloadButton = new GuiButton(6, width / 2 + 5, 18, 150, 20,
				"Start download in these ranges");
		startDownloadButton.enabled = WDLPluginChannels.canDownloadAtAll();
		this.buttonList.add(startDownloadButton);
		
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
		if (button.id == 6) {
			if (!WDLPluginChannels.canDownloadAtAll()) {
				button.enabled = false;
				return;
			}
			WDL.startDownload();
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
		
		if (mouseY > TOP_MARGIN && mouseY < height - BOTTOM_MARGIN && mouseButton == 0) {
			dragging = true;
			lastTickX = mouseX;
			lastTickY = mouseY;
		}
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
		if (state == 0) {
			dragging = false;
		}
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY,
			int clickedMouseButton, long timeSinceLastClick) {
		if (dragging) {
			int deltaX = lastTickX - mouseX;
			int deltaY = lastTickY - mouseY;
			
			lastTickX = mouseX;
			lastTickY = mouseY;
			
			scrollX += deltaX / (float)SCALE;
			scrollZ += deltaY / (float)SCALE;
		}
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
		
		//TODO: remove list
		//this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		Utils.drawListBackground(TOP_MARGIN, BOTTOM_MARGIN, 0, 0, height, width);
		
		for (Multimap<String, ChunkRange> group : WDLPluginChannels.getChunkOverrides().values()) {
			for (ChunkRange range : group.values()) {
				drawRange(range, RNG_SEED, 0xFF);
			}
		}
		for (ChunkRange range : WDLPluginChannels.getChunkOverrideRequests()) {
			// Fancy sin alpha changing by time.
			int alpha = 127 + (int)(Math.sin(Minecraft.getSystemTime() * Math.PI / 5000) * 64);
			drawRange(range, 0x808080, alpha);
		}
		
		// Player position.
		int playerPosX = (int)(((WDL.thePlayer.posX / 16) - scrollX) * SCALE + (width / 2));
		int playerPosZ = (int)(((WDL.thePlayer.posZ / 16) - scrollZ) * SCALE + (height / 2));
		
		drawHorizontalLine(playerPosX - 3, playerPosX + 3, playerPosZ, 0xFFFFFFFF);
		// Vertical is 1px taller because it seems to be needed to make it proportional
		drawVerticalLine(playerPosX, playerPosZ - 4, playerPosZ + 4, 0xFFFFFFFF);
		
		//TODO: Drawing twice for clipping reasons - it may be suboptimal.
		Utils.drawBorder(TOP_MARGIN, BOTTOM_MARGIN, 0, 0, height, width);
		
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
	
	/**
	 * Default color for a chunk range with tag. Xor'd with the hashcode. <br/>
	 * Preview: <span style=
	 * "width: 100px; height: 50px; background-color: #0BBDFC; color: #0BBDFC;"
	 * >&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
	 */
	private static final int RNG_SEED = 0xBBDFC;
	
	/**
	 * Draws the given range at the proper position on screen.
	 * 
	 * @param range The range to draw.
	 * @param seed The default color for a tagless range. (See {@link #RNG_SEED})
	 * @param alpha The transparency.  0xFF: Fully solid, 0x00: Fully transparent
	 */
	private void drawRange(ChunkRange range, int seed, int alpha) {
		int color = (range.tag.hashCode() ^ seed) & 0x00FFFFFF;
		
		int x1 = chunkXToDisplayX(range.x1);
		int z1 = chunkZToDisplayZ(range.z1);
		int x2 = chunkXToDisplayX(range.x2) + SCALE - 1;
		int z2 = chunkZToDisplayZ(range.z2) + SCALE - 1;
		
		drawRect(x1, z1, x2, z2, color + (alpha << 24));
		
		int colorDark = darken(color);
		
		drawVerticalLine(x1, z1, z2, colorDark + (alpha << 24));
		drawVerticalLine(x2, z1, z2, colorDark + (alpha << 24));
		drawHorizontalLine(x1, x2, z1, colorDark + (alpha << 24));
		drawHorizontalLine(x1, x2, z2, colorDark + (alpha << 24));
	}
	
	/**
	 * Converts a chunk x coordinate to a display x coordinate, taking
	 * into account the value of {@link scrollX}. 
	 * 
	 * @param chunkX The chunk's x coordinate. 
	 * @return The display position.
	 */
	private int chunkXToDisplayX(int chunkX) {
		return (int)((chunkX - scrollX) * SCALE + (width / 2));
	}
	/**
	 * Converts a chunk z coordinate to a display z coordinate, taking
	 * into account the value of {@link scrollZ}. 
	 * 
	 * @param chunkZ The chunk's z coordinate. 
	 * @return The display position.
	 */
	private int chunkZToDisplayZ(int chunkZ) {
		return (int)((chunkZ - scrollZ) * SCALE + (height / 2));
	}
	
	/**
	 * Halves the brightness of the given color.
	 */
	private int darken(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		r /= 2;
		g /= 2;
		b /= 2;

		return (r << 16) + (g << 8) + b;
	}
}
