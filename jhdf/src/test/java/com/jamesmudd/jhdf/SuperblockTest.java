package com.jamesmudd.jhdf;

import static com.jamesmudd.jhdf.Utils.UNDEFINED_ADDRESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jamesmudd.jhdf.exceptions.HdfException;

public class SuperblockTest {
  private FileChannel fc;
  private RandomAccessFile raf;

  @Before
  public void setUp() throws FileNotFoundException {
    final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
    raf = new RandomAccessFile(new File(testFileUrl), "r");
    fc = raf.getChannel();
  }

  @After
  public void after() throws IOException {
    raf.close();
    fc.close();
  }

  @Test
  public void testExtractSuperblockFromFile() throws IOException {
    Superblock sb = new Superblock(raf, 0);
    assertThat(sb.getVersionOfSuperblock(), is(equalTo(0)));
    assertThat(sb.getVersionNumberOfTheFileFreeSpaceInformation(), is(equalTo(0)));
    assertThat(sb.getVersionOfRootGroupSymbolTableEntry(), is(equalTo(0)));
    assertThat(sb.getVersionOfSharedHeaderMessageFormat(), is(equalTo(0)));
    assertThat(sb.getSizeOfOffsets(), is(equalTo(8)));
    assertThat(sb.getSizeOfLengths(), is(equalTo(8)));
    assertThat(sb.getGroupLeafNodeK(), is(equalTo(4)));
    assertThat(sb.getGroupInternalNodeK(), is(equalTo(16)));
    assertThat(sb.getBaseAddressByte(), is(equalTo(0L)));
    assertThat(sb.getAddressOfGlobalFreeSpaceIndex(), is(equalTo(UNDEFINED_ADDRESS)));
    assertThat(sb.getEndOfFileAddress(), is(equalTo(raf.length())));
    assertThat(sb.getRootGroupSymbolTableAddress(), is(equalTo(56L)));
  }

  @Test
  public void testVerifySuperblock() throws Exception {
    assertThat(Superblock.verifySignature(raf, 0), is(true));
  }

  @Test
  public void testVerifySuperblockReturnsFalseWhenNotCorrect() throws Exception {
    assertThat(Superblock.verifySignature(raf, 3), is(false));
  }

  @Test(expected = HdfException.class)
  public void testSuperblockCOnstructorThrowsWhenGivenInvalidOffset() throws Exception {
    new Superblock(raf, 5);
  }
}
