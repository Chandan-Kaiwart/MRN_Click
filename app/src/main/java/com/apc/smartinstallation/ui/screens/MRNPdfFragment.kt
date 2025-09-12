package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
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
import com.apc.smartinstallation.databinding.FragPdfNewBinding
import com.apc.smartinstallation.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MrnPdfFragment : Fragment() {}
//{
//    private lateinit var mContext: Context
//    private lateinit var navController: NavController
//    private lateinit var binding: FragPdfNewBinding
//
//    private val vm: MainViewModel by activityViewModels()
//    private var currentPdfFile: File? = null
//    private var pdfRenderer: PdfRenderer? = null
//    private var currentPage = 0
//    private var totalPages = 0
//
//    val oldMeter = vm.ocrResults.value?.getOrNull(0)
//    val consumer = vm.consumer.value
//
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        mContext = context
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragPdfNewBinding.inflate(inflater)
//        return binding.root
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        navController = Navigation.findNavController(view)
//
//        setupClickListeners()
//        setupBackPressHandler()
//
//        // Check if we have required data
//        validateRequiredData()
//
//
//        val oldMeter = vm.ocrResults.value?.getOrNull(0)
//        val newMeter = vm.ocrResults.value?.getOrNull(1)
//        val consumer = vm.consumer.value
//
//// OLD Meter
//        binding.etOldMeterNo.setText(oldMeter?.ocr_mno ?: "")
//        binding.etOldMeterReading.setText(oldMeter?.manual_reading ?: oldMeter?.ocr_reading ?: "")
//        binding.etOldMeterManufacturer.setText(oldMeter?.ocr_meter_make ?: "")
//
//// NEW Meter
//        binding.etNewMeterNo.setText(newMeter?.ocr_mno ?: "")
//        binding.etNewMeterReading.setText(newMeter?.manual_reading ?: newMeter?.ocr_reading ?: "")
//        binding.etNewMeterManufacturer.setText(newMeter?.ocr_meter_make ?: "")
//
//// Consumer Info
//        binding.etConsumerName.setText(consumer?.name ?: "")
//        binding.etConsumerNo.setText(consumer?.acct_id ?: "")
//        binding.etConsumerMobile.setText(consumer?.mobile ?: "")
//        binding.etConsumerAddress.setText(consumer?.address ?: "")
//
//// Current DateTime
//        binding.etDateTime.setText(getFormattedDateTime())
//
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun setupClickListeners() {
//        binding.btnBack.setOnClickListener {
//            navController.navigateUp()
//        }
//
//        binding.btnGeneratePdf.setOnClickListener {
//            generateComprehensivePdf()
//        }
//
//        binding.btnOpenPdf.setOnClickListener {
//            currentPdfFile?.let { openPdfWithExternalApp(it) }
//        }
//
//        binding.btnDownloadPdf.setOnClickListener {
//            currentPdfFile?.let { downloadPdf(it) }
//        }
//
//        binding.btnSharePdf.setOnClickListener {
//            currentPdfFile?.let { sharePdf(it) }
//        }
//
//        binding.btnPrevPage.setOnClickListener {
//            if (currentPage > 0) {
//                currentPage--
//                renderCurrentPage()
//            }
//        }
//
//        binding.btnNextPage.setOnClickListener {
//            if (currentPage < totalPages - 1) {
//                currentPage++
//                renderCurrentPage()
//            }
//        }
//    }
//
//    private fun setupBackPressHandler() {
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            navController.navigateUp()
//        }
//    }
//
//    private fun validateRequiredData() {
//        val ocrResults = vm.ocrResults.value
//        val consumer = vm.consumer.value
//
//        if (ocrResults == null || ocrResults.size < 2 || consumer == null) {
//            Toast.makeText(mContext, "Required data missing. Returning...", Toast.LENGTH_SHORT).show()
//            navController.navigateUp()
//            return
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun generateComprehensivePdf() {
//        binding.progressBar.visibility = View.VISIBLE
//        binding.btnGeneratePdf.isEnabled = false
//
//        try {
//            val ocrResults = vm.ocrResults.value!!
//            val consumer = vm.consumer.value!!
//            val ocrRes1 = ocrResults[0]
//            val ocrRes2 = ocrResults[1]
//
//            currentPdfFile = createComprehensiveMeterReplacementPDF(
//                context = mContext,
//                // Basic Information
//                bookNo = "09",
//                mrnSlipNo = "0426",
//                circleName = "Circle A",
//                division = "Division 1",
//                subDivision = "Sub Division 1",
//                feederName = "Feeder 123",
//                dtrNameCode = "DTR-456",
//                dateTime = getCurrentDateTime(),
//                poleNo = "P-789",
//                consumerName = consumer.name,
//                consumerNo = consumer.acct_id,
//                consumerMobile = consumer.mobile,
//                consumerAddress = consumer.address,
//
//                // OLD Meter Information
//                oldMeterNo = ocrRes1.ocr_mno,
//                oldMeterPhase = "1",
//                oldMeterReading = ocrRes1.ocr_reading,
//                oldMeterReading2 = ocrRes1.ocr_reading,
//                oldMeterMD = "5.6",
//                oldMeterManufacturer = ocrRes1.ocr_meter_make,
//                oldBoxSeal = "No",
//                oldBodySeal = "No",
//                oldTerminalSeal = "No",
//                oldMeterStatus = "Working",
//
//                // NEW Meter Information
//                newMeterNo = ocrRes2.ocr_mno,
//                newMeterPhase = "1",
//                newMeterReading = ocrRes2.ocr_reading,
//                newMeterReading2 = ocrRes2.ocr_reading,
//                newMeterMD = "0.0",
//                newMeterManufacturer = ocrRes2.ocr_meter_make,
//                newBoxSeal = "Yes",
//                newBodySeal = "Yes",
//                newTerminalSeal = "Yes",
//                newMeterStatus = "Installed",
//
//                // MRN Information
//                mrnOldMeterNo = ocrRes1.ocr_mno,
//                mrnSmartMeterNo = "MGTCPG0008530",
//                mrnOldReading = "169797",
//                mrnSmartReading = "03",
//                mrnOldManufacturer = "Genus",
//                mrnSmartManufacturer = "Capital",
//
//                // Digital vs Field MRN
//                digitalOldMeterNo = "902966",
//                digitalSmartMeterNo = ocrRes2.ocr_mno,
//                fieldOldMeterNo = ocrRes2.ocr_mno,
//                fieldSmartMeterNo = "SC10512222",
//
//                // Representative
//                repName = "Technician A"
//            )
//
//            renderPdfPreview()
//            showActionButtons()
//
//        } catch (e: Exception) {
//            Log.e("PDFGeneration", "Error generating PDF", e)
//            Toast.makeText(mContext, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
//        } finally {
//            binding.progressBar.visibility = View.GONE
//            binding.btnGeneratePdf.isEnabled = true
//        }
//    }
//
//    private fun createComprehensiveMeterReplacementPDF(
//        context: Context,
//        // Basic Information
//        bookNo: String,
//        mrnSlipNo: String,
//        circleName: String,
//        division: String,
//        subDivision: String,
//        feederName: String,
//        dtrNameCode: String,
//        dateTime: String,
//        poleNo: String,
//        consumerName: String,
//        consumerNo: String,
//        consumerMobile: String,
//        consumerAddress: String,
//
//        // OLD Meter Information
//        oldMeterNo: String,
//        oldMeterPhase: String,
//        oldMeterReading: String,
//        oldMeterReading2: String,
//        oldMeterMD: String,
//        oldMeterManufacturer: String,
//        oldBoxSeal: String,
//        oldBodySeal: String,
//        oldTerminalSeal: String,
//        oldMeterStatus: String,
//
//        // NEW Meter Information
//        newMeterNo: String,
//        newMeterPhase: String,
//        newMeterReading: String,
//        newMeterReading2: String,
//        newMeterMD: String,
//        newMeterManufacturer: String,
//        newBoxSeal: String,
//        newBodySeal: String,
//        newTerminalSeal: String,
//        newMeterStatus: String,
//
//        // MRN Information
//        mrnOldMeterNo: String,
//        mrnSmartMeterNo: String,
//        mrnOldReading: String,
//        mrnSmartReading: String,
//        mrnOldManufacturer: String,
//        mrnSmartManufacturer: String,
//
//        // Digital vs Field MRN
//        digitalOldMeterNo: String,
//        digitalSmartMeterNo: String,
//        fieldOldMeterNo: String,
//        fieldSmartMeterNo: String,
//
//        // Representative
//        repName: String
//    ): File {
//        val pdfDocument = PdfDocument()
//
//        // Create multiple pages for comprehensive data
//        createPage1(pdfDocument, bookNo, mrnSlipNo, circleName, division, subDivision,
//            feederName, dtrNameCode, dateTime, poleNo, consumerName, consumerNo,
//            consumerMobile, consumerAddress, oldMeterNo, oldMeterPhase, oldMeterReading,
//            oldMeterReading2, oldMeterMD, oldMeterManufacturer, oldBoxSeal, oldBodySeal,
//            oldTerminalSeal, oldMeterStatus)
//
//        createPage2(pdfDocument, newMeterNo, newMeterPhase, newMeterReading, newMeterReading2,
//            newMeterMD, newMeterManufacturer, newBoxSeal, newBodySeal, newTerminalSeal,
//            newMeterStatus, mrnOldMeterNo, mrnSmartMeterNo, mrnOldReading, mrnSmartReading,
//            mrnOldManufacturer, mrnSmartManufacturer)
//
//        createPage3(pdfDocument, digitalOldMeterNo, digitalSmartMeterNo, fieldOldMeterNo,
//            fieldSmartMeterNo, repName, consumerName)
//
//        val file = File(context.filesDir, "comprehensive_meter_replacement_note_${System.currentTimeMillis()}.pdf")
//        FileOutputStream(file).use {
//            pdfDocument.writeTo(it)
//        }
//        pdfDocument.close()
//        return file
//    }
//
//    private fun createPage1(
//        pdfDocument: PdfDocument,
//        bookNo: String, mrnSlipNo: String, circleName: String, division: String,
//        subDivision: String, feederName: String, dtrNameCode: String, dateTime: String,
//        poleNo: String, consumerName: String, consumerNo: String, consumerMobile: String,
//        consumerAddress: String, oldMeterNo: String, oldMeterPhase: String,
//        oldMeterReading: String, oldMeterReading2: String, oldMeterMD: String,
//        oldMeterManufacturer: String, oldBoxSeal: String, oldBodySeal: String,
//        oldTerminalSeal: String, oldMeterStatus: String
//    ) {
//        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas = page.canvas
//        val paint = Paint()
//
//        var yPosition = 60f
//
//        // Header
//        paint.textSize = 32f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.BLACK
//        canvas.drawText("MGVCL - Meter Replacement Note", 40f, yPosition, paint)
//        yPosition += 60f
//
//        // Book and MRN Info
//        paint.textSize = 22f
//        paint.typeface = Typeface.DEFAULT
//        canvas.drawText("Book No.: $bookNo", 40f, yPosition, paint)
//        canvas.drawText("MRN Slip No.: $mrnSlipNo", 800f, yPosition, paint)
//        yPosition += 50f
//
//        // Basic Information Section
//        paint.textSize = 26f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.rgb(76, 175, 80) // Green color
//        canvas.drawText("Basic Information", 40f, yPosition, paint)
//        yPosition += 50f
//
//        paint.textSize = 20f
//        paint.typeface = Typeface.DEFAULT
//        paint.color = Color.BLACK
//
//        val basicInfoData = listOf(
//            "Circle Name: $circleName" to "Division: $division",
//            "Sub Division: $subDivision" to "Feeder Name: $feederName",
//            "DTR Name/Code: $dtrNameCode" to "Date/Time: $dateTime",
//            "Pole No.: $poleNo" to "Consumer Name: $consumerName",
//            "Consumer No.: $consumerNo" to "Consumer Mobile: $consumerMobile"
//        )
//
//        basicInfoData.forEach { (left, right) ->
//            canvas.drawText(left, 40f, yPosition, paint)
//            canvas.drawText(right, 600f, yPosition, paint)
//            yPosition += 40f
//        }
//
//        canvas.drawText("Consumer Address: $consumerAddress", 40f, yPosition, paint)
//        yPosition += 60f
//
//        // OLD Meter Information Section
//        paint.textSize = 26f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.rgb(76, 175, 80)
//        canvas.drawText("OLD Meter Information", 40f, yPosition, paint)
//        yPosition += 50f
//
//        // Table headers
//        paint.textSize = 18f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.BLACK
//        canvas.drawText("S.No.", 40f, yPosition, paint)
//        canvas.drawText("Parameters", 120f, yPosition, paint)
//        canvas.drawText("Details", 600f, yPosition, paint)
//        yPosition += 40f
//
//        // Draw line under headers
//        paint.strokeWidth = 2f
//        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)
//
//        paint.textSize = 16f
//        paint.typeface = Typeface.DEFAULT
//
//        val oldMeterData = listOf(
//            "1." to "Old Meter No." to oldMeterNo,
//            "2." to "Old Meter Phase" to oldMeterPhase,
//            "3." to "Old Meter Reading (kWh)" to oldMeterReading,
//            "4." to "Meter Reading (kWh)" to oldMeterReading2,
//            "5." to "Meter MD (KW) Only LTMD" to oldMeterMD,
//            "6." to "Old Meter Manufacturer" to oldMeterManufacturer,
//            "7." to "Old Box Seal Available (Yes/No)" to oldBoxSeal,
//            "8." to "Old Meter Body Seal (Yes/No)" to oldBodySeal,
//            "9." to "Old Terminal Seal No. (Yes/No)" to oldTerminalSeal,
//            "10." to "Meter Status" to oldMeterStatus
//        )
//
//        oldMeterData.forEach { (serial, param, detail) ->
//            canvas.drawText(serial, 40f, yPosition, paint)
//            canvas.drawText(param, 120f, yPosition, paint)
//            canvas.drawText(detail, 600f, yPosition, paint)
//            yPosition += 35f
//        }
//
//        pdfDocument.finishPage(page)
//    }
//
//    private fun createPage2(
//        pdfDocument: PdfDocument,
//        newMeterNo: String, newMeterPhase: String, newMeterReading: String,
//        newMeterReading2: String, newMeterMD: String, newMeterManufacturer: String,
//        newBoxSeal: String, newBodySeal: String, newTerminalSeal: String,
//        newMeterStatus: String, mrnOldMeterNo: String, mrnSmartMeterNo: String,
//        mrnOldReading: String, mrnSmartReading: String, mrnOldManufacturer: String,
//        mrnSmartManufacturer: String
//    ) {
//        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 2).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas = page.canvas
//        val paint = Paint()
//
//        var yPosition = 60f
//
//        // NEW Meter Information Section
//        paint.textSize = 26f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.rgb(33, 150, 243) // Blue color
//        canvas.drawText("NEW Meter Information", 40f, yPosition, paint)
//        yPosition += 50f
//
//        // Table headers
//        paint.textSize = 18f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.BLACK
//        canvas.drawText("S.No.", 40f, yPosition, paint)
//        canvas.drawText("Parameters", 120f, yPosition, paint)
//        canvas.drawText("Details", 600f, yPosition, paint)
//        yPosition += 40f
//
//        paint.strokeWidth = 2f
//        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)
//
//        paint.textSize = 16f
//        paint.typeface = Typeface.DEFAULT
//
//        val newMeterData = listOf(
//            "1." to "New Meter No." to newMeterNo,
//            "2." to "New Meter Phase" to newMeterPhase,
//            "3." to "New Meter Reading (kWh)" to newMeterReading,
//            "4." to "Meter Reading (kWh)" to newMeterReading2,
//            "5." to "Meter MD (KW) Only LTMD" to newMeterMD,
//            "6." to "New Meter Manufacturer" to newMeterManufacturer,
//            "7." to "New Box Seal Available (Yes/No)" to newBoxSeal,
//            "8." to "New Meter Body Seal (Yes/No)" to newBodySeal,
//            "9." to "New Terminal Seal No. (Yes/No)" to newTerminalSeal,
//            "10." to "Meter Status" to newMeterStatus
//        )
//
//        newMeterData.forEach { (serial, param, detail) ->
//            canvas.drawText(serial, 40f, yPosition, paint)
//            canvas.drawText(param, 120f, yPosition, paint)
//            canvas.drawText(detail, 600f, yPosition, paint)
//            yPosition += 35f
//        }
//
//        yPosition += 40f
//
//        // MRN Information Section
//        paint.textSize = 26f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.rgb(255, 152, 0) // Orange color
//        canvas.drawText("MRN Information", 40f, yPosition, paint)
//        yPosition += 50f
//
//        // MRN Comparison Table
//        paint.textSize = 18f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.BLACK
//        canvas.drawText("S.No.", 40f, yPosition, paint)
//        canvas.drawText("Parameters", 120f, yPosition, paint)
//        canvas.drawText("Old Meter", 500f, yPosition, paint)
//        canvas.drawText("Smart Meter", 800f, yPosition, paint)
//        yPosition += 40f
//
//        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)
//
//        paint.textSize = 16f
//        paint.typeface = Typeface.DEFAULT
//
//        val mrnData = listOf(
//            "1." to "Meter No." to mrnOldMeterNo to mrnSmartMeterNo,
//            "2." to "Meter Phase" to "1" to "",
//            "3." to "Meter Reading (kWh)" to mrnOldReading to mrnSmartReading,
//            "4." to "Meter Reading (kWh)" to "" to "",
//            "5." to "Meter MD (KW) Only LTMD" to "" to "",
//            "6." to "Meter Manufacturer" to mrnOldManufacturer to mrnSmartManufacturer,
//            "7." to "Box Seal Available (Yes/No)" to "No" to "Yes",
//            "8." to "Meter Body Seal (Yes/No)" to "No" to "Yes",
//            "9." to "Terminal Seal No. (Yes/No)" to "No" to "Yes",
//            "10." to "Meter Status" to "Working" to "Installed"
//        )
//
//        mrnData.forEach { (serial, param, oldValue, smartValue) ->
//            canvas.drawText(serial, 40f, yPosition, paint)
//            canvas.drawText(param, 120f, yPosition, paint)
//            canvas.drawText(oldValue, 500f, yPosition, paint)
//            canvas.drawText(smartValue, 800f, yPosition, paint)
//            yPosition += 35f
//        }
//
//        pdfDocument.finishPage(page)
//    }
//
//    private fun createPage3(
//        pdfDocument: PdfDocument,
//        digitalOldMeterNo: String, digitalSmartMeterNo: String,
//        fieldOldMeterNo: String, fieldSmartMeterNo: String,
//        repName: String, consumerName: String
//    ) {
//        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 3).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas = page.canvas
//        val paint = Paint()
//
//        var yPosition = 60f
//
//        // Digital MRN vs Field MRN Comparison
//        paint.textSize = 26f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.rgb(156, 39, 176) // Purple color
//        canvas.drawText("Digital MRN vs Field MRN Comparison", 40f, yPosition, paint)
//        yPosition += 50f
//
//        // Comparison Table Headers
//        paint.textSize = 14f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        paint.color = Color.BLACK
//        canvas.drawText("S.No.", 40f, yPosition, paint)
//        canvas.drawText("Parameter", 100f, yPosition, paint)
//        canvas.drawText("Digital Old", 300f, yPosition, paint)
//        canvas.drawText("Digital Smart", 500f, yPosition, paint)
//        canvas.drawText("Field Old", 700f, yPosition, paint)
//        canvas.drawText("Field Smart", 900f, yPosition, paint)
//        yPosition += 40f
//
//        canvas.drawLine(40f, yPosition - 10f, 1160f, yPosition - 10f, paint)
//
//        paint.textSize = 12f
//        paint.typeface = Typeface.DEFAULT
//
//        val comparisonData = listOf(
//            "1." to "Meter No." to digitalOldMeterNo to digitalSmartMeterNo to fieldOldMeterNo to fieldSmartMeterNo,
//            "2." to "Meter Phase" to "1" to "1" to "1" to "1",
//            "3." to "Meter Reading" to "2649" to "0000010" to "169797" to "1.1",
//            "4." to "Meter Reading" to "" to "" to "" to "",
//            "5." to "Meter MD (KW)" to "" to "" to "" to "",
//            "6." to "Manufacturer" to "HPL" to "L&T" to "Genus" to "Capital",
//            "7." to "Box Seal" to "Yes" to "Yes" to "Yes" to "Yes",
//            "8." to "Body Seal" to "No" to "Yes" to "No" to "Yes",
//            "9." to "Terminal Seal" to "Yes" to "Yes" to "Yes" to "Yes",
//            "10." to "Status" to "Ok" to "Installed" to "Ok" to "Ok"
//        )
//
//        comparisonData.forEach { (serial, param, digitalOld, digitalSmart, fieldOld, fieldSmart) ->
//            canvas.drawText(serial, 40f, yPosition, paint)
//            canvas.drawText(param, 100f, yPosition, paint)
//            canvas.drawText(digitalOld, 300f, yPosition, paint)
//            canvas.drawText(digitalSmart, 500f, yPosition, paint)
//            canvas.drawText(fieldOld, 700f, yPosition, paint)
//            canvas.drawText(fieldSmart, 900f, yPosition, paint)
//            yPosition += 30f
//        }
//
//        yPosition += 60f
//
//        // Representative Information
//        paint.textSize = 20f
//        paint.typeface = Typeface.DEFAULT
//        canvas.drawText("Intellismart Representative Name: $repName", 40f, yPosition, paint)
//        yPosition += 40f
//        canvas.drawText("Helpline No.: 19124", 40f, yPosition, paint)
//        yPosition += 80f
//
//        // Signatures Section
//        paint.textSize = 24f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        canvas.drawText("Signatures", 40f, yPosition, paint)
//        yPosition += 60f
//
//        // Add signature images if available
//        if (!vm.sign1.value.isNullOrEmpty() || !vm.sign2.value.isNullOrEmpty()) {
//            try {
//                vm.sign1.value?.let { signaturePath ->
//                    val consumerBitmap = BitmapFactory.decodeFile(signaturePath)
//                    consumerBitmap?.let {
//                        val resized = Bitmap.createScaledBitmap(it, 300, 120, false)
//                        paint.textSize = 18f
//                        paint.typeface = Typeface.DEFAULT
//                        canvas.drawText("Consumer Signature:", 40f, yPosition, paint)
//                        canvas.drawBitmap(resized, 40f, yPosition + 20f, null)
//                    }
//                }
//
//                vm.sign2.value?.let { signaturePath ->
//                    val repBitmap = BitmapFactory.decodeFile(signaturePath)
//                    repBitmap?.let {
//                        val resized = Bitmap.createScaledBitmap(it, 300, 120, false)
//                        paint.textSize = 18f
//                        paint.typeface = Typeface.DEFAULT
//                        canvas.drawText("Representative Signature:", 600f, yPosition, paint)
//                        canvas.drawBitmap(resized, 600f, yPosition + 20f, null)
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("PDFGeneration", "Error adding signatures", e)
//            }
//        } else {
//            // Draw signature boxes
//            paint.style = Paint.Style.STROKE
//            paint.strokeWidth = 2f
//            canvas.drawRect(40f, yPosition + 20f, 340f, yPosition + 140f, paint)
//            canvas.drawRect(600f, yPosition + 20f, 900f, yPosition + 140f, paint)
//
//            paint.style = Paint.Style.FILL
//            paint.textSize = 18f
//            canvas.drawText("Consumer Signature:", 40f, yPosition, paint)
//            canvas.drawText("Representative Signature:", 600f, yPosition, paint)
//        }
//
//        pdfDocument.finishPage(page)
//    }
//
//    private fun renderPdfPreview() {
//        currentPdfFile?.let { file ->
//            try {
//                val bitmap = renderPdfToBitmap(file, 0)
//                binding.imageView2.setImageBitmap(bitmap)
//
//                // Setup page navigation if multiple pages
//                setupPageNavigation(file)
//
//            } catch (e: Exception) {
//                Log.e("PDFPreview", "Error rendering PDF preview", e)
//                Toast.makeText(mContext, "Error displaying PDF preview", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun setupPageNavigation(file: File) {
//        try {
//            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//            pdfRenderer = PdfRenderer(fileDescriptor)
//            totalPages = pdfRenderer!!.pageCount
//
//            if (totalPages > 1) {
//                binding.pageNavigation.visibility = View.VISIBLE
//                updatePageInfo()
//            }
//
//        } catch (e: Exception) {
//            Log.e("PDFNavigation", "Error setting up page navigation", e)
//        }
//    }
//
//    private fun renderCurrentPage() {
//        currentPdfFile?.let { file ->
//            val bitmap = renderPdfToBitmap(file, currentPage)
//            binding.imageView2.setImageBitmap(bitmap)
//            updatePageInfo()
//        }
//    }
//
//    private fun updatePageInfo() {
//        binding.tvPageInfo.text = "Page ${currentPage + 1} of $totalPages"
//        binding.btnPrevPage.isEnabled = currentPage > 0
//        binding.btnNextPage.isEnabled = currentPage < totalPages - 1
//    }
//
//    private fun renderPdfToBitmap(file: File, pageIndex: Int = 0): Bitmap? {
//        return try {
//            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//            val pdfRenderer = PdfRenderer(fileDescriptor)
//            val page = pdfRenderer.openPage(pageIndex)
//
//            val bitmap = createBitmap(page.width * 2, page.height * 2) // Higher resolution
//            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//
//            page.close()
//            pdfRenderer.close()
//            fileDescriptor.close()
//
//            bitmap
//        } catch (e: Exception) {
//            Log.e("PDFRender", "Error rendering PDF to bitmap", e)
//            null
//        }
//    }
//
//    private fun showActionButtons() {
//        binding.actionButtonsLayout.visibility = View.VISIBLE
//        binding.btnGeneratePdf.text = "Regenerate PDF"
//    }
//
//    private fun openPdfWithExternalApp(file: File) {
//        try {
//            val uri = FileProvider.getUriForFile(
//                mContext,
//                "${mContext.packageName}.provider",
//                file
//            )
//
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                setDataAndType(uri, "application/pdf")
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
//                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
//            }
//
//            pdfViewLauncher.launch(Intent.createChooser(intent, "Open PDF with"))
//
//        } catch (e: Exception) {
//            Toast.makeText(mContext, "No app found to open PDF", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun downloadPdf(file: File) {
//        // Implementation for downloading PDF to Downloads folder
//        Toast.makeText(mContext, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun sharePdf(file: File) {
//        try {
//            val uri = FileProvider.getUriForFile(
//                mContext,
//                "${mContext.packageName}.provider",
//                file
//            )
//
//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "application/pdf"
//                putExtra(Intent.EXTRA_STREAM, uri)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            }
//
//            startActivity(Intent.createChooser(intent, "Share PDF"))
//
//        } catch (e: Exception) {
//            Toast.makeText(mContext, "Error sharing PDF", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun getFormattedDateTime(): String {
//        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
//        return sdf.format(java.util.Date())
//    }
//
//    private fun getCurrentDateTime(): String {
//        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
//        return sdf.format(Date())
//    }
//
//    private var pdfViewLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        // Handle return from PDF viewer
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        pdfRenderer?.close()
//    }
//}
