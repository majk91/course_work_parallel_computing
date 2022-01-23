package com.course;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Index {
        Map<UUID,String> sources;
        HashMap<String, HashSet<UUID>> index;

        public Index(){
            sources = new HashMap<UUID,String>();//список файлов в формате [id -> name]
            index = new HashMap<String, HashSet<UUID>>();//список слов и файлов в которых встречается каждое слово
        }

        public void buildIndex(List<File> files){
            for(File file:files){
                UUID id = UUID.randomUUID();

                try(BufferedReader fileBuffer = new BufferedReader(new FileReader(file)))
                {
                    sources.put(id, file.getName());//делаем список файлов  - [id, name]
                    String ln;
                    //вычитываем файл по-строчно
                    while( (ln = fileBuffer.readLine()) !=null) {
                        String[] words = ln.split("\\W+");//разделяем на слова - один или больше не-alphanumeric символ, то же что и [^a-zA-Z0-9]+
                        for(String word:words){
                            word = word.toLowerCase();// приводим к нижнему регистру

                            //!!!!Синхронизация потоков!!!!!!!
                            synchronized (Index.class) {
                                if (!index.containsKey(word))
                                    index.put(word, new HashSet<UUID>());//добавляем новый хеш индекс и слово
                                //устанавливаем идентификатор файла в котором находится слово
                                index.get(word).add(id);
                            }
                        }
                    }
                } catch (IOException e){
                    System.out.println("File "+file.getName()+" not found. Skip it");
                }
            }

        }

        public String find(String phrase){
            String[] words = phrase.split("\\W+");
            StringBuilder result = new StringBuilder("[");

            //получить список файлов для первого слова
            HashSet<UUID> res = new HashSet<UUID>(index.get(words[0].toLowerCase()));

            for(String word: words){
                //retainAll() Сохраняет только те элементы в этой коллекции, которые содержатся в указанной коллекции
                res.retainAll(index.get(word));
            }

            if(res.size()==0) {
                System.out.println("Not found");
                return result.append("]").toString();
            }

            //System.out.println("Found in: ");
            for(UUID num : res){
                //System.out.println("\t"+sources.get(num));
                result.append("'").append(sources.get(num)).append("',").toString();
            }

            return result.append("]").toString();
        }
}
