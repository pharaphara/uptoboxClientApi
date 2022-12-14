package com.pharaphara.uptoboxClientApi.services;

import com.pharaphara.uptoboxClientApi.entity.Download;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@EnableAsync
public class FileDownloaderImpl implements FileDownloader{

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Value("${downloadFolder}")
    private String downloadFolder;
    @Override
    @Async
    public void downloadFile(Download download) {

        File downloadDirectory = new File(downloadFolder);
        if(!downloadDirectory.exists()){
            try {
                Files.createDirectories(downloadDirectory.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



        String path = downloadFolder+download.getName();

        File f = new File(path);

        if(f.exists() && !f.isDirectory()) {
            download.setName(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hhmmss_ddMMyy_"))+download.getName());
            path= downloadFolder+download.getName();
        }

        System.out.println(path);

            Flux<DataBuffer> dataBuffer = webClientBuilder.build()
                .get()
                .uri(download.getDownloadUrl())
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        FileOutputStream fout;
        try {
            fout = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to create targetFile", e);
        }

        DataBufferUtils.write(dataBuffer, download.getOutputStream(fout)  ).subscribe( DataBufferUtils.releaseConsumer() );
        System.out.println("Download Started, path="+path);

        do {
            String progresString = String.format("%.2f", download.getProgress() * 100.0);
            System.out.println( progresString +"% - "+download.getName() );
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        } while(!download.finished());

        try {
            fout.close();
            Path filePath = Paths.get(path);
            Set<PosixFilePermission> perms = Files.readAttributes(filePath, PosixFileAttributes.class).permissions();

            System.out.format("Permissions before: %s%n",  PosixFilePermissions.toString(perms));

            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(filePath, perms);

            System.out.format("Permissions after:  %s%n",  PosixFilePermissions.toString(perms));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }





    }
}
