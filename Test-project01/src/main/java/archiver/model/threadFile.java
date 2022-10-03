package archiver.model;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class threadFile implements Runnable{
    private List<File> files;
    private File n;
    Map<File, String> fileMap = new HashMap<>();

    public threadFile (List<File> files, File n, Map<File, String> fileMap) {
        this.files = files;
        this.n = n;
        this.fileMap = fileMap;
    }

    @Override
    public void run() {
        files.forEach(file1 ->{
           ZipFile zipFile = new ZipFile(file1.getAbsolutePath());
          try {
              if(zipFile.isEncrypted()) {
                  zipFile = new ZipFile(file1.getAbsolutePath(), fileMap.get(file1).toCharArray());
              }
              zipFile.extractAll(n.getAbsolutePath());
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
        });

        return ;
    }


}
