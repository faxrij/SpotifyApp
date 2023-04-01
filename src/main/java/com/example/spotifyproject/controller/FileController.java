//package com.example.spotifyproject.controller;
//
//import com.example.spotifyproject.model.response.SpotifyFileResponse;
//import com.example.spotifyproject.service.FileService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@AllArgsConstructor
//@RestController
//@RequestMapping("/file")
//public class FileController {
//
//    private final FileService fileService;
//
//    @GetMapping("/{id}")
//    public SpotifyFileResponse getFile(@PathVariable String id) {
//        return fileService.getFile(id);
//    }
//
//    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public SpotifyFileResponse uploadFile(@RequestPart("file") MultipartFile file) {
//        return fileService.uploadFile(file);
//    }
//}
