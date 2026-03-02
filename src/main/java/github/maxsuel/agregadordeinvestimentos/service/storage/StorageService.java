package github.maxsuel.agregadordeinvestimentos.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadFile(MultipartFile file);
    void deleteFile(String fileUrl);

}
