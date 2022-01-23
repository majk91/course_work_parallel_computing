package com.course.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

    //считываем только определенное количество
    //maxSize<=0 - получить все файлы
    public  static List<File> getFilesFromDir(String path, int maxSize){
        List<File> result = new ArrayList<>();

        File dir = new File(path); //path указывает на директорию
        File[] arrFiles = dir.listFiles();//вычитываем файлы в массив

        if (arrFiles == null || arrFiles.length == 0) {
            System.out.println("ERROR! Files not exist in folder ["+path+"].");
            return result;
        }

        result = Arrays.asList(arrFiles);//конвертируем в коллекцию

        if(maxSize > 0 && maxSize < arrFiles.length)//если есть ограничение количества и оно меньше реального количества
            result = result.stream().limit(maxSize).collect(Collectors.toList());

        System.out.println("SUCCESS! We got "+result.size()+" files from folder ["+path+"].");

        return  result;
    }

}
