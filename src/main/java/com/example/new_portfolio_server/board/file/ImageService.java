package com.example.new_portfolio_server.board.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.repsoitory.FileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final FileRepository fileRepository;

    @Autowired
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 단일
    public String uploadFile(MultipartFile multipartFile){
        if(multipartFile == null || multipartFile.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 파일입니다.");
        }

        return uploadToS3(multipartFile);
    }

    public List<String> uploadFile(List<MultipartFile> multipartFiles){
        List<String> fileNameList = new ArrayList<>();

        for(MultipartFile file : multipartFiles){
            if(file == null || file.isEmpty()){
                continue;
            }
            fileNameList.add(uploadToS3(file));
        }
        return fileNameList;
    }

    private String uploadToS3(MultipartFile file){
        String fileName = createFileName(file.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try(InputStream inputStream = file.getInputStream()){
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
        }
        catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        return fileName;
    }

    public ResponseEntity<byte[]> getObject(Long fileId) throws IOException{
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일이 존재하지 않습니다."));

        String fileKey = file.getFileUrl();

        S3Object o = amazonS3.getObject(new GetObjectRequest(bucket, fileKey));
        S3ObjectInputStream objectInputStream = o.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectInputStream);

        String fileName = URLEncoder.encode(fileKey, "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }

    // s3에 있는 파일 데이터 삭제
    public void deleteFile(String fileKey){
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
    }

    // 파일 이름
    private String createFileName(String fileName){
        return "2025-321-files-save/" + UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    // 파일 확장자 확인
    private String getFileExtension(String fileName){
        try{
            String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(); // 확장자 소문자로 통일
            List<String> allowExtensions = List.of(".jpg", ".jpeg", ".gif", ".png", ".pdf", ".svg");

            if(!allowExtensions.contains(extension)){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "허용되지 않은 파일 확장자 입니다. (" + extension + ")"
                );
            }
            return extension;
        }
        catch (StringIndexOutOfBoundsException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ")입니다.");
        }
    }

    // 파일 조회
    public ResponseEntity<Resource> getImageFile(String fileKey){
        String extension = fileKey.substring(fileKey.lastIndexOf(".")).toLowerCase();

        List<String> allowedImageExtension = List.of(".jpg", ".jpeg", ".gif", ".png", ".svg", ".pdf");
        if(!allowedImageExtension.contains(extension)){
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "이미지 파일만 가능합니다.");
        }

        String fileUrl = amazonS3.getUrl(bucket, fileKey).toString();

        Resource resource;
        try {
            resource = new UrlResource(fileUrl);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 리소스를 불러올 수 없습니다.");
        }

        MediaType mediaType = switch (extension){
            case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG;
            case ".png" -> MediaType.IMAGE_PNG;
            case ".gif" -> MediaType.IMAGE_GIF;
            case ".svg" -> MediaType.valueOf("image/svg+xml");
            case ".pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);

    }
}
