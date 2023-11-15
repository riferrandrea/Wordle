import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.*;



public class Wordle implements Runnable {

	private Socket socket;
	// parole presenti nel file "words.txt".
	private List<String> chosenwords;
	private BufferedReader in;
	private PrintWriter out;
	private boolean stop = false;
	// parola da indovinare. 
	private static String guessWord;
	private static InetAddress group;
	// array dove vengono salvati tutti gli account.
	private ArrayList<Accounts> userJson;
	// array contenente le statistiche condivise.
	private static ArrayList<String> messageShared;
	// parola da indovinare.
	private String wordToGuess;
	// parole testate per ogni game.
	private static ArrayList<String> wordsEntered;
	// account corrente che sta giocando.
	private Accounts currentAccount;
	private ConcurrentHashMap<String, ArrayList<String>> attemptsToShare;
    
	
	public Wordle(Socket socket, List<String> chosenwords, ArrayList<Accounts> userJson) {
		this.socket = socket;
		this.chosenwords = chosenwords;
		this.userJson = userJson;
	}	
	

	public void run() {
        
		try {

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			group = InetAddress.getByName("228.5.6.7");
			attemptsToShare = new ConcurrentHashMap<>();
			messageShared = new ArrayList<>();
			currentAccount = new Accounts(null, null, null, null, 0, null);
			while(true){
				
				action();
				if(stop) break;

			}  
			in.close();
			out.close();
			socket.close();
		}
		catch (Exception e) {
			// in caso di crash, aggiorno lo status dell'utente corrente.
			if(e.getMessage().equals("Connection reset")) {
				for(int i = 0; i < userJson.size(); i++) {
					if(userJson.get(i).getUsername().equals(currentAccount.getUsername())){
						userJson.get(i).setStatus("offline");
						break;
					}
				}
				updateJson(userJson);
				
			}
		}
	}

	// aggiorno periodicamente mediante TaskWord la parola corrente.
	public static void setWordToGuess(String word){
		guessWord = word;
	}

	public static String getWordToGuess(){
	    return guessWord;
    }

    
	
	public void action() throws IOException {
		
		// ricevo il comando da eseguire dal client.
		String line = in.readLine();
		switch (line) {
			case "1":
				register();
			break;
			case "2":
				// controllo se esistono account registrati.
				if (userJson.isEmpty()){ 
					String message = "empty";
					out.printf("%s\n", message);
				} else {
					String message = "not empty";
					out.printf("%s\n", message);
					login();
				}
            break;
			case "3":
				logout();
			break;
			case "4":
                playWordle();
            break;
			case "5":
                sendMeStatistics();
            break; 
			case "6":
			    share();
            break;
			case "7":
            break;
			default:
				out.printf("invalid command\n");
			break;
			}
    }

	// trovo la posizione dell'account corrispondente all'username.
	public synchronized Integer getPosAccount(String username){
		int pos = 0;
		for(int k = 0; k < userJson.size(); k++){
			if (userJson.get(k).getUsername().equals(username)) {
				pos = k;
				break;
			}
		}
		return pos;
	}

