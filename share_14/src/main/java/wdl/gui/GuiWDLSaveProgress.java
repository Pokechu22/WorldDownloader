/*
 * This file is part of World Downloader: A mod to make backups of your multiplayer worlds.
 * https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2520465-world-downloader-mod-create-backups-of-your-builds
 *
 * Copyright (c) 2014 nairol, cubic72
 * Copyright (c) 2017-2019 Pokechu22, julialy
 *
 * This project is licensed under the MMPLv2.  The full text of the MMPL can be
 * found in LICENSE.md, or online at https://github.com/iopleke/MMPLv2/blob/master/LICENSE.md
 * For information about this the MMPLv2, see https://stopmodreposts.org/
 *
 * Do not redistribute (in modified or unmodified form) without prior permission.
 */
package wdl.gui;

import java.util.function.Supplier;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import wdl.WDL;

/**
 * GUI screen shown while the world is being saved.
 * <br/>
 * Based off of vanilla minecraft's
 * {@link net.minecraft.client.gui.GuiScreenWorking GuiScreenWorking}.
 */
public class GuiWDLSaveProgress extends GuiTurningCameraBase {
	private String majorTaskMessage = "";
	private Supplier<String> minorTaskMessageProvider = () -> "";
	private int majorTaskNumber;
	private final int majorTaskCount;
	private int minorTaskProgress;
	private int minorTaskMaximum;
	private boolean includeProgressInMinorTask = true;

	// Actually used for rendering
	private static final int FULL_BAR_WIDTH = 182;

	// Vary between 0 and 1.  prev values are from the previous tick, for interpolation
	private float majorBar, prevMajorBar;
	private float minorBar, prevMinorBar;

	private boolean doneWorking = false;

	private boolean cancelAttempted = false;

	/**
	 * Creates a new GuiWDLSaveProgress.
	 *
	 * @param wdl The WDL instance.
	 * @param title The title.
	 * @param taskCount The total number of major tasks that there will be.
	 */
	public GuiWDLSaveProgress(WDL wdl, ITextComponent title, int taskCount) {
		super(wdl, title);
		this.majorTaskCount = taskCount;
		this.majorTaskNumber = 0;
	}

	/**
	 * Starts a new major task with the given message.
	 */
	public synchronized void startMajorTask(String message, int minorTaskMaximum) {
		this.majorTaskMessage = message;
		this.majorTaskNumber++;

		this.minorTaskMessageProvider = () -> message;
		this.minorTaskProgress = 0;
		this.minorTaskMaximum = minorTaskMaximum;
	}

	/**
	 * Updates the progress on the current minor task.
	 *
	 * @param message
	 *            The message -- should be something like "saving chunk at x,z";
	 *            the current position and maximum and the percent are
	 *            automatically appended after it.
	 */
	public synchronized void setMinorTaskProgress(String message, int progress) {
		this.minorTaskMessageProvider = () -> message;
		this.minorTaskProgress = progress;
	}

	/**
	 * Updates the progress on the current minor task.
	 *
	 * @param messageProvider
	 *            Provides the message to be displayed.
	 */
	public synchronized void setMinorTaskProgress(Supplier<String> messageProvider, int progress) {
		this.minorTaskMessageProvider = messageProvider;
		this.minorTaskProgress = progress;
	}

	/**
	 * Updates the progress on the minor task.
	 */
	public synchronized void setMinorTaskProgress(int progress) {
		this.minorTaskProgress = progress;
	}

	/**
	 * Updates the number of minor tasks.
	 */
	public synchronized void setMinorTaskCount(int count) {
		this.minorTaskMaximum = count;
		this.includeProgressInMinorTask = true;
	}

	/**
	 * Updates the number of minor tasks, possibly hiding display of the actual
	 * number.
	 */
	public synchronized void setMinorTaskCount(int count, boolean show) {
		this.minorTaskMaximum = count;
		this.includeProgressInMinorTask = show;
	}

	/**
	 * Sets the GUI as done working, meaning it will be closed next tick.
	 */
	public synchronized void setDoneWorking() {
		this.doneWorking = true;
	}

	@Override
	public synchronized void tick() {
		super.tick();
		prevMajorBar = majorBar;
		prevMinorBar = minorBar;
		if (minorTaskMaximum > 0) {
			// Make the major bar also reflect the minor bar.
			majorBar = (float)((majorTaskNumber * minorTaskMaximum) + minorTaskProgress) /
					((majorTaskCount + 1) * minorTaskMaximum);
		} else {
			majorBar = (float)majorTaskNumber / majorTaskCount;
		}

		minorBar = (float)minorTaskProgress / minorTaskMaximum;
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	@Override
	public synchronized void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();

		if (this.doneWorking) {
			this.minecraft.displayGuiScreen((Screen) null);
		} else {
			Utils.drawBorder(32, 32, 0, 0, height, width);

			String majorTaskInfo = majorTaskMessage;
			if (majorTaskCount > 1) {
				majorTaskInfo = I18n.format(
						"wdl.gui.saveProgress.progressInfo", majorTaskMessage,
						majorTaskNumber, majorTaskCount);
			}
			String minorTaskInfo = minorTaskMessageProvider.get();
			if (minorTaskMaximum > 1 && includeProgressInMinorTask) {
				minorTaskInfo = I18n.format(
						"wdl.gui.saveProgress.progressInfo", minorTaskInfo,
						minorTaskProgress, minorTaskMaximum);
			}

			this.drawCenteredString(this.font,
					majorTaskInfo, this.width / 2, 100, 0xFFFFFF);
			this.drawProgressBar(110, 84, 89,
						prevMajorBar + (majorBar - prevMajorBar) * partialTicks);

			this.drawCenteredString(this.font, minorTaskInfo,
					this.width / 2, 130, 0xFFFFFF);
			this.drawProgressBar(140, 64, 69,
					prevMinorBar + (minorBar - prevMinorBar) * partialTicks);

			super.render(mouseX, mouseY, partialTicks);
		}
	}

	/**
	 * Draws a progress bar on the screen. (A lot of things are always kept the
	 * same and thus aren't arguments, such as x-position being the center of
	 * the screen).
	 *
	 * @param y
	 *            Y-position of the progress bar.
	 * @param emptyV
	 *            The vertical coordinate of the empty part in the icon map
	 *            (icons.png)
	 * @param filledV
	 *            The vertical coordinate of the full part in the icon map
	 *            (icons.png)
	 * @param progress
	 *            The progress value of the bar, between 0 and 1
	 */
	private void drawProgressBar(int y, int emptyV, int filledV, float progress) {
		progress = MathHelper.clamp(progress, 0, 1);

		this.minecraft.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);

		final int height = 5;

		final int x = (this.width / 2) - (FULL_BAR_WIDTH / 2);
		final int u = 0; //Texture position.

		blit(x, y, u, emptyV, FULL_BAR_WIDTH, height);
		blit(x, y, u, filledV, (int)(FULL_BAR_WIDTH * progress), height);
	}

	@Override
	public boolean onCloseAttempt() {
		this.cancelAttempted = true;
		// Don't allow closing this GUI with escape
		return false;
	}

	public boolean cancelAttempted() {
		return this.cancelAttempted;
	}
}
