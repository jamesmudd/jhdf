package com.jamesmudd.jhdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.nio.ByteBuffer;
import org.junit.Test;

public class UtilsTest {

  @Test
  public void testToHex() {
    assertThat(Utils.toHex(88), is(equalTo("0x58")));
  }

  @Test
  public void testToHexWithUndefinedAddress() {
    assertThat(Utils.toHex(Utils.UNDEFINED_ADDRESS), is(equalTo("UNDEFINED")));
  }

  @Test
  public void testReadUntilNull() throws Exception {
    ByteBuffer bb = ByteBuffer.allocate(4);
    byte[] b = new byte[] {'H', 'D', 'F', Utils.NULL};
    bb.put(b);
    bb.rewind();
    assertThat(Utils.readUntilNull(bb), is(equalTo("HDF")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadUntilNullThrowsIfNoNullIsFound() throws Exception {
    ByteBuffer bb = ByteBuffer.allocate(3);
    byte[] b = new byte[] {'H', 'D', 'F'};
    bb.put(b);
    bb.rewind();
    Utils.readUntilNull(bb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadUntilNullThrowsIfNonAlphanumericCharacterIsSeen() throws Exception {
    ByteBuffer bb = ByteBuffer.allocate(3);
    byte[] b = new byte[] {'H', 'D', ' '};
    bb.put(b);
    bb.rewind();
    Utils.readUntilNull(bb);
  }

  @Test
  public void testValidNameReturnsTrue() throws Exception {
    assertThat(Utils.validateName("hello"), is(true));
  }

  @Test
  public void testNameContainingDotIsInvalid() throws Exception {
    assertThat(Utils.validateName("hello."), is(false));
  }

  @Test
  public void testNameContainingSlashIsInvalid() throws Exception {
    assertThat(Utils.validateName("hello/"), is(false));
  }

  @Test
  public void testNameContainingNonAsciiIsInvalid() throws Exception {
    assertThat(Utils.validateName("helloμ"), is(false));
  }
}
