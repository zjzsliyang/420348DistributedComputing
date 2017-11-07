import com.sun.xml.internal.ws.server.ServerRtException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */

public class App {
  static String producerFileName = "producer.txt";
  static String MultiOutputFileName = "multi-output.txt";
  static String HdfsOutputFileName = "hads-output.txt";
  static String CassandraOutputFileName = "cassandra-output.txt";
  static int range = 2014 * 512;
  static int times = 256;

  public static void dataGeneration() {
    try {
      Producer.producer(range, times, fileName);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public static void multiWriter(String fileName) {
    if (fileName != null) {
      try {
        MTWriter.multiThreadWriter(producerFileName, range, times, 8);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  public static void hdfsWriter() {

  }

  public static void cassandraWriter() {

  }

  public static void main(String[] args) {
    dataGeneration();

  }
}