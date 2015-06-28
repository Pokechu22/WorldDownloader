package wdl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.text.Collator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;

/**
 * GUI that controls what entities are saved.
 */
public class GuiWDLEntities extends GuiScreen {
	private class GuiEntityList extends GuiListExtended {
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			try {
				List<String> passiveEntities = new ArrayList<String>();
				List<String> hostileEntities = new ArrayList<String>();
				List<String> otherEntities = new ArrayList<String>();
				
				//Attempt to steal the 'stringToClassMapping' field. 
				Map<String, Class> stringToClassMapping = null;
				
				for (Field field : EntityList.class.getDeclaredFields()) {
					if (field.getType().equals(Map.class)) {
						field.setAccessible(true);
						Map m = (Map)field.get(null);
						
						Map.Entry e = (Map.Entry)m.entrySet().toArray()[0];
						if (e.getKey() instanceof String && 
								e.getValue() instanceof Class) {
							stringToClassMapping = (Map<String, Class>)m;
						}
					}
				}
				
				if (stringToClassMapping == null) {
					throw new Exception("Failed to find stringToClassMapping");
				}
				
				//Now build an actual list.
				for (Map.Entry<String, Class> e : 
						stringToClassMapping.entrySet()) {
					String entity = e.getKey();
					Class c = e.getValue();
					
					if (IMob.class.isAssignableFrom(c)) {
						hostileEntities.add(entity);
					} else if (IAnimals.class.isAssignableFrom(c)) {
						passiveEntities.add(entity);
					} else {
						otherEntities.add(entity);
					}
				}
				
				Collections.sort(hostileEntities, Collator.getInstance());
				Collections.sort(passiveEntities, Collator.getInstance());
				Collections.sort(otherEntities, Collator.getInstance());
				
				//TODO: Create the actual list of objects.
			} catch (Exception e) {
				WDL.chatError("Error setting up Entity List UI: " + e);
				e.printStackTrace();
				
				Minecraft.getMinecraft().displayGuiScreen(null);
			}
		}};
		
		public GuiEntityList(Minecraft mc) {
			super(mc, GuiWDLEntities.this.width, GuiWDLEntities.this.height,
					63, GuiWDLEntities.this.height - 32, 20);
		}

		@Override
		public IGuiListEntry getListEntry(int p_148180_1_) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected int getSize() {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
