package company;

import java.sql.*;
import java.util.*;

public class Main {

    private static Connection con;
    static int maxCount;
    static int entryCount;
    public static void main(String[] args) {
        if(ConnectDB("Vocabulary")){
            System.out.println("Připojeno!");
            int correctCount = startTestingLesson();
            finishTesting(correctCount);
            exitContinueOptions(getOptions());
        }
    }

    public static int startTestingLesson (){
        LangEnum langEnum = getLanguage();
        entryCount = setWordCount();

        return testing(langEnum, entryCount);
    }

    public static LangEnum getLanguage (){
        LangEnum entryLangEnum = null;
        do {
            System.out.println("Napiš \"CZ\" pro překlad z Českého jazyka do Anglického a \"EN\" pro obrácený překlad.");
            Scanner scan = new Scanner(System.in);
            entryLangEnum = getEnum(scan.nextLine());
        }
        while (entryLangEnum == null);
        return entryLangEnum;
    }

    public static int setWordCount (){
        maxCount = getMaxCountFromDB();
        System.out.println("Zvol počet zkoušených slovíček, maximálně: "+ maxCount);
        Scanner scan = new Scanner(System.in);
        Integer entryCount = checkCount(scan.nextInt()); //Zvoli pocet slov
        return entryCount;
    }

    public static int testing (LangEnum entryLangEnum ,int wordCount){
        LinkedList<Word> testingList = createWordList(getDataFromDB(wordCount));
        LinkedList<Word> answersList = new LinkedList<>();
        int testingListSize = testingList.size();
        int correctCount = 0;
        for (int i = 0; i < testingListSize; i++){
            Word currentWord = getRandomWord(testingList);
            boolean result = wordTest(i+1, currentWord, entryLangEnum);
            if (result){
                correctCount ++;
            }
            answersList.add(currentWord);
            testingList.remove(currentWord);
        }
        return correctCount;
    }

    public static void finishTesting (int correctCount){
        System.out.println(String.format("Počet bodů je: %s z %s", correctCount, entryCount));


        //Nakonec vypsat dalsi moznosti pokracovan
        // restart, exit, přidej slovo

    }

//    public static ExitContinueOptions getOptions() {
//        ExitContinueOptions exitOptions = null;
//        do {
//            System.out.println("Jak chcete pokračovat? Napište \"restart\" pro novou lekci, nebo \"exit\" pro ukončení.");
//            exitOptions = getOptionsEnum(scan.nextLine());
//        }
//        while (exitOptions == null);
//        return exitOptions;
//    }

//    public static ExitContinueOptions getOptionsEnum(String optionsEntry){
//        try{
//            return ExitContinueOptions.valueOf(optionsEntry.toUpperCase());
//        } catch (Exception ex){
//            return null;
//        }
//    }



    public static String getOptions(){
        System.out.println("Jak chcete pokračovat? Napište \"restart\" pro novou lekci, \"exit\" pro ukončení, nebo \"insert\" pro přidání dalších slov.");
        Scanner scan3 = new Scanner(System.in);
        String exitOptions = scan3.nextLine();
        return exitOptions.toLowerCase().trim();
    }


    public static void exitContinueOptions (String options) {
        if(options.equals("restart")){
            int corectCount = startTestingLesson();
            finishTesting(corectCount);
            exitContinueOptions(getOptions());
        } else if (options.equals("exit")){
            closeConnection(); // na konci programu je nutne odpojit se od databaze.
            System.out.println("Odpojeno!");
            System.exit(0);
        } else if (options.equals("insert")){
            addNewWord();
            exitContinueOptions(getOptions());
        }
        else {
            System.out.println("Chybně zadáno");
            exitContinueOptions(getOptions());
        }
    }

    public static String setNewWordCZ(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Zadej nové slovíčko česky:\n(Pro ukončení napište \"done\")");
        String word_cz = scan.nextLine();
        if(word_cz.equals("done")){
            return null;
        }
        return word_cz.toLowerCase().trim();
    }

    public static String setNewWordEN(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Zadej nové slovíčko anglicky:\n(Pro ukončení napište \"done\")");
        String word_eng = scan.nextLine();
        if(word_eng.equals("done")){
            return null;
        }
        return word_eng.toLowerCase().trim();
    }

