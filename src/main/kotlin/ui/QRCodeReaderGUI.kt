package ui

import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.KeyCode
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.Stage
import qr.QRCodeReader
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Modern JavaFX GUI for QR Code Reader
 * Implements tasks from tasks.md:
 * - Task 41: Graphical user interface (GUI)
 * - Task 42: Drag-and-drop support
 * - Task 43: Image preview
 * - Task 44: History feature
 * - Task 46: Batch processing
 * - Task 48: User-friendly error reporting
 * - Task 50: Keyboard shortcuts
 */
class QRCodeReaderGUI : Application() {
    
    private val qrReader = QRCodeReader()
    private val historyData: ObservableList<QRResult> = FXCollections.observableArrayList()
    
    // UI Components
    private lateinit var imageView: ImageView
    private lateinit var resultTextArea: TextArea
    private lateinit var statusLabel: Label
    private lateinit var progressBar: ProgressBar
    private lateinit var historyTable: TableView<QRResult>
    private lateinit var batchListView: ListView<String>
    private val batchFiles = FXCollections.observableArrayList<String>()
    
    override fun start(primaryStage: Stage) {
        primaryStage.title = "QR Code Reader v2.0 - Modern UI"
        primaryStage.isResizable = true
        primaryStage.minWidth = 900.0
        primaryStage.minHeight = 600.0
        
        val root = createMainLayout()
        setupKeyboardShortcuts(root, primaryStage)
        
        val scene = Scene(root, 1200.0, 800.0)
        scene.stylesheets.add(javaClass.getResource("/styles/modern-style.css")?.toExternalForm() ?: "")
        
        primaryStage.scene = scene
        primaryStage.show()
        
        updateStatus("Ready - Drop an image or click 'Select Image' to start")
    }
    
    private fun createMainLayout(): BorderPane {
        val root = BorderPane()
        root.padding = Insets(10.0)
        
        // Top toolbar
        root.top = createToolbar()
        
        // Center content with tabs
        root.center = createCenterTabs()
        
        // Bottom status bar
        root.bottom = createStatusBar()
        
        return root
    }
    
    private fun createToolbar(): ToolBar {
        val toolbar = ToolBar()
        
        val selectButton = Button("📁 Select Image")
        selectButton.setOnAction { selectImage() }
        
        val batchButton = Button("📦 Batch Process")
        batchButton.setOnAction { processBatch() }
        
        val clearHistoryButton = Button("🗑️ Clear History")
        clearHistoryButton.setOnAction { clearHistory() }
        
        val aboutButton = Button("ℹ️ About")
        aboutButton.setOnAction { showAbout() }
        
        toolbar.items.addAll(
            selectButton,
            Separator(),
            batchButton,
            Separator(),
            clearHistoryButton,
            Separator(),
            aboutButton
        )
        
        return toolbar
    }
    
    private fun createCenterTabs(): TabPane {
        val tabPane = TabPane()
        
        // Main processing tab
        val mainTab = Tab("🔍 QR Scanner")
        mainTab.isClosable = false
        mainTab.content = createMainContent()
        
        // Batch processing tab
        val batchTab = Tab("📦 Batch Processing")
        batchTab.isClosable = false
        batchTab.content = createBatchContent()
        
        // History tab
        val historyTab = Tab("📚 History")
        historyTab.isClosable = false
        historyTab.content = createHistoryContent()
        
        tabPane.tabs.addAll(mainTab, batchTab, historyTab)
        
        return tabPane
    }
    
    private fun createMainContent(): VBox {
        val mainContent = VBox(10.0)
        mainContent.padding = Insets(15.0)
        
        // Drag and drop area
        val dropArea = createDragDropArea()
        
        // Image preview and results
        val contentHBox = HBox(15.0)
        
        // Left: Image preview
        val imagePane = createImagePreviewPane()
        
        // Right: Results
        val resultsPane = createResultsPane()
        
        contentHBox.children.addAll(imagePane, resultsPane)
        HBox.setHgrow(imagePane, Priority.ALWAYS)
        HBox.setHgrow(resultsPane, Priority.ALWAYS)
        
        mainContent.children.addAll(dropArea, contentHBox)
        VBox.setVgrow(contentHBox, Priority.ALWAYS)
        
        return mainContent
    }
    
