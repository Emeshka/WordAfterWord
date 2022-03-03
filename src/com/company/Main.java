package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Main {
    private static String filepath = System.getProperty("user.home") + "/word_after_word_data.txt";
    private static Scanner in = null;
    private static PrintStream out = null;

    private static HashMap<Character, SortedSet<String>> dictionary = new HashMap<>();
    private static TreeSet<String> history = new TreeSet<>();

    private static String text(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }/*

    public static boolean isReadable(String path) {
        File f = new File(path);
        boolean canRead = false;
        try {
            AccessController.checkPermission(new FilePermission(path, "read"));
            canRead = true;
        } catch (Exception e) {
            canRead = false;
        }
        return f.exists() && f.isFile() && canRead;
    }

    public static boolean isWriteable(String path) {
        boolean canWrite = false;
        try {
            AccessController.checkPermission(new FilePermission(path, "write"));
            canWrite = true;
        } catch (Exception e) {
            canWrite = false;
        }
        out.println();
        out.println(canWrite);
        out.println(isReadable(path));
        out.println(new File(path).exists());
        return isReadable(path) && canWrite;
    }*/

    private static boolean read() {
        boolean r = true;
        for (char c = 'а'; c <= 'я'; c++) {
            dictionary.put(c, new TreeSet<String>());
        }
        dictionary.put('ё', new TreeSet<String>());
        try {
            FileInputStream fstream = new FileInputStream(filepath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream, StandardCharsets.UTF_8));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.isEmpty() || !consistsOfRussianLetters(strLine)) continue;
                char first = strLine.charAt(0);
                dictionary.get(first).add(strLine);
            }

            Files.copy(Paths.get(filepath), Paths.get(System.getProperty("user.home") + "/word_after_word_data-backup.txt"), StandardCopyOption.REPLACE_EXISTING);

            fstream.close();
            br.close();
        } catch (FileNotFoundException e) {
            try {
                File file = new File(filepath);
                boolean res = file.getParentFile().exists() || file.getParentFile().mkdirs();
                //out.println(res);
                res = res && file.createNewFile();
                //out.println(res);
                if (!res) throw new IOException();
            } catch (IOException failedToCreate) {
                out.println("file at '" + filepath + "' does not exist.");
                out.println("Couldn\'t create the file.\n");
                out.println("  Программа будет закрыта.");
                out.println(text(e));
                out.println(text(failedToCreate));
                //out.println("isWritable(file): "+isWriteable(filepath));
                //out.println("isWritable(dir): "+isWriteable(System.getProperty("user.home")));
                r = false;
            }
        } catch (IOException e) {
            out.println("! Ошибка чтения файла " + filepath);
            out.println("  Программа будет закрыта.");
            out.println(text(e));
            r = false;
        }
        return r;
    }

    private static boolean write() {
        boolean res = true;
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filepath), StandardCharsets.UTF_8), true);
            out.println(dictionaryToString());
            out.close();
        } catch (IOException e) {
            out.println("! Ошибка записи файла " + filepath);
            out.println("  Изменения не будут сохранены.");
            out.println(text(e));
            res = false;
        }
        return res;
    }

    private static String dictionaryToString() {
        //String Builder повышает производительность операций над строками
        StringBuilder result = new StringBuilder();
        Set<Character> keys = dictionary.keySet();
        for (Character key : keys) {
            SortedSet<String> words = dictionary.get(key);
            for (String word : words) result.append(word).append("\n");
        }
        return result.toString();
    }

    private static boolean isLatinLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static boolean isRussianLetter(char c) {//и дефис и апостроф
        return (c >= 'А' && c <= 'Я') || (c >= 'а' && c <= 'я') || (c == 'Ё') || (c == 'ё') || (c == '\'') || (c == '-');
    }

    private static boolean consistsOfRussianLetters(String str) {
        boolean res = true;
        for (int i = 0; i < str.length() && res; i++) {
            res = isRussianLetter(str.charAt(i));
        }
        return res;
    }

    private static char lastChar(String str) {
        char r = 0;
        int i = 1;
        while ((r == 0 || r == 'ъ' || r == 'ы' || r == 'ь') && i <= str.length()) {
            r = str.charAt(str.length() - i);
            i++;
        }
        if (r == 0) {
            out.println("! Битое слово '" + str + "' в базе! Удалите при первой возможности!");
        }
        return r;
    }

    private static int cost(String str) {
        int cost = 0;
        for (int i = 0; i < str.length(); i++) {
            char t = str.charAt(i);
            switch (t) {
                case 'а':case 'у':case 'к':case 'е':case 'н':case 'п':case 'р':case 'о':case 'л':case 'с':case 'и':case 'т':case 'м': {
                    cost+=1;
                    break;
                }
                case 'г':case 'з':case 'в':case 'д':case 'ы':case 'я':case 'б':case 'ь': {
                    cost+=2;
                    break;
                }
                case 'ё':case 'ж':case 'ш':case 'й':case 'ф':case 'х':case 'ц':case 'ч':case 'щ':case 'ъ':case 'э':case 'ю': {
                    cost+=3;
                    break;
                }
            }
        }
        return cost;
    }

    public static void main(String[] args) {
        Locale loc = new Locale("ru");
        in = new Scanner(System.in, "UTF-8");
        in.useLocale(loc);
        try {
            out = new PrintStream(System.out, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        out.println("Игра в слова: называть по очереди с компом слово на последнюю букву.\n---");
        out.println("сдаться (f), справка и правила (h).");
        out.println("* Компьютер может 'забыть' слово и сдаться с тем большей вероятностью,");
        out.println("чем меньше слов на загаданную букву он знает.");
        out.println("* Если внесли изменения в словарь, то закрывайте программу не крестиком, а 'f'; '0'!");
        if (!read()) System.exit(-1);

        boolean wannaPlay = true;
        while (wannaPlay) {
            int compCount = 0, playerCount = 0;
            out.println("---Загадайте первое слово:");

            String playerWord = "", compWord = "";
            boolean fin = false;
            double computerForgetProb = 0.005d;//связано с divideBy
            double divideBy = 1000;
            //Если мы знаем 1000 слов на букву О, то P{random() < 0.005}==0.005
            //Если мы знаем 1 слово на букву О, то P{1/32 * random() < 0.005} == P{random() < 0.16}==0.16
            //функция sqrt выбрана из соображения, что она медленнее всех возрастает, но возрастает!
            while (!fin) {
                if ((playerWord = in.nextLine().toLowerCase()).isEmpty()) continue;
                //out.println("0");
                char c;
                if (isLatinLetter(c = playerWord.charAt(0))) {
                    if (c == 'f') {
                        out.println("---Вы сдались! Компьютер победил!");
                        break;
                    } else if (c == 'h') {
                        out.println("*     Любые русские существительные в именительном падеже в единственном числе, или, если невозможно, то во множественном.");
                        out.println("*     МОЖНО: названия стран, областей и территорий, гор, рек, озер, островов, континентов, геологических периодов.");
                        out.println("*     НЕЛЬЗЯ: названия населенных пунктов, музеев, театров и т.д., морей, океанов; имена собственные, клички, аббревиатуры. Детали:");
                        out.println("*     Если название с прилагательным, цифрами, латинскими буквами или пишется раздельно(а у морей и океанов это всегда так), то НЕЛЬЗЯ:");
                        out.println("Чёрная река\nКавказские горы\nЮрский период\nМосква-река\nБольшая Канава\nЗападная Сахара" +
                                "\nПапуа-Новая Гвинея\nБосния и Герцеговина\nРеспублика Конго\nСаудовская Аравия\nТёмная Долина");
                        out.println("*     Одно существительное, в т.ч. с дефисом и с апострофом, и адаптированные аббревиатуры МОЖНО:");
                        out.println("Москва (река)\nСочи (река)\nОбь\nЛена (река)\nРим (древнее государство)\nДартмур (территория в Англии)\n" +
                                "Юра (период)\nАнды\nКавказ\nТянь-Шань\nКот-д'Ивуар\nГвинея-Бисау");
                        out.println("*     НЕЛЬЗЯ: аббревиатуры, жаргонная транслитерация иностранных слов,\n  названия каких-либо заведений, станций и остановок, улиц, городов, имена:");
                        out.println("КНДР\nОАЭ\nЧАЭС-1\nгост (ГОСТ)\nхенд-мейд (hand-made)\nгеттер (getter)\nбэкграунд (background)\nЛувр (название музея)\n" +
                                "Третьяковка (Третьяковская галерея)\nШаболовка (улица)\nШаболовская (ст.метро)\nПариж\nВладимир (имя и город)\nЧернобыль\nКленовница (имя/кличка)");
                        out.println("*     МОЖНО: адаптированные аббревиатуры, прижившиеся заимствования, названия территорий внутри города, ставшие нарицательными," +
                                "\n      названия вымышленных феноменов и территорий из книг, игр и т.д., жаргон, имена, ставшие нарицательными:");
                        out.println("ава (аватарка) (avatar)\nачивка (achievement)\nгет (get, жарг. убитый враг в компьютерной игре)\nстим (Steam - сервис распространения игр)\n" +
                                "зарплата (заработная плата)\nфизра (физкультура) (физическая культура)\nАрбат (район с рынками, художниками и т.д.)\n" +
                                "Лужники (район вокруг московского стадиона, спортивный мем)\nКремль\nнепись (NPC, Not Player Character)\n" +
                                "камаз (грузовой автомобиль производства КАМАЗ)\nшмон (тюремный жаргон)\nАгропром\nмурка (любая кошка)\n");
                        //
                        out.println("* Буква Ё считается самостоятельной и не заменима на Е!");
                        out.println("* Регистр букв (большие или маленькие) неважен");
                        out.println("* Если слово кончается на ъ,ы,ь, то берется предыдущая буква.");
                        out.println("* Компьютер верит, что вы не читер. Если вы говорите, что такое слово бывает,");
                        out.println("  то игра запоминает слово и может использовать его сама!");
                        out.println("  Поэтому тщательно проверяем правильное написание.\n");
                        out.println("Очки (стоимость слова) засчитывается следующим образом:");
                        out.println("За буквы А, У, К, Е, Н, П, Р, О, Л, С, И, Т, М - 1 очко");
                        out.println("За буквы Г, З, В, Д, Ы, Я, Б, Ь - 2 очка");
                        out.println("За буквы Ё, Ж, Ш, Щ, Й, Ф, Х, Ц, Ч, Ъ, Э, Ю - 3 очка");

                        out.println(">> " + compWord);
                        continue;
                    }
                }
                //out.println("0.1");

                //out.println("'"+playerWord+"'");
                if (consistsOfRussianLetters(playerWord)) {
                    //out.println("1");
                    char b = playerWord.charAt(0);
                    SortedSet<String> words = dictionary.get(b);
                    if (!compWord.isEmpty() && lastChar(compWord) != b) {
                        out.println("---Начинается не на ту букву!");
                        out.println(">> " + compWord);
                        continue;
                    }
                    //out.println("2");
                    if (history.contains(playerWord)) {
                        out.println("---Такое слово уже было!");
                        out.println(">> " + compWord);
                        continue;
                    }
                    //out.println("3");
                    if (!words.contains(playerWord)) {
                        boolean dontIncludeWord = false;
                        boolean inputOk = false;
                        while (!inputOk) {
                            out.println("---Неизвестное слово! Добавить? да(1)/нет(0)");
                            try {
                                int input = in.nextInt();
                                if (input == 1) {
                                    words.add(playerWord);
                                    out.println("---Спасибо! Буду знать.");
                                    //переход к ходу компьютера
                                } else if (input == 0) {
                                    out.println(">> " + compWord);
                                    dontIncludeWord = true;
                                    //повтор хода человека
                                } else continue;
                                inputOk = true;
                            } catch (InputMismatchException e) {
                                out.println("---Введите 0 или 1");
                            } finally {
                                in.nextLine();
                            }
                        }
                        if (dontIncludeWord) continue;
                    }
                    //out.println("4");
                    history.add(playerWord);
                    playerCount += cost(playerWord);

                    //ход компьютера
                    boolean wordWasUsed = true;
                    while (wordWasUsed) {
                        char e = lastChar(playerWord);
                        words = dictionary.get(e);
                        double seed = Math.random();
                        double coef = Math.sqrt(words.size() / divideBy);
                        double forgot = coef * seed;
                        //out.println("() seed: " + seed + ", coef: " + coef + "; forgot: " + forgot);
                        if (forgot < computerForgetProb) {
                            out.println("---Компьютер сдался! Вы победили!");
                            fin = true;
                            break;
                        }
                        int index = new Random().nextInt(words.size());
                        Iterator<String> it = words.iterator();
                        for (int i = 0; i < index; i++) it.next();//complexity O(n)
                        //список бы давал большую скорость на get(i), но не защищал от дубликатов, что куда хуже
                        compWord = it.next();
                        if (!history.contains(compWord)) {
                            wordWasUsed = false;
                            history.add(compWord);
                            compCount += cost(compWord);
                        }
                    }
                    if (!fin) out.println(">> " + compWord);
                }
            }

            int steps = history.size();
            out.println("В партии было всего названо " + steps + " слов.");
            if (steps > 10) {
                if (steps < 50) {
                    out.println("Неплохо!");
                } else if (steps < 300) {
                    out.println("Хороший результат!");
                } else {
                    out.println("Отличный результат!");
                }
            }
            if (steps != 0) {
                double averageCompCost = ((double) compCount) / (((double) steps) / 2);
                double averagePlayerCost = ((double) playerCount) / (((double) steps) / 2);
                out.println("Средняя стоимость слова:");
                out.printf("\tкомпьютера: %.2f", averageCompCost);
                out.printf("\tигрока: %.2f\n", averagePlayerCost);
                if (averageCompCost > averagePlayerCost) out.println("Компьютер придумывал более заковыристые слова!");
                else out.println("Вы знаете больше сложных слов, чем компьютер!");
            }

            boolean inputOk = false;
            while (!inputOk) {
                out.println("\nЕще раз? да(1)/нет(0)");
                try {
                    int input = in.nextInt();
                    if (input == 0) wannaPlay = false;
                    else if (input == 1) {
                        out.println("\n\n-------------------------\n");
                        history.clear();
                    } else continue;
                    inputOk = true;
                } catch (InputMismatchException e) {
                    out.println("---Введите 0 или 1");
                } finally {
                    in.nextLine();
                }
            }
        }
        boolean retry = true;
        while (retry && !write()) {
            boolean inputOk = false;
            while (!inputOk) {
                out.println("! Проверьте, что файл существует, что у него нет атрибута 'Только чтение'");
                out.println("  и что пользователь, под которым вы зашли, имеет право на запись этого файла");
                out.println("--Хотите закрыть программу или попробовать еще раз? закрыть(1) / повторить попытку(0)");
                try {
                    int input = in.nextInt();
                    if (input == 1) retry = false;
                    else if (input != 0) continue;
                    inputOk = true;
                } catch (InputMismatchException e) {
                    out.println("---Введите 0 или 1");
                } finally {
                    in.nextLine();
                }
            }
        }
    }
}
