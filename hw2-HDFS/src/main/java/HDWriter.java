import java.io.IOException;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDWriter {
  private static final int MAX_NUM = 2014 * 512;
  private static final short TIMES = 256;
  private static final int TOTAL = MAX_NUM * TIMES;
  private static final String OUTPUT_PATH = "hdfs://172.17.0.2:8080/user/user06";
  private static FileSystem fs = null;
  private static int threadNo = 1;
  private static int[] data;

  public void writeFromMemory(int threadNo, int[] data) {
    this.data = data;

    Configuration conf = new Configuration();
    conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
    conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
    this.fs = null;
    try {
      fs = FileSystem.get(URI.create(OUTPUT_PATH), conf, "user06");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    HDWriter writer = new HDWriter();
    for (int current = 0; current < threadNo; current++) {
      new Thread(writer.new MemoryConsumer(current)).start();
    }
  }

  class MemoryConsumer implements Runnable {
    private int currentNo;

    public MemoryConsumer(int currentNo) {
      this.currentNo = currentNo;
    }

    @Override
    public void run() {
      long costTime = -1;
      long startTime = System.currentTimeMillis();

      FSDataOutputStream output= null;
      try {
        output = fs.create(new Path(OUTPUT_PATH + "/writeFromMemory" + currentNo));
        for (int i = MAX_NUM / threadNo; i < MAX_NUM / threadNo; i++) {
          for (int j = 0; j < TIMES; j++) {
            output.write(intToByteArray(data[i+j]));
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      long endTime = System.currentTimeMillis();
      costTime = endTime - startTime;
      System.out.println("writeFromMemory Thread_" + currentNo +  " costs: " + costTime);
    }
  }

  public void writeFromDisk(int threadNo) {
    Configuration conf = new Configuration();
    conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
    conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
    this.fs = null;
    try {
      fs = FileSystem.get(URI.create(OUTPUT_PATH), conf, "user06");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    HDWriter writer = new HDWriter();
    for (int current = 0; current < threadNo; current++) {
      new Thread(writer.new DiskConsumer(current)).start();
    }
  }

  class DiskConsumer implements Runnable {
    private int currentNo;

    public DiskConsumer(int currentNo) {
      this.currentNo = currentNo;
    }

    @Override
    public void run() {
      long costTime = -1;
      long startTime = System.currentTimeMillis();

      int bytesRead;
      byte[] buffer = new byte[TOTAL * 4 / threadNo];
      FSDataInputStream input = null;
      FSDataOutputStream output= null;
      try {
        output = fs.create(new Path(OUTPUT_PATH + "/writeFromDisk" + currentNo));
        input = fs.open(new Path(OUTPUT_PATH + "/writeFromMemory" + currentNo));

        while ((bytesRead = input.read(buffer)) > 0 ) {
          output.write(buffer, 0, bytesRead);
        }

      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          input.close();
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      long endTime = System.currentTimeMillis();
      costTime = endTime - startTime;
      System.out.println("writeFromDisk Thread_" + currentNo +  " costs: " + costTime);
    }
  }

  public static byte[] intToByteArray(int i) {
    byte[] result = new byte[4];
    result[0] = (byte) ((i >> 24) & 0xFF);
    result[1] = (byte) ((i >> 16) & 0xFF);
    result[2] = (byte) ((i >> 8) & 0xFF);
    result[3] = (byte) (i & 0xFF);
    return result;
  }
}

