import java.util.ArrayList;
import java.util.List;

public class Accounts {
    private String username;
    private final String password;
    private String status;
    private List<Integer> statistics;
    private double guessDistribution;
    private List<String> playedWords;
    public Accounts(String username, String password, String status, List<Integer> statistics, double guessDistribution, List<String> playedWords) {
        this.username = username;
        this.password = password;
        this.status = status;
        this.statistics = statistics;
        this.guessDistribution = guessDistribution;
        this.playedWords = playedWords;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return this.status;
    }

    public void setPlayedWords(List<String> playedWords) {
        this.playedWords = playedWords;
    }

    public List<String> getPlayedWords() {
        return this.playedWords;
    }

    public List<Integer> getStatistics() {
        return this.statistics;
    }

    public void setStatistics(Integer games, Integer wins, Integer currentStreak, Integer maxStreak){
        List<Integer> stats = new ArrayList<Integer>();
        stats.add(games);
        stats.add(wins);
        stats.add(currentStreak);
        stats.add(maxStreak);
        this.statistics = stats;        
    }

    public double getGuessDistribution() {
        return this.guessDistribution;
    }

    public void setGuessDistribution(double guessDistribution) {
        this.guessDistribution = guessDistribution;
    } 
}
