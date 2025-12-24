public class SongDTO {
    public int songId;
    public String title;
    public String artist;
    public String genre;
    public int duration;
    public String filePath;
    public String imagePath;

    public SongDTO(int songId, String title, String artist, 
            String genre, int duration, String filePath, String imagePath) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.filePath = filePath;
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}