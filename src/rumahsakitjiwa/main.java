package rumahsakitjiwa;

import rumahsakitjiwa.view.login;

public class main {
    public static void main(String[] args) {
        // Jalankan form login
        javax.swing.SwingUtilities.invokeLater(() -> {
            login login = new login();
            login.setVisible(true);
        });
    }
}
