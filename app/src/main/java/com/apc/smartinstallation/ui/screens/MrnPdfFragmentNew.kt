package com.apc.smartinstallation.ui.screens

import com.apc.smartinstallation.R
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.databinding.FragPdfBinding
import com.apc.smartinstallation.vm.MainViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView

@AndroidEntryPoint
class MrnPdfFragmentNew : Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
    private var _binding: FragPdfBinding? = null
    private val binding get() = _binding!!

    private val vm: MainViewModel by activityViewModels()
    private var currentPdfFile: File? = null
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage = 0
    private var totalPages = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragPdfBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            navController = Navigation.findNavController(view)

            // Initialize with safe defaults
            initializeViewModelDefaults()

            // Validate data before proceeding
            if (!validateRequiredData()) {
                return
            }

            loadMeterImages()
            setupClickListeners()
            setupBackPressHandler()

        } catch (e: Exception) {
            Log.e("MrnPdfFragment", "Error in onViewCreated", e)
            handleError("Error initializing PDF fragment: ${e.message}")
        }
    }

    private fun initializeViewModelDefaults() {
        // Ensure OCR results exist
        if (vm.ocrResults.value.isNullOrEmpty()) {
            val defaultList = mutableListOf(
                vm.ocrResults.value?.getOrNull(0) ?: createDefaultOcrResult("Old Meter (KWH)"),
                vm.ocrResults.value?.getOrNull(1) ?: createDefaultOcrResult("New Meter (KWH)")
            )
            vm.ocrResults.value = defaultList
        }

        // Ensure consumer exists
        if (vm.consumer.value == null) {
            Log.w("MrnPdfFragment", "Consumer data is null")
        }
    }

    private fun createDefaultOcrResult(register: String) =
        com.apc.smartinstallation.dataClasses.ocr.response.OcrResult(
            "", "", "", "", 1, "", "", "", "", "", "", 0, register
        )

    private fun loadMeterImages() {
        try {
            val ocrResults = vm.ocrResults.value ?: return

            val oldImgPath = ocrResults.getOrNull(0)?.img_path
            val newImgPath = ocrResults.getOrNull(1)?.img_path

            val oldImgView = view?.findViewById<ImageView>(R.id.ivOldMeterImage)
            val newImgView = view?.findViewById<ImageView>(R.id.ivNewMeterImage)

            // Load OLD meter image safely
            oldImgPath?.takeIf { it.isNotBlank() }?.let { path ->
                oldImgView?.let { imageView ->
                    val url = path.replace(
                        "/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/",
                        "http://195.35.20.141:3100/images/"
                    )
                    Glide.with(this)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(imageView)
                }
            }

            // Load NEW meter image safely
            newImgPath?.takeIf { it.isNotBlank() }?.let { path ->
                newImgView?.let { imageView ->
                    val url = path.replace(
                        "/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/",
                        "http://195.35.20.141:3100/images/"
                    )
                    Glide.with(this)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(imageView)
                }
            }
        } catch (e: Exception) {
            Log.e("MrnPdfFragment", "Error loading images", e)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            safeNavigateBack()
        }

        binding.btnGeneratePdf.setOnClickListener {
            generatePdfSafely()
        }

        binding.btnOpenPdf.setOnClickListener {
            currentPdfFile?.let { openPdfWithExternalApp(it) }
        }

        binding.btnDownloadPdf.setOnClickListener {
            currentPdfFile?.let { downloadPdf(it) }
        }

        binding.btnSharePdf.setOnClickListener {
            currentPdfFile?.let { sharePdf(it) }
        }

        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                renderCurrentPage()
            }
        }

        binding.btnNextPage.setOnClickListener {
            if (currentPage < totalPages - 1) {
                currentPage++
                renderCurrentPage()
            }
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            safeNavigateBack()
        }
    }

    private fun safeNavigateBack() {
        try {
            if (::navController.isInitialized) {
                navController.navigateUp()
            } else {
                requireActivity().onBackPressed()
            }
        } catch (e: Exception) {
            Log.e("MrnPdfFragment", "Error navigating back", e)
            requireActivity().finish()
        }
    }

    private fun validateRequiredData(): Boolean {
        val ocrResults = vm.ocrResults.value
        val consumer = vm.consumer.value

        if (ocrResults.isNullOrEmpty() || ocrResults.size < 2) {
            handleError("Meter reading data missing. Cannot generate PDF.")
            return false
        }

        if (consumer == null) {
            Log.w("MrnPdfFragment", "Consumer data missing, using defaults")
            // Don't fail here, just use defaults
        }

        return true
    }

    private fun generatePdfSafely() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(mContext, "PDF generation requires Android 5.0 or higher", Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGeneratePdf.isEnabled = false

        try {
            val ocrResults = vm.ocrResults.value
            if (ocrResults == null || ocrResults.size < 2) {
                throw IllegalStateException("Insufficient meter data")
            }

            val consumer = vm.consumer.value
            val ocrRes1 = ocrResults[0]
            val ocrRes2 = ocrResults[1]

            val oldReading = getSafeReading(ocrRes1)
            val newReading = getSafeReading(ocrRes2)
            val mrn = vm.mrnData.value

            currentPdfFile = createComprehensiveMeterReplacementPDF(
                context = mContext,
                // Basic Information
                bookNo = vm.bookNo.value ?: "09",
                mrnSlipNo = vm.mrnSlipNo.value ?: "0426",
                circleName = vm.circleName.value ?: "Circle A",
                division = vm.division.value ?: "Division 1",
                subDivision = vm.subDivision.value ?: "Sub Division 1",
                feederName = vm.feederName.value ?: "Feeder 123",
                dtrNameCode = vm.dtrNameCode.value ?: "DTR-456",
                dateTime = getCurrentDateTime(),
                poleNo = vm.poleNo.value ?: "P-789",
                consumerName = consumer?.name ?: "Unknown Consumer",
                consumerNo = consumer?.acct_id ?: "Unknown ID",
                consumerMobile = consumer?.mobile ?: "N/A",
                consumerAddress = consumer?.address ?: "N/A",

                // OLD Meter Information
                oldMeterNo = ocrRes1.ocr_mno ?: "UNKNOWN",
                oldMeterPhase = vm.oldMeterPhase.value ?: "1",
                oldMeterReading = oldReading,
                oldMeterReading2 = oldReading,
                oldMeterMD = vm.oldMeterMD.value ?: "5.6",
                oldMeterManufacturer = ocrRes1.ocr_meter_make ?: "UNKNOWN",
                oldBoxSeal = vm.oldBoxSeal.value ?: "No",
                oldBodySeal = vm.oldBodySeal.value ?: "No",
                oldTerminalSeal = vm.oldTerminalSeal.value ?: "No",
                oldMeterStatus = vm.oldMeterStatus.value ?: "Working",

                // NEW Meter Information
                newMeterNo = ocrRes2.ocr_mno ?: "UNKNOWN",
                newMeterPhase = vm.newMeterPhase.value ?: "1",
                newMeterReading = newReading,
                newMeterReading2 = newReading,
                newMeterMD = vm.newMeterMD.value ?: "0.0",
                newMeterManufacturer = ocrRes2.ocr_meter_make ?: "UNKNOWN",
                newBoxSeal = vm.newBoxSeal.value ?: "Yes",
                newBodySeal = vm.newBodySeal.value ?: "Yes",
                newTerminalSeal = vm.newTerminalSeal.value ?: "Yes",
                newMeterStatus = vm.newMeterStatus.value ?: "Installed",

                // MRN Information
                mrnOldMeterNo = mrn?.old_meter_no ?: ocrRes1.ocr_mno ?: "N/A",
                mrnSmartMeterNo = mrn?.image_name ?: ocrRes2.ocr_mno ?: "N/A", // you might want a real new meter no field later
                mrnOldReading = mrn?.old_meter_reading ?: oldReading,
                mrnSmartReading = mrn?.reading ?: newReading,   // <-- use `reading`
                mrnOldManufacturer = mrn?.old_meter_make ?: ocrRes1.ocr_meter_make ?: "N/A",
                mrnSmartManufacturer = mrn?.meter_make ?: ocrRes2.ocr_meter_make ?: "N/A",

                // Digital vs Field MRN
                digitalOldMeterNo = ocrRes1.ocr_mno ?: "N/A",
                digitalSmartMeterNo = ocrRes2.ocr_mno ?: "N/A",
                fieldOldMeterNo = ocrRes1.ocr_mno ?: "N/A",
                fieldSmartMeterNo = ocrRes2.ocr_mno ?: "N/A",

                // Representative
                repName = vm.repName.value ?: "Technician A",
                oldMeterImagePath = ocrRes1.img_path,
                newMeterImagePath = ocrRes2.img_path,
                mrnImagePath = vm.meterSlipPath.value
            )

            renderPdfPreview()
//            showActionButtons()
            Toast.makeText(mContext, "PDF generated successfully!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("PDFGeneration", "Error generating PDF", e)
            handleError("Error generating PDF: ${e.message}")
        } finally {
            binding.progressBar.visibility = View.GONE
            binding.btnGeneratePdf.isEnabled = true
        }
    }

    private fun getSafeReading(ocrResult: com.apc.smartinstallation.dataClasses.ocr.response.OcrResult): String {
        return ocrResult.manual_reading?.takeIf { it.isNotBlank() }
            ?: ocrResult.ocr_reading?.takeIf { it.isNotBlank() }
            ?: "N/A"
    }

    private fun handleError(message: String) {
        Log.e("MrnPdfFragment", message)
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
        safeNavigateBack()
    }

    private fun createComprehensiveMeterReplacementPDF(
        context: Context,
        bookNo: String, mrnSlipNo: String, circleName: String, division: String,
        subDivision: String, feederName: String, dtrNameCode: String, dateTime: String,
        poleNo: String, consumerName: String, consumerNo: String, consumerMobile: String,
        consumerAddress: String, oldMeterNo: String, oldMeterPhase: String,
        oldMeterReading: String, oldMeterReading2: String, oldMeterMD: String,
        oldMeterManufacturer: String, oldBoxSeal: String, oldBodySeal: String,
        oldTerminalSeal: String, oldMeterStatus: String, newMeterNo: String,
        newMeterPhase: String, newMeterReading: String, newMeterReading2: String,
        newMeterMD: String, newMeterManufacturer: String, newBoxSeal: String,
        newBodySeal: String, newTerminalSeal: String, newMeterStatus: String,
        mrnOldMeterNo: String, mrnSmartMeterNo: String, mrnOldReading: String,
        mrnSmartReading: String, mrnOldManufacturer: String, mrnSmartManufacturer: String,
        digitalOldMeterNo: String, digitalSmartMeterNo: String, fieldOldMeterNo: String,
        fieldSmartMeterNo: String, repName: String, oldMeterImagePath: String?,
        newMeterImagePath: String?, mrnImagePath: String?
    ): File {
        val pdfDocument = PdfDocument()

        try {
            // Create pages safely
            createPage1(pdfDocument, bookNo, mrnSlipNo, circleName, division, subDivision,
                feederName, dtrNameCode, dateTime, poleNo, consumerName, consumerNo,
                consumerMobile, consumerAddress, oldMeterNo, oldMeterPhase, oldMeterReading,
                oldMeterReading2, oldMeterMD, oldMeterManufacturer, oldBoxSeal, oldBodySeal,
                oldTerminalSeal, oldMeterStatus)

            createPage2(pdfDocument, newMeterNo, newMeterPhase, newMeterReading, newMeterReading2,
                newMeterMD, newMeterManufacturer, newBoxSeal, newBodySeal, newTerminalSeal,
                newMeterStatus, mrnOldMeterNo, mrnSmartMeterNo, mrnOldReading, mrnSmartReading,
                mrnOldManufacturer, mrnSmartManufacturer)

            createPage3(pdfDocument, digitalOldMeterNo, digitalSmartMeterNo, fieldOldMeterNo,
                fieldSmartMeterNo, repName, consumerName, oldMeterImagePath, newMeterImagePath, mrnImagePath)

            val file = File(context.filesDir, "meter_replacement_note_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use {
                pdfDocument.writeTo(it)
            }
            return file

        } catch (e: Exception) {
            Log.e("PDFCreation", "Error creating PDF", e)
            throw e
        } finally {
            try {
                pdfDocument.close()
            } catch (e: Exception) {
                Log.e("PDFCreation", "Error closing PDF document", e)
            }
        }
    }

    private fun createPage1(
        pdfDocument: PdfDocument, bookNo: String, mrnSlipNo: String, circleName: String,
        division: String, subDivision: String, feederName: String, dtrNameCode: String,
        dateTime: String, poleNo: String, consumerName: String, consumerNo: String,
        consumerMobile: String, consumerAddress: String, oldMeterNo: String,
        oldMeterPhase: String, oldMeterReading: String, oldMeterReading2: String,
        oldMeterMD: String, oldMeterManufacturer: String, oldBoxSeal: String,
        oldBodySeal: String, oldTerminalSeal: String, oldMeterStatus: String
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }

        var yPosition = 80f

        // Header
        paint.apply {
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        canvas.drawText("MGVCL - Meter Replacement Note", 40f, yPosition, paint)
        yPosition += 70f

        // Book and MRN Info
        paint.apply {
            textSize = 20f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText("Book No.: $bookNo", 40f, yPosition, paint)
        canvas.drawText("MRN Slip No.: $mrnSlipNo", 650f, yPosition, paint)
        yPosition += 60f

        // Basic Information Section
        drawSectionHeader(canvas, paint, "Basic Information", yPosition)
        yPosition += 60f

        paint.apply {
            textSize = 18f
            typeface = Typeface.DEFAULT
            color = Color.BLACK
        }

        val basicInfo = arrayOf(
            "Circle Name: $circleName" to "Division: $division",
            "Sub Division: $subDivision" to "Feeder Name: $feederName",
            "DTR Name/Code: $dtrNameCode" to "Date/Time: $dateTime",
            "Pole No.: $poleNo" to "Consumer Name: $consumerName",
            "Consumer No.: $consumerNo" to "Consumer Mobile: $consumerMobile"
        )

        basicInfo.forEach { (left, right) ->
            canvas.drawText(left, 40f, yPosition, paint)
            canvas.drawText(right, 600f, yPosition, paint)
            yPosition += 35f
        }

        canvas.drawText("Consumer Address: $consumerAddress", 40f, yPosition, paint)
        yPosition += 70f

        // OLD Meter Information
        drawSectionHeader(canvas, paint, "OLD Meter Information", yPosition)
        yPosition += 60f

        drawTableHeaders(canvas, paint, yPosition)
        yPosition += 50f

        val oldMeterData = arrayOf(
            Triple("1.", "Old Meter No.", oldMeterNo),
            Triple("2.", "Old Meter Phase", oldMeterPhase),
            Triple("3.", "Old Meter Reading (kWh)", oldMeterReading),
            Triple("4.", "Meter Reading (kWh)", oldMeterReading2),
            Triple("5.", "Meter MD (KW) Only LTMD", oldMeterMD),
            Triple("6.", "Old Meter Manufacturer", oldMeterManufacturer),
            Triple("7.", "Old Box Seal Available (Yes/No)", oldBoxSeal),
            Triple("8.", "Old Meter Body Seal (Yes/No)", oldBodySeal),
            Triple("9.", "Old Terminal Seal No. (Yes/No)", oldTerminalSeal),
            Triple("10.", "Meter Status", oldMeterStatus)
        )

        paint.textSize = 16f
        oldMeterData.forEach { (serial, param, detail) ->
            canvas.drawText(serial, 40f, yPosition, paint)
            canvas.drawText(param, 120f, yPosition, paint)
            canvas.drawText(detail ?: "N/A", 600f, yPosition, paint)
            yPosition += 32f
        }

        pdfDocument.finishPage(page)
    }

    private fun createPage2(
        pdfDocument: PdfDocument, newMeterNo: String, newMeterPhase: String,
        newMeterReading: String, newMeterReading2: String, newMeterMD: String,
        newMeterManufacturer: String, newBoxSeal: String, newBodySeal: String,
        newTerminalSeal: String, newMeterStatus: String, mrnOldMeterNo: String,
        mrnSmartMeterNo: String, mrnOldReading: String, mrnSmartReading: String,
        mrnOldManufacturer: String, mrnSmartManufacturer: String
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 2).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }

        var yPosition = 80f

        // NEW Meter Information
        drawSectionHeader(canvas, paint, "NEW Meter Information", yPosition)
        yPosition += 60f

        drawTableHeaders(canvas, paint, yPosition)
        yPosition += 50f

        val newMeterData = arrayOf(
            Triple("1.", "New Meter No.", newMeterNo),
            Triple("2.", "New Meter Phase", newMeterPhase),
            Triple("3.", "New Meter Reading (kWh)", newMeterReading),
            Triple("4.", "Meter Reading (kWh)", newMeterReading2),
            Triple("5.", "Meter MD (KW) Only LTMD", newMeterMD),
            Triple("6.", "New Meter Manufacturer", newMeterManufacturer),
            Triple("7.", "New Box Seal Available (Yes/No)", newBoxSeal),
            Triple("8.", "New Meter Body Seal (Yes/No)", newBodySeal),
            Triple("9.", "New Terminal Seal No. (Yes/No)", newTerminalSeal),
            Triple("10.", "Meter Status", newMeterStatus)
        )

        paint.textSize = 16f
        paint.color = Color.BLACK
        newMeterData.forEach { (serial, param, detail) ->
            canvas.drawText(serial, 40f, yPosition, paint)
            canvas.drawText(param, 120f, yPosition, paint)
            canvas.drawText(detail ?: "N/A", 600f, yPosition, paint)
            yPosition += 32f
        }

        yPosition += 50f

        // MRN Comparison
        drawSectionHeader(canvas, paint, "MRN Information", yPosition)
        yPosition += 60f

        // MRN headers
        paint.apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Parameter", 120f, yPosition, paint)
        canvas.drawText("Old Meter", 500f, yPosition, paint)
        canvas.drawText("Smart Meter", 800f, yPosition, paint)
        yPosition += 40f

        paint.strokeWidth = 2f
        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)

        paint.apply {
            textSize = 14f
            typeface = Typeface.DEFAULT
        }

        val mrnData = arrayOf(
            Triple("Meter No.", mrnOldMeterNo, mrnSmartMeterNo),
            Triple("Meter Reading", mrnOldReading, mrnSmartReading),
            Triple("Manufacturer", mrnOldManufacturer, mrnSmartManufacturer)
        )

        mrnData.forEach { (param, oldValue, smartValue) ->
            canvas.drawText(param, 120f, yPosition, paint)
            canvas.drawText(oldValue ?: "N/A", 500f, yPosition, paint)
            canvas.drawText(smartValue ?: "N/A", 800f, yPosition, paint)
            yPosition += 35f
        }

        pdfDocument.finishPage(page)
    }

    private fun createPage3(
        pdfDocument: PdfDocument, digitalOldMeterNo: String, digitalSmartMeterNo: String,
        fieldOldMeterNo: String, fieldSmartMeterNo: String, repName: String,
        consumerName: String, oldMeterImagePath: String?, newMeterImagePath: String?,
        mrnImagePath: String?
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 3).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }

        var yPosition = 80f

        // Representative info
        paint.apply {
            textSize = 20f
            typeface = Typeface.DEFAULT
            color = Color.BLACK
        }
        canvas.drawText("Representative: $repName", 40f, yPosition, paint)
        yPosition += 40f
        canvas.drawText("Consumer: $consumerName", 40f, yPosition, paint)
        yPosition += 60f

        // Add images safely
        yPosition = addMeterImageSafely(canvas, paint, "Old Meter Image", oldMeterImagePath, 40f, yPosition)
        yPosition += 30f
        yPosition = addMeterImageSafely(canvas, paint, "New Meter Image", newMeterImagePath, 40f, yPosition)
        yPosition += 30f
        yPosition = addMeterImageSafely(canvas, paint, "MRN Image", mrnImagePath, 40f, yPosition)

        // Signatures
        yPosition += 100f
        addSignatures(canvas, paint, yPosition)

        pdfDocument.finishPage(page)
    }

    private fun addMeterImageSafely(canvas: Canvas, paint: Paint, title: String, imagePath: String?, x: Float, y: Float): Float {
        var currentY = y

        paint.apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        canvas.drawText(title, x, currentY, paint)
        currentY += 30f

        try {
            if (!imagePath.isNullOrBlank() && File(imagePath).exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                bitmap?.let {
                    val scaledBitmap = Bitmap.createScaledBitmap(it, 300, 200, false)
                    canvas.drawBitmap(scaledBitmap, x, currentY, null)
                    scaledBitmap.recycle()
                    currentY += 220f
                } ?: run {
                    paint.textSize = 14f
                    canvas.drawText("Image not available", x, currentY, paint)
                    currentY += 30f
                }
            } else {
                paint.textSize = 14f
                canvas.drawText("Image not available", x, currentY, paint)
                currentY += 30f
            }
        } catch (e: Exception) {
            Log.e("PDFGeneration", "Error adding image: $title", e)
            paint.textSize = 14f
            canvas.drawText("Error loading image", x, currentY, paint)
            currentY += 30f
        }

        return currentY
    }

    private fun addSignatures(canvas: Canvas, paint: Paint, yPosition: Float) {
        paint.apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        canvas.drawText("Signatures", 40f, yPosition, paint)

        val sigY = yPosition + 40f

        try {
            // Consumer signature
            val sign1Path = vm.sign1.value
            if (!sign1Path.isNullOrBlank() && File(sign1Path).exists()) {
                val bitmap = BitmapFactory.decodeFile(sign1Path)
                bitmap?.let {
                    val scaled = Bitmap.createScaledBitmap(it, 250, 100, false)
                    canvas.drawText("Consumer:", 40f, sigY, paint)
                    canvas.drawBitmap(scaled, 40f, sigY + 20f, null)
                    scaled.recycle()
                }
            } else {
                // Draw signature box
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawRect(40f, sigY + 20f, 290f, sigY + 120f, paint)
                paint.style = Paint.Style.FILL
                paint.textSize = 16f
                canvas.drawText("Consumer Signature", 50f, sigY + 75f, paint)
            }

            // Representative signature
            // Representative signature
            val sign2Path = vm.sign2.value
            if (!sign2Path.isNullOrBlank() && File(sign2Path).exists()) {
                val bitmap = BitmapFactory.decodeFile(sign2Path)
                bitmap?.let {
                    val scaled = Bitmap.createScaledBitmap(it, 250, 100, false)
                    canvas.drawText("Representative:", 350f, sigY, paint)
                    canvas.drawBitmap(scaled, 350f, sigY + 20f, null)
                    scaled.recycle()
                }
            } else {
                // Draw signature box
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawRect(350f, sigY + 20f, 600f, sigY + 120f, paint)
                paint.style = Paint.Style.FILL
                paint.textSize = 16f
                canvas.drawText("Representative Signature", 360f, sigY + 75f, paint)
            }
        } catch (e: Exception) {
            Log.e("PDFGeneration", "Error adding signatures", e)
            paint.textSize = 16f
            canvas.drawText("Error loading signatures", 40f, yPosition + 150f, paint)
        }
    }

    private fun renderPdfPreview() {
        try {
            currentPdfFile?.let { file ->
                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor)
                totalPages = pdfRenderer?.pageCount ?: 0
                currentPage = 0
                renderCurrentPage()
            }
        } catch (e: Exception) {
            Log.e("PDFPreview", "Error rendering PDF", e)
            Toast.makeText(mContext, "Failed to render PDF preview", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderCurrentPage() {
        try {
            val renderer = pdfRenderer ?: return
            if (currentPage < 0 || currentPage >= renderer.pageCount) return

            renderer.openPage(currentPage).use { page ->
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                binding.imageView2.setImageBitmap(bitmap)
                binding.tvPageInfo.text = "Page ${currentPage + 1} of $totalPages"
                binding.pageNavigation.visibility = if (totalPages > 1) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            Log.e("PDFPreview", "Error rendering page", e)
        }
    }

    private fun openPdfWithExternalApp(file: File) {
        try {
            val uri = FileProvider.getUriForFile(mContext, "${mContext.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("OpenPDF", "Error opening PDF", e)
            Toast.makeText(mContext, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadPdf(file: File) {
        try {
            val downloads = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path)
            if (!downloads.exists()) downloads.mkdirs()
            val destFile = File(downloads, file.name)
            file.copyTo(destFile, overwrite = true)
            Toast.makeText(mContext, "Saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DownloadPDF", "Error saving PDF", e)
            Toast.makeText(mContext, "Failed to save PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(mContext, "${mContext.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            Log.e("SharePDF", "Error sharing PDF", e)
            Toast.makeText(mContext, "Failed to share PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun drawSectionHeader(canvas: Canvas, paint: Paint, title: String, y: Float) {
        paint.apply {
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLUE
        }
        canvas.drawText(title, 40f, y, paint)
    }

    private fun drawTableHeaders(canvas: Canvas, paint: Paint, y: Float) {
        paint.apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        canvas.drawText("Sr.", 40f, y, paint)
        canvas.drawText("Parameter", 120f, y, paint)
        canvas.drawText("Details", 600f, y, paint)
    }
}
