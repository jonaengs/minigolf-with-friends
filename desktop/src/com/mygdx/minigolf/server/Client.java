package com.mygdx.minigolf.server;


import com.mygdx.minigolf.Game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Random;

class Client {
    static final String[] names = {"Hannah", "Ludo", "Bathilda", "Katie", "Cuthbert", "Phineas", "Sirius", "Amelia", "Susan", "Terry", "Lavender", "Millicent", "Charity", "Frank", "Alecto", "Amycus", "Reginald", "Mary", "Cho", "Penelope", "Michael", "Vincent", "Vincent", "Colin", "Dennis", "Dirk", "Bartemius", "Bartemius", "Roger", "Dawlish", "Fleur", "Gabrielle", "Dedalus", "Amos", "Cedric", "Elphias", "Antonin", "Aberforth", "Albus", "Dudley", "Marjorie", "Petunia", "Vernon", "Marietta", "Arabella", "Argus", "Justin", "Seamus", "Marcus", "Mundungus", "Filius", "Florean", "Cornelius", "Marvolo", "Merope", "Morfin", "Anthony", "Goyle", "Gregory", "Hermione", "Astoria", "Gregorovitch", "Fenrir", "Gellert", "Wilhelmina", "Godric", "Rubeus", "Madam", "Mafalda", "Helga", "Lee", "Bertha", "Igor", "Viktor", "Bellatrix", "Rabastan", "Rodolphus", "Gilderoy", "Alice", "Augusta", "Frank", "Neville", "Luna", "Xenophilius", "Remus", "Edward", "Walden", "Draco", "Lucius", "Narcissa", "Scorpius", "Madam", "Griselda", "Madam", "Olympe", "Ernie", "Minerva", "Cormac", "Graham", "Alastor", "Auntie", "Theodore", "Bob", "Garrick", "Pansy", "Padma", "Parvati", "Peter", "Antioch", "Cadmus", "Ignotus", "Irma", "Sturgis", "Poppy", "Harry", "James", "Lily", "Quirinus", "Helena", "Rowena", "Tom", "Demelza", "Augustus", "Albert", "Newt", "Rufus", "Kingsley", "Stanley", "Aurora", "Rita", "Horace", "Salazar", "Hepzibah", "Zacharias", "Severus", "Alicia", "Pomona", "Pius", "Dean", "Andromeda", "Nymphadora", "Ted", "Travers", "Sybill", "Wilky", "Dolores", "Emmeline", "Romilda", "Septima", "Lord", "Angelina", "Myrtle", "Arthur", "Bill", "Charlie", "Fred", "George", "Ginny", "Hugo", "Molly", "Percy", "Ron", "Rose", "Oliver", "Yaxley", "Blaise"};

    String lobbyId;
    Socket socket;
    BufferedWriter out;
    PushbackInputStream pbin;
    BufferedReader in;
    String name;

    public Client() throws IOException {
        this("localhost", 8888);
    }

    public Client(String name) throws IOException {
        this("localhost", 8888);
        this.name = name;
    }

    public Client(String url, int port) throws IOException {
        socket = new Socket(url, port);
        pbin = new PushbackInputStream(socket.getInputStream());
        in = new BufferedReader(new InputStreamReader(pbin));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        name = names[new Random().nextInt(names.length)];
    }

    public String createLobby() throws IOException {
        send("CREATE NAME: " + name);
        return recv();
    }

    public void joinLobby(String id) throws IOException {
        send("JOIN " + id + " NAME: " + name);
    }

    public void startGame() throws IOException {
        send("ENTER GAME");
    }

    public void send(String msg) throws IOException {
        System.out.println(name + " sends: " + msg);
        out.write(msg + "\n");
        out.flush();
    }

    public String recv() throws IOException {
        String msg = in.readLine();
        System.out.println(name + " recv: " + msg);
        return msg;
    }

    public void close() throws IOException {
        socket.close();
    }

    public void runAsThread() {
        new Thread(() -> {
            Thread.currentThread().setName(this.getClass().getName() + "-" + name);
            while (true) {
                String msg;
                try {
                    if (Utils.isEOF(socket, pbin)) {
                        break;
                    }
                    msg = recv();
                    if (msg != null && msg.contentEquals("ENTER GAME")) {
                        send("GAME READY");
                    }
                } catch (IOException e) {
                    break;
                }
            }
            System.out.println(name + "'s printer exiting...");
        }).start();
    }
}
