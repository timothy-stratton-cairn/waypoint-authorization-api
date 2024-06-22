package com.cairnfg.waypoint.authorization.utility.fileupload;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileUpload {

  String uploadFile(MultipartFile multipartFile, String uploader, String baseKey)
      throws IOException;

  Object downloadFile(String fileName) throws IOException;

  boolean delete(String fileName);
}
