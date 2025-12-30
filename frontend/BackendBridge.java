import java.io.*;
import java.util.*;

public class BackendBridge {

    private static Process backend;
    private static PrintWriter out;
    private static BufferedReader in;

    // ==================== PROCESS CONTROL ====================

    public static void startBackend() {
        if (backend != null && backend.isAlive()) return;

        try {
            backend = new ProcessBuilder("C:/Users/PMLS/Desktop/Music Streaming Platform/backend.exe")
                    .redirectErrorStream(true)
                    .start();

            out = new PrintWriter(
                    new OutputStreamWriter(backend.getOutputStream()), true);
            in = new BufferedReader(
                    new InputStreamReader(backend.getInputStream()));

            // wait for READY
            String line = in.readLine();
            if (!"READY".equals(line)) {
                throw new RuntimeException("Backend not ready: " + line);
            }

        } catch (Exception e) {
            throw new RuntimeException("Backend start failed", e);
        }
    }

    public static boolean isRunning() {
        return backend != null && backend.isAlive();
    }

    public static void shutdown() {
        if (!isRunning()) return;
        send("EXIT");
        backend.destroy();
        backend = null;
    }

    public static void setUser(String user) {
    if (!isRunning()) startBackend();
    String result = send("SET_USER " + user);
    System.out.println("SET_USER -> " + result);
}

    public static boolean signupFull(String fullname, String user, String pass){
    if(!isRunning()) startBackend();
    String result = send("SIGNUP " + user + " " + pass + " " + fullname.replace(" ","_"));
    return result.equals("OK");
}


    public static boolean signup(String username, String password) {
        if (!isRunning()) {
            startBackend();
        }
        String result = send("SIGNUP " + username + " " + password);
        return "OK".equals(result);
    }

    public static boolean login(String username, String password) {
        if (!isRunning()) {
            startBackend();
        }
        String result = send("LOGIN " + username + " " + password);
        return "OK".equals(result);
    }


    // ==================== CORE COMM ====================

    private static String send(String cmd) {
        out.println(cmd);
        try {
            return in.readLine();
        } catch (IOException e) {
            return "ERROR";
        }
    }

    private static List<String> sendMulti(String cmd) {
        List<String> lines = new ArrayList<>();
        out.println(cmd);

        try {
            String line;
            while ((line = in.readLine()) != null) {
                if ("END".equals(line)) break;
                lines.add(line);
            }
        } catch (IOException ignored) {}

        return lines;
    }

    // ==================== SONGS ====================

    public static List<SongDTO> getAllSongs() {
        return parseSongs(sendMulti("GET_ALL"));
    }

    public static List<SongDTO> searchSongs(String query) {
        return parseSongs(sendMulti("SEARCH " + query));
    }

    public static SongDTO getSong(int id) {
        out.println("GET_SONG " + id);
        try {
            String line = in.readLine();
            if (line == null || line.isEmpty()) return null;
            return parseSong(line);
        } catch (IOException e) {
            return null;
        }
    }

    // ==================== PLAY / RECENT ====================

    public static void playSong(int songId) {
        send("PLAY " + songId);
    }

    // ==================== FAVORITES ====================

    public static boolean addFavorite(int songId) {
        return send("FAV_ADD " + songId).equals("OK");
    }

    public static boolean removeFavorite(int songId) {
        return send("FAV_REMOVE " + songId).equals("OK");
    }

    public static boolean isFavorite(int songId) {
        return send("FAV_CHECK " + songId).equals("YES");
    }

    public static List<SongDTO> getFavorites() {
        return parseSongs(sendMulti("FAV_LIST"));
    }

    // ==================== PLAYLIST ====================

    public static boolean createPlaylist(int playlistId, String name) {
        return send("PL_CREATE " + playlistId + " " + name).equals("OK");
    }

    public static boolean addToPlaylist(int playlistId, int songId) {
        return send("PL_ADD " + playlistId + " " + songId).equals("OK");
    }
    public static List<SongDTO> getPlaylist(int playlistId) {
        return parseSongs(sendMulti("PL_GET " + playlistId));
    }

    public static boolean removeFromPlaylist(int playlistId, int songId) {
        return send("PL_REMOVE " + playlistId + " " + songId).equals("OK");
    }

    public static List<String> getPlaylists() {
        return sendMulti("PL_LIST");
    }


    // ==================== RECOMMENDATION ====================

    public static List<SongDTO> recommend(int songId) {
    send("RECOMMEND " + songId);

    List<SongDTO> result = new ArrayList<>();
    String line;

    try {
        while (!(line = in.readLine()).equals("END")) {
            result.add(parseSong(line));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return result;
}

    // ==================== RECENTLY PLAYED ====================
    public static List<SongDTO> getRecent() {
        return parseSongs(sendMulti("GET_RECENT"));
    }


    // ==================== PARSING ====================

    private static SongDTO parseSong(String line) {
        String[] p = line.split(",", 7);

        return new SongDTO(
                Integer.parseInt(p[0]),
                p[1],
                p[2],
                p[3],
                Integer.parseInt(p[4]),
                p[5],
                p.length > 6 ? p[6] : ""
        );
    }

    private static List<SongDTO> parseSongs(List<String> lines) {
        List<SongDTO> list = new ArrayList<>();
        for (String l : lines) {
            if (!l.isEmpty() && Character.isDigit(l.charAt(0))) {
                list.add(parseSong(l));
            }
        }
        return list;
    }
}
