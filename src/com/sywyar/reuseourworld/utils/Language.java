package com.sywyar.reuseourworld.utils;

import com.sywyar.reuseourworld.ReUseOurWorld;
import com.sywyar.superjsonobject.SuperJsonObject;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;

import static com.sywyar.reuseourworld.ReUseOurWorld.*;

public class Language {
    public static String getString(String str) {
        try {
            File file =  new File(System.getProperty("user.dir")+"/Language/"+ ReUseOurWorld.language+".json");
            if (!file.exists() || !getHashCode(file.getPath()).equals(getFileHashCode(ReUseOurWorld.language))){
                file.delete();
                InputStream inputStream = ReUseOurWorld.class.getResourceAsStream("/com/sywyar/reuseourworld/language/"+ReUseOurWorld.language+".json");
                file = saveInputStream(inputStream,System.getProperty("user.dir")+"/Language/"+ReUseOurWorld.language+".json",getFileHashCode(ReUseOurWorld.language));
                if (file==null){
                    showErrorDialog(Language.getString("useourworld_error_title"),Language.getString("useourworld_download_jar_error"));
                    return "";
                }
            }
            SuperJsonObject json = new SuperJsonObject(file);
            return json.getAsString(str);
        } catch (FileNotFoundException e) {
            saveError(e);
            throw new RuntimeException(e);
        }
    }

    public static String getFileHashCode(String str){
        switch (str){
            case "zh-CN":return "d350321ceef1f1a7cf10df7ab8f0f679435fac28eb35b387983be960ffeff250";
            case "en-US":return "747a3cc67c8923330d2f245d172f9fef642c8e5a02d2ef23b1766cc3c861cca";
            default:return "";
        }
    }

    public static File saveInputStream(InputStream inputStream, String path, String hashCode){
        if (inputStream==null){
            return null;
        }else {
            try {
                File file = new File(path);
                if (!file.getParentFile().exists())file.getParentFile().mkdirs();
                if (!file.exists())file.createNewFile();
                byte[] fileBytes = new byte[1024];
                OutputStream output = Files.newOutputStream(file.toPath());
                int len;
                while((len = inputStream.read(fileBytes)) != -1)output.write(fileBytes, 0, len);
                output.close();
                if (!getHashCode(path).equals(hashCode))return null;
            } catch (IOException e) {
                return null;
            }
        }
        return new File(path);
    }

    public static String getHashCode(String filePath) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        return getHashCode(fis);
    }

    public static String getHashCode(InputStream fis) {
        try {
            //SHA-1,SHA-256,MD5
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer, 0, 1024)) != -1)  md.update(buffer, 0, length);
            fis.close();
            byte[] md5Bytes  = md.digest();
            BigInteger bigInt = new BigInteger(1, md5Bytes);
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
