package com.ct.builder.application.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void compressDirectory(Path soureDirectory, Path destinationFile)
            throws FileNotFoundException, IOException {
        compressDirectory(soureDirectory.toFile(), destinationFile.toFile());
    }

    public static void compressDirectory(String soureDirectoryStr, String destinationFileStr)
            throws FileNotFoundException, IOException {
        File soureDirectory = new File(soureDirectoryStr);
        File destinationFile = new File(destinationFileStr);
        compressDirectory(soureDirectory, destinationFile);
    }

    public static void compressDirectory(File soureDirectory, File destinationFile)
            throws FileNotFoundException, IOException {
        List<File> filesListInDir = populateFilesList(soureDirectory);
        try (FileOutputStream fos = new FileOutputStream(destinationFile);
                ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.UTF_8)) {
            // zos.setMethod(ZipOutputStream.STORED);
            zos.setLevel(Deflater.NO_COMPRESSION);
            for (File file : filesListInDir) {
                if (file.isFile()) {
                    zos.putNextEntry(new ZipEntry(getRelativePath(soureDirectory, file)));
                    byte[] bytes = Files.readAllBytes(Path.of(file.toURI()));
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                }
            }
        }
    }

    private static List<File> populateFilesList(File dir) throws IOException {
        List<File> filesListInDir = new ArrayList<File>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                filesListInDir.add(file);
            } else {
                List<File> filesSubDir = populateFilesList(file);
                filesListInDir.addAll(filesSubDir);
            }
        }
        return filesListInDir;
    }

    public static void compressSingleFile(String soureFileStr, String destinationFileStr) throws IOException {
        File soureFile = new File(soureFileStr);
        File destinationFile = new File(destinationFileStr);
        FileOutputStream fos = new FileOutputStream(destinationFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        zos.putNextEntry(new ZipEntry(getRelativePath(soureFile.getParentFile(), soureFile)));
        byte[] bytes = Files.readAllBytes(Path.of(soureFile.toURI()));
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
        zos.close();

    }

    private static String getRelativePath(File rootFile, File targetFile) {
        URI rootURI = rootFile.toURI();
        URI targetURI = targetFile.toURI();
        URI relativeURI = rootURI.relativize(targetURI);
        return relativeURI.getPath();
    }

    private static String getRelativePath(Path rootFile, Path targetFile) {
        Path relativePath = rootFile.relativize(targetFile);
        return relativePath.toString();
    }

}
