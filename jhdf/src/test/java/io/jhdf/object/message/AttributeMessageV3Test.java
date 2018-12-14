package io.jhdf.object.message;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.jhdf.Superblock;
import io.jhdf.object.message.AttributeMessage;

public class AttributeMessageV3Test {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;
	private ByteBuffer bb;

	@Before
	public void setUp() throws IOException {
		final String testFileUrl = this.getClass().getResource("../../test_file2.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
		bb = fc.map(READ_ONLY, 270, 65);
		bb.order(LITTLE_ENDIAN);
	}

	@After
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void test() throws CharacterCodingException {
		AttributeMessage am = new AttributeMessage(bb, sb);
		assertThat(am.getVersion(), is(equalTo(3)));
		assertThat(am.getName(), is(equalTo("string_attr")));
		assertThat(am.getDataType().getDataClass(), is(equalTo(9)));
		assertThat(am.getDataSpace().getTotalLentgh(), is(equalTo(1)));
		assertThat(am.getData().capacity(), is(equalTo(20)));

		String test = StandardCharsets.UTF_8.newDecoder().decode(am.getData()).toString();
	}

}