    public static String setNewWordPhon(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Zadej nové slovíčko foneticky:\n(Pro ukončení napište \"done\")");
        String phonetics_eng = scan.nextLine();
        if(phonetics_eng.equals("done")){
            return null;
        }
        return phonetics_eng.toLowerCase().trim();
    }

    public static void addNewWord(){
        boolean isDone = false;
        String sqlString = "";
        int rowCount = 0;
        while (!isDone){
            String cz = setNewWordCZ();
            if(cz == null){
                isDone = true;
            } else {
                String en = setNewWordEN();
                if(en == null){
                    isDone = true;
                } else {
                    String phon = setNewWordPhon();
                    if(phon == null){
                        isDone = true;
                    } else {
                        sqlString += "INSERT INTO words (word_cz, word_eng, phonetics_eng) VALUES ('"+ cz +"','"+ en +"',N'" + phon +"');\n";
                        rowCount ++;
                    }
                }
            }
        }
        try{
            con.setAutoCommit(false);
            Statement query = con.createStatement();
            query.executeUpdate(sqlString);
            con.commit();
            System.out.println(String.format("Dokončeno. Počet přidaných slov: %s", rowCount));
        } catch (Exception ex) {
            ex.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                }catch (SQLException e){
                    System.out.println("Nastala chyba při rollbacku.");
                    e.printStackTrace();
                }
            }
        }
    }

    public static LangEnum getEnum(String langEntry){
        try{
           return LangEnum.valueOf(langEntry.toUpperCase());
        } catch (Exception ex){
            return null;
        }
    }

    public static boolean wordTest(int order, Word word, LangEnum lang){
        System.out.println(String.format("%s. Napiště překlad slova '%s'", order, word.getQuestion(lang)));
        Scanner scan2 = new Scanner(System.in);
        String answer = scan2.nextLine();
        if (word.getAnswer(lang).equals(answer.toLowerCase().trim()) ){
            System.out.println(String.format("Správně. %s", word.getPhonetics(lang)));
            return true;
        }
        else {
            System.out.println(String.format("Špatná odpověď. Správně: %s. %s", word.getAnswer(lang), word.getPhonetics(lang)));
            return false;
        }
    }

    public static boolean ConnectDB(String dbName){
        //String conStrUrl = "jdbc:sqlserver://DESKTOP-UF0PE6E\\SQLEXPRESS:57411;integratedSecurity=true;databaseName=" +dbName;
        String conStrUrl ="jdbc:sqlserver://SQL5107.site4now.net\\SQLEXPRESS:1433;user=db_a846e3_vocabulary_admin;password=Fantomas09;databaseName=db_a846e3_vocabulary";
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(conStrUrl);
            return true;
        } catch (SQLException e) {
            System.out.println("Jejda, nastala chyba: ");
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.out.println("Jejda, nastala chyba: ");
            ex.printStackTrace();
            return false;
        }
    }
    public static void closeConnection(){
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int checkCount (int count){
        if (maxCount < count){
            count = maxCount;
        }
        return count;
    }

    public static ResultSet getDataFromDB (int count){
        try {
            Statement query = con.createStatement();
            String sqlStr = "SELECT TOP "+ count +" * FROM words ORDER BY NEWID();";
            return query.executeQuery(sqlStr);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getMaxCountFromDB (){
        try {
            Statement query = con.createStatement();
            String sqlStr = "SELECT COUNT(id) FROM words;";
            ResultSet max = query.executeQuery(sqlStr);
            int countMax = 0;
            while(max.next()){
               countMax = max.getInt(1);
            }
            return countMax;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static LinkedList<Word> createWordList (ResultSet data){
        LinkedList<Word> wordList = new LinkedList<Word>();

        while(true) {
            try {
                if (!data.next()) break;
                Word word = new Word();
                word.id = data.getInt("id");
                word.wordCz = data.getNString("word_cz");
                word.wordEng = data.getNString("word_eng");
                word.PhoneticsEng = data.getNString("phonetics_eng");
                wordList.add(word);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return wordList;
    }

    public static Word getRandomWord(List<Word> wordList){
        return wordList.get((int) (Math.random() * wordList.size()));
    }
}
