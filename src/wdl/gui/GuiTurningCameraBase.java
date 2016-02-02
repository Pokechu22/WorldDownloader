package wdl.gui;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
	 * The previous chat visibility.
	 */
	private EnumChatVisibility oldChatVisibility;
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
			this.oldChatVisibility = WDL.minecraft.gameSettings.chatVisibility;
			WDL.minecraft.gameSettings.thirdPersonView = 0;
			WDL.minecraft.gameSettings.hideGUI = true;
			WDL.minecraft.gameSettings.showDebugInfo = false;
			WDL.minecraft.gameSettings.chatVisibility = EnumChatVisibility.HIDDEN;
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
	
	/**
	 * Truncates the distance so that the camera does not clip into blocks.
	 * Based off of {@link net.minecraft.client.renderer.EntityRenderer#orientCamera(float)}.
	 * @param camX X-position of the camera.
	 * @param camZ Z-position of the camera.
	 * @param currentDistance Current distance from the camera.
	 * @return A new distance, equal to or less than <code>currentDistance</code>.
	 */
	private double truncateDistanceIfBlockInWay(double camX, double camZ, double currentDistance) {
		Vec3 playerPos = WDL.thePlayer.getPositionVector().addVector(0, WDL.thePlayer.getEyeHeight(), 0);
		Vec3 offsetPos = new Vec3(WDL.thePlayer.posX - currentDistance * camX, WDL.thePlayer.posY + WDL.thePlayer.getEyeHeight(), WDL.thePlayer.posZ + camZ);
		
		// NOTE: Vec3.addVector and Vec3.add return new vectors and leave the
		// current vector unmodified.
		for (int i = 0; i < 9; i++) {
			// Check offset slightly in all directions.
			float offsetX = ((i & 0x01) != 0) ? -.1f : .1f;
			float offsetY = ((i & 0x02) != 0) ? -.1f : .1f;
			float offsetZ = ((i & 0x04) != 0) ? -.1f : .1f;
			
			if (i == 8) {
				offsetX = 0;
				offsetY = 0;
				offsetZ = 0;
			}
			
			Vec3 from = playerPos.addVector(offsetX, offsetY, offsetZ);
			Vec3 to = offsetPos.addVector(offsetX, offsetY, offsetZ);
			
			MovingObjectPosition pos = mc.theWorld
					.rayTraceBlocks(from, to);
	
			if (pos != null) {
				double distance = pos.hitVec.distanceTo(playerPos);
				if (distance < currentDistance && distance > 0) {
					currentDistance = distance;
				}
			}
		}
		
		return currentDistance - .25;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.cam != null) {
			this.cam.prevRotationPitch = this.cam.rotationPitch = 0.0F;
			this.cam.prevRotationYaw = this.cam.rotationYaw = this.yaw;
			
			double x = Math.sin(this.yaw / 180.0D * Math.PI);
			double z = Math.cos(this.yaw / 180.0D * Math.PI);
			
			double distance = truncateDistanceIfBlockInWay(x, z, .5);
			this.cam.lastTickPosY = this.cam.prevPosY = this.cam.posY = WDL.thePlayer.posY;
			this.cam.lastTickPosX = this.cam.prevPosX = this.cam.posX = WDL.thePlayer.posX
					- distance * x;
			this.cam.lastTickPosZ = this.cam.prevPosZ = this.cam.posZ = WDL.thePlayer.posZ
					+ distance * z;
			
			float rotationSpeed = 1.0F;
			this.yaw = (float)(this.yaw + rotationSpeed
					* (1.0D + 0.699999988079071D * Math.cos((this.yaw + 45.0F)
							/ 45.0D * Math.PI)));
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		WDL.minecraft.gameSettings.thirdPersonView = this.oldCameraMode;
		WDL.minecraft.gameSettings.hideGUI = oldHideHud;
		WDL.minecraft.gameSettings.showDebugInfo = oldShowDebug;
		WDL.minecraft.gameSettings.chatVisibility = oldChatVisibility;
		WDL.minecraft.func_175607_a(this.oldRenderViewEntity);
	}
}
