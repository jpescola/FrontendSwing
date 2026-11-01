import view.LoginView;

public class Main {
    public static void main(String[] args) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        new LoginView().setVisible(true);
    }
}

