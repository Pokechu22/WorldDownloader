package wdl.gui;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import wdl.WDL;

/**
 * Base GUI with the player in the background turning slowly to show the 
 * entire world.
 */
public abstract class GuiTurningCameraBase extends GuiScreen {
	/**
	 * Current yaw.
	 */
	private float yaw;
	/**
	 * The previous mode for the camera (First person, 3rd person, ect)
	 */
	private int oldCameraMode;
	/**
	 * The previous state as to whether the hud was hidden with F1.
	 */
	private boolean oldHideHud;
	/**
	 * The previous state as to whether the debug menu was enabled with F#.
	 */
	private boolean oldShowDebug;
	/**
	 * The player to preview.
	 */
	private EntityPlayerSP cam;
	/**
	 * The previous render view entity (the entity which Minecraft uses
	 * for the camera)
	 */
	private Entity oldRenderViewEntity;
	/**
	 * Whether the camera has been set up.
	 */
	private boolean initializedCamera = false;
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		if (!initializedCamera) {
			this.cam = new EntityPlayerSP(WDL.minecraft, WDL.worldClient,
					WDL.thePlayer.sendQueue, WDL.thePlayer.getStatFileWriter());
			this.cam.setLocationAndAngles(WDL.thePlayer.posX, WDL.thePlayer.posY
					- WDL.thePlayer.getYOffset(), WDL.thePlayer.posZ,
					WDL.thePlayer.rotationYaw, 0.0F);
			this.yaw = WDL.thePlayer.rotationYaw;
			this.oldCameraMode = WDL.minecraft.gameSettings.thirdPersonView;
			this.oldHideHud = WDL.minecraft.gameSettings.hideGUI;
			this.oldShowDebug = WDL.minecraft.gameSettings.showDebugInfo;
			WDL.minecraft.gameSettings.thirdPersonView = 0;
			WDL.minecraft.gameSettings.hideGUI = true;
			WDL.minecraft.gameSettings.showDebugInfo = false;
			
			// Gets the render view entity for minecraft.
			this.oldRenderViewEntity = WDL.minecraft.func_175606_aa();
			
			initializedCamera = true;
		}
		
		// Sets the render view entity for minecraft.
		// When obfuscation changes, look in
		// net.minecraft.client.renderer.EntityRenderer.updateRenderer() for
		// code that looks something like this:
		//
		// if (this.mc.renderViewEntity == null) {
		//     this.mc.renderViewEntity = this.mc.thePlayer;
        // }
		WDL.minecraft.func_175607_a(this.cam);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.cam.prevRotationPitch = this.cam.rotationPitch = 0.0F;
		this.cam.prevRotationYaw = this.cam.rotationYaw = this.yaw;
		float motionSpeed = 0.475F;
		this.cam.lastTickPosY = this.cam.prevPosY = this.cam.posY = WDL.thePlayer.posY;
		this.cam.lastTickPosX = this.cam.prevPosX = this.cam.posX = WDL.thePlayer.posX
				- motionSpeed * Math.sin(this.yaw / 180.0D * Math.PI);
		this.cam.lastTickPosZ = this.cam.prevPosZ = this.cam.posZ = WDL.thePlayer.posZ
				+ motionSpeed * Math.cos(this.yaw / 180.0D * Math.PI);
		float rotationSpeed = 1.0F;
		this.yaw = (float)(this.yaw + rotationSpeed
				* (1.0D + 0.699999988079071D * Math.cos((this.yaw + 45.0F)
						/ 45.0D * Math.PI)));
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		WDL.minecraft.gameSettings.thirdPersonView = this.oldCameraMode;
		WDL.minecraft.gameSettings.hideGUI = oldHideHud;
		WDL.minecraft.gameSettings.showDebugInfo = oldShowDebug;
		WDL.minecraft.func_175607_a(this.oldRenderViewEntity);
	}
}
