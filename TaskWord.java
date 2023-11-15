import java.util.List;

public class TaskWord implements Runnable {
    private static List<String> chosenwords;

    public TaskWord(List<String> chosenwords) {
        TaskWord.chosenwords = chosenwords;
    }

    public void run() {
        String wordToGuess = chosenwords.get((int)Math.floor(Math.random() * chosenwords.size()));
        Wordle.setWordToGuess(wordToGuess);
        System.out.println(wordToGuess);

    }
    
}
