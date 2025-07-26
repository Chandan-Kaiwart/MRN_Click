package com.apc.smartinstallation.ui.screens

import com.apc.smartinstallation.R
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
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
    private lateinit var binding: FragPdfBinding

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
        binding = FragPdfBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val oldImgPath = vm.ocrResults.value?.getOrNull(0)?.img_path
        val newImgPath = vm.ocrResults.value?.getOrNull(1)?.img_path

        val oldImgView = view.findViewById<ImageView>(R.id.ivOldMeterImage)
        val newImgView = view.findViewById<ImageView>(R.id.ivNewMeterImage)

        // Load OLD meter image
        if (!oldImgPath.isNullOrBlank()) {
            val url = oldImgPath.replace(
                "/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/",
                "http://195.35.20.141:3100/images/"
            )
            Glide.with(requireContext())
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(oldImgView)
        }

        // Load NEW meter image
        if (!newImgPath.isNullOrBlank()) {
            val url = newImgPath.replace(
                "/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/",
                "http://195.35.20.141:3100/images/"
            )
            Glide.with(requireContext())
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(newImgView)
        }


        setupClickListeners()
        setupBackPressHandler()

        // Check if we have required data
        validateRequiredData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }

        binding.btnGeneratePdf.setOnClickListener {
            generateComprehensivePdf()
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
            navController.navigateUp()
        }
    }

    private fun validateRequiredData() {
        val ocrResults = vm.ocrResults.value
        val consumer = vm.consumer.value

        if (ocrResults == null || ocrResults.size < 2 || consumer == null) {
            Toast.makeText(mContext, "Required data missing. Returning...", Toast.LENGTH_SHORT).show()
            navController.navigateUp()
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateComprehensivePdf() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGeneratePdf.isEnabled = false

        try {
            val ocrResults = vm.ocrResults.value!!
            val consumer = vm.consumer.value!!
            val ocrRes1 = ocrResults[0]
            val ocrRes2 = ocrResults[1]
            val oldReading = ocrRes1.manual_reading?.ifBlank { ocrRes1.ocr_reading } ?: "N/A"
            val newReading = ocrRes2.manual_reading?.ifBlank { ocrRes2.ocr_reading } ?: "N/A"
            val mrn = vm.mrnData.value
            val oldMeterImagePath = ocrRes1.img_path
            val newMeterImagePath = ocrRes2.img_path
            val mrnImagePath = vm.meterSlipPath.value // Or wherever you store MRN image path


            currentPdfFile = createComprehensiveMeterReplacementPDF(


                context = mContext,
                // Basic Information
                bookNo = "09",
                mrnSlipNo = "0426",
                circleName = "Circle A",
                division = "Division 1",
                subDivision = "Sub Division 1",
                feederName = "Feeder 123",
                dtrNameCode = "DTR-456",
                dateTime = getCurrentDateTime(),
                poleNo = "P-789",
                consumerName = consumer.name,
                consumerNo = consumer.acct_id,
                consumerMobile = consumer.mobile,
                consumerAddress = consumer.address,

                // OLD Meter Information
                oldMeterNo = ocrRes1.ocr_mno ?: "UNKOWN",
                oldMeterPhase = "3",
                oldMeterReading = oldReading,
                oldMeterReading2 = oldReading,
                oldMeterMD = "5.6",
                oldMeterManufacturer = ocrRes1.ocr_meter_make ?: "UNKNOWN",
                oldBoxSeal = "No",
                oldBodySeal = "No",
                oldTerminalSeal = "No",
                oldMeterStatus = "Working",

                // NEW Meter Information
                newMeterNo = ocrRes2.ocr_mno ?: "UNKNOWN",
                newMeterPhase = "1",
                newMeterReading = newReading,
                newMeterReading2 = newReading,
                newMeterMD = "0.0",
                newMeterManufacturer = ocrRes2.ocr_meter_make ?: "UNKNOWN",
                newBoxSeal = "Yes",
                newBodySeal = "Yes",
                newTerminalSeal = "Yes",
                newMeterStatus = "Installed",

                // MRN Information
                mrnOldMeterNo = mrn?.old_meter_no ?: "N/A",
                mrnSmartMeterNo =  "Dummy 123",
                mrnOldReading = mrn?.old_meter_reading ?: "N/A",
                mrnSmartReading =  "Dummy 123",
                mrnOldManufacturer = mrn?.old_meter_make ?: "N/A",
                mrnSmartManufacturer = mrn?.meter_make ?: "N/A",

                // Digital vs Field MRN
                digitalOldMeterNo = "902966",
                digitalSmartMeterNo = ocrRes2.ocr_mno,
                fieldOldMeterNo = ocrRes2.ocr_mno,
                fieldSmartMeterNo = "SC10512222",

                // Representative
                repName = "Technician A",
                oldMeterImagePath = oldMeterImagePath,
                newMeterImagePath = newMeterImagePath,
                mrnImagePath = mrnImagePath
            )

            renderPdfPreview()
            showActionButtons()

        } catch (e: Exception) {
            Log.e("PDFGeneration", "Error generating PDF", e)
            Toast.makeText(mContext, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            binding.progressBar.visibility = View.GONE
            binding.btnGeneratePdf.isEnabled = true
        }
    }

    private fun createComprehensiveMeterReplacementPDF(
        context: Context,
        // Basic Information
        bookNo: String,
        mrnSlipNo: String,
        circleName: String,
        division: String,
        subDivision: String,
        feederName: String,
        dtrNameCode: String,
        dateTime: String,
        poleNo: String,
        consumerName: String,
        consumerNo: String,
        consumerMobile: String,
        consumerAddress: String,

        // OLD Meter Information
        oldMeterNo: String,
        oldMeterPhase: String,
        oldMeterReading: String,
        oldMeterReading2: String,
        oldMeterMD: String,
        oldMeterManufacturer: String,
        oldBoxSeal: String,
        oldBodySeal: String,
        oldTerminalSeal: String,
        oldMeterStatus: String,

        // NEW Meter Information
        newMeterNo: String,
        newMeterPhase: String,
        newMeterReading: String,
        newMeterReading2: String,
        newMeterMD: String,
        newMeterManufacturer: String,
        newBoxSeal: String,
        newBodySeal: String,
        newTerminalSeal: String,
        newMeterStatus: String,

        // MRN Information
        mrnOldMeterNo: String,
        mrnSmartMeterNo: String,
        mrnOldReading: String,
        mrnSmartReading: String,
        mrnOldManufacturer: String,
        mrnSmartManufacturer: String,

        // Digital vs Field MRN
        digitalOldMeterNo: String,
        digitalSmartMeterNo: String,
        fieldOldMeterNo: String,
        fieldSmartMeterNo: String,

        // Representative
        repName: String,
        oldMeterImagePath: String?,
        newMeterImagePath: String?,
        mrnImagePath: String?
    ): File {
        val pdfDocument = PdfDocument()

        // Create multiple pages for comprehensive data
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
            fieldSmartMeterNo, repName, consumerName,oldMeterImagePath, newMeterImagePath, mrnImagePath)

        val file = File(context.filesDir, "comprehensive_meter_replacement_note_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use {
            pdfDocument.writeTo(it)
        }
        pdfDocument.close()
        return file
    }

    private fun createPage1(
        pdfDocument: PdfDocument,
        bookNo: String, mrnSlipNo: String, circleName: String, division: String,
        subDivision: String, feederName: String, dtrNameCode: String, dateTime: String,
        poleNo: String, consumerName: String, consumerNo: String, consumerMobile: String,
        consumerAddress: String, oldMeterNo: String, oldMeterPhase: String,
        oldMeterReading: String, oldMeterReading2: String, oldMeterMD: String,
        oldMeterManufacturer: String, oldBoxSeal: String, oldBodySeal: String,
        oldTerminalSeal: String, oldMeterStatus: String
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var yPosition = 60f

        // Header
        paint.textSize = 32f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("MGVCL - Meter Replacement Note", 40f, yPosition, paint)
        yPosition += 60f

        // Book and MRN Info
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Book No.: $bookNo", 40f, yPosition, paint)
        canvas.drawText("MRN Slip No.: $mrnSlipNo", 800f, yPosition, paint)
        yPosition += 50f

        // Basic Information Section
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(76, 175, 80) // Green color
        canvas.drawText("Basic Information", 40f, yPosition, paint)
        yPosition += 50f

        paint.textSize = 20f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.BLACK

        val basicInfoData = listOf(
            "Circle Name: $circleName" to "Division: $division",
            "Sub Division: $subDivision" to "Feeder Name: $feederName",
            "DTR Name/Code: $dtrNameCode" to "Date/Time: $dateTime",
            "Pole No.: $poleNo" to "Consumer Name: $consumerName",
            "Consumer No.: $consumerNo" to "Consumer Mobile: $consumerMobile"
        )

        basicInfoData.forEach { (left, right) ->
            canvas.drawText(left, 40f, yPosition, paint)
            canvas.drawText(right, 600f, yPosition, paint)
            yPosition += 40f
        }

        canvas.drawText("Consumer Address: $consumerAddress", 40f, yPosition, paint)
        yPosition += 60f

        // OLD Meter Information Section
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(76, 175, 80)
        canvas.drawText("OLD Meter Information", 40f, yPosition, paint)
        yPosition += 50f

        // Table headers
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("S.No.", 40f, yPosition, paint)
        canvas.drawText("Parameters", 120f, yPosition, paint)
        canvas.drawText("Details", 600f, yPosition, paint)
        yPosition += 40f

        // Draw line under headers
        paint.strokeWidth = 2f
        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)

        paint.textSize = 16f
        paint.typeface = Typeface.DEFAULT

        // Fixed: Using Triple instead of nested pairs
        val oldMeterData = listOf(
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

        oldMeterData.forEach { (serial, param, detail) ->
            canvas.drawText(serial, 40f, yPosition, paint)
            canvas.drawText(param, 120f, yPosition, paint)
            canvas.drawText(detail, 600f, yPosition, paint)
            yPosition += 35f
        }

        pdfDocument.finishPage(page)
    }
    private fun createPage2(
        pdfDocument: PdfDocument,
        newMeterNo: String, newMeterPhase: String, newMeterReading: String,
        newMeterReading2: String, newMeterMD: String, newMeterManufacturer: String,
        newBoxSeal: String, newBodySeal: String, newTerminalSeal: String,
        newMeterStatus: String, mrnOldMeterNo: String, mrnSmartMeterNo: String,
        mrnOldReading: String, mrnSmartReading: String, mrnOldManufacturer: String,
        mrnSmartManufacturer: String
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 2).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var yPosition = 60f

        // NEW Meter Information Section
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(33, 150, 243) // Blue color
        canvas.drawText("NEW Meter Information", 40f, yPosition, paint)
        yPosition += 50f

        // Table headers
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("S.No.", 40f, yPosition, paint)
        canvas.drawText("Parameters", 120f, yPosition, paint)
        canvas.drawText("Details", 600f, yPosition, paint)
        yPosition += 40f

        paint.strokeWidth = 2f
        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)

        paint.textSize = 16f
        paint.typeface = Typeface.DEFAULT

        // Fixed: Using Triple instead of nested pairs
        val newMeterData = listOf(
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

        newMeterData.forEach { (serial, param, detail) ->
            canvas.drawText(serial, 40f, yPosition, paint)
            canvas.drawText(param, 120f, yPosition, paint)
            canvas.drawText(detail, 600f, yPosition, paint)
            yPosition += 35f
        }

        yPosition += 40f

        // MRN Information Section
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(255, 152, 0) // Orange color
        canvas.drawText("MRN Information", 40f, yPosition, paint)
        yPosition += 50f

        // MRN Comparison Table
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("S.No.", 40f, yPosition, paint)
        canvas.drawText("Parameters", 120f, yPosition, paint)
        canvas.drawText("Old Meter", 500f, yPosition, paint)
        canvas.drawText("Smart Meter", 800f, yPosition, paint)
        yPosition += 40f

        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)

        paint.textSize = 16f
        paint.typeface = Typeface.DEFAULT

        // Fixed: Using proper data class for MRN data
        data class MrnDataRow(
            val serial: String,
            val parameter: String,
            val oldValue: String,
            val smartValue: String
        )

        val mrnData = listOf(
            MrnDataRow("1.", "Meter No.", mrnOldMeterNo, mrnSmartMeterNo),
            MrnDataRow("2.", "Meter Phase", "1", ""),
            MrnDataRow("3.", "Meter Reading (kWh)", mrnOldReading, mrnSmartReading),
            MrnDataRow("4.", "Meter Reading (kWh)", "", ""),
            MrnDataRow("5.", "Meter MD (KW) Only LTMD", "", ""),
            MrnDataRow("6.", "Meter Manufacturer", mrnOldManufacturer, mrnSmartManufacturer),
            MrnDataRow("7.", "Box Seal Available (Yes/No)", "No", "Yes"),
            MrnDataRow("8.", "Meter Body Seal (Yes/No)", "No", "Yes"),
            MrnDataRow("9.", "Terminal Seal No. (Yes/No)", "No", "Yes"),
            MrnDataRow("10.", "Meter Status", "Working", "Installed")
        )

        mrnData.forEach { row ->
            canvas.drawText(row.serial, 40f, yPosition, paint)
            canvas.drawText(row.parameter, 120f, yPosition, paint)
            canvas.drawText(row.oldValue, 500f, yPosition, paint)
            canvas.drawText(row.smartValue, 800f, yPosition, paint)
            yPosition += 35f
        }

        pdfDocument.finishPage(page)
    }

    private fun createPage3(
        pdfDocument: PdfDocument,
        digitalOldMeterNo: String, digitalSmartMeterNo: String,
        fieldOldMeterNo: String, fieldSmartMeterNo: String,
        repName: String, consumerName: String,
        oldMeterImagePath: String? = null,
        newMeterImagePath: String? = null,
        mrnImagePath: String?= null
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 3).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var yPosition = 60f

        // Digital MRN vs Field MRN Comparison
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(156, 39, 176) // Purple color
        canvas.drawText("Digital MRN vs Field MRN Comparison", 40f, yPosition, paint)
        yPosition += 50f

        // Comparison Table Headers
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("S.No.", 40f, yPosition, paint)
        canvas.drawText("Parameter", 100f, yPosition, paint)
        canvas.drawText("Digital Old", 300f, yPosition, paint)
        canvas.drawText("Digital Smart", 500f, yPosition, paint)
        canvas.drawText("Field Old", 700f, yPosition, paint)
        canvas.drawText("Field Smart", 900f, yPosition, paint)
        yPosition += 40f

        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT

        // Fixed: Using proper data class for comparison data
        data class ComparisonDataRow(
            val serial: String,
            val parameter: String,
            val digitalOld: String,
            val digitalSmart: String,
            val fieldOld: String,
            val fieldSmart: String
        )

        val comparisonData = listOf(
            ComparisonDataRow("1.", "Meter No.", digitalOldMeterNo, digitalSmartMeterNo, fieldOldMeterNo, fieldSmartMeterNo),
            ComparisonDataRow("2.", "Meter Phase", "1", "1", "1", "1"),
            ComparisonDataRow("3.", "Meter Reading", "2649", "0000010", "169797", "1.1"),
            ComparisonDataRow("4.", "Meter Reading", "", "", "", ""),
            ComparisonDataRow("5.", "Meter MD (KW)", "", "", "", ""),
            ComparisonDataRow("6.", "Manufacturer", "HPL", "L&T", "Genus", "Capital"),
            ComparisonDataRow("7.", "Box Seal", "Yes", "Yes", "Yes", "Yes"),
            ComparisonDataRow("8.", "Body Seal", "No", "Yes", "No", "Yes"),
            ComparisonDataRow("9.", "Terminal Seal", "Yes", "Yes", "Yes", "Yes"),
            ComparisonDataRow("10.", "Status", "Ok", "Installed", "Ok", "Ok")
        )

        comparisonData.forEach { row ->
            canvas.drawText(row.serial, 40f, yPosition, paint)
            canvas.drawText(row.parameter, 100f, yPosition, paint)
            canvas.drawText(row.digitalOld, 300f, yPosition, paint)
            canvas.drawText(row.digitalSmart, 500f, yPosition, paint)
            canvas.drawText(row.fieldOld, 700f, yPosition, paint)
            canvas.drawText(row.fieldSmart, 900f, yPosition, paint)
            yPosition += 30f
        }

        yPosition += 60f

        // Representative Information
        paint.textSize = 20f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Intellismart Representative Name: $repName", 40f, yPosition, paint)
        yPosition += 40f
        canvas.drawText("Helpline No.: 19124", 40f, yPosition, paint)
        yPosition += 80f

        // Display meter images
        try {
            val resizedWidth = 300
            val resizedHeight = 200

            oldMeterImagePath?.takeIf { it.isNotBlank() }?.let {
                val bitmap = BitmapFactory.decodeFile(it)
                val resized = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
                canvas.drawText("Old Meter Image", 40f, yPosition, paint)
                canvas.drawBitmap(resized, 40f, yPosition + 20f, null)
                yPosition += resizedHeight + 40f
            }

            newMeterImagePath?.takeIf { it.isNotBlank() }?.let {
                val bitmap = BitmapFactory.decodeFile(it)
                val resized = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
                canvas.drawText("New Meter Image", 40f, yPosition, paint)
                canvas.drawBitmap(resized, 40f, yPosition + 20f, null)
                yPosition += resizedHeight + 40f
            }

            mrnImagePath?.takeIf { it.isNotBlank() }?.let {
                val bitmap = BitmapFactory.decodeFile(it)
                val resized = Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
                canvas.drawText("MRN Image", 40f, yPosition, paint)
                canvas.drawBitmap(resized, 40f, yPosition + 20f, null)
                yPosition += resizedHeight + 40f
            }
        } catch (e: Exception) {
            Log.e("PDFGeneration", "Error adding meter images", e)
        }


        // Signatures Section
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Signatures", 40f, yPosition, paint)
        yPosition += 60f

        // Add signature images if available
        if (!vm.sign1.value.isNullOrEmpty() || !vm.sign2.value.isNullOrEmpty()) {
            try {
                vm.sign1.value?.let { signaturePath ->
                    val consumerBitmap = BitmapFactory.decodeFile(signaturePath)
                    consumerBitmap?.let {
                        val resized = Bitmap.createScaledBitmap(it, 300, 120, false)
                        paint.textSize = 18f
                        paint.typeface = Typeface.DEFAULT
                        canvas.drawText("Consumer Signature:", 40f, yPosition, paint)
                        canvas.drawBitmap(resized, 40f, yPosition + 20f, null)
                    }
                }

                vm.sign2.value?.let { signaturePath ->
                    val repBitmap = BitmapFactory.decodeFile(signaturePath)
                    repBitmap?.let {
                        val resized = Bitmap.createScaledBitmap(it, 300, 120, false)
                        paint.textSize = 18f
                        paint.typeface = Typeface.DEFAULT
                        canvas.drawText("Representative Signature:", 600f, yPosition, paint)
                        canvas.drawBitmap(resized, 600f, yPosition + 20f, null)
                    }
                }
            } catch (e: Exception) {
                Log.e("PDFGeneration", "Error adding signatures", e)
            }
        } else {
            // Draw signature boxes
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(40f, yPosition + 20f, 340f, yPosition + 140f, paint)
            canvas.drawRect(600f, yPosition + 20f, 900f, yPosition + 140f, paint)

            paint.style = Paint.Style.FILL
            paint.textSize = 18f
            canvas.drawText("Consumer Signature:", 40f, yPosition, paint)
            canvas.drawText("Representative Signature:", 600f, yPosition, paint)
        }

        pdfDocument.finishPage(page)
    }

    private fun renderPdfPreview() {
        currentPdfFile?.let { file ->
            try {
                val bitmap = renderPdfToBitmap(file, 0)
                binding.imageView2.setImageBitmap(bitmap)

                // Setup page navigation if multiple pages
                setupPageNavigation(file)

            } catch (e: Exception) {
                Log.e("PDFPreview", "Error rendering PDF preview", e)
                Toast.makeText(mContext, "Error displaying PDF preview", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPageNavigation(file: File) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            totalPages = pdfRenderer!!.pageCount

            if (totalPages > 1) {
                binding.pageNavigation.visibility = View.VISIBLE
                updatePageInfo()
            }

        } catch (e: Exception) {
            Log.e("PDFNavigation", "Error setting up page navigation", e)
        }
    }

    private fun renderCurrentPage() {
        currentPdfFile?.let { file ->
            val bitmap = renderPdfToBitmap(file, currentPage)
            binding.imageView2.setImageBitmap(bitmap)
            updatePageInfo()
        }
    }

    private fun updatePageInfo() {
        binding.tvPageInfo.text = "Page ${currentPage + 1} of $totalPages"
        binding.btnPrevPage.isEnabled = currentPage > 0
        binding.btnNextPage.isEnabled = currentPage < totalPages - 1
    }

    private fun renderPdfToBitmap(file: File, pageIndex: Int = 0): Bitmap? {
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val page = pdfRenderer.openPage(pageIndex)

            val bitmap = createBitmap(page.width * 2, page.height * 2) // Higher resolution
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            pdfRenderer.close()
            fileDescriptor.close()

            bitmap
        } catch (e: Exception) {
            Log.e("PDFRender", "Error rendering PDF to bitmap", e)
            null
        }
    }

    private fun showActionButtons() {
        binding.actionButtonsLayout.visibility = View.VISIBLE
        binding.btnGeneratePdf.text = "Regenerate PDF"
    }

    private fun openPdfWithExternalApp(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                mContext,
                "${mContext.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }

            pdfViewLauncher.launch(Intent.createChooser(intent, "Open PDF with"))

        } catch (e: Exception) {
            Toast.makeText(mContext, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadPdf(file: File) {
        // Implementation for downloading PDF to Downloads folder
        Toast.makeText(mContext, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
    }

    private fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                mContext,
                "${mContext.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share PDF"))

        } catch (e: Exception) {
            Toast.makeText(mContext, "Error sharing PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private var pdfViewLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle return from PDF viewer
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
    }
}
