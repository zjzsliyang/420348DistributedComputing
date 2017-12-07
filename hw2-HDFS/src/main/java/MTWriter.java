import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class MTWriter {

  private static final int MAX_NUM = 2014 * 512;
  private static final short TIMES = 256;
  private static final int TOTAL = MAX_NUM * TIMES;
  private static final String INPUT_PATH = "data_generation.txt";
  private static final String OUTPUT_PATH = "data_output.txt";
  private static int threadNo = 1;
  private static int[] data;


  public void produceWhileWrite(int threadNo) {
    MTWriter writer = new MTWriter();
    for (int currentNo = 0; currentNo < threadNo; currentNo++) {
      new Thread(writer.new ProduceConsumer(currentNo)).start();
    }
  }

  class ProduceConsumer implements Runnable {
    private int currentNo;

    public ProduceConsumer(int currentNo) {
      this.currentNo = currentNo;
    }

    @Override
    public void run() {
      long costTime = -1;
      long startTime = System.currentTimeMillis();

      RandomAccessFile os = null;
      FileChannel fo = null;
      try {
        os = new RandomAccessFile(OUTPUT_PATH, "rw");
        fo = os.getChannel();
        try {
          IntBuffer oIb = fo.map(MapMode.READ_WRITE, TOTAL * 4 / threadNo * currentNo, TOTAL * 4 / threadNo).asIntBuffer();
          for (int i = MAX_NUM / threadNo * currentNo; i < MAX_NUM / threadNo; i++) {
            for (int j = 0; j < TIMES; j++) {
              oIb.put(i);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();;
      } finally {
        try {
          fo.close();
          os.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      long endTime = System.currentTimeMillis();
      costTime = endTime - startTime;
      System.out.println("produceWhileWrite Thread_" + currentNo +  " costs: " + costTime);
    }
  }


  public void writeFromDisk(int threadNo) {
    this.threadNo = threadNo;

    MTWriter writer = new MTWriter();
    for (int currentNo = 0; currentNo < threadNo; currentNo++) {
      new Thread(writer.new DiskConsumer(currentNo)).start();
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

      FileInputStream is = null;
      RandomAccessFile os = null;
      FileChannel fi = null;
      FileChannel fo = null;
      try {
        is = new FileInputStream(INPUT_PATH);
        os = new RandomAccessFile(OUTPUT_PATH, "rw");
        fi = is.getChannel();
        fo = os.getChannel();
        try {
          IntBuffer iIb = fi.map(MapMode.READ_ONLY, TOTAL * 4 / threadNo * currentNo, TOTAL * 4 / threadNo).asIntBuffer();
          IntBuffer oIb = fo.map(MapMode.READ_WRITE, TOTAL * 4 / threadNo * currentNo, TOTAL * 4 / threadNo).asIntBuffer();
          int[] tmp = new int[TOTAL / threadNo];
          while (iIb.hasRemaining()) {
            int read = iIb.get();
            oIb.put(read);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();;
      } finally {
        try {
          fi.close();
          fo.close();
          is.close();
          os.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      long endTime = System.currentTimeMillis();
      costTime = endTime - startTime;
      System.out.println("writeFromDisk Thread_" + currentNo +  " costs: " + costTime);
    }
  }


  public void writeFromMemory(int threadNo, int[] data) {
    this.data = data;
    this.threadNo = threadNo;

    MTWriter writer = new MTWriter();
    for (int currentNo = 0; currentNo < threadNo; currentNo++) {
      new Thread(writer.new MemoryConsumer(currentNo)).start();
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

      RandomAccessFile os = null;
      FileChannel fo = null;
      try {
        os = new RandomAccessFile(OUTPUT_PATH, "rw");
        fo = os.getChannel();
        try {
          IntBuffer oIb = fo.map(MapMode.READ_WRITE, TOTAL * 4 / threadNo * currentNo, TOTAL * 4 / threadNo).asIntBuffer();
          for (int i = MAX_NUM / threadNo * currentNo; i < MAX_NUM / threadNo; i++) {
            for (int j = 0; j < TIMES; j++) {
              oIb.put(data[i+j]);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();;
      } finally {
        try {
          fo.close();
          os.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      long endTime = System.currentTimeMillis();
      costTime = endTime - startTime;
      System.out.println("writeFromMemory Thread_" + currentNo +  " costs: " + costTime);
    }
  }

}
