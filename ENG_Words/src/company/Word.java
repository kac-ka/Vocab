package company;

public class Word {
    public int id;
    public String wordCz;
    public String wordEng;
    public String PhoneticsEng;

    public String getQuestion(LangEnum lang){
        String questionWord = "";
        switch (lang){
            case CZ:
                questionWord = this.wordCz;
                break;
            case EN:
                questionWord = this.wordEng;
                break;
        }
        return questionWord;
    }

    public String getAnswer(LangEnum lang){
        switch (lang){
            case CZ:
                return this.wordEng;
            case EN:
                return this.wordCz;
            default: return "";
        }
    }

    public String getPhonetics(LangEnum lang){
        switch (lang){
            case CZ:
                return String.format("Foneticky: %s", this.PhoneticsEng);
            case EN:
                return String.format("Foneticky: %s", this.PhoneticsEng);
            default: return "";
        }
    }
    }
