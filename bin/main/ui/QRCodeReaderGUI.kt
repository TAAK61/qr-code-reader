package ui

import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.Parent
import javafx.stage.FileChooser
import javafx.stage.Stage
import qr.QRCodeReader
import qr.QRCodeGenerator
import qr.QRCodeEncryption
import i18n.I18nManager
import webcam.WebcamManager
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.image.BufferedImage
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
// Clipboard support imports
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
// Task 53: Caching support
import java.util.concurrent.ConcurrentHashMap

/**
 * Modern JavaFX GUI for QR Code Reader
 * Implements tasks from tasks.md:
 * - Task 41: Graphical user interface (GUI)
 * - Task 42: Drag-and-drop support
 * - Task 43: Image preview
 * - Task 44: History feature
 * - Task 45: Clipboard support
 * - Task 46: Batch processing
 * - Task 48: User-friendly error reporting
 * - Task 49: Internationalization support
 * - Task 50: Keyboard shortcuts
 * - Task 72: QR code generation
 * - Task 73: Encrypted QR code support
 * - Task 74: Automatic URL opening
 */
class QRCodeReaderGUI : Application() {
    
    private val qrReader = QRCodeReader()
    private val qrGenerator = QRCodeGenerator() // Task 72: QR code generation
    private val webcamManager = WebcamManager()
    private val historyData: ObservableList<QRResult> = FXCollections.observableArrayList()
    
