import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Tennis extends JPanel implements KeyListener, ActionListener {

    private final int WIDTH = 700;
    private final int HEIGHT = 500;

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

        // Ball reset if missed
        if (ballX < 0) {
            ballX = WIDTH / 2;
            ballY = HEIGHT / 2;
        }

        repaint();
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
