package programmieraufgaben;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Die Server-Klasse enthält alle Methoden zum Erstellen, Verwenden und Schließen des Servers.
 *
 * Für die Lösung der Aufgabe müssen die Methoden execute, disconnect
 * und checkPort befüllt werden.
 * Es dürfen beliebig viele Methoden und Klassen erzeugt werden, solange
 * die von den oben genannten Methoden aufgerufen werden.
 */
public class Server{
    private int port;
    private ServerSocket ssocket;
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner clientInput;

    /**
     * Diese Methode beinhaltet die gesamte Ausführung (Verbindungsaufbau und Beantwortung
     * der Client-Anfragen) des Servers.
     */
    public void execute() {
        try {
            //hier wird das Server Socket initialisiert.
            if(ssocket==null){
                ssocket = new ServerSocket();
                ssocket.bind(new InetSocketAddress(port));
            }
            //Solange der Server verbunden ist, kann er auf Clientanfragen warten.
            while (true){
                //hier wird die Verbindung zum Client akzeptiert (falls vorhanden).
                client = ssocket.accept();
                //Initialisierung von BufferedReader, PrintWriter und Scanner, um Nachrichten zwischen Server und Client auszutauschen.
                in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
                clientInput = new Scanner(in);
                //ArrayList wird benutzt um die History zu speichern.
                ArrayList<String> history = new ArrayList<String>();
                while(clientInput.hasNext()){
                    String input = clientInput.nextLine();    //Anfrage von Client.
                    if(input.equals("empty")){
                        out.println("Unbekannte Anfrage!");
                        continue;
                    }
                    //hier werden die Clientanfragen in der History hinzugefuegt.(Außer Discard).
                    if (!input.startsWith("DISCARD")) {
                        history.add(input);
                    }
                    String[] numbers; //wird fuer die Rechenoperationen benutzt.
                    //if-else statements, wo die Serverantwort gemäß die Clientanfrage gewählt wird.
                    if (input.equals("GET Time")) {
                        String timePattern = "HH:mm:ss";
                        SimpleDateFormat timeFormat = new SimpleDateFormat(timePattern);
                        String time = timeFormat.format(new Date());
                        out.println("TIME " + time);
                    } else if (input.equals("GET Date")) {
                        String datePattern = "dd.MM.yyyy";
                        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
                        String date = dateFormat.format(new Date());
                        out.println("DATE " + date);
                    }
                    //Regex wird benutzt, um die richtige Format von Clientangabe zu ueberpruefen.
                    else if (input.matches("(ADD|SUB|DIV|MUL)\\s+[0-9]+\\s+[0-9]+") ||
                            input.matches("(ADD|SUB|DIV|MUL)\\s+[0-9]+.[0-9]+\\s+[0-9]+.[0-9]+") ||
                            input.matches("(ADD|SUB|DIV|MUL)\\s+[0-9]+.[0-9]+\\s+[0-9]+") ||
                            input.matches("(ADD|SUB|DIV|MUL)\\s+[0-9]+\\s+[0-9]+.[0-9]+")) {
                        int num1; //in num1 und num2 werden die Zahlen fuer die Rechenoperationen gespeichert.
                        int num2;
                        int result; // hier wird die Resultat der Rechenoperationen gespeichert.
                        Double divresult; //double fuer division.
                        if(input.endsWith(" ")){
                            out.println("Falsches Format");
                        }
                        try {
                            /*split wird benutzt, um die Zahlen der Rechenoperationen zu teilen,und entsprechend die richtige
                            Operation darauf anzuwenden. Die geteilte Zahlen werden in num1 und num2 als Integer gespeichert. */
                            numbers = input.split(" ");
                            if (numbers.length == 3){
                                num1 = Integer.parseInt(numbers[1]);
                                num2 = Integer.parseInt(numbers[2]);
                                if (numbers[0].equals("ADD")) {
                                    result = num1 + num2;
                                    out.println("SUM " + result);
                                } else if (numbers[0].equals("SUB")) {
                                    result = num1 - num2;
                                    out.println("DIFFERENCE " + result);
                                } else if (numbers[0].equals("MUL")) {
                                    result = num1 * num2;
                                    out.println("PRODUCT " + result);
                                } else {
                                    if (num2 != 0) {
                                        divresult = ((double) num1 / (double) num2);
                                        out.println("QUOTIENT " + divresult);
                                    } else {
                                        out.println("QUOTIENT undefined");
                                    }
                                }
                            }else{
                                out.println("Falsches Format");
                            }

                        } catch (NumberFormatException e) {
                            out.println("Falsches Format");
                        }
                    } else if (input.startsWith("ECHO ")) {
                        out.println(input);
                    } else if (input.startsWith("DISCARD ")) {
                        out.println("-");
                    } else if (input.equals("PING")) {
                        out.println("PONG");
                    } else if (input.equals("HISTORY") || input.matches("HISTORY\\s[0-9]+")) {
                        numbers = input.split(" ");
                        int x;
                        String output = "HISTORY";
                        if (history.size() > 1) {
                            //Fall 1: Eingabe ist: HISTORY + parameter (Bsp. HISTORY 5).
                            if (numbers.length == 2 && Integer.parseInt(numbers[1]) < history.size() - 1) {
                                x = Integer.parseInt(numbers[1]);
                                for (int i = history.size() - 2; i >= history.size() - x - 1; i--) {
                                    output = output + "-" + history.get(i); //for split
                                }
                            }//Fall 2: Eingabe ist nur HISTORY. (Bsp. HISTORY). Hier werden alle bisher gestellten Anfragen ausgegeben.
                            else {
                                x = history.size();
                                for (int i = history.size() - 2; i >= history.size() - x; i--) {
                                    output = output + "-" + history.get(i); //for split
                                }
                            }
                            out.println(output);
                        } else {
                            out.println("ERROR Keine Historie vorhanden!");
                        }
                    } else {
                        if(input.startsWith("ADD") || input.startsWith("SUB") || input.startsWith("MUL") || input.startsWith("DIV") ||
                        input.startsWith("ECHO") || input.startsWith("HISTORY")|| input.startsWith("PING") || input.startsWith("GET Time")||
                        input.startsWith("GET Date")){
                            out.println("Falsches Format");
                        }else{
                            out.println("Unbekannte Anfrage!");
                        }

                    }
                }
                client = null;
            }

        } catch (IOException e) {
        }

    }

    /**
     * Hier wird die Verbindung und alle Streams geschlossen.
     */
    public void disconnect() {
        try {
            ssocket.close();
        }catch(java.io.IOException e) {
        }

        if(client != null) {
            try {
                client.close();
                in.close();
                out.close();
                clientInput.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Überprüfung der Port-Nummer und Speicherung dieser in die Klassen-Variable "port"
     * @param port Portnummer als String
     * @return Port-Nummer ist akzeptabel TRUE oder nicht FALSE
     */
    public boolean checkPort(String port) {
        if(port.equals("2020")) {
            this.port=Integer.parseInt(port);
            return true;
        }
        System.out.println("");
        System.out.println("Kein korrekter Port! Aktuell ist nur Port 2020 möglich.");
        System.out.println("");
        return false;
    }

    /**
     * Gibt die akzeptierte und gespeicherte Port-Nummer zurück
     * @return
     */
    public int getPort() {
        return port;
    }

}
