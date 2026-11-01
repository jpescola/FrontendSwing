package view;

import service.AuthService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private JPanel principal;
    private JTextField txtUsuario;
    private JButton btnEntrar;
    private JPasswordField txtSenha;

    public LoginView() {
        setContentPane(principal); // exibe o painel principal
        setTitle("Entrar"); // define o título da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // fecha no X
        setResizable(false); // desabilita ajustar largura/altura
        setLocationRelativeTo(null); // Centraliza na tela
        setSize(700, 400); // tamanho da janela

        btnEntrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsuario.getText();
                String senha = String.valueOf(txtSenha.getPassword());

                if (username.isEmpty() || senha.isEmpty()) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Preencha o usuário e a senha!",
                            "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    AuthService authService = new AuthService();
                    String tokenJwt = authService.realizarLogin(username, senha);
                    //JOptionPane.showMessageDialog(getParent(), "Sucesso!");

                    new ProdutoView(tokenJwt).setVisible(true);
                    dispose(); // fecha a janela atual

                } catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Erro ao autenticar: " + ex.getMessage(),
                            "Erro de Login", javax.swing.JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

    }
}
