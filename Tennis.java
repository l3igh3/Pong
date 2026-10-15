<<<<<<< HEAD
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ThreadLocalRandom;

public class Tennis extends JPanel implements ActionListener {

    private enum GameState {MENU, PLAYING, GAME_OVER}
    private GameState gameState = GameState.MENU;

    private int aiSpeedEasy = 2;
    private int aiSpeedMedium = 4;
    private int aiSpeedHard = 6;
    private int currentAiSpeed = aiSpeedEasy;
    private int menuSelection = 0;

    private final int WIDTH = 700;
    private final int HEIGHT = 500;

    private int playerScore = 0;
    private int aiScore = 0;

    // Game objects
    private int paddleY = 200;
    private final int paddleWidth = 15;
    private final int paddleHeight = 80;

    private int aiPaddleY = 200;
    private final int aiPaddleX = WIDTH - 40;

    private int ballX = 350;
    private int ballY = 250;
    private int ballSize = 20;

    private int ballVelX = 4;
    private int ballVelY = 4;

    private boolean upPressed = false;
    private boolean downPressed = false;

    private Timer timer;

    public Tennis() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);

        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        setFocusable(true);
        bindKey("menu-up", KeyEvent.VK_UP, () -> {
            if (gameState == GameState.MENU) {
                menuSelection = (menuSelection + 3) % 4;
                repaint();
            } else {
                upPressed = true;
            }
        });
        bindKey("menu-down", KeyEvent.VK_DOWN, () -> {
            if (gameState == GameState.MENU) {
                menuSelection = (menuSelection + 1) % 4;
                repaint();
            } else {
                downPressed = true;
            }
        });
        bindKey("up-released", KeyEvent.VK_UP, () -> upPressed = false,
                JComponent.WHEN_IN_FOCUSED_WINDOW, true);
        bindKey("down-released", KeyEvent.VK_DOWN, () -> downPressed = false,
                JComponent.WHEN_IN_FOCUSED_WINDOW, true);
        bindKey("menu-select", KeyEvent.VK_ENTER, this::selectMenuOption);
        bindKey("menu-exit", KeyEvent.VK_ESCAPE, () -> {
           System.exit(0);
            
        });
        bindKey("difficulty-easy", KeyEvent.VK_1, () -> startWithDifficulty(aiSpeedEasy));
        bindKey("difficulty-medium", KeyEvent.VK_2, () -> startWithDifficulty(aiSpeedMedium));
        bindKey("difficulty-hard", KeyEvent.VK_3, () -> startWithDifficulty(aiSpeedHard));
    }

    private void bindKey(String name, int keyCode, Runnable action) {
        bindKey(name, keyCode, action, JComponent.WHEN_IN_FOCUSED_WINDOW, false);
    }

    private void bindKey(String name, int keyCode, Runnable action, int condition, boolean onKeyRelease) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0, onKeyRelease);
        String actionKey = name;
        getInputMap(condition).put(keyStroke, actionKey);
        getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private void selectMenuOption() {
        if (gameState != GameState.MENU) {
            return;
        }

        switch (menuSelection) {
            case 0 -> startWithDifficulty(aiSpeedEasy);
            case 1 -> startWithDifficulty(aiSpeedMedium);
            case 2 -> startWithDifficulty(aiSpeedHard);
            case 3 -> System.exit(0);
            default -> throw new IllegalStateException("Unexpected menu selection: " + menuSelection);
        }
    }

    private void startWithDifficulty(int speed) {
        if (gameState != GameState.MENU) {
            return;
        }
        currentAiSpeed = speed;
        gameState = GameState.PLAYING;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState == GameState.MENU) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Tennis Game", WIDTH / 2 - 100, 100);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString(
                "Use UP/DOWN arrows and ENTER to select",
                WIDTH / 2 - 150,
                150
            );

            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Select Difficulty", WIDTH / 2 - 110, 200);

            drawMenuOption(g, "Easy", 0, 250);
            drawMenuOption(g, "Medium", 1, 300);
            drawMenuOption(g, "Hard", 2, 350);

            drawMenuOption(g, "Exit", 3, 450);

            return;
        }


        // Paddle
        g.setColor(Color.WHITE);
        g.fillRect(20, paddleY, paddleWidth, paddleHeight);

        //AI Paddle
        g.fillRect(aiPaddleX, aiPaddleY, paddleWidth, paddleHeight);

        // Ball
        g.fillOval(ballX, ballY, ballSize, ballSize);
        
        //score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Player: " + playerScore, 50, 50);
        g.drawString("Computer: " + aiScore, WIDTH - 150, 50);
    }

    private void drawMenuOption(Graphics g, String label, int option, int y) {
        g.setColor(menuSelection == option ? Color.YELLOW : Color.WHITE);
        g.drawString((menuSelection == option ? "> " : "  ") + label, WIDTH / 2 - 50, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        // Paddle movement
        if (upPressed && paddleY > 0) {
            paddleY -= 5;
        }
        if (downPressed && paddleY < HEIGHT - paddleHeight) {
            paddleY += 5;
        }

        // Ball movement
        ballX += ballVelX;
        ballY += ballVelY;

        // Ball bounce top/bottom
        if (ballY <= 0 || ballY >= HEIGHT - ballSize) {
            ballVelY *= -1;
        }

        // Ball bounce off paddle
        if (ballX <= 20 + paddleWidth &&
            ballY + ballSize >= paddleY &&
            ballY <= paddleY + paddleHeight) {
            ballVelX *= -1;
        }

        //AI Paddle movement
         if (ballX + ballSize >= aiPaddleX &&
            ballY + ballSize >= aiPaddleY &&
            ballY <= aiPaddleY + paddleHeight) {
            ballVelX *= -1;
        }
        
        aiMove();

        if (ballX < 0) {
            aiScore++;
            resetBall();
        } 
        if (ballX > WIDTH) {
            playerScore++;
            resetBall();
        }

        repaint();
    }

    private void resetBall() {
        ballX = WIDTH / 2;
        ballY = HEIGHT / 2;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        ballVelX = random.nextInt(3, 6) * (random.nextBoolean() ? 1 : -1);
        ballVelY = random.nextInt(2, 6) * (random.nextBoolean() ? 1 : -1);
    }
 

    private void aiMove() {
        int ballCenter = ballY + ballSize / 2;
        int aiCenter = aiPaddleY + paddleHeight / 2;

        if (ballCenter < aiCenter && aiPaddleY > 0) {
            aiPaddleY -= currentAiSpeed;
        } else if (ballCenter > aiCenter && aiPaddleY < HEIGHT - paddleHeight) {
            aiPaddleY += currentAiSpeed;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pong");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Tennis());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
=======
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Tennis extends JPanel implements KeyListener, ActionListener {

    private final int WIDTH = 700;
    private final int HEIGHT = 500;

    private int playerScore = 0;
    private int aiScore = 0;

    // Game objects
    private int paddleY = 200;
    private final int paddleWidth = 15;
    private final int paddleHeight = 80;

    private int aiPaddleY = 200;
    private final int aiPaddleX = WIDTH - 40;
    private final int aiSpeed = 3;

    private int ballX = 350;
    private int ballY = 250;
    private int ballSize = 20;

    private int ballVelX = 4;
    private int ballVelY = 4;

    private boolean upPressed = false;
    private boolean downPressed = false;

    private Timer timer;

    public Tennis() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);

        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Paddle
        g.setColor(Color.WHITE);
        g.fillRect(20, paddleY, paddleWidth, paddleHeight);

        //AI Paddle
        g.fillRect(aiPaddleX, aiPaddleY, paddleWidth, paddleHeight);

        // Ball
        g.fillOval(ballX, ballY, ballSize, ballSize);
        
        //score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Player: " + playerScore, 50, 50);
        g.drawString("AI: " + aiScore, WIDTH - 150, 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Paddle movement
        if (upPressed && paddleY > 0) {
            paddleY -= 5;
        }
        if (downPressed && paddleY < HEIGHT - paddleHeight) {
            paddleY += 5;
        }

        // Ball movement
        ballX += ballVelX;
        ballY += ballVelY;

        // Ball bounce top/bottom
        if (ballY <= 0 || ballY >= HEIGHT - ballSize) {
            ballVelY *= -1;
        }

        // Ball bounce off paddle
        if (ballX <= 20 + paddleWidth &&
            ballY + ballSize >= paddleY &&
            ballY <= paddleY + paddleHeight) {
            ballVelX *= -1;
        }

        //AI Paddle movement
         if (ballX + ballSize >= aiPaddleX &&
            ballY + ballSize >= aiPaddleY &&
            ballY <= aiPaddleY + paddleHeight) {
            ballVelX *= -1;
        }
        
        aiMove();

        if (ballX < 0) {
            aiScore++;
            resetBall();
        } 
        if (ballX > WIDTH) {
            playerScore++;
            resetBall();
        }

        repaint();
    }

    private void resetBall() {
        ballX = WIDTH / 2;
        ballY = HEIGHT / 2;
        ballVelX = -4; // Ball moves towards the player
        ballVelY = 4;
    }
 

    private void aiMove() {
        int ballCenter = ballY + ballSize / 2;
        int aiCenter = aiPaddleY + paddleHeight / 2;

        if (ballCenter < aiCenter && aiPaddleY > 0) {
            aiPaddleY -= aiSpeed;
        } else if (ballCenter > aiCenter && aiPaddleY < HEIGHT - paddleHeight) {
            aiPaddleY += aiSpeed;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pong");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Tennis());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
>>>>>>> 5e269182027f6e19e52793f4622dec4a711f7d13
