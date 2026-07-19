package com.chekrol.dms.service;

import com.chekrol.dms.dao.DocumentDAO;
import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import com.chekrol.dms.util.*;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DocumentService {
    private final DocumentDAO dao=new DocumentDAO();

    public long create(User user,DocumentRecord doc,Collection<Part> imageParts,Part primaryPdf,Collection<Part> attachments,boolean submit)throws IOException,SQLException{
        doc.setCreatedById(user.getId());
        doc.setDocumentCode("DOC-"+LocalDate.now().getYear()+"-"+UUID.randomUUID().toString().substring(0,8).toUpperCase());
        doc.setDueDate(submit?LocalDate.now().plusDays("URGENT".equals(doc.getPriority())?2:5):null);
        List<DocumentFile> files=new ArrayList<>();

        List<Path> tempImages=new ArrayList<>();
        if(imageParts!=null){for(Part p:imageParts){if(p.getSize()<=0)continue;if(!ValidationUtil.isImage(p))throw new IOException("Camera pages must be JPG, JPEG or PNG.");FileStorageUtil.StoredFile stored=FileStorageUtil.store(p,doc.getDocumentCode(),"temporary");tempImages.add(stored.getPath());}}
        if(!tempImages.isEmpty()){
            Path output=AppConfig.storageRoot().resolve("documents").resolve(doc.getDocumentCode()).resolve("main").resolve("captured-document.pdf");
            ImageToPdfService.convert(tempImages,output);
            DocumentFile f=new DocumentFile();f.setOriginalName("captured-document.pdf");f.setStorageName(output.getFileName().toString());f.setStoragePath(output.toString());f.setMimeType("application/pdf");f.setFileSize(Files.size(output));f.setPrimaryFile(true);files.add(f);
            for(Path p:tempImages)Files.deleteIfExists(p);
        }else if(primaryPdf!=null&&primaryPdf.getSize()>0){
            if(!"pdf".equals(ValidationUtil.extension(primaryPdf.getSubmittedFileName())))throw new IOException("The main uploaded document must be PDF when camera pages are not used.");
            FileStorageUtil.StoredFile s=FileStorageUtil.store(primaryPdf,doc.getDocumentCode(),"main");files.add(toFile(s,true));
        }else throw new IOException("Capture at least one image or upload a main PDF document.");

        if(attachments!=null){for(Part p:attachments){if(p.getSize()<=0)continue;FileStorageUtil.StoredFile s=FileStorageUtil.store(p,doc.getDocumentCode(),"attachments");files.add(toFile(s,false));}}
        if(AppConfig.isDemoMode()){DemoData.createDocument(user,doc,submit);return doc.getId();}
        return dao.create(doc,files,submit);
    }

    private DocumentFile toFile(FileStorageUtil.StoredFile s,boolean primary){DocumentFile f=new DocumentFile();f.setOriginalName(s.getOriginalName());f.setStorageName(s.getStorageName());f.setStoragePath(s.getPath().toString());f.setMimeType(s.getMimeType());f.setFileSize(s.getSize());f.setPrimaryFile(primary);return f;}
}
