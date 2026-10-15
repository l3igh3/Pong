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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                paddleY = Math.max(0, Math.min(paddleY, getHeight() - paddleHeight));
                aiPaddleY = Math.max(0, Math.min(aiPaddleY, getHeight() - paddleHeight));
            }
        });

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

        int width = getWidth();
        int height = getHeight(); // Allows for dynamic resizing, but we will use fixed WIDTH and HEIGHT for game logic

        if (gameState == GameState.MENU) {
            g.setColor(Color.WHITE);

            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Tennis Game", width / 2 - 100, height /4);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString(
                "Use UP/DOWN arrows and ENTER to select",
                width / 2 - 190,
                150
            );

            int menuY = height / 2;

            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Select Difficulty", width / 2 - 100, menuY);

            drawMenuOption(g, "Easy", 0, menuY + 50);
            drawMenuOption(g, "Medium", 1, menuY + 100);
            drawMenuOption(g, "Hard", 2, menuY + 150);

            drawMenuOption(g, "Exit", 3, menuY + 200);

            return;
        }

        // Paddle
        g.setColor(Color.WHITE);
        g.fillRect(20, paddleY, paddleWidth, paddleHeight);

       /* 
        //For DEBUGGING
        g.setColor(Color.RED);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        g.drawString(
            "Screen: " + getWidth() + "x" + getHeight(),
            10,
            20
        );
        */

        //AI Paddle
        int aiPaddleX= getWidth() - 40;
        g.fillRect(aiPaddleX, aiPaddleY, paddleWidth, paddleHeight);

        // Ball
        g.fillOval(ballX, ballY, ballSize, ballSize);
        
        //score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Player: " + playerScore, 50, 50);
        g.drawString("Computer: " + aiScore, width - 200, 50);
    }

    private void drawMenuOption(Graphics g, String label, int option, int y) {
        g.setColor(menuSelection == option ? Color.YELLOW : Color.WHITE);
        g.drawString((menuSelection == option ? "> " : "  ") + label, getWidth() / 2 - 50, y);
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
        if (downPressed && paddleY < getHeight() - paddleHeight) {
            paddleY += 5;
        }

        // Ball movement
        ballX += ballVelX;
        ballY += ballVelY;

        // Ball bounce top/bottom
        if (ballY <= 0 || ballY >= getHeight() - ballSize) {
            ballVelY *= -1;
        }

        int aiPaddleX = getWidth() - 40;

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
        if (ballX > getWidth()) {
            playerScore++;
            resetBall();
        }

        repaint();
    }

    private void resetBall() {
        ballX = getWidth() / 2;
        ballY = getHeight() / 2;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        ballVelX = random.nextInt(3, 6) * (random.nextBoolean() ? 1 : -1);
        ballVelY = random.nextInt(2, 6) * (random.nextBoolean() ? 1 : -1);
    }
 

    private void aiMove() {
        int ballCenter = ballY + ballSize / 2;
        int aiCenter = aiPaddleY + paddleHeight / 2;

        if (ballCenter < aiCenter && aiPaddleY > 0) {
            aiPaddleY -= currentAiSpeed;
        } else if (ballCenter > aiCenter && aiPaddleY < getHeight() - paddleHeight) {
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
        frame.setResizable(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
