package ui

import qr.QRCodeReader
import qr.QRCodeProcessor
import utils.ImageUtils
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Interface utilisateur pour l'application de lecture de QR code
 */
class QRCodeReaderUI : JFrame("Lecteur de QR Code") {
    
    private val qrReader = QRCodeReader()
    private val qrProcessor = QRCodeProcessor()
    
    private val imagePanel = JPanel()
    private val resultPanel = JPanel()
    private val controlPanel = JPanel()
    
    private var currentImage: BufferedImage? = null
    private var processedImage: BufferedImage? = null
    private var imageLabel = JLabel()
    private var resultTextArea = JTextArea(5, 30)
    
    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(800, 600)
        layout = BorderLayout(10, 10)
        
        setupUI()
        isVisible = true
    }
    
    private fun setupUI() {
        // Configuration du panneau d'image
        imagePanel.layout = BorderLayout()
        imagePanel.border = EmptyBorder(10, 10, 10, 10)
        imagePanel.add(JScrollPane(imageLabel), BorderLayout.CENTER)
        
        // Configuration du panneau de résultat
        resultPanel.layout = BorderLayout()
        resultPanel.border = EmptyBorder(10, 10, 10, 10)
        resultTextArea.isEditable = false
        resultTextArea.lineWrap = true
        resultTextArea.wrapStyleWord = true
        resultPanel.add(JLabel("Résultat:"), BorderLayout.NORTH)
        resultPanel.add(JScrollPane(resultTextArea), BorderLayout.CENTER)
        
        // Configuration du panneau de contrôle
        controlPanel.layout = FlowLayout(FlowLayout.CENTER, 10, 10)
        
        val openButton = JButton("Ouvrir une image")
        openButton.addActionListener { openImage() }
        
        val scanButton = JButton("Scanner le QR code")
        scanButton.addActionListener { scanQRCode() }
        
        val processButton = JButton("Prétraiter l'image")
        processButton.addActionListener { preprocessImage() }
        
        val saveButton = JButton("Sauvegarder l'image traitée")
        saveButton.addActionListener { saveProcessedImage() }
        
        controlPanel.add(openButton)
        controlPanel.add(scanButton)
        controlPanel.add(processButton)
        controlPanel.add(saveButton)
        
        // Ajout des panneaux à la fenêtre principale
        val mainPanel = JPanel(BorderLayout(10, 10))
        mainPanel.border = EmptyBorder(10, 10, 10, 10)
        
        val topPanel = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanel, resultPanel)
        topPanel.resizeWeight = 0.7
        
        mainPanel.add(topPanel, BorderLayout.CENTER)
        mainPanel.add(controlPanel, BorderLayout.SOUTH)
        
        add(mainPanel)
    }
    
    private fun openImage() {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Sélectionner une image"
        fileChooser.fileFilter = FileNameExtensionFilter(
            "Images (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", 
            "jpg", "jpeg", "png", "gif", "bmp"
        )
        
        val result = fileChooser.showOpenDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                val selectedFile = fileChooser.selectedFile
                currentImage = ImageIO.read(selectedFile)
                displayImage(currentImage)
                resultTextArea.text = "Image chargée: ${selectedFile.name}"
                processedImage = null
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de l'ouverture de l'image: ${e.message}",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }
    
    private fun scanQRCode() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez d'abord ouvrir une image",
                "Aucune image",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }
        
        try {
            // Utiliser l'image prétraitée si disponible, sinon utiliser l'image originale
            val imageToScan = processedImage ?: currentImage
            
            // Créer un fichier temporaire pour l'image
            val tempFile = File.createTempFile("qrcode_", ".png")
            ImageIO.write(imageToScan, "png", tempFile)
            
            // Lire le QR code
            val result = qrReader.readQRCode(tempFile)
            
            if (result != null) {
                resultTextArea.text = "QR Code détecté!\n\nContenu: $result"
            } else {
                resultTextArea.text = "Aucun QR code trouvé dans l'image."
            }
            
            // Supprimer le fichier temporaire
            tempFile.delete()
            
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors de la lecture du QR code: ${e.message}",
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
    
    private fun preprocessImage() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez d'abord ouvrir une image",
                "Aucune image",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }
        
        try {
            // Prétraiter l'image
            processedImage = ImageUtils.preprocessImage(currentImage!!)
            
            // Afficher l'image prétraitée
            displayImage(processedImage)
            resultTextArea.text = "Image prétraitée avec succès."
            
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors du prétraitement de l'image: ${e.message}",
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
    
    private fun saveProcessedImage() {
        if (processedImage == null) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez d'abord prétraiter une image",
                "Aucune image traitée",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }
        
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Sauvegarder l'image traitée"
        fileChooser.fileFilter = FileNameExtensionFilter("PNG Image", "png")
        
        val result = fileChooser.showSaveDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                var selectedFile = fileChooser.selectedFile
                
                // Ajouter l'extension .png si nécessaire
                if (!selectedFile.name.toLowerCase().endsWith(".png")) {
                    selectedFile = File(selectedFile.absolutePath + ".png")
                }
                
                ImageIO.write(processedImage, "png", selectedFile)
                resultTextArea.text = "Image traitée sauvegardée: ${selectedFile.name}"
                
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la sauvegarde de l'image: ${e.message}",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }
    
    private fun displayImage(image: BufferedImage?) {
        if (image == null) {
            imageLabel.icon = null
            return
        }
        
        // Redimensionner l'image si elle est trop grande pour l'affichage
        val maxWidth = 600
        val maxHeight = 400
        
        val displayImage = if (image.width > maxWidth || image.height > maxHeight) {
            qrProcessor.resizeImage(image, maxWidth, maxHeight)
        } else {
            image
        }
        
        imageLabel.icon = ImageIcon(displayImage)
        imagePanel.revalidate()
        imagePanel.repaint()
    }
}