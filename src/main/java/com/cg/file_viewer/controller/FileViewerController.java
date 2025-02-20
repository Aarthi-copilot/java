package com.cg.file_viewer.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;


@RestController
@RequestMapping("/fileviewer")
//@CrossOrigin(origins = "http://localhost:3000") // Adjust as needed
@CrossOrigin(origins = "*")
public class FileViewerController {


    private static final List<String> TRUSTED_DOMAINS = List.of(
            "https://file-examples.com",
            "https://example.com",
            "https://www.w3.org",
            "https://cdn-icons-png.flaticon.com",
            "https://w3.pppl.gov",
            "https://pixabay.com",
            "https://docs.google.com",
            "https://view.officeapps.live.com",
            "https://txt2html.sourceforge.net",
            "https://www.antennahouse.com",
            "https://www.tesourodireto.com.br",
            "https://i.imgur.com"
    );

    @GetMapping("/")
    public ResponseEntity<String> hello()
    {
        System.out.println("Hello---------------");
        return ResponseEntity.ok("Hello---------------");
    }

    @GetMapping("/hellofile")
    public ResponseEntity<String> helloFile()
    {
        System.out.println("Hello FileViewer---------------");
        return ResponseEntity.ok("Hello FileViewer---------------");
    }
    @GetMapping("/file_old")
    public ResponseEntity<Resource> getFileOld(@RequestParam String filetype, @RequestParam String fileurl) {
        try {
            if(!(filetype!=null && (filetype.equalsIgnoreCase("txt")
                    || filetype.equalsIgnoreCase("pdf")
                    || filetype.equalsIgnoreCase("jpeg")
            ))) {
                System.out.println("fileType: " + filetype);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This file type is not supported");
            }
            System.out.println(filetype);
            System.out.println(fileurl);

           /* // ✅ Prevent SSRF: Allow only trusted domains
            if (!isTrustedUrl(fileurl)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ByteArrayResource("Untrusted URL".getBytes()));
            }*/

            RestTemplate restTemplate = new RestTemplate();

            disableSSLCertificateVerification();

            // ✅ Handle Redirects
            RequestEntity<Void> request = RequestEntity.get(URI.create(fileurl)).build();
            ResponseEntity<byte[]> response = restTemplate.exchange(request, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                HttpHeaders headers = new HttpHeaders();
                //headers.add("Access-Control-Allow-Origin", "*");
                headers.setContentType(getContentType(fileurl)); // ✅ Set proper file type
                return ResponseEntity.ok().headers(headers)
                        .body(new ByteArrayResource(response.getBody()));
            }

            return ResponseEntity.status(response.getStatusCode()).body(null);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource(("Error fetching file: " + e.getMessage()).getBytes()));
        }
    }

    @GetMapping("/file")
    public ResponseEntity<String> getFile(@RequestParam String filetype, @RequestParam String fileurl) {
        try {
            if(!(filetype!=null && (filetype.equalsIgnoreCase("txt")
                    || filetype.equalsIgnoreCase("pdf")
                    || filetype.equalsIgnoreCase("jpeg")
            ))) {
                System.out.println("fileType: " + filetype);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This file type is not supported");
            }
            System.out.println(filetype);
            System.out.println(fileurl);

           /* // ✅ Prevent SSRF: Allow only trusted domains
            if (!isTrustedUrl(fileurl)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ByteArrayResource("Untrusted URL".getBytes()));
            }*/

            RestTemplate restTemplate = new RestTemplate();

            disableSSLCertificateVerification();

            // ✅ Handle Redirects
            RequestEntity<Void> request = RequestEntity.get(URI.create(fileurl)).build();
            ResponseEntity<byte[]> response = restTemplate.exchange(request, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                HttpHeaders headers = new HttpHeaders();
                //headers.add("Access-Control-Allow-Origin", "*");
                headers.setContentType(getContentType(fileurl)); // ✅ Set proper file type
                String base64Content = Base64.getEncoder().encodeToString(response.getBody());

                return ResponseEntity.ok().headers(headers).body(base64Content);

            }

            return ResponseEntity.status(response.getStatusCode()).body(null);

        } catch (Exception e) {
            String errorMessage = "Error fetching file: " + e.getMessage();
            String base64ErrorMessage = Base64.getEncoder().encodeToString(errorMessage.getBytes());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(base64ErrorMessage);
        }
    }


    private static String convertToBase64(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        byte[] byteArray = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }

    // ✅ Restrict external URLs for security
    private boolean isTrustedUrl(String url) {
        return TRUSTED_DOMAINS.stream().anyMatch(url::startsWith);
    }

    // ✅ Detect file type based on URL
    private MediaType getContentTypeOld(String url) {
        if (url.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (url.endsWith(".docx")) return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (url.endsWith(".doc")) return MediaType.parseMediaType("application/msword");
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (url.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (url.endsWith(".txt")) return MediaType.TEXT_PLAIN;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
    private MediaType getContentType(String url) {
        if (url.endsWith(".pdf")) return MediaType.TEXT_PLAIN;
        if (url.endsWith(".docx")) return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (url.endsWith(".doc")) return MediaType.parseMediaType("application/msword");
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) return MediaType.TEXT_PLAIN;
        if (url.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (url.endsWith(".txt")) return MediaType.TEXT_PLAIN;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    // Disable SSL verification
    private static void disableSSLCertificateVerification() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCertificates, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception ignored) {
        }
    }
    @GetMapping("/readfile")
    public ResponseEntity<String> readFile(@RequestParam String filetype, @RequestParam String fileurl) {
        try {
            if(!(filetype!=null && (filetype.equalsIgnoreCase("word")
                    || filetype.equalsIgnoreCase("excel")
            )))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This file type is not supported");

            // ✅ Prevent SSRF: Allow only trusted domains
        /*    if (!isTrustedUrl(fileurl)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Untrusted URL");
            }*/
            System.out.println(filetype);
            System.out.println(fileurl);


            //RestTemplate restTemplate = new RestTemplate();

            disableSSLCertificateVerification();

            return ResponseEntity.ok(fileurl);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Error Fetching File");
        }
    }

    @GetMapping("/readfilepng")
    public ResponseEntity<String> readFilePng(@RequestParam String filetype, @RequestParam String fileurl) {
        try {
            if(!filetype.equalsIgnoreCase("png")) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This file type is not supported");
            }
            System.out.println(filetype);
            System.out.println(fileurl);

            RestTemplate restTemplate = new RestTemplate();


            RequestEntity<Void> request = RequestEntity.get(URI.create(fileurl)).build();
            ResponseEntity<byte[]> response = restTemplate.exchange(request, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String base64Content = Base64.getEncoder().encodeToString(response.getBody());
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);

                return ResponseEntity.ok().headers(headers).body(base64Content);
            }

            return ResponseEntity.status(response.getStatusCode()).body(null);

        } catch (Exception e) {
            String errorMessage = "Error fetching file: " + e.getMessage();
            String base64ErrorMessage = Base64.getEncoder().encodeToString(errorMessage.getBytes());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(base64ErrorMessage);
        }
    }
}
