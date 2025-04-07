package com.example.new_portfolio_server.fileUpload;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<List<File>> uploadFiles(@RequestParam("files") MultipartFile[] files){
        try{
            List<File> saved = fileService.saveFiles(files);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 부분 조회
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable("id") Long id) {
        return fileService.getFile(id)
                .map(file -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                        .contentType(MediaType.parseMediaType(file.getContentType()))
                        .body(file.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<File>> listAllFiles() {
        List<File> files = fileService.getAllFiles();

        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable("id") Long id){
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }


}
