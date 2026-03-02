package github.maxsuel.agregadordeinvestimentos.service.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import github.maxsuel.agregadordeinvestimentos.exceptions.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {
        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(bucketName);
            setBucketPublicPolicy(bucketName);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

            String rawUrl = s3Client.getUrl(bucketName, fileName).toString();

            return rawUrl.replace("minio:9000", "localhost:9000");

        } catch (IOException e) {
            log.error("Failed to read file input stream", e);
            throw new FileStorageException("Error uploading file.");
        } catch (com.amazonaws.AmazonServiceException e) {
            log.error("AWS S3 communication error.", e);
            throw new FileStorageException("Communication error with the storage service.");
        }
    }

    @Override
    public void deleteFile(@NotNull String fileUrl) {
        if (fileUrl.isBlank()) return;

        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            s3Client.deleteObject(bucketName, fileName);
            log.info("File deleted from S3: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileUrl, e);
        }
    }

    private void setBucketPublicPolicy(String bucketName) {
        String policyJson = """
        {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Effect": "Allow",
                    "Principal": "*",
                    "Action": ["s3:GetObject"],
                    "Resource": ["arn:aws:s3:::%s/*"]
                }
            ]
        }
        """.formatted(bucketName);

        try {
            s3Client.setBucketPolicy(bucketName, policyJson);
            log.info("Public policy set for bucket: {}", bucketName);
        } catch (Exception e) {
            log.error("Warning: Could not set public policy for bucket {}", bucketName);
        }

    }

}
