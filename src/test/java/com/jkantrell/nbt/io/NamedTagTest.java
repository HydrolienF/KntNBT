package com.jkantrell.nbt.io;

import com.jkantrell.NBTTestCase;
import com.jkantrell.nbt.tag.ByteTag;
import com.jkantrell.nbt.tag.ShortTag;

public class NamedTagTest extends NBTTestCase {

	public void testCreate() {
		ByteTag t = new ByteTag();
		NamedTag n = new NamedTag("name", t);
		assertEquals("name", n.getName());
		assertTrue(n.getTag() == t);
	}

	public void testSet() {
		ByteTag t = new ByteTag();
		NamedTag n = new NamedTag("name", t);
		n.setName("blah");
		assertEquals("blah", n.getName());
		ShortTag s = new ShortTag();
		n.setTag(s);
		assertTrue(n.getTag() == s);
	}
}
