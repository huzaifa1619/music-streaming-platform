import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        try {
            BackendBridge.startBackend();
        } catch(Exception e) {
            System.out.println("Backend failed: " + e.getMessage());
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginUI().setVisible(true);
        });
    }
}

