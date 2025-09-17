package rumahsakitjiwa.view;

import javax.swing.*;
import java.awt.*;

public class main extends JFrame {

    public main() {
        setTitle("Main Window - Rumah Sakit Jiwa");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Contoh isi jendela utama
        JLabel label = new JLabel("Selamat datang di aplikasi Rumah Sakit Jiwa");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }

    // Untuk testing langsung tanpa login
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new main().setVisible(true));
    }
}
