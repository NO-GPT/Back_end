package com.example.new_portfolio_server.fileUpload;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image_upload")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 파일 식별

    @Column(name = "file_name")
    private String fileName; // 파일 이름

    @Column(name = "contentType")
    private String contentType; // 파일 확장자

    @Column(name = "img_size")
    private Long size; // 파일 사이즈

    @Lob
    @Column(columnDefinition = "LONGBLOB", name = "img_data")
    private byte[] data; // 파일 정보(byte 형식)

}
