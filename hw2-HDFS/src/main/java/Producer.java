import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class Producer implements Runnable {

  private static final String OUTPUT_PATH = "data_generation.txt";
  private static final int MAX_NUM = 2014 * 512;
  private static final short TIMES = 256;
  private static final int TOTAL = MAX_NUM * TIMES;

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
        IntBuffer oIb = fo.map(MapMode.READ_WRITE, 0, TOTAL * 1000).asIntBuffer();
        for (int i = 1; i <= MAX_NUM; i++) {
          for (int j = 0; j < TIMES; j++) {
            oIb.put(i);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fo != null) {
          fo.close();
        }
        if (os != null) {
          os.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    long endTime = System.currentTimeMillis();
    costTime = endTime - startTime;
    System.out.println(costTime);
  }
}
