package com.groupware.service;

import com.groupware.dto.BoardFileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path directory;
    public FileStorageService(@Value("${app.upload-dir}") String directory) {
        this.directory=Path.of(directory).toAbsolutePath().normalize();
    }
    @PostConstruct
    public void prepare() throws IOException {
        Files.createDirectories(directory);
        Path demo=path("demo-guide.txt");
        if(!Files.exists(demo)) {
            try(InputStream source=getClass().getResourceAsStream("/demo/demo-guide.txt")) {
                if(source!=null) Files.copy(source,demo);
            }
        }
    }
    public Path path(String savedName) {
        if(savedName==null) throw new IllegalArgumentException("파일 이름이 없습니다.");
        Path target=directory.resolve(savedName).normalize();
        if(!target.getParent().equals(directory)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        return target;
    }
    public BoardFileDTO store(MultipartFile file) throws IOException {
        String original=file.getOriginalFilename();
        if(original==null || original.isBlank()) original="attachment";
        original=original.replace('\\','/');
        original=original.substring(original.lastIndexOf('/')+1).replaceAll("[\\r\\n\\x00]", "");
        if(original.length()>200) throw new IllegalArgumentException("파일 이름이 너무 깁니다.");
        String saved=UUID.randomUUID()+"_"+original;
        try(InputStream input=file.getInputStream()) { Files.copy(input,path(saved)); }
        BoardFileDTO dto=new BoardFileDTO();
        dto.setOrgName(original);dto.setSavedName(saved);dto.setFileSize(file.getSize());return dto;
    }
    public void delete(String savedName) throws IOException { Files.deleteIfExists(path(savedName)); }
}
