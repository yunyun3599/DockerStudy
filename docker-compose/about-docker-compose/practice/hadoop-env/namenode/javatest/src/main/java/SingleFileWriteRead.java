import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class SingleFileWriteRead {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("input args: <filename>, <contents>");
            System.exit(2);
        }
        try {
            Configuration conf = new Configuration();
            FileSystem hdfs = FileSystem.get(conf);

            Path path = new Path(args[0]);
            if (hdfs.exists(path)) {
                hdfs.delete(path, true);
            }

            FSDataOutputStream outputStream = hdfs.create(path);
            outputStream.writeUTF(args[1]);
            outputStream.close();

            FSDataInputStream inputStream = hdfs.open(path);
            String inputString = inputStream.readUTF();
            inputStream.close();

            System.out.println("input string: " + inputString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
