package com.tfkfan;

public class AssetsFileContent {
    private String fileName;
    private String content;

    public AssetsFileContent(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }
}
