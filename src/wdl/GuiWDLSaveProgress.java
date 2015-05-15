package wdl;

import java.io.IOException;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IProgressUpdate;

/**
 * GUI screen shown while the world is being saved.
 * <br/>
 * Based off of vanilla minecraft's 
 * {@link net.minecraft.client.gui.GuiScreenWorking GuiScreenWorking}.
 */
public class GuiWDLSaveProgress extends GuiScreen implements IProgressUpdate {
	private String mainMessage = "";
	private String subMessage = "";
	private int progress;
	private boolean doneWorking;

	/**
	 * Sets the main message for display.
	 * <br/>
	 * Blame the horrible name on {@link IProgressUpdate}.
	 */
	@Override
	public void displaySavingString(String message) {
		this.resetProgressAndMessage(message);
	}

	/**
	 * Sets the progress to 0, and changes the sub message.
	 */
	@Override
	public void resetProgressAndMessage(String subMessage) {
		this.subMessage = subMessage;
		this.setLoadingProgress(0);
	}

	/**
	 * Sets the sub message.
	 * <br/>
	 * Blame the horrible name on {@link IProgressUpdate}.
	 */
	@Override
	public void displayLoadingString(String message) {
		this.subMessage = message;
	}

	/**
	 * Updates the progress bar on the loading screen to the specified amount.
	 * 
	 * @param progress The loading progress, a percentage.
	 */
	@Override
	public void setLoadingProgress(int progress) {
		this.progress = progress;
	}

	/**
	 * Sets the GUI as done working, meaning it will be closed next tick.
	 */
	@Override
	public void setDoneWorking() {
		this.doneWorking = true;
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		final int PROGRESS_BAR_WIDTH = 182;
		final int PROGRESS_BAR_X = (this.width / 2) - (PROGRESS_BAR_WIDTH / 2);
		final int PROGRESS_BAR_Y = (this.height / 2);
		
		if (this.doneWorking) {
			this.mc.displayGuiScreen((GuiScreen) null);
		} else {
			this.drawDefaultBackground();
			this.drawCenteredString(this.fontRendererObj, this.mainMessage,
					this.width / 2, 70, 0xFFFFFF);
			this.drawCenteredString(this.fontRendererObj, this.subMessage,
					this.width / 2, 90, 0xFFFFFF);
			
			this.mc.getTextureManager().bindTexture(Gui.icons);
			
			drawTexturedModalRect(PROGRESS_BAR_X, PROGRESS_BAR_Y, 0, 74,
					PROGRESS_BAR_WIDTH, 5);
			drawTexturedModalRect(PROGRESS_BAR_X, PROGRESS_BAR_Y, 0, 74,
					PROGRESS_BAR_WIDTH, 5);
			drawTexturedModalRect(PROGRESS_BAR_X, PROGRESS_BAR_Y, 0, 79,
					(this.progress * 182) / 100, 5);

			drawCenteredString(this.fontRendererObj, this.progress + "%",
					this.width / 2, PROGRESS_BAR_Y - 10, 0xFF00FF);
			
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		//Don't call the super method, as that causes the UI to close if escape
		//is pressed.
	}
}
