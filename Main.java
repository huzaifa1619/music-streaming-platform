import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        try {
            SongScanner.runScan();
            BackendBridge.startBackend();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new Dashboard("").setVisible(true));

        Runtime.getRuntime().addShutdownHook(
            new Thread(BackendBridge::shutdown)
        );
    }
}