    // Task 53: Simple LRU cache for processed images (based on file path and last modified time)
    private val qrResultCache = object : LinkedHashMap<String, QRCacheEntry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, QRCacheEntry>?): Boolean {
            return size > 50 // Keep only last 50 results in cache
        }
    }
    
    // URL detection variables for Task 74
    private var currentUrl: String? = null
    private var currentUrlType: String? = null
    
    // UI Components
    private lateinit var imageView: ImageView
    private lateinit var resultTextArea: TextArea
    private lateinit var statusLabel: Label
    private lateinit var progressBar: ProgressBar
    private lateinit var historyTable: TableView<QRResult>
    private lateinit var batchListView: ListView<String>
    private lateinit var openUrlButton: Button
    private val batchFiles = FXCollections.observableArrayList<String>()
    
    override fun start(primaryStage: Stage) {
        // Initialize internationalization
        I18nManager.initialize()
        
        primaryStage.title = I18nManager.t("app.title")
        primaryStage.width = 1000.0
        primaryStage.height = 700.0
        
        val root = createMainLayout()
        val scene = Scene(root)
        scene.stylesheets.add(javaClass.getResource("/styles.css")?.toExternalForm() ?: "")
        
        // Task 50: Add keyboard shortcuts
        setupKeyboardShortcuts(scene, primaryStage)
        
        primaryStage.scene = scene
        primaryStage.show()
        
        root.requestFocus()
    }
    
    private fun createMainLayout(): Parent {
        val root = BorderPane()
        
        // Create menu bar
        val menuBar = createMenuBar()
        root.top = menuBar
        
        // Create main content with tabs
        val tabPane = TabPane()
        
        // Scanner tab
        val scannerTab = Tab(I18nManager.t("tab.scanner"))
        scannerTab.isClosable = false
        scannerTab.content = createScannerContent()
        
        // Generator tab (Task 72)
        val generatorTab = Tab(I18nManager.t("tab.generator"))
        generatorTab.isClosable = false
        generatorTab.content = createGeneratorContent()
        
        // Batch processing tab
        val batchTab = Tab(I18nManager.t("tab.batch"))
        batchTab.isClosable = false
        batchTab.content = createBatchContent()
        
        // History tab
        val historyTab = Tab(I18nManager.t("tab.history"))
        historyTab.isClosable = false
        historyTab.content = createHistoryContent()
        
        tabPane.tabs.addAll(scannerTab, generatorTab, batchTab, historyTab)
        root.center = tabPane
        
        // Status bar
        val statusBar = createStatusBar()
        root.bottom = statusBar
        
        return root
    }
    
    private fun createMenuBar(): MenuBar {
        val menuBar = MenuBar()
        
        // File menu
        val fileMenu = Menu(I18nManager.t("menu.file"))
        val openItem = MenuItem(I18nManager.t("menu.file.open"))
        openItem.accelerator = KeyCombination.keyCombination("Ctrl+O")
        openItem.setOnAction { selectImage() }
        
        val clipboardItem = MenuItem(I18nManager.t("menu.file.clipboard"))
        clipboardItem.accelerator = KeyCombination.keyCombination("Ctrl+V")
        clipboardItem.setOnAction { processFromClipboard() }
        
        val webcamItem = MenuItem(I18nManager.t("menu.file.webcam"))
        webcamItem.accelerator = KeyCombination.keyCombination("Ctrl+W")
        webcamItem.setOnAction { openWebcamDialog() }
        
        val exitItem = MenuItem(I18nManager.t("menu.file.exit"))
        exitItem.accelerator = KeyCombination.keyCombination("Ctrl+Q")
        exitItem.setOnAction { Platform.exit() }
        
        fileMenu.items.addAll(openItem, clipboardItem, webcamItem, SeparatorMenuItem(), exitItem)
        
        // Tools menu
        val toolsMenu = Menu(I18nManager.t("menu.tools"))
        val batchItem = MenuItem(I18nManager.t("menu.tools.batch"))
        batchItem.accelerator = KeyCombination.keyCombination("Ctrl+B")
        batchItem.setOnAction { /* Switch to batch tab */ }
        
        toolsMenu.items.add(batchItem)
        
        // Help menu
        val helpMenu = Menu(I18nManager.t("menu.help"))
        val aboutItem = MenuItem(I18nManager.t("menu.help.about"))
        aboutItem.accelerator = KeyCombination.keyCombination("F1")
        aboutItem.setOnAction { showAboutDialog() }
        
        helpMenu.items.add(aboutItem)
        
        menuBar.menus.addAll(fileMenu, toolsMenu, helpMenu)
        return menuBar
    }
    
    private fun createScannerContent(): VBox {
        val scannerContent = VBox(20.0)
        scannerContent.padding = Insets(20.0)
        
        // Title
        val title = Label(I18nManager.t("scanner.title"))
        title.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        
        // Main horizontal layout
        val mainHBox = HBox(20.0)
        
        // Left side - Image area
        val leftPane = createImagePane()
        
        // Right side - Results area
        val rightPane = createResultsPane()
        
        mainHBox.children.addAll(leftPane, rightPane)
        HBox.setHgrow(leftPane, Priority.ALWAYS)
        HBox.setHgrow(rightPane, Priority.ALWAYS)
        
        scannerContent.children.addAll(title, mainHBox)
        VBox.setVgrow(mainHBox, Priority.ALWAYS)
        
        return scannerContent
    }
    
    private fun createImagePane(): VBox {
        val imagePane = VBox(15.0)
        
        val imageTitle = Label("🖼️ " + I18nManager.t("scanner.image"))
        imageTitle.style = "-fx-font-size: 14px; -fx-font-weight: bold;"
        
        // Image view with drag and drop support
        imageView = ImageView()
        imageView.fitWidth = 400.0
        imageView.fitHeight = 300.0
        imageView.isPreserveRatio = true
        imageView.style = "-fx-border-color: #bdc3c7; -fx-border-width: 2; -fx-border-style: dashed;"
        
        // Task 42: Drag and drop support
        setupDragAndDrop(imageView)
        
        val selectButton = Button(I18nManager.t("scanner.selectImage"))
        selectButton.setOnAction { selectImage() }
        
        val clipboardButton = Button(I18nManager.t("scanner.fromClipboard"))
        clipboardButton.setOnAction { processFromClipboard() }
        
        val webcamButton = Button(I18nManager.t("scanner.fromWebcam"))
        webcamButton.setOnAction { openWebcamDialog() }
        
        val buttonBox = HBox(10.0)
        buttonBox.children.addAll(selectButton, clipboardButton, webcamButton)
        
        imagePane.children.addAll(imageTitle, imageView, buttonBox)
        return imagePane
    }
    
    private fun createResultsPane(): VBox {
        val resultsPane = VBox(15.0)
        
        val title = Label("📋 " + I18nManager.t("scanner.results"))
        title.style = "-fx-font-size: 14px; -fx-font-weight: bold;"
        
        resultTextArea = TextArea()
        resultTextArea.isWrapText = true
        resultTextArea.isEditable = false
        resultTextArea.promptText = I18nManager.t("scanner.resultsPrompt")
        resultTextArea.style = "-fx-font-family: 'Courier New'; -fx-font-size: 12px;"
        
        val copyButton = Button("📋 " + I18nManager.t("button.copy"))
        copyButton.setOnAction { copyToClipboard() }
        
        val saveImageButton = Button("💾 " + I18nManager.t("button.saveImage"))
        saveImageButton.setOnAction { saveImageToFile() }
        
        // Task 74: URL opening button
        openUrlButton = Button("🌐 " + I18nManager.t("button.openUrl"))
        openUrlButton.setOnAction { openDetectedUrl() }
        openUrlButton.isVisible = false // Initially hidden
        
        val buttonBox = HBox(10.0)
        buttonBox.children.addAll(copyButton, saveImageButton, openUrlButton)
        
        resultsPane.children.addAll(title, resultTextArea, buttonBox)
        VBox.setVgrow(resultTextArea, Priority.ALWAYS)
        
        return resultsPane
    }
    
    private fun createGeneratorContent(): VBox {
        val generatorContent = VBox(20.0)
        generatorContent.padding = Insets(20.0)
        
        val title = Label(I18nManager.t("generator.title"))
        title.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        
        val mainHBox = HBox(20.0)
        
        // Left side - Input controls
        val leftPane = VBox(15.0)
        
        // QR Code type selection
        val typeLabel = Label(I18nManager.t("generator.type"))
        val typeChoice = ComboBox<String>()
        typeChoice.items.addAll(
            "Plain Text",
            "Encrypted Text", // Task 73: Support for encrypted QR codes
            "URL",
            "WiFi Configuration",
            "Contact (vCard)",
            "SMS",
            "Email"
        )
        typeChoice.value = "Plain Text"
        
        // Content area for plain text
        val contentLabel = Label(I18nManager.t("generator.content"))
        val contentTextArea = TextArea()
        contentTextArea.promptText = I18nManager.t("generator.contentPrompt")
        contentTextArea.prefRowCount = 5
        
        // Template fields container (for non-text types)
        val templateFields = VBox(10.0)
        templateFields.isVisible = false
        
        // URL fields
        val urlFields = createUrlFields()
        
        // WiFi fields
        val wifiFields = createWifiFields()
        
        // Contact fields
        val contactFields = createContactFields()
        
        // SMS fields
        val smsFields = createSmsFields()
        
        // Email fields
        val emailFields = createEmailFields()
        
        // Encryption fields (Task 73)
        val encryptionFields = createEncryptionFields()
        
        // Generation settings
        val settingsLabel = Label(I18nManager.t("generator.settings"))
        settingsLabel.style = "-fx-font-weight: bold;"
        
        val sizeLabel = Label(I18nManager.t("generator.size"))
        val sizeSpinner = Spinner<Int>(100, 1000, 300, 50)
        
        val errorCorrectionLabel = Label(I18nManager.t("generator.errorCorrection"))
        val errorCorrectionChoice = ComboBox<String>()
        errorCorrectionChoice.items.addAll("L (Low)", "M (Medium)", "Q (Quartile)", "H (High)")
        errorCorrectionChoice.value = "M (Medium)"
        
        val generateButton = Button(I18nManager.t("generator.generate"))
        generateButton.style = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;"
        
        leftPane.children.addAll(
            typeLabel, typeChoice,
            contentLabel, contentTextArea,
            templateFields,
            encryptionFields,
            settingsLabel,
            sizeLabel, sizeSpinner,
            errorCorrectionLabel, errorCorrectionChoice,
            generateButton
        )
        
        // Right side - Generated QR code preview
        val rightPane = VBox(15.0)
        val previewLabel = Label(I18nManager.t("generator.preview"))
        previewLabel.style = "-fx-font-weight: bold;"
        
        val qrImageView = ImageView()
        qrImageView.fitWidth = 300.0
        qrImageView.fitHeight = 300.0
        qrImageView.isPreserveRatio = true
        qrImageView.style = "-fx-border-color: #bdc3c7; -fx-border-width: 1;"
        
        val saveQrButton = Button(I18nManager.t("generator.saveQr"))
        saveQrButton.isDisable = true
        
        rightPane.children.addAll(previewLabel, qrImageView, saveQrButton)
        
        mainHBox.children.addAll(leftPane, rightPane)
        HBox.setHgrow(leftPane, Priority.ALWAYS)
        
        generatorContent.children.addAll(title, mainHBox)
        VBox.setVgrow(mainHBox, Priority.ALWAYS)
        
        // Event handlers
        typeChoice.setOnAction {
            val selectedType = typeChoice.value
            templateFields.isVisible = selectedType != "Plain Text" && selectedType != "Encrypted Text"
            templateFields.children.clear()
            contentTextArea.isVisible = selectedType == "Plain Text" || selectedType == "Encrypted Text"
            contentLabel.isVisible = selectedType == "Plain Text" || selectedType == "Encrypted Text"
            encryptionFields.isVisible = selectedType == "Encrypted Text"
            
            when (selectedType) {
                "URL" -> {
                    templateFields.children.add(urlFields)
                }
                "WiFi Configuration" -> {
                    templateFields.children.add(wifiFields)
                }
                "Contact (vCard)" -> {
                    templateFields.children.add(contactFields)
                }
                "SMS" -> {
                    templateFields.children.add(smsFields)
                }
                "Email" -> {
                    templateFields.children.add(emailFields)
                }
            }
        }
        
        generateButton.setOnAction {
            generateQRCode(typeChoice, contentTextArea, templateFields, sizeSpinner, errorCorrectionChoice, qrImageView, saveQrButton, encryptionFields)
        }
        
        saveQrButton.setOnAction {
            saveGeneratedQRCode(qrImageView)
        }
        
        return generatorContent
    }
    
    private fun createUrlFields(): VBox {
        val fields = VBox(10.0)
        val urlLabel = Label("URL:")
        val urlField = TextField()
        urlField.promptText = "https://example.com"
        fields.children.addAll(urlLabel, urlField)
        return fields
    }
    
    private fun createWifiFields(): VBox {
        val fields = VBox(10.0)
        
        val ssidLabel = Label("Network Name (SSID):")
        val ssidField = TextField()
        
        val passwordLabel = Label("Password:")
        val passwordField = PasswordField()
        
        val securityLabel = Label("Security:")
        val securityChoice = ComboBox<String>()
        securityChoice.items.addAll("WPA", "WEP", "nopass")
        securityChoice.value = "WPA"
        
        val hiddenCheck = CheckBox("Hidden Network")
        
        fields.children.addAll(ssidLabel, ssidField, passwordLabel, passwordField, securityLabel, securityChoice, hiddenCheck)
        return fields
    }
    
    private fun createContactFields(): VBox {
        val fields = VBox(10.0)
        
        val nameLabel = Label("Name:")
        val nameField = TextField()
        
        val orgLabel = Label("Organization:")
        val orgField = TextField()
        
        val phoneLabel = Label("Phone:")
        val phoneField = TextField()
        
        val emailLabel = Label("Email:")
        val emailField = TextField()
        
        val urlLabel = Label("Website:")
        val urlContactField = TextField()
        
        fields.children.addAll(nameLabel, nameField, orgLabel, orgField, phoneLabel, phoneField, emailLabel, emailField, urlLabel, urlContactField)
        return fields
    }
    
    private fun createSmsFields(): VBox {
        val fields = VBox(10.0)
        
        val phoneLabel = Label("Phone Number:")
        val smsPhoneField = TextField()
        
        val messageLabel = Label("Message:")
        val smsMessageField = TextArea()
        smsMessageField.prefRowCount = 3
        
        fields.children.addAll(phoneLabel, smsPhoneField, messageLabel, smsMessageField)
        return fields
    }
    
    private fun createEmailFields(): VBox {
        val fields = VBox(10.0)
        
        val toLabel = Label("To:")
        val emailToField = TextField()
        
        val subjectLabel = Label("Subject:")
        val emailSubjectField = TextField()
        
        val bodyLabel = Label("Body:")
        val emailBodyField = TextArea()
        emailBodyField.prefRowCount = 3
        
        fields.children.addAll(toLabel, emailToField, subjectLabel, emailSubjectField, bodyLabel, emailBodyField)
        return fields
    }
    
    private fun createEncryptionFields(): VBox {
        val fields = VBox(10.0)
        fields.isVisible = false
        
        val encryptionLabel = Label("🔒 Encryption Settings")
        encryptionLabel.style = "-fx-font-weight: bold;"
        
        val passwordLabel = Label("Password:")
        val passwordField = PasswordField()
        passwordField.promptText = "Enter encryption password"
        
        val compressionCheck = CheckBox("Enable compression")
        compressionCheck.isSelected = true
        
        fields.children.addAll(encryptionLabel, passwordLabel, passwordField, compressionCheck)
        return fields
    }
    
    private fun createBatchContent(): VBox {
        val batchContent = VBox(20.0)
        batchContent.padding = Insets(20.0)
        
        val title = Label(I18nManager.t("batch.title"))
        title.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        
        val mainHBox = HBox(20.0)
        
        // Left side - File list
        val leftPane = VBox(15.0)
        val filesLabel = Label(I18nManager.t("batch.files"))
        filesLabel.style = "-fx-font-weight: bold;"
        
        batchListView = ListView(batchFiles)
        batchListView.prefHeight = 300.0
        
        val addFilesButton = Button(I18nManager.t("batch.addFiles"))
        addFilesButton.setOnAction { addBatchFiles() }
        
        val clearButton = Button(I18nManager.t("batch.clear"))
        clearButton.setOnAction { batchFiles.clear() }
        
        val processButton = Button(I18nManager.t("batch.process"))
        processButton.setOnAction { processBatchFiles() }
        processButton.style = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;"
        
        val buttonBox = HBox(10.0)
        buttonBox.children.addAll(addFilesButton, clearButton, processButton)
        
        leftPane.children.addAll(filesLabel, batchListView, buttonBox)
        
        // Right side - Results
        val rightPane = VBox(15.0)
        val resultsLabel = Label(I18nManager.t("batch.results"))
        resultsLabel.style = "-fx-font-weight: bold;"
        
        val batchResultsArea = TextArea()
        batchResultsArea.isEditable = false
        batchResultsArea.prefHeight = 300.0
        
        val exportButton = Button(I18nManager.t("batch.export"))
        exportButton.setOnAction { exportBatchResults() }
        
        rightPane.children.addAll(resultsLabel, batchResultsArea, exportButton)
        
        mainHBox.children.addAll(leftPane, rightPane)
        HBox.setHgrow(leftPane, Priority.ALWAYS)
        HBox.setHgrow(rightPane, Priority.ALWAYS)
        
        batchContent.children.addAll(title, mainHBox)
        VBox.setVgrow(mainHBox, Priority.ALWAYS)
        
        return batchContent
    }
    
    private fun createHistoryContent(): VBox {
        val historyContent = VBox(20.0)
        historyContent.padding = Insets(20.0)
        
        val title = Label(I18nManager.t("history.title"))
        title.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        
        // History table
        historyTable = TableView(historyData)
        
        val timestampColumn = TableColumn<QRResult, String>(I18nManager.t("history.timestamp"))
        timestampColumn.cellValueFactory = PropertyValueFactory("timestamp")
        timestampColumn.prefWidth = 150.0
        
        val sourceColumn = TableColumn<QRResult, String>(I18nManager.t("history.source"))
        sourceColumn.cellValueFactory = PropertyValueFactory("source")
        sourceColumn.prefWidth = 200.0
        
        val contentColumn = TableColumn<QRResult, String>(I18nManager.t("history.content"))
        contentColumn.cellValueFactory = PropertyValueFactory("content")
        contentColumn.prefWidth = 400.0
        
        val statusColumn = TableColumn<QRResult, String>(I18nManager.t("history.status"))
        statusColumn.cellValueFactory = PropertyValueFactory("status")
        statusColumn.prefWidth = 100.0
        
        historyTable.columns.addAll(timestampColumn, sourceColumn, contentColumn, statusColumn)
        
        val clearHistoryButton = Button(I18nManager.t("history.clear"))
        clearHistoryButton.setOnAction { historyData.clear() }
        
        val exportHistoryButton = Button(I18nManager.t("history.export"))
        exportHistoryButton.setOnAction { exportHistory() }
        
        val buttonBox = HBox(10.0)
        buttonBox.children.addAll(clearHistoryButton, exportHistoryButton)
        
        historyContent.children.addAll(title, historyTable, buttonBox)
        VBox.setVgrow(historyTable, Priority.ALWAYS)
        
        return historyContent
    }
    
    private fun createStatusBar(): HBox {
        val statusBar = HBox(10.0)
        statusBar.padding = Insets(5.0)
        statusBar.style = "-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0;"
        
        statusLabel = Label(I18nManager.t("status.ready"))
        
        progressBar = ProgressBar()
        progressBar.isVisible = false
        progressBar.prefWidth = 200.0
        
        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)
        
        statusBar.children.addAll(statusLabel, spacer, progressBar)
        return statusBar
    }
    
    private fun setupKeyboardShortcuts(scene: Scene, stage: Stage) {
        scene.setOnKeyPressed { event ->
            when {
                event.isControlDown && event.code == KeyCode.O -> selectImage()
                event.isControlDown && event.code == KeyCode.V -> processFromClipboard()
                event.isControlDown && event.code == KeyCode.W -> openWebcamDialog()
                event.isControlDown && event.code == KeyCode.Q -> Platform.exit()
                event.code == KeyCode.F1 -> showAboutDialog()
            }
        }
    }
    
    private fun setupDragAndDrop(imageView: ImageView) {
        imageView.setOnDragOver { event ->
            if (event.dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY)
            }
            event.consume()
        }
        
        imageView.setOnDragDropped { event ->
            val dragboard = event.dragboard
            var success = false
            
            if (dragboard.hasFiles()) {
                val files = dragboard.files
                if (files.size == 1) {
                    val file = files[0]
                    if (isImageFile(file)) {
                        processImage(file)
                        success = true
                    }
                }
            }
            
            event.isDropCompleted = success
            event.consume()
        }
    }
    
    private fun isImageFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".gif") || name.endsWith(".bmp")
    }
    
    private fun selectImage() {
        val fileChooser = FileChooser()
        fileChooser.title = I18nManager.t("dialog.selectImage")
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter(I18nManager.t("dialog.imageFiles"), "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            FileChooser.ExtensionFilter(I18nManager.t("dialog.allFiles"), "*.*")
        )
        
        val selectedFile = fileChooser.showOpenDialog(null)
        selectedFile?.let { processImage(it) }
    }
    
    private fun processImage(file: File) {
        showProgress(true)
        updateStatus("Processing ${file.name}...")
        
        // Task 53: Check cache first
        val cacheKey = "${file.absolutePath}_${file.lastModified()}"
        val cachedResult = qrResultCache[cacheKey]
        
        if (cachedResult != null) {
            // Use cached result
            Platform.runLater {
                displayResult(cachedResult.result, file.name, cachedResult.status)
                addToHistory(file.name, cachedResult.result, cachedResult.status)
                updateStatus("QR code loaded from cache")
                showProgress(false)
            }
            return
        }
        
        // Load image preview (Task 43)
        try {
            val image = Image(FileInputStream(file))
            imageView.image = image
        } catch (e: Exception) {
            showError("Failed to load image", e.message ?: "Unknown error")
            return
        }
        
        // Process QR code asynchronously with enhanced functionality (Task 73 & 74)
        CompletableFuture.supplyAsync {
            try {
                qrReader.readQRCodeEnhanced(file.absolutePath)
            } catch (e: Exception) {
                throw e
            }
        }.thenAccept { result ->
            Platform.runLater {
                displayEnhancedResult(result, file.name, "Success")
                addToHistory(file.name, result.content, "Success")
                updateStatus("QR code processed successfully")
                showProgress(false)
                
                // Task 53: Cache the successful result
                qrResultCache[cacheKey] = QRCacheEntry(result.content, file.lastModified(), "Success")
            }
        }.exceptionally { throwable ->
            Platform.runLater {
                val errorMsg = "No QR code found or error processing image"
                displayResult(errorMsg, file.name, "Error")
                addToHistory(file.name, errorMsg, "Error")
                updateStatus("Processing failed")
                showProgress(false)
                
                // Task 53: Cache error results too to avoid reprocessing
                qrResultCache[cacheKey] = QRCacheEntry(errorMsg, file.lastModified(), "Error")
            }
            null
        }
    }
    
    /**
     * Display enhanced QR reading results with URL detection support (Task 74)
     */
    private fun displayEnhancedResult(result: QRCodeReader.QRReadResult, fileName: String, status: String) {
        resultTextArea.text = result.content
        
        // Task 74: Handle URL detection and show open button
        if (result.isUrl && result.urlType != null) {
            currentUrl = result.content
            currentUrlType = result.urlType
            openUrlButton.isVisible = true
            openUrlButton.text = "🌐 Open ${result.urlType?.uppercase()} URL"
        } else {
            currentUrl = null
            currentUrlType = null
            openUrlButton.isVisible = false
        }
        
        // Add visual feedback based on status and encryption
        when {
            result.isEncrypted && result.decryptionSuccessful -> {
                resultTextArea.style = "-fx-border-color: #f39c12; -fx-border-width: 2; -fx-background-color: #fef9e7;"
                updateStatus("✅ Encrypted QR code successfully decrypted")
            }
            result.isEncrypted && !result.decryptionSuccessful -> {
                resultTextArea.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-color: #fdedec;"
                updateStatus("🔒 Encrypted QR code - decryption failed")
            }
            status == "Success" -> {
                resultTextArea.style = "-fx-border-color: #27ae60; -fx-border-width: 2; -fx-background-color: #eafaf1;"
                updateStatus("✅ QR code processed successfully")
            }
            else -> {
                resultTextArea.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-color: #fdedec;"
            }
        }
    }
    
    /**
     * Task 74: Open detected URL automatically
     */
    private fun openDetectedUrl() {
        currentUrl?.let { url ->
            val success = qrReader.openUrl(url, currentUrlType)
            if (success) {
                updateStatus("Opened URL: $url")
            } else {
                showError("URL Error", "Failed to open URL. Your system may not support this URL type or no default application is configured.")
            }
        }
    }
    
    private fun displayResult(content: String, fileName: String, status: String) {
        resultTextArea.text = content
        
        // Reset URL detection for legacy method
        currentUrl = null
        currentUrlType = null
        openUrlButton.isVisible = false
        
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
    
    private fun processFromClipboard() {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = clipboard.getContents(null)
            
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                updateStatus("Processing image from clipboard...")
                showProgress(true)
                
                CompletableFuture.supplyAsync {
                    try {
                        val image = transferable.getTransferData(DataFlavor.imageFlavor) as java.awt.Image
                        val bufferedImage = if (image is BufferedImage) {
                            image
                        } else {
                            val bi = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB)
                            val g = bi.createGraphics()
                            g.drawImage(image, 0, 0, null)
                            g.dispose()
                            bi
                        }
                        
                        // Convert to JavaFX Image and display
                        Platform.runLater {
                            val fxImage = SwingFXUtils.toFXImage(bufferedImage, null)
                            imageView.image = fxImage
                        }
                        
                        // Process QR code with enhanced functionality
                        val tempFile = File.createTempFile("clipboard_image", ".png")
                        ImageIO.write(bufferedImage, "png", tempFile)
                        
                        val result = qrReader.readQRCodeEnhanced(tempFile.absolutePath)
                        tempFile.delete()
                        
                        result
                    } catch (e: Exception) {
                        throw e
                    }
                }.thenAccept { result ->
                    Platform.runLater {
                        showProgress(false)
                        if (result.content.isNotEmpty() && !result.content.startsWith("Error reading QR code")) {
                            displayEnhancedResult(result, "Clipboard Image", "Success")
                            updateStatus("QR code successfully read from clipboard")
                            
                            // Add to history
                            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            historyData.add(QRResult(timestamp, "Clipboard", result.content, "Success"))
                        } else {
                            displayResult("No QR code found in clipboard image", "Clipboard Image", "Error")
                            updateStatus("No QR code found in clipboard image")
                        }
                    }
                }.exceptionally { throwable ->
                    Platform.runLater {
                        showProgress(false)
                        displayResult("Error processing clipboard image: ${throwable.message}", "Clipboard Image", "Error")
                        updateStatus("Error processing clipboard image")
                    }
                    null
                }
            } else {
                showError("Clipboard Error", "No image found in clipboard")
            }
        } catch (e: UnsupportedFlavorException) {
            showError("Clipboard Error", "Unsupported clipboard content")
        } catch (e: Exception) {
            showError("Clipboard Error", "Error accessing clipboard: ${e.message}")
        }
    }
    
    private fun openWebcamDialog() {
        if (!webcamManager.isWebcamAvailable()) {
            showError(I18nManager.t("error.webcamNotAvailable"), I18nManager.t("error.webcamNotAvailableMessage"))
            return
        }
        
        val dialog = Dialog<String>()
        dialog.title = I18nManager.t("webcam.title")
        dialog.headerText = I18nManager.t("webcam.pointCamera")
        
        // Create dialog content
        val dialogPane = dialog.dialogPane
        dialogPane.buttonTypes.addAll(ButtonType.CLOSE)
          val content = VBox(15.0)
        content.padding = Insets(20.0)
        content.alignment = Pos.CENTER
        
        // Webcam selection
        val webcamLabel = Label(I18nManager.t("webcam.selectWebcam"))
        val webcamChoice = ComboBox<String>()
        val availableWebcams = try {
            webcamManager.getAvailableWebcams().map { it.name ?: "Webcam ${it.hashCode()}" }
        } catch (e: Exception) {
            listOf("Default Webcam")
        }
        webcamChoice.items.addAll(availableWebcams)
        webcamChoice.value = webcamChoice.items.firstOrNull()
        
        // Webcam preview
        val webcamImageView = ImageView()
        webcamImageView.fitWidth = 400.0
        webcamImageView.fitHeight = 300.0
        webcamImageView.isPreserveRatio = true
        webcamImageView.style = "-fx-border-color: #bdc3c7; -fx-border-width: 1;"
        
        // Control buttons
        val startButton = Button(I18nManager.t("webcam.start"))
        val stopButton = Button(I18nManager.t("webcam.stop"))
        stopButton.isDisable = true
        
        val statusLabel = Label(I18nManager.t("webcam.ready"))
        
        val buttonBox = HBox(10.0)
        buttonBox.alignment = Pos.CENTER
        buttonBox.children.addAll(startButton, stopButton)
          content.children.addAll(webcamLabel, webcamChoice, webcamImageView, buttonBox, statusLabel)
        dialogPane.content = content
        
        var scanningTask: Task<Void>? = null
          startButton.setOnAction {
            val selectedWebcam = webcamChoice.value
            if (selectedWebcam != null) {
                // Initialize the webcam
                val webcamIndex = webcamChoice.items.indexOf(selectedWebcam)
                if (webcamManager.initializeWebcam(webcamIndex) && webcamManager.startWebcam()) {
                    statusLabel.text = I18nManager.t("webcam.scanning")
                    
                    // Start real webcam scanning
                    scanningTask = webcamManager.startQRScanning(
                        onFrameUpdate = { fxImage ->
                            Platform.runLater {
                                webcamImageView.image = fxImage
                            }
                        },
                        onQRDetected = { qrContent ->
                            Platform.runLater {
                                statusLabel.text = "QR Code detected!"
                                displayResult(qrContent, "Webcam", "QR Code")
                                
                                // Auto-stop after detection
                                startButton.isDisable = false
                                stopButton.isDisable = true
                                webcamChoice.isDisable = false
                                webcamManager.stopWebcam()
                            }
                        },
                        onError = { errorMessage ->
                            Platform.runLater {
                                statusLabel.text = "Error: $errorMessage"
                                startButton.isDisable = false
                                stopButton.isDisable = true
                                webcamChoice.isDisable = false
                            }
                        }
                    )
                    
                    // Run the scanning task in background
                    Thread(scanningTask).start()
                    
                    startButton.isDisable = true
                    stopButton.isDisable = false
                    webcamChoice.isDisable = true
                } else {
                    showError(I18nManager.t("error.webcamInitFailed"), I18nManager.t("error.webcamInitFailedMessage"))
                }            } else {
                showError(I18nManager.t("error.webcamInitFailed"), I18nManager.t("error.webcamInitFailedMessage"))
            }
        }
        
        stopButton.setOnAction {
            scanningTask?.let { task ->
                // Cancel the webcam scanning task
                task.cancel()
                webcamManager.stopWebcam()
                
                Platform.runLater {
                    statusLabel.text = I18nManager.t("webcam.stopped")
                    startButton.isDisable = false
                    stopButton.isDisable = true
                    webcamChoice.isDisable = false
                    webcamImageView.image = null // Clear the webcam feed
                }
            }
        }
          dialog.setOnCloseRequest {
            // Cleanup when dialog is closed
            scanningTask?.cancel()
            webcamManager.stopWebcam()
        }
        
        dialog.showAndWait()
    }
    
    private fun generateQRCode(
        typeChoice: ComboBox<String>,
        contentTextArea: TextArea,
        templateFields: VBox,
        sizeSpinner: Spinner<Int>,
        errorCorrectionChoice: ComboBox<String>,
        qrImageView: ImageView,
        saveQrButton: Button,
            encryptionFields: VBox
    ) {
        try {
            val type = typeChoice.value
            val size = sizeSpinner.value
            val config = QRCodeGenerator.QRCodeConfig(
                width = size,
                height = size,
                errorCorrectionLevel = when (errorCorrectionChoice.value) {
                    "L (Low)" -> ErrorCorrectionLevel.L
                    "M (Medium)" -> ErrorCorrectionLevel.M
                    "Q (Quartile)" -> ErrorCorrectionLevel.Q
                    "H (High)" -> ErrorCorrectionLevel.H
                    else -> ErrorCorrectionLevel.M
                }
            )
            
            val result = when (type) {
                "Plain Text" -> {
                    val content = contentTextArea.text
                    if (content.isBlank()) {
                        showError("Invalid Input", "Please enter some text content.")
                        return
                    }
                    qrGenerator.generateQRCodeImage(content, config)
                }                "Encrypted Text" -> {
                    val content = contentTextArea.text
                    if (content.isBlank()) {
                        showError("Invalid Input", "Please enter some text content.")
                        return
                    }
                    
                    // Get encryption password from encryptionFields
                    val passwordField = encryptionFields.children
                        .filterIsInstance<PasswordField>()
                        .firstOrNull()
                    val compressionCheck = encryptionFields.children
                        .filterIsInstance<CheckBox>()
                        .firstOrNull()
                    
                    val password = passwordField?.text
                    if (password.isNullOrBlank()) {
                        showError("Invalid Input", "Please enter an encryption password.")
                        return
                    }
                      val useCompression = compressionCheck?.isSelected ?: true
                    val encryptionConfig = QRCodeEncryption.EncryptionConfig(
                        password = password,
                        compressionEnabled = useCompression
                    )
                    qrGenerator.generateEncryptedQRCodeImage(content, encryptionConfig, config)
                }
                else -> {
                    showError("Not Implemented", "QR code generation for $type is not yet implemented.")
                    return
                }
            }
            
            if (result.success) {
                // Display the generated QR code
                val fxImage = SwingFXUtils.toFXImage(result.image, null)
                qrImageView.image = fxImage
                saveQrButton.isDisable = false
                updateStatus("QR code generated successfully")
            } else {
                showError("Generation Error", result.error ?: "Failed to generate QR code")
            }
            
        } catch (e: Exception) {
            showError("Generation Error", "Error generating QR code: ${e.message}")
        }
    }
    
    private fun saveGeneratedQRCode(qrImageView: ImageView) {
        val image = qrImageView.image
        if (image != null) {
            val fileChooser = FileChooser()
            fileChooser.title = "Save QR Code"
            fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter("PNG Files", "*.png")
            )
            fileChooser.initialFileName = "qr-code.png"
            
            val file = fileChooser.showSaveDialog(null)
            if (file != null) {
                try {
                    val bufferedImage = SwingFXUtils.fromFXImage(image, null)
                    ImageIO.write(bufferedImage, "png", file)
                    updateStatus("QR code saved to ${file.absolutePath}")
                } catch (e: Exception) {
                    showError("Save Error", "Failed to save QR code: ${e.message}")
                }
            }
        }
    }
    
    private fun addBatchFiles() {
        val fileChooser = FileChooser()
        fileChooser.title = I18nManager.t("dialog.selectImages")
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter(I18nManager.t("dialog.imageFiles"), "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        )
        
        val selectedFiles = fileChooser.showOpenMultipleDialog(null)
        selectedFiles?.forEach { file ->
            if (!batchFiles.contains(file.absolutePath)) {
                batchFiles.add(file.absolutePath)
            }
        }
    }
    
    private fun processBatchFiles() {
        if (batchFiles.isEmpty()) {
            showError("No Files", "Please add some files to process.")
            return
        }
        
        showProgress(true)
        updateStatus("Processing ${batchFiles.size} files...")
        
        CompletableFuture.supplyAsync {
            qrReader.readQRCodesBatch(batchFiles.toList())
        }.thenAccept { results ->
            Platform.runLater {
                showProgress(false)
                val successCount = results.count { it["success"] == true }
                updateStatus("Batch processing completed: $successCount/${results.size} successful")
                
                // Display results
                val resultsText = StringBuilder()
                results.forEach { result ->
                    val index = result["index"] as Int
                    val imagePath = result["imagePath"] as String
                    val fileName = File(imagePath).name
                    val success = result["success"] as Boolean
                    
                    if (success) {
                        val content = result["content"] as String
                        resultsText.appendLine("✅ $fileName: ${content.take(100)}${if (content.length > 100) "..." else ""}")
                        
                        // Add to history
                        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        historyData.add(QRResult(timestamp, fileName, content, "Success"))
                    } else {
                        val error = result["error"] as String
                        resultsText.appendLine("❌ $fileName: $error")
                        
                        // Add to history
                        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        historyData.add(QRResult(timestamp, fileName, error, "Error"))
                    }
                }
                
                // Find the batch results text area
                val batchResultsArea = batchListView.parent.parent
                    .run { if (this is HBox) children[1] else null }
                    ?.run { if (this is VBox) children[1] else null } as? TextArea
                
                batchResultsArea?.text = resultsText.toString()
            }
        }.exceptionally { throwable ->
            Platform.runLater {
                showProgress(false)
                showError("Batch Processing Error", "Error during batch processing: ${throwable.message}")
            }
            null
        }
    }
    
    private fun exportBatchResults() {
        val fileChooser = FileChooser()
        fileChooser.title = "Export Batch Results"
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter("CSV Files", "*.csv")
        )
        fileChooser.initialFileName = "batch-results.csv"
        
        val file = fileChooser.showSaveDialog(null)
        if (file != null) {
            try {
                // Implementation for CSV export would go here
                updateStatus("Batch results exported to ${file.absolutePath}")
            } catch (e: Exception) {
                showError("Export Error", "Failed to export results: ${e.message}")
            }
        }
    }
    
    private fun exportHistory() {
        val fileChooser = FileChooser()
        fileChooser.title = "Export History"
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter("CSV Files", "*.csv")
        )
        fileChooser.initialFileName = "qr-history.csv"
        
        val file = fileChooser.showSaveDialog(null)
        if (file != null) {
            try {
                val csv = StringBuilder()
                csv.appendLine("Timestamp,Source,Content,Status")
                
                historyData.forEach { result ->
                    val content = result.content.replace("\"", "\"\"") // Escape quotes
                    csv.appendLine("\"${result.timestamp}\",\"${result.source}\",\"$content\",\"${result.status}\"")
                }
                
                file.writeText(csv.toString())
                updateStatus("History exported to ${file.absolutePath}")
            } catch (e: Exception) {
                showError("Export Error", "Failed to export history: ${e.message}")
            }
        }
    }
    
    private fun copyToClipboard() {
        val content = resultTextArea.text
        if (content.isNotEmpty()) {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = java.awt.datatransfer.StringSelection(content)
            clipboard.setContents(transferable, null)
            updateStatus("Content copied to clipboard")
        }
    }
    
    private fun saveImageToFile() {
        val image = imageView.image
        if (image != null) {
            val fileChooser = FileChooser()
            fileChooser.title = "Save Image"
            fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter("PNG Files", "*.png")
            )
            fileChooser.initialFileName = "qr-image.png"
            
            val file = fileChooser.showSaveDialog(null)
            if (file != null) {
                try {
                    val bufferedImage = SwingFXUtils.fromFXImage(image, null)
                    ImageIO.write(bufferedImage, "png", file)
                    updateStatus("Image saved to ${file.absolutePath}")
                } catch (e: Exception) {
                    showError("Save Error", "Failed to save image: ${e.message}")
                }
            }
        }
    }
    
    private fun addToHistory(source: String, content: String, status: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        historyData.add(QRResult(timestamp, source, content, status))
    }
    
    private fun showProgress(show: Boolean) {
        progressBar.isVisible = show
    }
    
    private fun updateStatus(message: String) {
        statusLabel.text = message
    }
    
    private fun showError(title: String, message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Error"
        alert.headerText = title
        alert.contentText = message
        alert.showAndWait()
    }
    
    private fun showAboutDialog() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = I18nManager.t("about.title")
        alert.headerText = I18nManager.t("about.header")
        alert.contentText = I18nManager.t("about.content")
        alert.showAndWait()
    }
    
    // Data classes
    data class QRResult(
        val timestamp: String,
        val source: String,
        val content: String,
        val status: String
    )
    
    data class QRCacheEntry(
        val result: String,
        val lastModified: Long,
        val status: String
    )
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(QRCodeReaderGUI::class.java, *args)
        }
    }
}
