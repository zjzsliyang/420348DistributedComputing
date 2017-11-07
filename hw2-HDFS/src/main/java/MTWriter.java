import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MTWriter {

  public static void multiThreadWriter(String fileName, int range, int times, int threadNo) throws FileNotFoundException {
    try {
      final RandomAccessFile raf = new RandomAccessFile(fileName, "r");
      Runnable[] threadPool = new Runnable[threadNo];
      for (int i = 0; i < threadNo; i++) {
        int cnt = i;
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            while (true) {
              int len = (int)(Math.ceil((double)range * (double)times / (double)threadNo));
              byte[] b = new byte[len];
              try {
                synchronized (raf) {
                  raf.seek(cnt * len);
                  raf.read(b);
                }
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        };
        threadPool[i] = runnable;
      }

    } catch (FileNotFoundException e) {
      throw e;
    }
  }
}
