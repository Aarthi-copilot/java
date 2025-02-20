package com.cg.file_viewer.service;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OutlookMsgReaderService {
    public Map<String, Object> readMsgFile(String filePath) {
        Map<String, Object> emailData = new HashMap<>();

        try {
            FileInputStream fis = new FileInputStream(new File(filePath));
            MAPIMessage msg = new MAPIMessage(fis);

            // Extract Email Details
            emailData.put("subject", msg.getSubject());
            emailData.put("from", msg.getDisplayFrom());
            emailData.put("to", msg.getDisplayTo());
            emailData.put("cc", msg.getDisplayCC());
            emailData.put("bcc", msg.getDisplayBCC());

            // Get both Plain Text and HTML versions
            /*String plainTextBody = msg.getTextBody();
            System.out.println(plainTextBody);*/

            System.out.println(111111111);
            /*String htmlBody = msg.getHtmlBody();
            System.out.println(htmlBody);
            System.out.println(2222222);

            if (htmlBody != null && !htmlBody.isEmpty()) {
                emailData.put("body", htmlBody);  // Prefer HTML if available
            } else {
                emailData.put("body", plainTextBody);  // Fallback to plain text
            }
*/
            emailData.put("body", msg.getTextBody()); // You can also use msg.getHtmlBody()

            // Extract Attachments
            List<String> attachments = new ArrayList<>();
            if (msg.getAttachmentFiles() != null) {
                for (AttachmentChunks attachment : msg.getAttachmentFiles()) {
                    if (attachment.getAttachLongFileName() != null) {
                        attachments.add(attachment.getAttachLongFileName().getValue());
                    }
                }
            }
            emailData.put("attachments", attachments);

        } catch (Exception e) {
            emailData.put("error", "Failed to read MSG file: " + e.getMessage());
        }

        return emailData;
    }
}
