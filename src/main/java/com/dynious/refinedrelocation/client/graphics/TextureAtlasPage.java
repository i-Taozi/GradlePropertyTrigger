package com.dynious.refinedrelocation.client.graphics;

import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class TextureAtlasPage {

	private final Map<String, TextureRegion> regionMap = Maps.newHashMap();
	public final String fileName;
	public final ResourceLocation texture;

	public TextureAtlasPage(String fileName, ResourceLocation texture) {
		this.fileName = fileName;
		this.texture = texture;
	}

	public void addRegion(TextureRegion region) {
		regionMap.put(region.name, region);
	}

	public TextureRegion getRegion(String name) {
		return regionMap.get(name);
	}

}
