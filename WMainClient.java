import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;





public class WMainClient {
	// percorso del file di configurazione del client.
	public static final String configFile = "client.properties";
	// nome host e porta del server.
	public static String hostname;
	public static int port;
	private static Scanner scanner = new Scanner(System.in);
	// Socket e relativi stream di input/output.
	private static Socket socket;
	private static BufferedReader in;
	private static PrintWriter out;
	// variabile per far terminare la connessione.
	private static boolean stop; 
	// array per contenere i messaggi ricevuti dal multicast.
	private static ArrayList<String> receivedMessages;

	
	public static void main(String[] args) {
		try {
			stop = false;
			// leggo il file di configurazione.
			readConfig();
			// apro la socket e gli stream di input e output.
			socket = new Socket(hostname, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		 	out = new PrintWriter(socket.getOutputStream(), true);
			// messaggi ricevuti con showMeSharing().
			receivedMessages = new ArrayList<>();
			 
			 
			// entro nel ciclo principale.
			while (true) {
	
				action();
				if(stop) break;

			}

			scanner.close();
			out.close();
            in.close();
			
			
		}
		catch (Exception e) {
			System.err.printf("Errore: %s\n", e.getMessage());
			System.exit(1);
		}
	
	}
	

	public static void readConfig() throws FileNotFoundException, IOException {
		// leggo hostname e porta dal file di configurazione.
		InputStream input = new FileInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        hostname = prop.getProperty("hostname");
        port = Integer.parseInt(prop.getProperty("port"));
        input.close();
	}


	public static void action() throws IOException {

		System.out.println("PLAY WORDLE! \nSELECT AN ACTION: \n(1) register (2) login");
		System.out.printf("> ");
		// leggo il comando da tastiera.
        String command = scanner.nextLine();
		while(!(command.equals("1") || command.equals("2"))) {
			System.out.println("PLAY WORDLE! \nSELECT AN ACTION: \n(1) register (2) login");
			System.out.printf("> ");
			command = scanner.nextLine();
		}
        // lo invio al server.
        out.println(command);
        // attendo la risposta dal server.
        switch (command) {
			// l'utente vuole registrarsi.
            case "1":
				System.out.println("REGISTER");
				
				System.out.println("username");
				System.out.printf("> ");
				String username = scanner.nextLine();
				out.println(username);
				String feedUser = in.readLine();
				
				// fino a che l'username inserito non è corretto, viene richiesto.
				while(feedUser.equals("username already exists or invalid.")) {
					System.out.println(feedUser);
					System.out.println("username");
					System.out.printf("> ");
					username = scanner.nextLine();
					out.println(username);
					feedUser = in.readLine();
				}
				System.out.println(feedUser);

				System.out.println("password");
				System.out.printf("> ");
				String password = scanner.nextLine();
				out.println(password);
				String feedPass = in.readLine();
				// fino a che la password non è corretta, viene richiesta.
                while(feedPass.equals("insert password.")) {
					System.out.println(feedPass);
					System.out.println("password");
					System.out.printf("> ");
					password = scanner.nextLine();
					out.println(password);
					feedPass = in.readLine();
				}
                System.out.println(feedPass); 
            break;
			case "2":
				// controllo se esistono account.
			    String dbempty = in.readLine();
				if(dbempty.equals("empty")){
					System.out.println("there are no registered accounts.");
					break;
				}
				System.out.println("LOGIN");

				System.out.println("username");
				System.out.printf("> ");
			    String userIn = scanner.nextLine();
				out.println(userIn);
				String feedUserIn = in.readLine();
				// controllo se l'username inserito è stato già registrato. 
				while(feedUserIn.equals("username not found.")) {
					System.out.println(feedUserIn);
					System.out.println("username");
					System.out.printf("> ");
					userIn = scanner.nextLine();
					out.println(userIn);
					feedUserIn = in.readLine();
				}
				
				if(feedUserIn.equals("account already online.")){
					System.out.println(feedUserIn);
					break;
				}
				System.out.println(feedUserIn);

				System.out.println("password");
				System.out.printf("> ");
                String passIn = scanner.nextLine();
				out.println(passIn);
				String feedPassIn = in.readLine();
				while(feedPassIn.equals("password incorrect.")) {
					System.out.println(feedPassIn);
					System.out.println("password");
					System.out.printf("> ");
					passIn = scanner.nextLine();
					out.println(passIn);
					feedPassIn = in.readLine();
				}
				System.out.println(feedPassIn);

				// il client corrente entra nel multicast per ricevere messaggi.
				InetAddress mcastaddr = InetAddress.getByName("228.5.6.7");
				InetSocketAddress group = new InetSocketAddress(mcastaddr, port);
				NetworkInterface netIf = NetworkInterface.getByName("bge0");
				MulticastSocket ms = new MulticastSocket(4000);
				ms.joinGroup(group, netIf);
                
				// esco dal ciclo quando il client non fa il logout.
				while(!stop){			
					System.out.println("SELECT AN ACTION: \n(3) logout (4) play wordle (5) statistics (6) share (7) show");
					
					System.out.printf("> ");
					String command2 = scanner.nextLine();
					System.out.println(command2);

					while(!(command2.equals("3") || command2.equals("4") || command2.equals("5") 
							|| command2.equals("6") || command2.equals("7"))) {
                                System.out.println("SELECT AN ACTION: \n(3) logout (4) play wordle (5) statistics (6) share (7) show");
                                System.out.printf("> ");
                                command2 = scanner.nextLine();
					}

					// lo invio al server.
					out.println(command2);
					// attendo la risposta dal server.
					switch (command2) {

						case "3":
							// effettuo il logout.
							System.out.println("LOGOUT");
							out.println(userIn);
							String feedUserOut = in.readLine();
							System.out.println(feedUserOut);
							stop = true;
							// esco dal multicast.
							ms.leaveGroup(group, netIf);
							ms.close();
						break;
						case "4":
							// gioco.
							System.out.println("PLAYING");
	
							int i = 1;
							System.out.println("check if you played for the current word...");
							out.println(userIn);
							String feedCheckWord = in.readLine();
							// controllo se l'utente ha giocato la parola corrente.
							if (feedCheckWord.equals("already played")){i = 13;};

							while(i <= 12){
								System.out.printf("> ");
								String attempt = scanner.nextLine();
								out.println(attempt);
								String feedAttempt = in.readLine();
								while(feedAttempt.equals("word not in vocabulary or already entered")) {
									System.out.println(feedAttempt);
									System.out.printf("> ");
									attempt = scanner.nextLine();
									out.println(attempt);
									feedAttempt = in.readLine();
								}
								i++;
								String clue = in.readLine();
								System.out.println(clue);
								if(clue.equals("++++++++++")){
									break;
								}
							}
							
							if (i > 12) System.out.println("attempts finished, wait for the next word");
							else System.out.println("you win!");
						break;
						case "5":
							// ricevo le statistiche dell'account corrente.
							out.println(userIn);
							
							System.out.println("STATISTICS:\n");
							String games = in.readLine();
							System.out.printf("games played: %s\n", games);
							String winrate = in.readLine();
							System.out.printf("win rate: %s\n", winrate);
							String currentStreak = in.readLine();
							System.out.printf("current streak: %s\n", currentStreak);
							String maxStreak = in.readLine();
							System.out.printf("max streak: %s\n", maxStreak);
							String guessDistribution = in.readLine();
							System.out.printf("guess distribution: %s\n\n", guessDistribution);
						break; 
						case "6":
							System.out.println("SHARE STATISTICS");
							out.println(userIn);
							System.out.println(in.readLine());
						break;
						case "7":
						    boolean stopReceive = false;

							byte[] buffer = new byte[8192];
							ms.setSoTimeout(1);
							try { 
							// ricevo tutti i messaggi caricati sul multicast.
							while(!stopReceive){
								DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
								ms.receive(dp);
								String packet = new String(dp.getData(), 0, dp.getLength());
                                
								// stampo il messaggio relativo all'ultima parola giocata dall'utente.
								if(!receivedMessages.contains(packet)){
									receivedMessages.add(packet);
									String[] linesPacket;
									linesPacket = packet.split("\n");
									System.out.printf("the statistics of the last game with secret word %s shared by player: %s\n", linesPacket[1], linesPacket[0]);
									for(int j = 2; j < linesPacket.length; j++) {
										System.out.println(linesPacket[j]);
									} 
								}
								}
							}
							catch (SocketTimeoutException e) {
								stopReceive = true;
							}
						break; 
					}
				}
			break;
			default:
				System.out.println("invalid command\n");
			break; 
        }
	}
}