    private fun createDragDropArea(): VBox {
        val dropArea = VBox(10.0)
        dropArea.alignment = Pos.CENTER
        dropArea.style = "-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-color: #ecf0f1;"
        dropArea.padding = Insets(20.0)
        dropArea.minHeight = 100.0
        
        val icon = Label("📷")
        icon.style = "-fx-font-size: 48px;"
        
        val label = Label("Drag & Drop images here or click 'Select Image'")
        label.style = "-fx-font-size: 16px; -fx-text-fill: #2c3e50;"
        
        val supportedFormats = Label("Supported: PNG, JPG, JPEG, GIF, BMP")
        supportedFormats.style = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;"
        
        dropArea.children.addAll(icon, label, supportedFormats)
        
        // Setup drag and drop (Task 42)
        setupDragAndDrop(dropArea)
        
        return dropArea
    }
    
    private fun createImagePreviewPane(): VBox {
        val imagePane = VBox(10.0)
        imagePane.style = "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1;"
        imagePane.padding = Insets(10.0)
        
        val title = Label("📷 Image Preview")
        title.style = "-fx-font-size: 14px; -fx-font-weight: bold;"
        
        imageView = ImageView()
        imageView.fitWidth = 400.0
        imageView.fitHeight = 300.0
        imageView.isPreserveRatio = true
        imageView.style = "-fx-background-color: #f8f9fa;"
        
        val scrollPane = ScrollPane(imageView)
        scrollPane.isFitToWidth = true
        scrollPane.isFitToHeight = true
        
        imagePane.children.addAll(title, scrollPane)
        VBox.setVgrow(scrollPane, Priority.ALWAYS)
        
        return imagePane
    }
    
    private fun createResultsPane(): VBox {
        val resultsPane = VBox(10.0)
        resultsPane.style = "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1;"
        resultsPane.padding = Insets(10.0)
        
        val title = Label("📋 QR Code Content")
        title.style = "-fx-font-size: 14px; -fx-font-weight: bold;"
        
        resultTextArea = TextArea()
        resultTextArea.isWrapText = true
        resultTextArea.isEditable = false
        resultTextArea.promptText = "QR code content will appear here..."
        resultTextArea.style = "-fx-font-family: 'Courier New'; -fx-font-size: 12px;"
        
        val copyButton = Button("📋 Copy to Clipboard")
        copyButton.setOnAction { copyToClipboard() }
        
        resultsPane.children.addAll(title, resultTextArea, copyButton)
        VBox.setVgrow(resultTextArea, Priority.ALWAYS)
        
        return resultsPane
    }
    
    private fun createBatchContent(): VBox {
        val batchContent = VBox(15.0)
        batchContent.padding = Insets(15.0)
        
        val title = Label("📦 Batch Processing")
        title.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        
        val instructionLabel = Label("Add multiple image files to process them all at once:")
        
        val buttonBox = HBox(10.0)
        val addFilesButton = Button("➕ Add Files")
        addFilesButton.setOnAction { addBatchFiles() }
        
        val removeButton = Button("➖ Remove Selected")
        removeButton.setOnAction { removeBatchFile() }
        
        val clearButton = Button("🗑️ Clear All")
        clearButton.setOnAction { batchFiles.clear() }
        
        buttonBox.children.addAll(addFilesButton, removeButton, clearButton)
        
        batchListView = ListView(batchFiles)
        batchListView.placeholder = Label("No files added yet. Click 'Add Files' to start.")
        
        val processBatchButton = Button("🚀 Process All Files")
        processBatchButton.setOnAction { processBatch() }
        processBatchButton.style = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;"
        
        batchContent.children.addAll(title, instructionLabel, buttonBox, batchListView, processBatchButton)
        VBox.setVgrow(batchListView, Priority.ALWAYS)
        
        return batchContent
    }
    
    private fun createHistoryContent(): VBox {
        val historyContent = VBox(15.0)
        historyContent.padding = Insets(15.0)
        
        val title = Label("📚 Processing History")
        title.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        
        historyTable = TableView(historyData)
        
        val timeColumn = TableColumn<QRResult, String>("Time")
        timeColumn.cellValueFactory = PropertyValueFactory("timestamp")
        timeColumn.prefWidth = 150.0
        
        val fileColumn = TableColumn<QRResult, String>("File")
        fileColumn.cellValueFactory = PropertyValueFactory("fileName")
        fileColumn.prefWidth = 200.0
        
        val contentColumn = TableColumn<QRResult, String>("QR Content")
        contentColumn.cellValueFactory = PropertyValueFactory("content")
        contentColumn.prefWidth = 300.0
        
        val statusColumn = TableColumn<QRResult, String>("Status")
        statusColumn.cellValueFactory = PropertyValueFactory("status")
        statusColumn.prefWidth = 100.0
        
        historyTable.columns.addAll(timeColumn, fileColumn, contentColumn, statusColumn)
        historyTable.placeholder = Label("No QR codes processed yet.")
        
        val exportButton = Button("💾 Export History")
        exportButton.setOnAction { exportHistory() }
        
        historyContent.children.addAll(title, historyTable, exportButton)
        VBox.setVgrow(historyTable, Priority.ALWAYS)
        
        return historyContent
    }
    
