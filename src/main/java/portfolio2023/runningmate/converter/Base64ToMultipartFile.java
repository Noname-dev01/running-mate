package portfolio2023.runningmate.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class Base64ToMultipartFile {

    public MultipartFile convert(String dataUrl, String fileName) throws IOException {
        String[] parts = dataUrl.split(",");
        String base64Data = parts[1];

        byte[] bytes = DatatypeConverter.parseBase64Binary(base64Data);
        String contentType = parts[0].split(";")[0].split(":")[1];

        FileItem fileItem = createFileItem(bytes, contentType,fileName);

        return new CommonsMultipartFile(fileItem);
    }

    private FileItem createFileItem(byte[] content, String contentType, String fileName) throws IOException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        String fieldName = "file";
        boolean isFormField = false;
        int sizeThreshold = 1024;

        factory.setSizeThreshold(sizeThreshold);
        FileItem fileItem = factory.createItem(fieldName, contentType, isFormField, fileName);
        fileItem.getOutputStream().write(content);
        return fileItem;
    }
}
