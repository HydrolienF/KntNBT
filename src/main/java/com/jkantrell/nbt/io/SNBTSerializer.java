package com.jkantrell.nbt.io;

import com.jkantrell.io.StringSerializer;
import com.jkantrell.nbt.tag.Tag;

import java.io.IOException;
import java.io.Writer;

public class SNBTSerializer implements StringSerializer<Tag<?>> {

	@Override
	public void toWriter(Tag<?> tag, Writer writer) throws IOException {
		SNBTWriter.write(tag, writer);
	}

	public void toWriter(Tag<?> tag, Writer writer, int maxDepth) throws IOException {
		SNBTWriter.write(tag, writer, maxDepth);
	}
}
