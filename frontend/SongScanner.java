import com.mpatric.mp3agic.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SongScanner {

    private static final String SONGS_DIR =
            "C:/Users/PMLS/Desktop/Music Streaming Platform/songs";
    private static final String COVERS_DIR =
            "C:/Users/PMLS/Desktop/Music Streaming Platform/covers";
    private static final String CSV_OUT =
            "C:/Users/PMLS/Desktop/Music Streaming Platform/songs.csv";

    public static void runScan() throws Exception {

        Files.createDirectories(Paths.get(COVERS_DIR));

        List<String> lines = new ArrayList<>();
        lines.add("songID,title,artist,genre,duration,path,imagePath");

        File dir = new File(SONGS_DIR);
        File[] files = dir.listFiles((d, name) -> {
            String l = name.toLowerCase();
            return l.endsWith(".mp3") || l.endsWith(".wav") || l.endsWith(".ogg");
        });

        if (files == null) files = new File[0];

        int songIdCounter = 1;

        for (File f : files) {

            SongMeta meta = extract(f);
            if (meta == null) continue;

            int songID = songIdCounter++;

            // write cover if present
            if (meta.albumImage != null && meta.albumImage.length > 0) {
                String imgPath = COVERS_DIR + "/" + songID + ".jpg";
                Files.write(Paths.get(imgPath), meta.albumImage);
                meta.imagePath = imgPath;
            }

            String csvLine =
                    songID + "," +
                    escapeCsv(meta.title) + "," +
                    escapeCsv(meta.artist) + "," +
                    escapeCsv(meta.genre) + "," +
                    meta.duration + "," +
                    escapeCsv(meta.path) + "," +
                    escapeCsv(meta.imagePath == null ? "" : meta.imagePath);

            lines.add(csvLine);
        }

        Files.write(Paths.get(CSV_OUT), lines);
        System.out.println("Song scan + CSV creation DONE.");
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\n", " ")
                .replace("\r", " ")
                .replace(",", " ");
    }

    private static SongMeta extract(File f) {

        try {
            Mp3File mp3 = new Mp3File(f);

            String title = f.getName().replaceAll("\\.[^.]+$", "");
            String artist = "";
            String genre = "";
            int duration = (int) Math.round(mp3.getLengthInSeconds());
            byte[] albumImage = null;

            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();
                if (tag.getTitle() != null && !tag.getTitle().trim().isEmpty())
                    title = clean(tag.getTitle());
                if (tag.getArtist() != null && !tag.getArtist().trim().isEmpty())
                    artist = clean(tag.getArtist());
                if (tag.getGenreDescription() != null &&
                        !tag.getGenreDescription().trim().isEmpty())
                    genre = clean(tag.getGenreDescription());
                albumImage = tag.getAlbumImage();

            } else if (mp3.hasId3v1Tag()) {
                ID3v1 tag = mp3.getId3v1Tag();
                if (tag.getTitle() != null && !tag.getTitle().trim().isEmpty())
                    title = clean(tag.getTitle());
                if (tag.getArtist() != null && !tag.getArtist().trim().isEmpty())
                    artist = clean(tag.getArtist());
                if (tag.getGenreDescription() != null &&
                        !tag.getGenreDescription().trim().isEmpty())
                    genre = clean(tag.getGenreDescription());
            }

            if (artist == null || artist.trim().isEmpty())
                artist = "Unknown";
            if (genre == null || genre.trim().isEmpty())
                genre = "Unknown";

            SongMeta meta = new SongMeta();
            meta.title = title;
            meta.artist = artist;
            meta.genre = genre;
            meta.duration = duration;
            meta.path = f.getAbsolutePath().replace("\\", "/");
            meta.albumImage = albumImage;
            meta.imagePath = "";

            return meta;

        } catch (Exception ex) {
            System.out.println(
                    "Failed reading file: " + f.getName() + " -> " + ex.getMessage()
            );
            return null;
        }
    }

    private static String clean(String s) {
        if (s == null) return "";
        s = s.trim();
        s = s.replaceAll("^\\?+", "");
        s = s.replaceAll("[^\\p{Print}\\s]", "");
        return s.trim();
    }

    private static class SongMeta {
        String title;
        String artist;
        String genre;
        int duration;
        String path;
        byte[] albumImage;
        String imagePath;
    }

    // quick test
    public static void main(String[] args) throws Exception {
        runScan();
    }
}
