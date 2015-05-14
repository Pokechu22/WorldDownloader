package wdl;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IProgressUpdate;

/**
 * GUI screen shown while the world is being saved.
 * <br/>
 * Based off of vanilla minecraft's 
 * {@link net.minecraft.client.gui.GuiScreenWorking GuiScreenWorking}.
 */
public class GuiWDLSaveProgress extends GuiScreen implements IProgressUpdate {
	private String field_146591_a = "";
	private String field_146589_f = "";
	private int field_146590_g;
	private boolean field_146592_h;
	private static final String __OBFID = "CL_00000707";

	/**
	 * Shows the 'Saving level' string.
	 */
	@Override
	public void displaySavingString(String message) {
		this.resetProgressAndMessage(message);
	}

	/**
	 * this string, followed by "working..." and then the "% complete" are the 3
	 * lines shown. This resets progress to 0, and the WorkingString to
	 * "working...".
	 */
	@Override
	public void resetProgressAndMessage(String p_73721_1_) {
		this.field_146591_a = p_73721_1_;
		this.displayLoadingString("Working...");
	}

	/**
	 * Displays a string on the loading screen supposed to indicate what is
	 * being done currently.
	 */
	@Override
	public void displayLoadingString(String message) {
		this.field_146589_f = message;
		this.setLoadingProgress(0);
	}

	/**
	 * Updates the progress bar on the loading screen to the specified amount.
	 * Args: loadProgress
	 */
	@Override
	public void setLoadingProgress(int progress) {
		this.field_146590_g = progress;
	}

	@Override
	public void setDoneWorking() {
		this.field_146592_h = true;
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.field_146592_h) {
			this.mc.displayGuiScreen((GuiScreen) null);
		} else {
			this.drawDefaultBackground();
			this.drawCenteredString(this.fontRendererObj, this.field_146591_a,
					this.width / 2, 70, 16777215);
			this.drawCenteredString(this.fontRendererObj, this.field_146589_f
					+ " " + this.field_146590_g + "%", this.width / 2, 90,
					16777215);
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
	}
}
