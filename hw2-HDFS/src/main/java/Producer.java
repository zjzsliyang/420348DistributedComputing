import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Producer {

  public static void producer(int range, int times, String fileName)
      throws FileNotFoundException, UnsupportedEncodingException {
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(fileName, "UTF-8");
      for (int i = 0; i < range; i++) {
        for (int j = 0; j < times; j++) {
          writer.println(i + 1);
        }
      }
      writer.close();
    } catch (FileNotFoundException e) {
      throw e;
    } catch (UnsupportedEncodingException e) {
      throw e;
    }
  }
}
