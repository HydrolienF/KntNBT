package com.jkantrell.mca;

import com.jkantrell.nbt.tag.Tag;

public record LocatedTag<T extends Tag<?>>(int x, int y, int z, T tag) {
}
