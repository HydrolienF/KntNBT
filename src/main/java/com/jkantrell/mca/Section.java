package com.jkantrell.mca;

import com.jkantrell.nbt.tag.CompoundTag;
import com.jkantrell.nbt.tag.StringTag;
import com.jkantrell.nbt.tag.Tag;

import java.util.*;
import java.util.function.Predicate;

public class Section implements Comparable<Section> {

	//FIELDS
	private int height_;
	private PaletteContainer<CompoundTag> blockPalette_;
	private PaletteContainer<StringTag> biomePalette_;
	private CompoundTag src_;


	//CONSTRUCTORS
	public Section(CompoundTag sectionRoot) {
		//Getting height
		if (!sectionRoot.containsKey("Y")) {
			throw new IllegalArgumentException("The provided CompoundTag is missing a 'Y' property");
		}
		this.height_ = sectionRoot.getByte("Y");
		this.blockPalette_ = this.craftPaletteContainer(sectionRoot, "block_states", 4096, 4);
		if (this.blockPalette_ == null) {
			this.blockPalette_ = new PaletteContainer<>(MCAUtil.airBlockTag(), 4096, 4);
		}
		this.biomePalette_ = this.craftPaletteContainer(sectionRoot, "biomes", 64, 1);
		this.src_ = sectionRoot;
	}


	//GETTERS
	public PaletteContainer<CompoundTag> getBlockStatePalette() {
		return blockPalette_;
	}
	public PaletteContainer<StringTag> getBiomePalette() {
		return biomePalette_;
	}
	public CompoundTag getSource() {
		return src_;
	}
	public int getHeight() {
		return height_;
	}

	//SETTERS
	public void setHeight(int height) {
		this.height_ = height_;
	}


	//UTIL
	@Override
	public int compareTo(Section o) {
		if (o == null) {
			return -1;
		}
		return Integer.compare(height_, o.height_);
	}
	public boolean isBlockStateEmpty() {
		return this.blockPalette_.isEmpty();
	}
	public boolean isBiomeEmpty() {
		return this.biomePalette_.isEmpty();
	}
	public CompoundTag getBlockStateAt(int blockX, int blockY, int blockZ) {
		int blockIndex = Section.getBlockIndexAt(blockX,blockY,blockZ);
		return this.blockPalette_.get(blockIndex);
	}
	public StringTag getBiomeAt(int blockX, int blockY, int blockZ) {
		if (this.biomePalette_ == null) { return null; }
		int biomeIndex = Section.getBiomeIndexAt(blockX, blockY, blockZ);
		return this.biomePalette_.get(biomeIndex);
	}
	public void setBlockStateAt(int blockX, int blockY, int blockZ, CompoundTag state) {
		this.blockPalette_.set(Section.getBlockIndexAt(blockX, blockY, blockZ), state);
	}
	public List<LocatedTag<CompoundTag>> getBlockLocations(Predicate<CompoundTag> checker) {
		return this.blockPalette_.indexedEntriesOf(checker).entrySet().stream()
				.map(e -> Section.locationFromIndex(16,16,e.getKey(),e.getValue()))
				.toList();
	}
	public List<LocatedTag<CompoundTag>> getBlockLocations(String blockName) {
		String name = (blockName.indexOf(':') < 0) ? "minecraft:" + blockName : blockName;
		return this.getBlockLocations(t -> t.getString("Name").equals(name));
	}


	//PRIVATE UTIL
	private <T extends Tag<?>> PaletteContainer<T> craftPaletteContainer(CompoundTag src, String name, int size, int minimumBitSize) {
		if (!src.containsKey(name)) { return null; }

		CompoundTag root = src.getCompoundTag(name);
		if (!root.containsKey("palette")) {
			throw new IllegalArgumentException("'" + name + "' property of " + this.height_ + " section is missing a 'palette' tag.");
		}

		List<T> palette = new LinkedList<>();
		root.getListTag("palette").forEach(t -> palette.add((T) t));

		if (root.containsKey("data")) {
			long[] data = root.getLongArrayTag("data").getValue();
			return new PaletteContainer<>(palette, size, minimumBitSize, data);
		}
		return new PaletteContainer<>(palette.get(0), size, minimumBitSize);
	}
	private static int getBlockIndexAt(int x, int y, int z) {
		return y*256 + z*16 + x;
	}
	private static int getBiomeIndexAt(int x, int y, int z) {
		x = x >> 2; y = y >> 2; z = z >> 2;
		return y*16 + z*4 + x;
	}
	private static <T extends Tag<?>> LocatedTag<T> locationFromIndex(int xDimension, int zDimension, int index, T tag) {
		int yLayer = (xDimension * zDimension);
		int y = index / yLayer;
		index = index - (yLayer * y);
		int z = index / zDimension;
		index = index - (zDimension * z);
		int x = index;
		return new LocatedTag<>(x, y, z, tag);
	}
}
