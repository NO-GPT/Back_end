package com.example.new_portfolio_server.board;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

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
}
