import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Tetris extends JPanel {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
    private final int BLOCK_SIZE = 30;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int score = 0;
    private int curX = 0;
    private int curY = 0;
    private Shape curPiece;
    private Shape.Tetrominoes[] board;

    public Tetris() {
        initBoard();
    }

    private void initBoard() {
        setFocusable(true);
        addKeyListener(new TAdapter());
        board = new Shape.Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        clearBoard();

        timer = new Timer(400, new GameCycle());
        timer.start();
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; i++) {
            board[i] = Shape.Tetrominoes.NoShape;
        }
    }

    private void dropDown() {
        while (curY < BOARD_HEIGHT - 1 && !isOccupied(curPiece, curX, curY + 1)) {
            curY++;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!isOccupied(curPiece, curX, curY + 1)) {
            curY++;
        }
        pieceDropped();
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.getX(i);
            int y = curY - curPiece.getY(i);
            board[y * BOARD_WIDTH + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean isLineFull = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i * BOARD_WIDTH + j] == Shape.Tetrominoes.NoShape) {
                    isLineFull = false;
                    break;
                }
            }
            if (isLineFull) {
                numFullLines++;
                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[k * BOARD_WIDTH + j] = board[(k + 1) * BOARD_WIDTH + j];
                    }
                }
            }
        }

        if (numFullLines > 0) {
            score += numFullLines;
            isFallingFinished = true;
            curPiece.setShape(Shape.Tetrominoes.NoShape);
            repaint();
        }
    }

    private void newPiece() {
        curPiece = new Shape();
        curX = BOARD_WIDTH / 2 - 1;
        curY = BOARD_HEIGHT - 1 + curPiece.getMinY();

        if (isOccupied(curPiece, curX, curY)) {
            timer.stop();
            isStarted = false;
            JOptionPane.showMessageDialog(this, "Game Over\nScore: " + score);
        }
    }

    private boolean isOccupied(Shape piece, int x, int y) {
        for (int i = 0; i < 4; i++) {
            int px = x + piece.getX(i);
            int py = y - piece.getY(i);
            if (px >= 0 && px < BOARD_WIDTH && py >= 0 && py < BOARD_HEIGHT) {
                if (board[py * BOARD_WIDTH + px] != Shape.Tetrominoes.NoShape) {
                    return true;
                }
            }
        }
        return false;
    }

    private void rotatePiece() {
        Shape rotatedPiece = curPiece.rotateRight();
        if (!isOccupied(rotatedPiece, curX, curY)) {
            curPiece = rotatedPiece;
        }
        repaint();
    }

    private void movePiece(int direction) {
        int newX = curX + direction;
        if (newX >= 0 && newX <= BOARD_WIDTH - curPiece.getMaxX()) {
            if (!isOccupied(curPiece, newX, curY)) {
                curX = newX;
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPiece(g);
    }

    private void drawBoard(Graphics g) {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Shape.Tetrominoes shape = board[i * BOARD_WIDTH + j];
                if (shape != Shape.Tetrominoes.NoShape) {
                    drawSquare(g, j * BLOCK_SIZE, (BOARD_HEIGHT - i - 1) * BLOCK_SIZE, shape);
                }
            }
        }
    }

    private void drawPiece(Graphics g) {
        if (curPiece.getShape() != Shape.Tetrominoes.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.getX(i);
                int y = curY - curPiece.getY(i);
                drawSquare(g, x * BLOCK_SIZE, (BOARD_HEIGHT - y - 1) * BLOCK_SIZE, curPiece.getShape());
            }
        }
    }

    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoes shape) {
        Color[] colors = {new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102),
                new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)};

        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
        g.setColor(color.brighter());
        g.drawLine(x, y + BLOCK_SIZE - 1, x, y);
        g.drawLine(x, y, x + BLOCK_SIZE - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
        g.drawLine(x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + 1);
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isFallingFinished) {
                isFallingFinished = false;
                newPiece();
            } else {
                oneLineDown();
            }
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curPiece.getShape() == Shape.Tetrominoes.NoShape) {
                return;
            }

            int keyCode = e.getKeyCode();

            if (keyCode == KeyEvent.VK_P) {
                isPaused = !isPaused;
                if (isPaused) {
                    timer.stop();
                } else {
                    timer.start();
                }
                return;
            }

            if (isPaused) {
                return;
            }

            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                    movePiece(-1);
                    break;
                case KeyEvent.VK_RIGHT:
                    movePiece(1);
                    break;
                case KeyEvent.VK_DOWN:
                    oneLineDown();
                    break;
                case KeyEvent.VK_UP:
                    rotatePiece();
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new Tetris());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
