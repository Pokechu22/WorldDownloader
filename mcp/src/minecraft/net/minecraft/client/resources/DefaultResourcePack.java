package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class DefaultResourcePack implements IResourcePack {
	public static final Set<String> defaultResourceDomains = ImmutableSet
			.<String> of("minecraft", "realms" /* WDL >>> */, "wdl" /* <<< WDL */);
	private final Map<String, File> mapAssets;

	public DefaultResourcePack(Map<String, File> mapAssetsIn) {
		this.mapAssets = mapAssetsIn;
	}

	public InputStream getInputStream(ResourceLocation location) throws IOException {
		InputStream inputstream = this.getResourceStream(location);

		if (inputstream != null) {
			return inputstream;
		} else {
			InputStream inputstream1 = this.getInputStreamAssets(location);

			if (inputstream1 != null) {
				return inputstream1;
			} else {
				throw new FileNotFoundException(location.getResourcePath());
			}
		}
	}

	public InputStream getInputStreamAssets(ResourceLocation location) throws IOException, FileNotFoundException {
		File file1 = (File)this.mapAssets.get(location.toString());
		return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
	}

	private InputStream getResourceStream(ResourceLocation location) {
		return DefaultResourcePack.class.getResourceAsStream("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath());
	}

	public boolean resourceExists(ResourceLocation location) {
		return this.getResourceStream(location) != null || this.mapAssets.containsKey(location.toString());
	}

	public Set<String> getResourceDomains() {
		return defaultResourceDomains;
	}

	public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) throws IOException {
		try {
			InputStream inputstream = new FileInputStream((File)this.mapAssets.get("pack.mcmeta"));
			return AbstractResourcePack.readMetadata(p_135058_1_, inputstream, p_135058_2_);
		} catch (RuntimeException var4) {
			return (T)null;
		} catch (FileNotFoundException var5) {
			return (T)null;
		}
	}

	public BufferedImage getPackImage() throws IOException {
		return TextureUtil.readBufferedImage(DefaultResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("pack.png")).getResourcePath()));
	}

	public String getPackName() {
		return "Default";
	}
}
