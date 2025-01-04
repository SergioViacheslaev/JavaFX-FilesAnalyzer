# JavaFX-FilesAnalyzer
![filesAnalyzer](https://user-images.githubusercontent.com/49530516/79638723-bed29680-818f-11ea-8fab-17147dddeda6.jpg)
## README

This application can find and read files with plain text inside (txt, log).

Supports reading large files (more than 1 GB):  <br>
     if file size is more than 100 MB, by default it will be opened in pages reading mode. <br>
     Each page is 50 MB of file.  <br>
     If you need to load all file in memory, enable “LargeFiles mode” checkbox. <br>
        
You can search files by some criterias: <br>
    • file extension  <br>
    • file content (searched text inside file) <br>
    • file mask (some part of file name), for example if you don’t remember the whole file name. <br>
    
To search text inside opened file, choose these buttons: <br>
![allFoundText](https://user-images.githubusercontent.com/49530516/79639647-b3826980-8195-11ea-9607-02651bdc0b1a.jpg) 
will show you all found text occurrences in new window.<br>

![nextFoundText](https://user-images.githubusercontent.com/49530516/79639752-4fac7080-8196-11ea-957c-2fd07ef98fcf.jpg)
will move cursor to the first and next found text occurrence.<br>

If you run from IDEA or -jar add VM options:
--module-path C:\Java\javafx-sdk-14\lib --add-modules javafx.controls,javafx.fxml

Supports English, Russian languages. <br>
Powered by OpenJFX https://openjfx.io/, RichTextFx https://github.com/FXMisc/RichTextFX 
