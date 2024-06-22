package com.cairnfg.waypoint.authorization.utility.fileupload;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class S3FileUpload implements FileUpload {

  private final AmazonS3 s3Client;

  @Value("${waypoint.authorization.s3.bucket}")
  private String bucketName;

  public S3FileUpload(AmazonS3 s3Client) {
    this.s3Client = s3Client;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  public String uploadFile(MultipartFile multipartFile, String uploader, String baseKey)
      throws IOException {
    // convert multipart file  to a file
    File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      fileOutputStream.write(multipartFile.getBytes());
    }

    // generate file name
    String fileName = generateFileName(multipartFile, uploader, baseKey);

    // upload file
    PutObjectRequest request = new PutObjectRequest(this.bucketName, fileName, file);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(
        "plain/" + FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
    metadata.addUserMetadata("Title", "File Upload - " + fileName);
    metadata.setContentLength(file.length());
    request.setMetadata(metadata);
    s3Client.putObject(request);

    // delete file
    file.delete();

    return fileName;
  }

  @Override
  public Object downloadFile(String fileName) throws IOException {
    S3Object object = s3Client.getObject(bucketName, fileName);
    try (S3ObjectInputStream s3is = object.getObjectContent()) {
      File tempFile = File.createTempFile(fileName, FilenameUtils.getExtension(fileName));
      try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile, false)) {
        byte[] readBuf = new byte[1024];
        int readLen;
        while ((readLen = s3is.read(readBuf)) > 0) {
          fileOutputStream.write(readBuf, 0, readLen);
        }
      }
      Resource resource = new UrlResource(tempFile.toPath().toUri());

      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("Could not read file " + fileName);
      }
    }
  }

  @Override
  public boolean delete(String fileName) {
    return false;
  }

  private String generateFileName(MultipartFile multiPart, String uploader, String baseKey) {
    return baseKey + new Date().getTime() + "-"
        + uploader + "-"
        + Objects.requireNonNull(multiPart.getOriginalFilename()).replace(" ", "_");
  }

}
