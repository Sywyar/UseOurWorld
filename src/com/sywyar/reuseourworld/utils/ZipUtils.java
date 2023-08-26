package com.sywyar.reuseourworld.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.sywyar.reuseourworld.ReUseOurWorld.saveError;

public class ZipUtils {
    public static void zip(String zipPath, String charset, PropertyChangeListener propertyChangeListener, String... folderPath) {
        try (ZipOutputStream zipOutput = new ZipOutputStream(Files.newOutputStream(Paths.get(zipPath)), Charset.forName(charset));
            BufferedOutputStream output = new BufferedOutputStream(zipOutput)) {
            for (String s:folderPath){
                File folder = new File(s);
                long totalSize = getTotalSize(folder);
                zip(zipOutput, output, folder, folder.getName(), totalSize, 0, zipPath, propertyChangeListener);
            }
        } catch (Exception e) {
            saveError(e);
            throw new RuntimeException(e);
        }
    }

    private static long zip(ZipOutputStream zipOutput, BufferedOutputStream output, File source, String sourceName, long totalSize, long readSize, String zipPath,
                            PropertyChangeListener propertyChangeListener) throws IOException {
        if (source.isDirectory()) {
            File[] flist = source.listFiles();
            if (flist.length == 0) {
                zipOutput.putNextEntry(new ZipEntry(sourceName + "/"));
            } else {
                for (File file : flist) {
                    readSize = zip(zipOutput, output, file, sourceName + "/" + file.getName(), totalSize, readSize, zipPath, propertyChangeListener);
                }
            }
        } else {
            zipOutput.putNextEntry(new ZipEntry(sourceName));
            try (BufferedInputStream input = new BufferedInputStream(Files.newInputStream(source.toPath()))) {
                byte[] b = new byte[1024];
                for (int len = input.read(b); len > 0; len = input.read(b)) {
                    output.write(b, 0, len);
                }
            } catch (Exception e) {
                saveError(e);
                throw new RuntimeException(e);
            }
            Integer oldValue = (int) ((readSize * 1.0 / totalSize) * 100);
            readSize += source.length();
            Integer newValue = (int) ((readSize * 1.0 / totalSize) * 100);
            if (propertyChangeListener != null) {
                propertyChangeListener.propertyChange(new PropertyChangeEvent(zipPath, "progress", oldValue, newValue));
            }
        }
        return readSize;
    }

    private static long getTotalSize(File file) {
        if (file.isFile()) {
            return file.length();
        }
        File[] list = file.listFiles();
        long total = 0;
        if (list != null) {
            for (File f : list) {
                total += getTotalSize(f);
            }
        }
        return total;
    }

    public static void unzip(String zipPath, String targetPath, String charset, PropertyChangeListener propertyChangeListener) {
        long totalSize = new File(zipPath).length();
        long readSize = 0;
        try (ZipInputStream zipInput = new ZipInputStream(Files.newInputStream(Paths.get(zipPath)), Charset.forName(charset))) {
            for (ZipEntry zipItem = zipInput.getNextEntry(); zipItem != null; zipItem = zipInput.getNextEntry()) {
                if (!zipItem.isDirectory()) {
                    File file = new File(targetPath, zipItem.getName());
                    if (!file.exists()) {
                        new File(file.getParent()).mkdirs();// 创建此文件的上级目录
                    }
                    try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
                        byte[] b = new byte[1024];
                        for (int len = zipInput.read(b); len > 0; len = zipInput.read(b)) {
                            out.write(b, 0, len);
                        }
                    } catch (Exception e) {
                        saveError(e);
                        throw new RuntimeException(e);
                    }
                    Integer oldValue = (int) ((readSize * 1.0 / totalSize) * 100);// 已解压的字节大小占总字节的大小的百分比
                    readSize += zipItem.getCompressedSize();// 累加字节长度
                    Integer newValue = (int) ((readSize * 1.0 / totalSize) * 100);// 已解压的字节大小占总字节的大小的百分比
                    if (propertyChangeListener != null) {// 通知调用者解压进度发生改变
                        propertyChangeListener.propertyChange(new PropertyChangeEvent(zipPath, "progress", oldValue, newValue));
                    }
                }
            }
        } catch (Exception e) {
            saveError(e);
            throw new RuntimeException(e);
        }
    }
}
