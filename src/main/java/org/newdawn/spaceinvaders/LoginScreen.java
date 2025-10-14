/*package org.newdawn.spaceinvaders;

import com.google.firebase.auth.UserRecord;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.api.core.ApiFuture;
import org.newdawn.spaceinvaders.firebase.FirebaseService;
import org.newdawn.spaceinvaders.firebase.UserData;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private JLabel statusLabel;

    private Firestore db;

    public LoginScreen() {
        FirebaseService.initialize();
        db = FirebaseService.getFirestore();
        initUI();
    }

    private void initUI() {
        setTitle("🎃 로그인 | Halloween Space Invaders");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel emailLabel = new JLabel("이메일:");
        emailLabel.setBounds(60, 50, 80, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(140, 50, 180, 25);
        add(emailField);

        JLabel passLabel = new JLabel("비밀번호:");
        passLabel.setBounds(60, 90, 80, 25);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(140, 90, 180, 25);
        add(passwordField);

        loginButton = new JButton("로그인");
        signupButton = new JButton("회원가입");
        loginButton.setBounds(80, 140, 100, 35);
        signupButton.setBounds(200, 140, 100, 35);
        add(loginButton);
        add(signupButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setBounds(50, 200, 300, 25);
        add(statusLabel);

        loginButton.addActionListener(e -> login());
        signupButton.addActionListener(e -> signup());
    }

    private void login() {
        try {
            String email = emailField.getText();
            FirebaseService.getAuth().getUserByEmail(email);
            statusLabel.setText("✅ 로그인 성공!");

            // Firestore에서 유저 데이터 로드
            ApiFuture<DocumentSnapshot> future = db.collection("users").document(email).get();
            DocumentSnapshot doc = future.get();
            if (!doc.exists()) {
                // 새 유저면 기본값 저장
                db.collection("users").document(email)
                        .set(new UserData(email, 1, 0, 0));
            }

            SwingUtilities.invokeLater(() -> {
                dispose();
                StartScreen start = new StartScreen();
                start.setLoggedInUser(email);
                start.setVisible(true);
            });
        } catch (Exception ex) {
            statusLabel.setText("❌ 로그인 실패: " + ex.getMessage());
        }
    }

    private void signup() {
        try {
            String email = emailField.getText();
            String pass = new String(passwordField.getPassword());

            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(pass);
            FirebaseService.getAuth().createUser(request);

            db.collection("users").document(email)
                    .set(new UserData(email, 1, 0, 0));

            statusLabel.setText("🎉 회원가입 성공!");
        } catch (Exception ex) {
            statusLabel.setText("❌ 회원가입 실패: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
*/