import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Main {
    private static FileLock lock;
    private static FileChannel channel;

    public static void main(String[] args) {
        // Sprawdzamy, czy aplikacja już działa
        if (!isAppActive()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Aplikacja Kalendarz i Zegar jest już uruchomiona w tle!",
                    "Informacja",
                    JOptionPane.WARNING_MESSAGE
            );
            System.exit(0);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            CalendarFrame frame = new CalendarFrame();
            frame.setVisible(true);
        });
    }

    /**
     * Próbuje założyć blokadę na plik w folderze domowym użytkownika.
     * Zwraca true, jeśli to pierwsza instancja, lub false, jeśli program już działa.
     */
    private static boolean isAppActive() {
        try {
            File lockFile = new File(System.getProperty("user.home"), ".calendar_app/app.lock");

            // Upewnij się, że katalog istnieje
            if (!lockFile.getParentFile().exists()) {
                lockFile.getParentFile().mkdirs();
            }

            channel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = channel.tryLock();

            if (lock == null) {
                // Plik jest zablokowany przez inną instancję
                channel.close();
                return false;
            }

            // Automatyczne zwolnienie blokady przy zamknięciu programu
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (lock != null) lock.release();
                    if (channel != null) channel.close();
                } catch (Exception ignored) {}
            }));

            return true;
        } catch (Exception e) {
            // W razie błędu dostępu uznajemy, że nie zablokowano
            return false;
        }
    }
}