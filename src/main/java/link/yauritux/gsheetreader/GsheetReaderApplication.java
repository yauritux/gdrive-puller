package link.yauritux.gsheetreader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@SpringBootApplication
public class GsheetReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GsheetReaderApplication.class, args);
    }
}

@Component
class CLIApp implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Build credential object.
        Credential credential = GDriveService.getCredentials(HTTP_TRANSPORT);

        // Create a Google Drive Service.
        var driveService = new Drive.Builder(HTTP_TRANSPORT, GDriveService.JSON_FACTORY, credential)
                .setApplicationName(GDriveService.APPLICATION_NAME).build();

        FileList result = driveService.files().list().setPageSize(10).setFields("nextPageToken, files(id, name, fileExtension)").execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found!");
        } else {
            System.out.println("Files.");
            for (File file : files) {
                var fileExt = file.getFileExtension() == null ? file.getFullFileExtension() : file.getFileExtension();
                System.out.println("fileExt = " + fileExt);
                System.out.printf("%s(%s)\n", file.getName(), file.getId());
//                HttpResponse httpResponse = null;
                var ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                System.out.println("ext = " + ext);
                var props = file.getAppProperties();
                if (fileExt == null || ext == null) {
                    continue;
                }
                if (ext.equalsIgnoreCase("csv") || fileExt.equalsIgnoreCase("csv")) {
                    var response = driveService.files().get(file.getId());
//                    httpResponse = driveService.files().export(file.getId(), "text/csv").executeMedia();
//                    Drive.Files.Get get = driveService.files().get(file.getId());
//                    httpResponse = get.executeMedia();
//                    System.out.println("httpResponse = " + httpResponse);
                    System.out.println("response = " + response);
                    var httpResponse = response.executeMedia();
                    System.out.println("httpResponse = " + httpResponse);
                    InputStream inStream = httpResponse.getContent();
                    FileOutputStream output = new FileOutputStream(file.getName());
                    try {
                        int l;
                        byte[] tmp = new byte[httpResponse.getContent().available()];
                        while ((l = inStream.read(tmp)) != -1) {
                            output.write(tmp, 0, l);
                        }
                    } finally {
                        output.close();
                        inStream.close();
                    }
                }
            }
        }
        // Build a new authorized API client service.
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        final String spreadsheetId = "1i23-EK7nQBpXxqUb61qYRqKYiu-2-i7R";
//        final String range = "A2:F10";
//        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, GSheetReader.JSON_FACTORY, GSheetReader.getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(GSheetReader.APPLICATION_NAME).build();
//        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            System.out.println("No data found!");
//        } else {
//            for (List row : values) {
//                System.out.printf("%s, %s\n", row.get(0), row.get(4));
//            }
//        }
    }
}
