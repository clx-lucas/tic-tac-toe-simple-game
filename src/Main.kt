import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager

private const val BOARD_SIZE = 3

fun main() = SwingUtilities.invokeLater {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    TicTacToeWindow().isVisible = true
}

private class TicTacToeWindow : JFrame("Tic-Tac-Toe") {
    private val board = Array(BOARD_SIZE) { Array<String?>(BOARD_SIZE) { null } }
    private val squares = Array(BOARD_SIZE) { arrayOfNulls<JButton>(BOARD_SIZE) }
    private val status = JLabel("X's turn — choose an empty square", SwingConstants.CENTER)
    private var currentPlayer = "X"
    private var gameOver = false

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(430, 540)
        setLocationByPlatform(true)

        val content = TexturedPanel(BorderLayout(16, 16)).apply {
            border = BorderFactory.createEmptyBorder(22, 22, 22, 22)
        }
        content.add(JLabel("TIC-TAC-TOE", SwingConstants.CENTER).apply {
            foreground = TEXT
            font = Font(Font.SANS_SERIF, Font.BOLD, 28)
        }, BorderLayout.NORTH)

        val grid = JPanel(GridLayout(BOARD_SIZE, BOARD_SIZE, 8, 8)).apply {
            background = Color(255, 255, 255, 150)
            isOpaque = false
        }
        repeat(BOARD_SIZE) { row ->
            repeat(BOARD_SIZE) { column ->
                val square = JButton().apply {
                    background = SQUARE
                    foreground = X_COLOR
                    font = Font(Font.SANS_SERIF, Font.BOLD, 54)
                    isFocusPainted = false
                    border = BorderFactory.createLineBorder(BORDER, 1, true)
                    addActionListener { takeTurn(row, column) }
                }
                squares[row][column] = square
                grid.add(square)
            }
        }
        content.add(grid, BorderLayout.CENTER)

        val footer = JPanel(BorderLayout(10, 10)).apply { isOpaque = false }
        status.apply {
            foreground = TEXT
            font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        }
        footer.add(status, BorderLayout.CENTER)
        footer.add(JButton("New game").apply {
            font = Font(Font.SANS_SERIF, Font.BOLD, 14)
            addActionListener { resetGame() }
        }, BorderLayout.SOUTH)
        content.add(footer, BorderLayout.SOUTH)

        contentPane = content
        pack()
    }

    private fun takeTurn(row: Int, column: Int) {
        if (gameOver || board[row][column] != null) return
        board[row][column] = currentPlayer
        squares[row][column]!!.apply {
            text = currentPlayer
            foreground = if (currentPlayer == "X") X_COLOR else O_COLOR
            isEnabled = false
        }

        val winningLine = findWinningLine()
        when {
            winningLine != null -> {
                gameOver = true
                status.text = "$currentPlayer wins!"
                winningLine.forEach { (winningRow, winningColumn) ->
                    squares[winningRow][winningColumn]!!.background = WINNING_SQUARE
                }
                disableRemainingSquares()
            }
            board.all { rowValues -> rowValues.all { it != null } } -> {
                gameOver = true
                status.text = "It's a draw!"
            }
            else -> {
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                status.text = "$currentPlayer's turn — choose an empty square"
            }
        }
    }

    private fun findWinningLine(): List<Pair<Int, Int>>? {
        val lines = listOf(
            listOf(0 to 0, 0 to 1, 0 to 2), listOf(1 to 0, 1 to 1, 1 to 2), listOf(2 to 0, 2 to 1, 2 to 2),
            listOf(0 to 0, 1 to 0, 2 to 0), listOf(0 to 1, 1 to 1, 2 to 1), listOf(0 to 2, 1 to 2, 2 to 2),
            listOf(0 to 0, 1 to 1, 2 to 2), listOf(0 to 2, 1 to 1, 2 to 0)
        )
        return lines.firstOrNull { line ->
            val (firstRow, firstColumn) = line.first()
            val mark = board[firstRow][firstColumn]
            mark != null && line.all { (row, column) -> board[row][column] == mark }
        }
    }

    private fun disableRemainingSquares() {
        squares.flatten().filterNotNull().forEach { it.isEnabled = false }
    }

    private fun resetGame() {
        currentPlayer = "X"
        gameOver = false
        status.text = "X's turn — choose an empty square"
        repeat(BOARD_SIZE) { row -> repeat(BOARD_SIZE) { column ->
            board[row][column] = null
            squares[row][column]!!.apply {
                text = ""
                isEnabled = true
                background = SQUARE
            }
        } }
    }

    private class TexturedPanel(layout: BorderLayout) : JPanel(layout) {
        private val backgroundImage: BufferedImage? = loadBackground()

        init { isOpaque = false }

        override fun paintComponent(graphics: Graphics) {
            val g = graphics.create() as Graphics2D
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, width, height, null)
                // A pale veil preserves the artwork while keeping controls legible.
                g.color = Color(255, 250, 252, 70)
                g.fillRect(0, 0, width, height)
            } else {
                g.color = BACKGROUND
                g.fillRect(0, 0, width, height)
            }
            g.dispose()
            super.paintComponent(graphics)
        }

        private fun loadBackground(): BufferedImage? = runCatching {
            val resource = TicTacToeWindow::class.java.getResourceAsStream("/anime-game-background.png")
            resource?.use(ImageIO::read)
                ?: ImageIO.read(File("src/resources/anime-game-background.png"))
        }.getOrNull()
    }

    private companion object {
        val BACKGROUND = Color(246, 248, 252)
        val SQUARE = Color.WHITE
        val WINNING_SQUARE = Color(220, 247, 232)
        val BORDER = Color(210, 216, 226)
        val TEXT = Color(35, 43, 60)
        val X_COLOR = Color(224, 83, 92)
        val O_COLOR = Color(57, 118, 210)
    }
}
