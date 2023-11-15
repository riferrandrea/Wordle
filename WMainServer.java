import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.stream.JsonReader;


public class WMainServer {
	// file di configurazione del server.
	public static final String configFile = "server.properties";
	public static int port;
	// tempo che intercorre nel cambio di parola.
	public static int maxDelay; 

	
	// pool di thread.
	public static final ExecutorService pool = Executors.newCachedThreadPool();
	// socket per ricevere le richieste dei client.
	public static ServerSocket serverSocket;
    
	
	public static void main(String[] args) throws Exception { 
		try {

			ArrayList<Accounts> userJson = new ArrayList<Accounts>();
		   
			// leggo il file di configurazione.
			readConfig();
			// apro la ServerSocket e resto in attesa di richieste.
			serverSocket = new ServerSocket(port);
			
			System.out.printf("[SERVER] In ascolto sulla porta: %d\n", port);

			List<String> words = new ArrayList<String>();
			// leggo le parole all'interno del file "words.txt".
			words = extrapolateWords("words.txt");
			// task per il cambio parola.
			TaskWord taskWord = new TaskWord(words);

			ScheduledExecutorService scheduledWordService = Executors.newSingleThreadScheduledExecutor();

			// cambio periodico della parola.
			scheduledWordService.scheduleAtFixedRate(taskWord, 0L, maxDelay, TimeUnit.SECONDS);

			// recupera lo stato del server ogni volta che viene avviato il server.
			restoreStatus(userJson);

	        while (true) {
	        	Socket socket = null;
	        	// accetto le richieste provenienti dai client.

	        	try {socket = serverSocket.accept();}
	        	catch (SocketException e) {break;}
	        	// avvio il task per interagire con il client.
				pool.execute(new Wordle(socket, words, userJson));
	        }
		
		}
		catch (Exception e) {
			System.err.printf("[SERVER] Errore: %s\n", e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static synchronized void restoreStatus(ArrayList<Accounts> userJson){
		try {
			userJson.clear();
            JsonReader reader = new JsonReader(new FileReader("accounts.json"));
			List<Integer> stats = new ArrayList<Integer>();
			List<String> wordsPlayed = new ArrayList<String>();
			reader.beginArray();
			while(reader.hasNext()) {
				reader.beginObject();
				Accounts account;
				reader.nextName();
                String username = reader.nextString();	
				reader.nextName();
				String password = reader.nextString();
				reader.nextName();
				String status = reader.nextString();
				status = "offline";
				reader.nextName();
				stats = readStoredStats(reader);
				reader.nextName();
				double guessDistribution = reader.nextDouble();
				reader.nextName();
				wordsPlayed = readStoredWords(reader);
				reader.endObject();
				account = new Accounts(username, password, status, stats, guessDistribution, wordsPlayed);
				if(!userJson.contains(account)) userJson.add(account);
			}
				reader.endArray();
				Wordle.updateJson(userJson);
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	}
	
	// legge le statistiche associate ad ogni utente.
	private static List<Integer> readStoredStats(JsonReader reader) throws IOException {
		List<Integer> stats = new ArrayList<Integer>();
		reader.beginArray();
		while(reader.hasNext()){
			stats.add(reader.nextInt());
		}
		reader.endArray();
		return stats;
	}

	// legge le parole giocate da ogni utente.
	private static List<String> readStoredWords(JsonReader reader) throws IOException {
		List<String> words = new ArrayList<String>();
		reader.beginArray();
		while(reader.hasNext()){
			words.add(reader.nextString());
		}
		reader.endArray();
		return words;
	}
	
	public static List<String> extrapolateWords(String nameFile){
		ArrayList<String> chosenwords = new ArrayList<String>();
		try (FileReader vocabolario = new FileReader(nameFile)) {
			try (BufferedReader reader = new BufferedReader(vocabolario)) {
				String word = reader.readLine();
				while(word != null){
					chosenwords.add(word);
					word = reader.readLine();
				}
			} catch (Exception e) {
			    e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return chosenwords;


	}
	
 	public static void readConfig() throws FileNotFoundException, IOException {
		InputStream input = new FileInputStream(configFile);
		Properties prop = new Properties();
		prop.load(input);
		port = Integer.parseInt(prop.getProperty("port"));
		maxDelay = Integer.parseInt(prop.getProperty("maxDelay"));
		input.close();
	}
	
}