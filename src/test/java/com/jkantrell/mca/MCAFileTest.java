package com.jkantrell.mca;

import com.jkantrell.nbt.tag.CompoundTag;
import com.jkantrell.nbt.tag.ListTag;
import com.jkantrell.nbt.tag.StringTag;
import com.jkantrell.nbt.tag.Tag;

import static com.jkantrell.mca.LoadFlags.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class MCAFileTest extends MCATestCase {

	public void testGetChunkIndex() {
		assertEquals(0, MCAFile.getChunkIndex(0, 0));
		assertEquals(-1, MCAFile.getChunkIndex(32, 32));
		assertEquals(-1, MCAFile.getChunkIndex(0, 32));
		assertEquals(1023, MCAFile.getChunkIndex(31, 31));
		assertEquals(-1, MCAFile.getChunkIndex(63, 63));
		int i = 0;
		for (int cz = 0; cz < 32; cz++) {
			for (int cx = 0; cx < 32; cx++) {
				assertEquals(i++, MCAFile.getChunkIndex(cx, cz));
			}
		}
	}

	public void testChangeData() {
		MCAFile mcaFile = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.2.2.mca")));
		assertNotNull(mcaFile);
		mcaFile.setChunk(0, null);
		File tmpFile = getNewTmpFile("r.2.2.mca");
		Integer x = assertThrowsNoException(() -> MCAUtil.write(mcaFile, tmpFile, true));
		assertNotNull(x);
		assertEquals(2, x.intValue());
		MCAFile again = assertThrowsNoException(() -> MCAUtil.read(tmpFile));
		assertNotNull(again);
	}

	public void testChangeLastUpdate() {
		MCAFile from = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.2.2.mca")));
		assertNotNull(from);
		File tmpFile = getNewTmpFile("r.2.2.mca");
		assertThrowsNoException(() -> MCAUtil.write(from, tmpFile, true));
		MCAFile to = assertThrowsNoException(() -> MCAUtil.read(tmpFile));
		assertNotNull(to);
	}

	private Chunk createChunkWithPos() {
		CompoundTag data = new CompoundTag();
		CompoundTag level = new CompoundTag();
		data.put("Level", level);
		return new Chunk(data);
	}

	public void testSetters() {
		MCAFile f = new MCAFile(2, 2);

		assertThrowsNoRuntimeException(() -> f.setChunk(0, createChunkWithPos()));
		assertEquals(createChunkWithPos().updateHandle(64, 64), f.getChunk(0, 0).updateHandle(64, 64));
		assertThrowsRuntimeException(() -> f.setChunk(1024, createChunkWithPos()), IndexOutOfBoundsException.class);
		assertThrowsNoRuntimeException(() -> f.setChunk(1023, createChunkWithPos()));
		assertThrowsNoRuntimeException(() -> f.setChunk(0, null));
		assertNull(f.getChunk(0, 0));
		assertThrowsNoRuntimeException(() -> f.setChunk(1023, createChunkWithPos()));
		assertThrowsNoRuntimeException(() -> f.setChunk(1023, createChunkWithPos()));
	}

	public void testGetBiomeAt() {
		MCAFile f = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.0.0.mca")));
		assertBiome("minecraft:taiga", f.getBiomeAt(0, -64, 0));
		assertBiome("minecraft:dripstone_caves", f.getBiomeAt(216, 28, 276));
		assertBiome("minecraft:river", f.getBiomeAt(78, 63, 363));
		assertBiome("minecraft:meadow", f.getBiomeAt(347, 125, 316));
	}

	public void testSetBiomeAt() {
		//To be updated
	}

	public void testSetBlockDataAt() {
		MCAFile f = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.0.0.mca")));
		assertFalse("minecraft:custom".equals(f.getBlockStateAt(0,0,0).getString("Name")));
		f.setBlockStateAt(0, 0, 0, block("minecraft:custom"));
		assertBlock("minecraft:custom", f.getBlockStateAt(0,0,0));
	}

	public void testGetBlockDataAt() {
		MCAFile f = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.0.0.mca")));
		assertBlock("minecraft:bedrock", f.getBlockStateAt(0, -64, 0));
		assertNull(f.getBlockStateAt(512, 0, 0));
		assertBlock("minecraft:lightning_rod", f.getBlockStateAt(15, 111, 465));
		assertBlock("minecraft:birch_log", f.getBlockStateAt(202, 82, 318));
		assertBlock("minecraft:pointed_dripstone", f.getBlockStateAt(141, 7, 272));
		assertNull(f.getBlockStateAt(3, 3, 512));
	}

	public void testGetChunkStatus() {
		MCAFile f = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.0.0.mca")));
		assertEquals("full", f.getChunk(0, 0).getStatus());
	}

	public void testSetChunkStatus() {
		MCAFile f = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.2.2.mca")));
		assertThrowsNoRuntimeException(() -> f.getChunk(0, 0).setStatus("base"));
		assertEquals("base", f.getChunk(0, 0).updateHandle(64, 64).getCompoundTag("Level").getString("Status"));
		assertNull(f.getChunk(1, 0));
	}

	public void testChunkInitReferences() {
		CompoundTag t = new CompoundTag();
		assertThrowsRuntimeException(() -> new Chunk(null), NullPointerException.class);
	}

	public void testChunkInvalidCompressionType() {
		assertThrowsException(() -> {
			try (RandomAccessFile raf = new RandomAccessFile(getResourceFile("invalid_compression.dat"), "r")) {
				Chunk c = new Chunk(0);
				c.deserialize(raf);
			}
		}, IOException.class);
	}

	public void testChunkInvalidDataTag() {
		assertThrowsException(() -> {
			try (RandomAccessFile raf = new RandomAccessFile(getResourceFile("invalid_data_tag.dat"), "r")) {
				Chunk c = new Chunk(0);
				c.deserialize(raf);
			}
		}, IOException.class);
	}

	public void testLocationsOf() {
		MCAFile f = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.0.0.mca")));
		Chunk chunk = f.getChunk(0,4);
		List<LocatedTag<CompoundTag>> list = chunk.locationsOf("minecraft:birch_log");
		assertEquals(19,list.size());
		chunk = f.getChunk(0,31);
		list = chunk.locationsOf("jack_o_lantern");
		assertEquals(1,list.size());
		assertEquals(14 ,list.get(0).x());
		assertEquals(65 ,list.get(0).y());
		assertEquals(1 ,list.get(0).z());
		chunk = f.getChunk(10,30);
		list = chunk.locationsOf("oak_stairs");
		assertEquals(10,list.size());
		assertTrue(list.stream().anyMatch(l -> l.x() == 0 && l.y() == 124 && l.z() == 8));
	}

	public void testPoss() {
		MCAFile f = assertThrowsNoException(() -> MCAUtil.read(copyResourceToTmp("r.0.0.mca")));
		int[][] poss = { {5, 3}, {6, 7}, {8, 15}, {11, 2}, {0, 25}};
		for (int[] p : poss) {
			Chunk chunk = f.getChunk(p[0], p[1]);
			assertEquals(p[0], chunk.getX());
			assertEquals(p[1], chunk.getZ());
		}
	}

	private void assertLoadFLag(Object field, long flags, long wantedFlag) {
		if((flags & wantedFlag) != 0) {
			assertNotNull(String.format("Should not be null. Flags=%08x, Wanted flag=%08x", flags, wantedFlag), field);
		} else {
			assertNull(String.format("Should be null. Flags=%08x, Wanted flag=%08x", flags, wantedFlag), field);
		}
	}

	private void assertPartialChunk(Chunk c, long loadFlags) {
		assertLoadFLag(c.getHeightMaps(), loadFlags, HEIGHTMAPS);
		assertLoadFLag(c.getEntities(), loadFlags, ENTITIES);
		assertLoadFLag(c.getCarvingMasks(), loadFlags, CARVING_MASKS);
		assertLoadFLag(c.getLights(), loadFlags, LIGHTS);
		assertLoadFLag(c.getPostProcessing(), loadFlags, POST_PROCESSING);
		assertLoadFLag(c.getLiquidTicks(), loadFlags, FLUID_TICKS);
		assertLoadFLag(c.getLiquidsToBeTicked(), loadFlags, LIQUIDS_TO_BE_TICKED);
		assertLoadFLag(c.getTileTicks(), loadFlags, BLOCK_TICKS);
		assertLoadFLag(c.getTileEntities(), loadFlags, TILE_ENTITIES);
		assertLoadFLag(c.getToBeTicked(), loadFlags, TO_BE_TICKED);
		assertLoadFLag(c.getSection(0), loadFlags, BLOCK_LIGHTS|BLOCK_STATES|SKY_LIGHT);
	}

	public void assertBlock(String name, CompoundTag block) {
		assertTrue(block.containsKey("Name"));
		String blockName = block.getString("Name");
		assertNotNull(blockName);
		assertEquals(name, blockName);
	}

	public void assertBiome(String name, StringTag biome) {
		assertNotNull(biome);
		assertEquals(name, biome.getValue());
	}
}
