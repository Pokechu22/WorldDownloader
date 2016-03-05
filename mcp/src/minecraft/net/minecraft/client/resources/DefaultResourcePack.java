package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class DefaultResourcePack implements IResourcePack {
	public static final Set<String> defaultResourceDomains = ImmutableSet
			.<String> of("minecraft", "realms" /* WDL >>> */, "wdl" /* <<< WDL */);
	private final ResourceIndex field_188549_b;

	public DefaultResourcePack(ResourceIndex p_i46541_1_) {
		this.field_188549_b = p_i46541_1_;
	}

	public InputStream getInputStream(ResourceLocation location)
			throws IOException {
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

	public InputStream getInputStreamAssets(ResourceLocation location)
			throws IOException, FileNotFoundException {
		File file1 = this.field_188549_b.func_188547_a(location);
		return file1 != null && file1.isFile() ? new FileInputStream(file1)
				: null;
	}

	private InputStream getResourceStream(ResourceLocation location) {
		return DefaultResourcePack.class.getResourceAsStream("/assets/"
				+ location.getResourceDomain() + "/"
				+ location.getResourcePath());
	}

	public boolean resourceExists(ResourceLocation location) {
		return this.getResourceStream(location) != null
				|| this.field_188549_b.func_188545_b(location);
	}

	public Set<String> getResourceDomains() {
		return defaultResourceDomains;
	}

	public <T extends IMetadataSection> T getPackMetadata(
			IMetadataSerializer metadataSerializer, String metadataSectionName)
			throws IOException {
		try {
			InputStream inputstream = new FileInputStream(
					this.field_188549_b.func_188546_a());
			return AbstractResourcePack.readMetadata(metadataSerializer,
					inputstream, metadataSectionName);
		} catch (RuntimeException var4) {
			return (T) null;
		} catch (FileNotFoundException var5) {
			return (T) null;
		}
	}

	public BufferedImage getPackImage() throws IOException {
		return TextureUtil
				.readBufferedImage(DefaultResourcePack.class
						.getResourceAsStream("/"
								+ (new ResourceLocation("pack.png"))
										.getResourcePath()));
	}

	public String getPackName() {
		return "Default";
	}
}