	// aggiorno il file json "accounts.json".
	public static synchronized void updateJson(List<Accounts> userJson){
		try (PrintWriter jout = new PrintWriter(new FileWriter("accounts.json"))) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonString = gson.toJson(userJson);
			jout.write(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	// controllo se all'interno dell'array di accounts esiste quello corrispondente ad username. 
	public synchronized boolean containUsername(String username) throws IOException {
		for(int i = 0; i < userJson.size(); i++){
			if(userJson.get(i).getUsername().equals(username)){
				return true;
			}
		} 
		return false;
	}

	// controllo se la password corrispondente all'username sia corretta.
	public synchronized boolean correctPassword(String username, String password) throws IOException {
		for(int k = 0; k < userJson.size(); k++){
			if(userJson.get(k).getUsername().equals(username) && userJson.get(k).getPassword().equals(password)){
				return true;
			}
		}
		return false;
	}

	public void register() throws IOException {
		String message;
        String username = in.readLine();
		// controllo se esistono account, se esistono controllo che l'utente non si registri con un username esistente.
		if(!userJson.isEmpty()){
			while((containUsername(username) || username.equals(""))){
				
				message = "username already exists or invalid.";
				out.printf("%s\n", message);
				
				username = in.readLine();
			}
		}
		message = "username accepted.";
		out.printf("%s\n", message);
		
		String password = in.readLine();

		// controllo che l'utente inserisca una password.
		while(password.equals("")){
			message = "insert password.";
			out.printf("%s\n", message);
			password = in.readLine();
		}
		message = "password accepted.";
		out.printf("%s\n", message);

		// creo l'account.
		ArrayList<Integer> stats = new ArrayList<Integer>();
		stats.add(0); // games
		stats.add(0); // wins
		stats.add(0); // currentStreak
		stats.add(0); // maxStreak
		
		Accounts account = new Accounts(username, password, "offline",stats, 0,  new ArrayList<String>());
		
		userJson.add(account);
		updateJson(userJson);
		
	}
		

	public void login() throws IOException {

		String message;
		// posizione dell'account che accede all'interno di userJson.
		int pos = 0;
		String username = in.readLine();
	
		// controllo se l'username è già registrato. 
        while(!containUsername(username)){
			message = "username not found.";
			out.printf("%s\n", message);
			username = in.readLine();
        }

		pos = getPosAccount(username);
	
		// controllo se l'account legato all'username è già online.
		if (userJson.get(pos).getStatus().equals("online")){
			message = "account already online.";
			out.printf("%s\n", message);
			return;
		}

		message = "username accepted.";
		out.printf("%s\n", message);

		String password = in.readLine();
		while(!correctPassword(username, password)){
			message = "password incorrect.";
			out.printf("%s\n", message);
			password = in.readLine();
		}

		// cambio lo status dell'utente in online.
		userJson.get(pos).setStatus("online");
		
		// aggiorno l'account corrente attivo.
		currentAccount = userJson.get(pos);
		updateJson(userJson);

		message = "account online.";
		out.printf("%s, %s\n", username, message);	
		
	}

	public void logout() throws IOException {

		String message;
		String username = in.readLine();

		userJson.get(getPosAccount(username)).setStatus("offline");

		message = "account offline.";
		out.printf("%s: %s\n", username, message);
		// termino la connessione con il client.
		stop = true;
		updateJson(userJson);

	}

	public void playWordle() throws IOException {
		// array dove salvo i risultati dei tentativi della parola corrente.
		ArrayList<String> wordTried = new ArrayList<>();
		// aggiorno la parola da indovinare.
		wordToGuess = getWordToGuess();
		// stampo la parola da indovinare nel server [test].
		
		String username = in.readLine();
		String message;
		// inizializzo il numero di tentativi effettuati a 13.
		Integer guess = 13; 
		// variabile per controllare se la parola è stata già giocata.
		boolean containPlayedWords = false;
		
		int pos = 0;
		int j = 1;
		
		for(int i = 0; i < userJson.size(); i++) {
			if(userJson.get(i).getUsername().equals(username)){
				if(userJson.get(i).getPlayedWords().contains(wordToGuess)){
					containPlayedWords = true;
					break;
				}
			}
		}
		
		if (containPlayedWords) {
			message = "already played";
			out.printf("%s\n", message);
			j = 13;
		} else {
			System.out.printf("user: %s; word to guess: %s\n", username, wordToGuess);
			message = "no-longer played";
			out.printf("%s\n", message);
		}
		wordTried.add(wordToGuess);
		wordsEntered = new ArrayList<>();
		while(j <= 12){
			String attempt = in.readLine();

			// controllo se la parola provata sia nel vocabolario o se è stata già testata.
			while(!chosenwords.contains(attempt.toLowerCase()) || wordsEntered.contains(attempt.toLowerCase())){
				message = "word not in vocabulary or already entered";
				out.printf("%s\n", message);
				attempt = in.readLine();
			}

			wordsEntered.add(attempt);
			message = "word accepted";
			out.printf("%s\n", message);

			String clue = "";
			for(int i = 0; i <= 9; i++){
				if(attempt.toLowerCase().substring(i,i+1).equals(wordToGuess.substring(i,i+1))){
					clue = clue + "+";
				}
				else if (wordToGuess.contains(attempt.toLowerCase().substring(i, i+1))){
					clue = clue + "?";
				}
				else {
					clue = clue + "X";
				}
			}
			
			if (clue.equals("++++++++++")){
				guess = j;
				j = 13;
			}
			else j++;
			out.println(clue);
			wordTried.add(clue);
			
		}

		// salvo all'interno della concurrent hash map la coppia username, con relativo array contenente
		// oltre che alla parola da indovinare (in prima posizione) anche i relativi tentativi
		if(attemptsToShare.containsKey(username)) attemptsToShare.replace(username, wordTried);
		else attemptsToShare.put(username, wordTried);

		pos = getPosAccount(username);
		

		List<String> playedWords = new ArrayList<>();
		for (String word: userJson.get(pos).getPlayedWords()){
			playedWords.add(word);
		}

		if(!containPlayedWords) {
			// aggiorno sia l'array di parole giocate che le statistiche.
			playedWords.add(wordToGuess);
			// aggiorno l'array di parole giocate.
			userJson.get(pos).setPlayedWords(playedWords);
			// aggiorno il numero medio di tentativi prima di arrivare al risultato. 
			userJson.get(pos).setGuessDistribution((Math.round((userJson.get(pos).getGuessDistribution()*userJson.get(pos).getStatistics().get(0)+guess)/(userJson.get(pos).getStatistics().get(0)+1)*100.0) / 100.0));

		}	
		List<Integer> statistics = new ArrayList<>();
		for (Integer stat: userJson.get(pos).getStatistics()){
			statistics.add(stat);
		}
		// l'utente ha finito i tentativi e non ha vinto.
		if(guess == 13){
			if(!containPlayedWords){
				// il numero di game aumenta.
				statistics.set(0, statistics.get(0)+1);
				// il numero di vittorie rimane uguale.
				statistics.set(1, statistics.get(1));
				// la current streak si azzera.
				statistics.set(2, 0); 

			}
		} else {
			// viene incrementato tutto di 1.
			statistics.set(0, statistics.get(0)+1);
			statistics.set(1, statistics.get(1)+1);
			statistics.set(2, statistics.get(2)+1);
	
		}
		// controllo qual è la streak piu alta.
		if(statistics.get(2) > statistics.get(3)) statistics.set(3, statistics.get(2));
		userJson.get(pos).setStatistics(statistics.get(0), statistics.get(1), statistics.get(2), statistics.get(3));
		
		for(int i = 0; i < userJson.size(); i++) {
			if(userJson.get(i).getUsername().equals(username)) {
                userJson.set(i, userJson.get(pos));
                break;
            }
		}

		updateJson(userJson);
	}

	public void sendMeStatistics() throws IOException {
		String username = in.readLine();
		List<Integer> statistics = new ArrayList<>();
		int pos = 0;
		pos = getPosAccount(username);
		for (Integer stat: userJson.get(pos).getStatistics()){
			statistics.add(stat);
		}
		// invio statistiche al client.
		
		// game giocati.
		out.printf("%s\n", statistics.get(0));
		// vittorie effettuate.
		out.printf("%s\n", Math.round((float)statistics.get(1)/(float)statistics.get(0)*100));
		// streak corrente.
		out.printf("%s\n", statistics.get(2));
		// streak maggiore ottenuta.
		out.printf("%s\n", statistics.get(3));
		// numero di tentativi medio per arrivare alla soluzione. 
		out.printf("%s\n", userJson.get(pos).getGuessDistribution());
	} 

	public void share() throws IOException{
		String username = in.readLine();
		// nel caso di 0 tentativi, non ci sono statistiche da condividere (in posizione 1 c'è la parola da indovinare dell'ultimo game).
		if(attemptsToShare.get(username).size() == 1) return;
		// parola di indovinare correlata ai tentativi. 
		String currentWord = attemptsToShare.get(username).get(0);
		// stringa che unifica tutti i tentativi effettuati (comprende l'username, utilizzato nella stampa).
		String statsAttempts = username + "\n";
	
		try(DatagramSocket socket = new DatagramSocket()){

			statsAttempts = statsAttempts.concat(currentWord + "\n");
			for(int i = 1; i < attemptsToShare.get(username).size(); i++){
			statsAttempts = statsAttempts.concat(attemptsToShare.get(username).get(i)) + "\n";
			}
			// messageShared contiene tutti i messaggi condivisi.
			messageShared.add(statsAttempts);
			// invio le statistiche sul multicast.
			for(int k = 0; k < messageShared.size(); k++){
				DatagramPacket packet = new DatagramPacket(messageShared.get(k).getBytes(), messageShared.get(k).getBytes().length, group, 4000);
				socket.send(packet);
			}

			String message = "shared";
			out.printf("%s\n", message); 

		} catch (IOException e){
			throw new RuntimeException();
		}
	}

}
   

