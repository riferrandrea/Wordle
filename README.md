# Wordle
progetto universitario valido per esame di Laboratorio III

## Architettura del progetto
- **WMainServer.java**
- **WMainClient.java**
- **Wordle.java**
- **TaskWord.java**
- **Accounts.java**

## Funzionalità
### WMainClient
il cuore del progetto consiste nella comunicazione client-server tramite una connessione TCP, la quale mette in contatto i due main program, ```WMainServer ``` ***WMainServer*** e ***WMainClient***. Quest’ultimo legge informazioni quali hostname e porta su cui connettersi al server dal file ***client.properties***, apre la socket utilizzando queste informazioni, inizializza gli stream utili a inviare e ricevere informazioni con il server ed entra nel ciclo principale. La funzione ***action()*** inizia col prendere in input (da tastiera) un numero tra 1 e 2, che corrisponde a registrarsi o ad accedere ad un account esistente, grazie ad un
primo switch. Una volta loggato, l’input da tastiera cambierà in altre azioni numerate da 3 a 7, e sono rispettivamente il logout, inviare le parole per provare ad indovinare la parola estratta, visualizzare le statistiche relative all’account corrente, condividere messaggi su un gruppo multicast e visualizzare tali messaggi recuperandoli direttamente dal multicast. Il tutto viene gestito con un secondo switch che parte proprio dal caso 2 del primo switch, ovvero dopo che l’utente accede al proprio account. Prima di tale switch però, l’utente si connette al gruppo multicast dove eventualmente condividerà o preleverà i messaggi condivisi da tutti i client connessi.

## WMainServer
inizia con l'estrapolare le informazioni contenute nel file ***server.properties***, la porta su quale collegare i client e il delay, il tempo che intercorre per il cambio parola. Estrapolo tutte le parole dal file ***word.txt*** e le inserisco all’interno di un ArrayList, che utilizzo nel ***TaskWord*** per cambiare la parola periodicamente. Ad ogni avvio del server viene recuperato lo stato generale con annessi account e relative statistiche, leggendo il tutto dal file json ***account.json***. Infine fa partire i vari task ***Wordle*** tramite il pool di task passandoad ognuno la socket su cui accettare le richieste, l'elenco di parole e l'array ***userJson*** contenente tutti gli account registrati.

## Wordle
E’ il task assegnato ai thread del pool per scambiare informazioni con il client. Anche in questo caso, come nel client, dopo aver inizializzato tutte le strutture necessarie, entra nel ciclo principale ricevendo il comando immesso da tastiera da parte dell’utente. Il tutto è gestito con uno switch con i 7 case possibili. Nel ***register()*** faccio comunicare il server con il clinet confrontando l'ArrayList ***userJson*** per verificare l'esistenza di account con lo stesso username, controllo che sia stat inserita una password valida, creo l'account con varie statistiche azzerate, lo aggiungo a ***userJson*** e aggiorno il json mediante ***updateJson***. 
Il ***login()*** si basa sullo stesso concetto del ***register()***, ma oltre a controllare l’esistenza dell’username imposta lo status dell’utente ad “online”. Tengo traccia dell’account corrente in caso di crash in modo da capire, in caso di crash del client, quale account si è scollegato e impostare lo status ad “offline” (gestito dall’eccezione rilevata nel ciclo principale). Anche in questo caso utilizzo ***updateJson***.
Il ***logout()*** funziona allo stesso modo settando lo status “offline”, mettendo la variabile booleana ***stop*** a ***true*** per terminare il ciclo principale e aggiornando il json. Il ***playWordle()*** fa diversi controlli prima di iniziare il gioco effettivo, quali: controllare se la parola da indovinare è stata già giocata dall’utente, se la parola inserita è contenuta all’interno del vocabolario di parole o se è stata già giocata nel game corrente. Per ogni parola corretta provata viene creata una stringa concatenando “+”, “?”, “X” rispettivamente nel caso di lettera corrispondente presente allo stesso posto corretta,
nel caso in cui la lettera è presente all’interno della parola ma non in quella posizione o nel caso in cui la lettera non è affatto presente. Aggiorno il numero di tentativi impiegati dall’utente per indovinare la parola e aggiungo questa stringa all’interno di un ArrayList ***wordTried*** (che contiene  già in prima posizione la parola che si sta provando ad indovinare), che verrà legato all’username corrispondente all’interno di una ConcurrentHashMap ***attemptsToShare***. Aggiorno le statistiche ***game***, ***wins***, ***currentStreak***, ***maxStreak*** e sovrascrivo l'account esistente prima del game all'interno di ***userJson***.
***sendMeStatistics()*** semplicemente invia quest'ultime statistiche all'utente che invoca il metodo. 
***share()*** dopo aver ricevuto l’username dell’utente che sta giocando e dopo aver controllato che nell’array contenente i risultati delle parole provate dall’utente stesso all’interno di ***attemptsToShare*** ci siano tali risultati, crea una stringa unica con tutti gli elementi di questo array per poi inviarla sul multicast, in modo che tutti gli utenti online nel momento dello ***share()*** possono avere la possibilità di vedere il messaggio.

## Accounts 
Classe costituita da metodi getter e setter per modificare e salvare i dati dell’utente e i
suoi progressi.
