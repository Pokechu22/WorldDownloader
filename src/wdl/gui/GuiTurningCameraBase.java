package wdl.gui;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
	 * The yaw for the next tick.  Lineraly interpolated between
	 * this and {@link #yaw}.
	 */
	private float yawNextTick;
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
					WDL.thePlayer.connection, WDL.thePlayer.getStatFileWriter());
			this.cam.setLocationAndAngles(WDL.thePlayer.posX, WDL.thePlayer.posY
					- WDL.thePlayer.getYOffset(), WDL.thePlayer.posZ,
					WDL.thePlayer.rotationYaw, 0.0F);
			this.yaw = this.yawNextTick = WDL.thePlayer.rotationYaw;
			this.oldCameraMode = WDL.minecraft.gameSettings.thirdPersonView;
			this.oldHideHud = WDL.minecraft.gameSettings.hideGUI;
			this.oldShowDebug = WDL.minecraft.gameSettings.showDebugInfo;
			this.oldChatVisibility = WDL.minecraft.gameSettings.chatVisibility;
			WDL.minecraft.gameSettings.thirdPersonView = 0;
			WDL.minecraft.gameSettings.hideGUI = true;
			WDL.minecraft.gameSettings.showDebugInfo = false;
			WDL.minecraft.gameSettings.chatVisibility = EnumChatVisibility.HIDDEN;
			this.oldRenderViewEntity = WDL.minecraft.getRenderViewEntity();
			
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
		WDL.minecraft.setRenderViewEntity(this.cam);
	}
	
	/**
	 * Speed for a rotation, as a rough scale, in degrees per frame.
	 */
	private static final float ROTATION_SPEED = 1.0f;
	/**
	 * Change between the slowest speed and the average speed.
	 */
	private static final float ROTATION_VARIANCE = .7f;
	
	/**
	 * Increment yaw to the yaw for the next tick.
	 */
	@Override
	public void updateScreen() {
		this.yaw = this.yawNextTick;
		
		// TODO: Rewrite this function as a function of time, rather than
		// an incremental function, if it's possible to do so.
		// Due to the fact that it refers to itself, I have no idea how to
		// approach the problem - it's some kind of integration that would be
		// needed, but it's really complex.
		
		// Yaw is in degrees, but Math.cos is in radians. The
		// "(this.yaw + 45) / 45.0 * Math.PI)" portion basically makes cosine
		// give the lowest values in each cardinal direction and the highest
		// while looking diagonally.  These are then multiplied by .7 and added
		// to 1, which creates a speed varying from .3 to 1.7.  This causes it
		// to speed through diagonals and go slow in cardinal directions, which
		// is the behavior we want.
		this.yawNextTick = (this.yaw + ROTATION_SPEED
				* (float) (1 + ROTATION_VARIANCE
						* Math.cos((this.yaw + 45) / 45.0 * Math.PI)));
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
		Vec3d playerPos = WDL.thePlayer.getPositionVector().addVector(0, WDL.thePlayer.getEyeHeight(), 0);
		Vec3d offsetPos = new Vec3d(WDL.thePlayer.posX - currentDistance * camX, WDL.thePlayer.posY + WDL.thePlayer.getEyeHeight(), WDL.thePlayer.posZ + camZ);
		
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
			
			Vec3d from = playerPos.addVector(offsetX, offsetY, offsetZ);
			Vec3d to = offsetPos.addVector(offsetX, offsetY, offsetZ);
			
			RayTraceResult pos = mc.theWorld.rayTraceBlocks(from, to);
	
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
			float yaw = this.yaw + (this.yawNextTick - this.yaw) * partialTicks;
			
			this.cam.prevRotationPitch = this.cam.rotationPitch = 0.0F;
			this.cam.prevRotationYaw = this.cam.rotationYaw = yaw;
			
			double x = Math.cos(yaw / 180.0D * Math.PI);
			double z = Math.sin((yaw - 90) / 180.0D * Math.PI);
			
			double distance = truncateDistanceIfBlockInWay(x, z, .5);
			this.cam.lastTickPosY = this.cam.prevPosY = this.cam.posY = WDL.thePlayer.posY;
			this.cam.lastTickPosX = this.cam.prevPosX = this.cam.posX = WDL.thePlayer.posX
					- distance * x;
			this.cam.lastTickPosZ = this.cam.prevPosZ = this.cam.posZ = WDL.thePlayer.posZ
					+ distance * z;
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
		WDL.minecraft.setRenderViewEntity(this.oldRenderViewEntity);
	}
}
