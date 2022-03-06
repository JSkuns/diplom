import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {

    final static int PORT = 8989;
    final static String HOST = "localhost";
    final static String WORD = "или";           // передаваемое слово
    final static String PATHS = "pdfs";         // путь к папке с пдф-ками

    public static void main(String[] args) throws Exception {
        // Server
        Thread thread = new Thread(Main::serverStart);
        thread.start();
        // Client
        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println(WORD);
            String res = prettyPrint(in.readLine());
            System.out.println(res);
        }
    }

    private static String prettyPrint(String json) {
        // делаем красивый вывод
        Gson gson2 = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(json);
        return gson2.toJson(je);
    }

    private static void serverStart() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket clientSocket = serverSocket.accept();
            while (!clientSocket.isClosed()) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.printf("New connection accepted. Port: %d%n", clientSocket.getPort());
                // Process
                String incomingString = in.readLine();
                String outputString = requestProcessing(incomingString);
                // Output to client
                out.printf("%s", outputString);
                clientSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String requestProcessing(String word) throws IOException {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File(PATHS));
        List<PageEntry> list = engine.search(word);
        Type listType = new TypeToken<List<PageEntry>>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(list, listType);
        return json;
    }

}