package com.cg.file_viewer.controller;
import com.cg.file_viewer.service.OutlookMsgReaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/fileviewer")
@CrossOrigin(origins = "*")
public class MsgFileController {
    private final OutlookMsgReaderService msgReaderService;

    public MsgFileController(OutlookMsgReaderService msgReaderService) {
        this.msgReaderService = msgReaderService;
    }

    @GetMapping("/readmsgold")
    ResponseEntity<Map<String, Object>> readMsgOld(@RequestParam String filetype, @RequestParam String fileurl) {
        Map<String, Object> emailData = msgReaderService.readMsgFile(fileurl);
        if(!(filetype!=null && (filetype.equalsIgnoreCase("msg")
        ))) {
            System.out.println("fileType: " + filetype);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This file type is not supported");
        }
        System.out.println(filetype);
        System.out.println(fileurl);

        if (emailData.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailData);
        }
        return ResponseEntity.ok(emailData);
    }

    @GetMapping("/readmsg")
    ResponseEntity<String> readMsg(@RequestParam String filetype, @RequestParam String fileurl) {
        Map<String, Object> emailData = msgReaderService.readMsgFile(fileurl);
        if(!(filetype!=null && (filetype.equalsIgnoreCase("msg")
        ))) {
            System.out.println("fileType: " + filetype);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This file type is not supported");
        }
        System.out.println(filetype);
        System.out.println(fileurl);

        try {
            if (emailData.containsKey("error")) {
                String errorMessage = (String) emailData.get("error");
                String base64ErrorMessage = Base64.getEncoder().encodeToString(errorMessage.getBytes());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(base64ErrorMessage);
            }

            String jsonData = new ObjectMapper().writeValueAsString(emailData);
            String base64Data = Base64.getEncoder().encodeToString(jsonData.getBytes());
            return ResponseEntity.ok(base64Data);
        } catch (Exception e) {
            String errorMessage = "Error processing response: " + e.getMessage();
            String base64ErrorMessage = Base64.getEncoder().encodeToString(errorMessage.getBytes());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(base64ErrorMessage);
        }

    }
}
