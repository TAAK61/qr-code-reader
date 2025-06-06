import ui.QRCodeReaderUI
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
    try {
        // Utiliser le look and feel du système
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Lancer l'interface utilisateur dans le thread Swing
    SwingUtilities.invokeLater {
        QRCodeReaderUI()
    }
}
