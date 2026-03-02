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
        }

        setBucketPublicPolicy(bucketName);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new FileStorageException("Error uploading file.");
        } catch (com.amazonaws.AmazonServiceException e) {
            throw new FileStorageException("Communication error with the storage service.");
        }
    }

    @Override
    public void deleteFile(@NotNull String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3Client.deleteObject(bucketName, fileName);
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
        } catch (Exception e) {
            log.error("Warning: Could not set public policy for bucket {}", bucketName);
        }
    }

}
