package com.example.new_portfolio_server.fileUpload;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.channels.MulticastChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository repository;

    // 파일 업로드
    public List<File> saveFiles(MultipartFile[] files) throws Exception{
        List<File> saved = new ArrayList<>();

        for(MultipartFile file : files){
            File attachment = new File();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setSize(file.getSize());
            attachment.setData(file.getBytes());

            saved.add(repository.save(attachment));
        }

        return saved;
    }

    // 부분 조회
    public Optional<File> getFile(Long id) {
        return repository.findById(id);
    }

    // 전체 조회
    public List<File> getAllFiles() {
        return repository.findAll();
    }

    // 삭제
    public void deleteFile(Long id) {
        repository.deleteById(id);
    }
}
