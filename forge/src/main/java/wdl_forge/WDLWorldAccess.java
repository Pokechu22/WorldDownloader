package wdl_forge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorldAccess;

public class WDLWorldAccess implements IWorldAccess
{

    // TODO: This needs a better solution!
    @Override
    public void onEntityDestroy(Entity entity)
    {
        /*
		if(WDL.shouldKeepEntity(entity))
		{
			//entity.isDead = false;
			WDL.chatDebug("Reviving entity " + entity);

			Entity test = WDL.wc.getEntityByID(entity.getEntityId());
			WDL.chatDebug("test=" + test);
			entity.isDead = false;
			//WDL.wc.addEntityToWorld(entity.getEntityId(), entity);
			//WDL.wc.spawnEntityInWorld(entity);
		}
         */
    }


    // Unused:

    @Override
    public void onEntityCreate(Entity a) {}

    @Override
    public void markBlockForUpdate(int a, int b, int c) {}

    @Override
    public void markBlockForRenderUpdate(int a, int b, int c) {}

    @Override
    public void markBlockRangeForRenderUpdate(int a, int b, int c, int d, int e, int f) {}

    @Override
    public void playSound(String a, double b, double c, double d, float e, float f) {}

    @Override
    public void playSoundToNearExcept(EntityPlayer a, String b, double c, double d, double e, float f, float g) {}

    @Override
    public void spawnParticle(String a, double b, double c, double d, double e, double f, double g) {}

    @Override
    public void playRecord(String a, int b, int c, int d) {}

    @Override
    public void broadcastSound(int a, int b, int c, int d, int e) {}

    @Override
    public void playAuxSFX(EntityPlayer a, int b, int c, int d, int e, int f) {}

    @Override
    public void destroyBlockPartially(int a, int b, int c, int d, int e) {}

    @Override
    public void onStaticEntitiesChanged() {}

}