    private fun createStatusBar(): HBox {
        val statusBar = HBox(10.0)
        statusBar.padding = Insets(5.0)
        statusBar.style = "-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0;"
        statusBar.alignment = Pos.CENTER_LEFT
        
        statusLabel = Label("Ready")
        statusLabel.style = "-fx-text-fill: #2c3e50;"
        
        progressBar = ProgressBar()
        progressBar.isVisible = false
        progressBar.prefWidth = 200.0
        
        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)
        
        val versionLabel = Label("v2.0-dev")
        versionLabel.style = "-fx-text-fill: #7f8c8d; -fx-font-size: 10px;"
        
        statusBar.children.addAll(statusLabel, spacer, progressBar, versionLabel)
        
        return statusBar
    }
    
    // Task 42: Drag and Drop implementation
    private fun setupDragAndDrop(node: VBox) {
        node.setOnDragOver { event: DragEvent ->
            if (event.dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY)
            }
            event.consume()
        }
        
        node.setOnDragDropped { event: DragEvent ->
            val dragboard: Dragboard = event.dragboard
            var success = false
            
            if (dragboard.hasFiles()) {
                val files = dragboard.files
                val imageFiles = files.filter { isImageFile(it) }
                
                if (imageFiles.isNotEmpty()) {
                    if (imageFiles.size == 1) {
                        processImage(imageFiles.first())
                    } else {
                        batchFiles.addAll(imageFiles.map { it.absolutePath })
                        updateStatus("Added ${imageFiles.size} files to batch processing")
                    }
                    success = true
                } else {
                    showError("No valid image files found", "Please drop PNG, JPG, JPEG, GIF, or BMP files.")
                }
            }
            
            event.isDropCompleted = success
            event.consume()
        }
    }
    
    // Task 50: Keyboard shortcuts
    private fun setupKeyboardShortcuts(root: BorderPane, stage: Stage) {
        root.setOnKeyPressed { event ->
            when {
                event.code == KeyCode.O && event.isControlDown -> selectImage()
                event.code == KeyCode.H && event.isControlDown -> clearHistory()
                event.code == KeyCode.B && event.isControlDown -> processBatch()
                event.code == KeyCode.Q && event.isControlDown -> Platform.exit()
                event.code == KeyCode.F1 -> showAbout()
            }
        }
        root.isFocusTraversable = true
        root.requestFocus()
    }
    
    private fun selectImage() {
        val fileChooser = FileChooser()
        fileChooser.title = "Select QR Code Image"
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        
        val selectedFile = fileChooser.showOpenDialog(null)
        selectedFile?.let { processImage(it) }
    }
    
    private fun processImage(file: File) {
        showProgress(true)
        updateStatus("Processing ${file.name}...")
        
        // Load image preview (Task 43)
        try {
            val image = Image(FileInputStream(file))
            imageView.image = image
        } catch (e: Exception) {
            showError("Failed to load image", e.message ?: "Unknown error")
            return
        }
        
        // Process QR code asynchronously
        CompletableFuture.supplyAsync {
            try {
                qrReader.readQRCode(file.absolutePath)
            } catch (e: Exception) {
                throw e
            }
        }.thenAccept { result ->
            Platform.runLater {
                displayResult(result, file.name, "Success")
                addToHistory(file.name, result, "Success")
                updateStatus("QR code processed successfully")
                showProgress(false)
            }
        }.exceptionally { throwable ->
            Platform.runLater {
                val errorMsg = "No QR code found or error processing image"
                displayResult(errorMsg, file.name, "Error")
                addToHistory(file.name, errorMsg, "Error")
                updateStatus("Processing failed")
                showProgress(false)
            }
            null
        }
    }
    
    private fun displayResult(content: String, fileName: String, status: String) {
        resultTextArea.text = content
        
        // Add visual feedback based on status
        when (status) {
            "Success" -> {
                resultTextArea.style = "-fx-border-color: #27ae60; -fx-border-width: 2;"
            }
            "Error" -> {
                resultTextArea.style = "-fx-border-color: #e74c3c; -fx-border-width: 2;"
            }
        }
    }
    
    // Task 44: History feature implementation
    private fun addToHistory(fileName: String, content: String, status: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val result = QRResult(timestamp, fileName, content, status)
        historyData.add(0, result) // Add to beginning
        
        // Keep only last 100 entries
        if (historyData.size > 100) {
            historyData.removeAt(historyData.size - 1)
        }
    }
    
    private fun addBatchFiles() {
        val fileChooser = FileChooser()
        fileChooser.title = "Select Images for Batch Processing"
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        )
        
        val selectedFiles = fileChooser.showOpenMultipleDialog(null)
        selectedFiles?.let { files ->
            batchFiles.addAll(files.map { it.absolutePath })
            updateStatus("Added ${files.size} files to batch queue")
        }
    }
    
    private fun removeBatchFile() {
        val selectedIndex = batchListView.selectionModel.selectedIndex
        if (selectedIndex >= 0) {
            batchFiles.removeAt(selectedIndex)
        }
    }
    
    // Task 46: Batch processing implementation
    private fun processBatch() {
        if (batchFiles.isEmpty()) {
            showError("No Files", "Please add files to the batch queue first.")
            return
        }
        
        showProgress(true)
        updateStatus("Processing batch of ${batchFiles.size} files...")
        
        CompletableFuture.runAsync {
            var processed = 0
            val total = batchFiles.size
            
            batchFiles.forEach { filePath ->
                val file = File(filePath)
                try {
                    val result = qrReader.readQRCode(filePath)
                    Platform.runLater {
                        addToHistory(file.name, result, "Success")
                    }
                } catch (e: Exception) {
                    Platform.runLater {
                        addToHistory(file.name, "Processing failed: ${e.message}", "Error")
                    }
                }
                
                processed++
                Platform.runLater {
                    updateStatus("Processing batch: $processed/$total completed")
                }
            }
            
            Platform.runLater {
                updateStatus("Batch processing completed: $processed files processed")
                showProgress(false)
                batchFiles.clear()
            }
        }
    }
    
    private fun clearHistory() {
        historyData.clear()
        updateStatus("History cleared")
    }
    
    private fun copyToClipboard() {
        if (resultTextArea.text.isNotEmpty()) {
            val clipboard = javafx.scene.input.Clipboard.getSystemClipboard()
            val content = javafx.scene.input.ClipboardContent()
            content.putString(resultTextArea.text)
            clipboard.setContent(content)
            updateStatus("Content copied to clipboard")
        }
    }
    
    private fun exportHistory() {
        val fileChooser = FileChooser()
        fileChooser.title = "Export History"
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter("CSV Files", "*.csv")
        )
        fileChooser.initialFileName = "qr-history-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))}.csv"
        
        val file = fileChooser.showSaveDialog(null)
        file?.let { exportFile ->
            try {
                exportFile.writeText(buildString {
                    appendLine("Timestamp,File,Content,Status")
                    historyData.forEach { result ->
                        appendLine("\"${result.timestamp}\",\"${result.fileName}\",\"${result.content.replace("\"", "\"\"")}\",\"${result.status}\"")
                    }
                })
                updateStatus("History exported to ${exportFile.name}")
            } catch (e: Exception) {
                showError("Export Failed", "Could not export history: ${e.message}")
            }
        }
    }
    
    // Task 48: User-friendly error reporting
    private fun showError(title: String, message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Error"
        alert.headerText = title
        alert.contentText = message
        alert.showAndWait()
    }
    
    private fun showAbout() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "About QR Code Reader"
        alert.headerText = "QR Code Reader v2.0-dev"
        alert.contentText = """
            A modern QR code reading application with GUI.
            
            Features:
            • Drag & Drop support
            • Image preview
            • Batch processing
            • Processing history
            • Export capabilities
            
            Keyboard Shortcuts:
            • Ctrl+O: Open file
            • Ctrl+B: Batch process
            • Ctrl+H: Clear history
            • Ctrl+Q: Quit
            • F1: About
            
            Built with Kotlin and JavaFX
        """.trimIndent()
        alert.showAndWait()
    }
    
    private fun updateStatus(message: String) {
        statusLabel.text = message
    }
    
    private fun showProgress(visible: Boolean) {
        progressBar.isVisible = visible
        if (visible) {
            progressBar.progress = ProgressIndicator.INDETERMINATE_PROGRESS
        }
    }
    
    private fun isImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf("png", "jpg", "jpeg", "gif", "bmp")
    }
    
    // Data class for history table
    data class QRResult(
        val timestamp: String,
        val fileName: String,
        val content: String,
        val status: String
    )
}

fun main() {
    Application.launch(QRCodeReaderGUI::class.java)
}
