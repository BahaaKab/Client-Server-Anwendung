package programmieraufgaben;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Die Server-Klasse enthält alle Methoden zum Erstellen, Verwenden und Schließen des Servers.
 *
 * Für die Lösung der Aufgabe müssen die Methoden connect, disconnect,
 * request und extract befüllt werden.
 * Es dürfen beliebig viele Methoden und Klassen erzeugt werden, solange
 * die von den oben genannten Methoden aufgerufen werden.
 */
public class Client {
    //Diese Variable gibt den Socket an an dem die Verbindung aufgebaut werden soll
    private Socket clientSocket;
    private Scanner clientInput;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner serverReply;

    /**
     * Hier werden die Verbindungsinformationen abgefragt und eine Verbindung eingerichtet.
     */
    public void connect() {
        //hier wird die IP-Addresse und Portnummer abgefragt und auf Korrektheit geprüft.
        clientInput = new Scanner(System.in);
        System.out.println("Mit welchem Server wollen Sie sich verbinden?");
        System.out.print("IP-Adresse: ");
        String ipAddress = clientInput.nextLine();
        if(!(ipAddress.equals("127.0.0.1") || ipAddress.equals("localhost"))){
            System.out.println("Falsche IP-Adresse! Aktuell ist nur die IPv4-Adresse" +
                    " 127.0.0.1 und die Eingabe localhost möglich.");
            System.out.println("");
            return;
        }
        System.out.print("Port: ");
        String port = clientInput.nextLine();
        if(!port.equals("2020")){
            System.out.println("Kein korrekter Port! Aktuell ist nur Port 2020 möglich.");
            System.out.println("");
            return;
        }
        //hier wird eine Verbindung zum Server hergestellt.
        clientSocket = new Socket();
        try {
            clientSocket.connect((new InetSocketAddress(ipAddress,Integer.parseInt(port))));
            System.out.println("\nEine TCP-Verbindung zum Server mit IP-Adresse localhost (Port: 2020) wurde " +
                    "hergestellt. Sie können nun Ihre Anfragen an den Server stellen.\n");
        } catch (IOException e) {
            System.out.println("\nFehler beim Verbindungsaufbau! Es konnte keine TCP-Verbindung " +
                    "zum Server mit IP-Adresse localhost (Port: 2020) hergestellt werden.\n");
        }


    }

    /**
     * Hier soll die Verbindung und alle Streams geschlossen werden.
     */
    public void disconnect() {


        try {
            clientSocket.close();
            clientSocket = null;
            if(in != null && out != null && clientInput != null) {
                in.close();
                out.close();
                clientInput.close();
            }
        } catch (IOException e) {
            System.out.println("FEHLER: Sockets und Streams konnten nicht geschlossen werden");
        }
        System.out.println("Die Verbindung zum Sever wurde beendet.\n");
    }

    /**
     * In dieser Methode werden die Eingaben des Benutzers an den Server gesendet und die Antwort empfangen
     * @param userInput Eingabe des Benutzers
     * @return Die vom Server empfangene Nachricht
     */
    public String request(String userInput) {
        String serverInput = "";
        //Initialisierung von PrintWriter, BufferedReader und Scanner, die für den Datenaustausch zwischen Client und Server notwendig sind.
        try {
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            serverReply = new Scanner(in);
            //wenn der Client eine leere Anfrage sendet.
            if(userInput.isEmpty() || userInput.matches("\\s+")){
                userInput = "empty";
            }
            out.println(userInput);
            if(serverReply.hasNextLine()) {
                serverInput = serverReply.nextLine();
            }else{
                System.out.println("Die Verbindung zum Server wurde unterbrochen.");
                clientSocket=null;
            }



        } catch (IOException e) {
        }
        return serverInput;
    }

    /**
     * Die vom Server empfangene Nachricht wird hier für die Konsolenausgabe aufbereitet.
     * @param reply Die vom Server empfangene Nachricht
     * @return Ausgabe für die Konsole
     */
    public String extract(String reply) {
        String[] output;   //die empfangene Nachricht wird mit der Split methode entsprechend aufgeteilt, und in Array output gespeichert.
        String message = "";  //die aufbereitet Nachricht wird hier gespeichert und zurueckgegeben.
        //if-else statements, damit die empfangene Nachricht gemäß ihres Inhalts aufgeteilt und fuer die Konsolenausgabe aufbereitet wird.
        if(reply.startsWith("TIME") || reply.startsWith("DATE") || reply.startsWith("SUM") || reply.startsWith("DIFFERENCE") ||
                reply.startsWith("PRODUCT")) {
            output = reply.split(" ");
            message = output[1] + "\n";
        }
        else if(reply.matches("QUOTIENT\\s[0-9]+.[0-9]+") || reply.equals("QUOTIENT undefined")) {
            output = reply.split(" ");
            message = output[1] + "\n";
        }
        else if(reply.equals("-")){
            message = "";
        }
        else if(reply.startsWith("ECHO")){
            message = reply.replace("ECHO ", "") + "\n";
        }
        else if(reply.startsWith("HISTORY")){
            output = reply.split("-");
            for(int i = output.length-1; i>0; i--){
                message = output[i] + "\n" + message;
            }
        }
        else{
            message = reply + "\n";
        }


        return message;
    }

    /**
     * Gibt den Status der Verbindung an
     * @return Wenn die Verbindung aufgebaut ist: TRUE sonst FALSE
     */
    public boolean isConnected() {
        return (clientSocket != null && clientSocket.isConnected());
    }
}
