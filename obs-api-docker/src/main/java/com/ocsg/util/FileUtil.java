package com.ocsg.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileUtil {

    public static List<String> listFiles(String path) throws IOException {
        List<String> list = new ArrayList<>();
        Files.find(Paths.get(path),
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> fileAttr.isRegularFile())
                .forEach(f ->
                      list.add(f.toString())
                        );
        System.out.println(list);
        return list;
    }

    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("YYMMdd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void main(String[] args){
        String path = "/Users/xianzhihai924/storage";
        try{
            FileUtil.listFiles(path);
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
