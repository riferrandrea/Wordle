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
