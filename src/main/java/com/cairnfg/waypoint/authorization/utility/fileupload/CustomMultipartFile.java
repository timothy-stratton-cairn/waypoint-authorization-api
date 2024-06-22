package com.cairnfg.waypoint.authorization.utility.fileupload;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class CustomMultipartFile implements MultipartFile {

  private byte[] input;
  private String filename;

  @Override
  public String getName() {
    return filename;
  }

  @Override
  public String getOriginalFilename() {
    return filename;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public boolean isEmpty() {
    return input == null || input.length == 0;
  }

  @Override
  public long getSize() {
    return input.length;
  }

  @Override
  public byte[] getBytes() throws IOException {
    return input;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(input);
  }

  @Override
  public void transferTo(File destination) throws IOException, IllegalStateException {
    try (FileOutputStream fos = new FileOutputStream(destination)) {
      fos.write(input);
    }
  }
}
