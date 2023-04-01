//package com.example.spotifyproject.service.client;
//
//import java.io.File;
//
//import com.example.spotifyproject.config.CdnConfig;
//import lombok.AllArgsConstructor;
//import lombok.SneakyThrows;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.UUID;
//
//@Service
//@AllArgsConstructor
//public class UploadClient {
//
//    private final CdnConfig cdnConfig;
//    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
//
//    @SneakyThrows
//    public String upload(MultipartFile multipartFile) {
//        // Sort and save the uploaded files by date in the uploadPath folder
//        // For example: /2019/06/06/cf13891e-4b95-4000-81eb-b6d70ae44930.png
//        String format = sdf.format(new Date());
//        File folder = new File(cdnConfig.getUploadPath());
//        if (!folder.isDirectory()) {
//            folder.mkdirs();
//        }
//
//        // Rename the uploaded file to avoid the same file name
//        String oldName = multipartFile.getOriginalFilename();
//        assert oldName != null;
//        String newName = UUID.randomUUID() + oldName.substring(oldName.lastIndexOf("."));
//
//        // File save
//        multipartFile.transferTo(new File(folder, newName));
//
//        // Return the access path of the uploaded file
//        return cdnConfig.getHost() + format + newName;
//    }
//
//}