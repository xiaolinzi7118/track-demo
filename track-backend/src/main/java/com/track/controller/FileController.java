package com.track.controller;

import com.track.common.Result;
import com.track.service.FileAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileAssetService fileAssetService;

    @PostMapping("/upload-image")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        return fileAssetService.uploadImage(file);
    }

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> preview(@PathVariable String fileId) {
        return fileAssetService.preview(fileId);
    }
}
