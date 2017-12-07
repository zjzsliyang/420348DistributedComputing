import java.nio.ByteBuffer;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class CAWriter {
  private static final int MAX_NUM = 2014 * 512;
  private static final short TIMES = 256;
  private static final int TOTAL = MAX_NUM * TIMES;
  private static final String ip = "148.100.92.158";
  private static final int port = 4392;
  private static final String keyspace = "keyspace_user06";
  private static int threadNo = 1;
  private static int[] data;
  private static TTransport tr;
  static Cassandra.Client client;

  private static void init() {
    tr = new TFramedTransport(new TSocket(ip, port));
    client = new Cassandra.Client(new TBinaryProtocol(tr));
    try {
      tr.open();
    } catch (TTransportException e) {
      e.printStackTrace();
    }
    try {
      client.set_keyspace(keyspace);
    } catch (TException e) {
      e.printStackTrace();
    }
  }

  public void writeFromMemry(int threadNo, int[] data) {
    this.data = data;
    init();
    CAWriter writer = new CAWriter();
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
      ColumnParent parent = new ColumnParent("thread_" + currentNo);
      long timestamp = System.currentTimeMillis();
      try {
        Column nameColumn = new Column(toByteBuffer("interger"));
        for (int i = MAX_NUM / threadNo; i < MAX_NUM / threadNo; i++) {
          for (int j = 0; j < TIMES; j++) {
            nameColumn.setValue(intToByteArray(data[i+j]));
            nameColumn.setTimestamp(timestamp);
            ByteBuffer nameColumnKey = toByteBuffer(i + "");
            client.insert(nameColumnKey, parent, nameColumn, ConsistencyLevel.ONE);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static ByteBuffer toByteBuffer(String value) throws Exception {
    return ByteBuffer.wrap(value.getBytes("UTF-8"));
  }

  public static String toString(ByteBuffer buffer) throws Exception {
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    return new String(bytes, "UTF-8");
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
