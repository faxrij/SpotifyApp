//package com.example.spotifyproject.service;
//
//import com.example.spotifyproject.entity.File;
//import com.example.spotifyproject.exception.BusinessException;
//import com.example.spotifyproject.exception.ErrorCode;
//import com.example.spotifyproject.model.response.SpotifyFileResponse;
//import com.example.spotifyproject.repository.FileRepository;
//import com.example.spotifyproject.service.client.UploadClient;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.transaction.Transactional;
//
//@Service
//@AllArgsConstructor
//@Transactional
//public class FileService {
//
//    private final FileRepository fileRepository;
//    private final UploadClient uploadClient;
//
//    public SpotifyFileResponse getFile(String id) {
//        File file = fileRepository.findById(id)
//                .orElseThrow(() -> new BusinessException(ErrorCode.resource_missing, "Resource does not exist!"));
//
//        return SpotifyFileResponse.fromEntity(file);
//    }
//
//    public SpotifyFileResponse uploadFile(MultipartFile multipartFile) {
//        String url = uploadClient.upload(multipartFile);
//
//        File file = new File();
//        file.setName(multipartFile.getOriginalFilename());
//        file.setUrl(url);
//        file.setContentType(multipartFile.getContentType());
//        fileRepository.save(file);
//
//        return SpotifyFileResponse.fromEntity(file);
//    }
//
//}
