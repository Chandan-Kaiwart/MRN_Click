package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import com.apc.smartinstallation.databinding.FragPdfBinding
import com.apc.smartinstallation.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class MrnPdfFrag : Fragment() {
//    private lateinit var mContext: Context
//    private lateinit var navController: NavController
//    private lateinit var binding: FragPdfBinding
//
//    private val vm: MainViewModel by activityViewModels()
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        mContext = context
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragPdfBinding.inflate(inflater)
//        return binding.root
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        navController = Navigation.findNavController(view)
//
//        val ocrResults = vm.ocrResults.value
//        val consumer = vm.consumer.value
//
//        if (ocrResults == null || ocrResults.size < 2 || consumer == null) {
//            Toast.makeText(mContext, "Required data missing. Returning...", Toast.LENGTH_SHORT).show()
//            navController.navigateUp()
//            return
//        }
//        Log.d("PDFFrag", "ocrResults: $ocrResults, consumer: $consumer")
//
//        val ocrRes1 = ocrResults[0]
//        val ocrRes2 = ocrResults[1]
//
//        val pdfFile = createMeterReplacementPDF(
//            context = mContext,
//            bookNo = "09",
//            mrnSlipNo = "0426",
//            circleName = "Circle A",
//            division = "Div 1",
//            subDivision = "Sub 1",
//            feederNo = "F123",
//            dtrCode = "DTR456",
//            dateTime = "2025-06-02 10:30AM",
//            poleNo = "P789",
//            consumerName = consumer.name,
//            consumerNo = consumer.acct_id,
//            consumerMobile = consumer.mobile,
//            consumerAddress = consumer.address,
//            meterPhase = "1PH",
//            oldMeterNo = ocrRes1.ocr_mno,
//            oldMeterReading = ocrRes1.ocr_reading,
//            oldMeterReading2 = ocrRes1.ocr_reading,
//            oldMeterMD = "5.6",
//            oldMeterManufacturer = ocrRes1.ocr_meter_make,
//            oldBoxSeal = "Yes",
//            oldBodySeal = "Yes",
//            oldTerminalSeal = "No",
//            oldMeterStatus = "Working",
//            newMeterNo = ocrRes2.ocr_mno,
//            newMeterReading = ocrRes2.ocr_reading,
//            newMeterReading2 = ocrRes2.ocr_reading,
//            newMeterMD = "0.0",
//            newMeterManufacturer = ocrRes2.ocr_meter_make,
//            newBoxSeal = "Yes",
//            newBodySeal = "Yes",
//            newTerminalSeal = "Yes",
//            newMeterStatus = "Installed",
//            repName = "Technician A"
//        )
//
//        val bitmap = renderPdfToBitmap(pdfFile)
//        binding.imageView2.setImageBitmap(bitmap)
//
//        binding.upBt.setOnClickListener {
//            openPdfWithExternalApp(pdfFile)
//        }
//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            navController.navigate(MrnPdfFragDirections.actionMrnPdfFragToConListFrag())
//        }
//    }
//
//    private var pdfViewLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        // Handle return from PDF viewer
//        // No need to do anything special, fragment will remain as is
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
//                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)  // ⬅️ Add this
//                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK) // ⬅️ Optional, but helps
//            }
//
//            pdfViewLauncher.launch(Intent.createChooser(intent, "Open PDF with"))
//
//        } catch (e: Exception) {
//            Toast.makeText(mContext, "No app found to open PDF", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun createMeterReplacementPDF(
//        context: Context,
//        bookNo: String,
//        mrnSlipNo: String,
//        circleName: String,
//        division: String,
//        subDivision: String,
//        feederNo: String,
//        dtrCode: String,
//        dateTime: String,
//        poleNo: String,
//        consumerName: String,
//        consumerNo: String,
//        consumerMobile: String,
//        consumerAddress: String,
//        meterPhase: String,
//        oldMeterNo: String,
//        oldMeterReading: String,
//        oldMeterReading2: String,
//        oldMeterMD: String,
//        oldMeterManufacturer: String,
//        oldBoxSeal: String,
//        oldBodySeal: String,
//        oldTerminalSeal: String,
//        oldMeterStatus: String,
//        newMeterNo: String,
//        newMeterReading: String,
//        newMeterReading2: String,
//        newMeterMD: String,
//        newMeterManufacturer: String,
//        newBoxSeal: String,
//        newBodySeal: String,
//        newTerminalSeal: String,
//        newMeterStatus: String,
//        repName: String
//    ): File {
//        val pdfDocument = PdfDocument()
//        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1700, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas = page.canvas
//        val paint = Paint()
//
//        paint.textSize = 30f
//        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
//        canvas.drawText("MGVCL - Meter Replacement Note", 40f, 60f, paint)
//
//        paint.textSize = 24f
//        paint.typeface = Typeface.DEFAULT
//        canvas.drawText("Book No.: $bookNo", 40f, 110f, paint)
//        canvas.drawText("MRN Slip No.: $mrnSlipNo", 900f, 110f, paint)
//
//        canvas.drawText("Circle Name: $circleName", 40f, 160f, paint)
//        canvas.drawText("Division: $division", 450f, 160f, paint)
//        canvas.drawText("Sub Division: $subDivision", 800f, 160f, paint)
//
//        canvas.drawText("Feeder No.: $feederNo", 40f, 210f, paint)
//        canvas.drawText("DTR Name/Code: $dtrCode", 450f, 210f, paint)
//        canvas.drawText("Date Time: $dateTime", 800f, 210f, paint)
//
//        canvas.drawText("Pole No.: $poleNo", 40f, 260f, paint)
//        canvas.drawText("Consumer Name: $consumerName", 450f, 260f, paint)
//        canvas.drawText("Consumer No.: $consumerNo", 800f, 260f, paint)
//
//        canvas.drawText("Consumer Mobile No.: $consumerMobile", 40f, 310f, paint)
//        canvas.drawText("Consumer Address: $consumerAddress", 40f, 360f, paint)
//
//        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
//        canvas.drawText("Parameters", 40f, 420f, paint)
//        canvas.drawText("Old Meter Details", 500f, 420f, paint)
//        canvas.drawText("Smart Meter Details", 850f, 420f, paint)
//
//        val lines = listOf(
//            "1. Meter No." to Pair(oldMeterNo, newMeterNo),
//            "2. Meter Phase" to Pair(meterPhase, meterPhase),
//            "3. Meter Reading (kWh)" to Pair(oldMeterReading, newMeterReading),
//            "4. Meter Reading (kWh)" to Pair(oldMeterReading2, newMeterReading2),
//            "5. Meter MD (KW) Only LTMD" to Pair(oldMeterMD, newMeterMD),
//            "6. Meter Manufacturer" to Pair(oldMeterManufacturer, newMeterManufacturer),
//            "7. Box Seal Available (Yes/No)" to Pair(oldBoxSeal, newBoxSeal),
//            "8. Meter Body Seal (Yes/No)" to Pair(oldBodySeal, newBodySeal),
//            "9. Terminal Seal No. (Yes/No)" to Pair(oldTerminalSeal, newTerminalSeal),
//            "10. Meter Status" to Pair(oldMeterStatus, newMeterStatus)
//        )
//
//        paint.typeface = Typeface.DEFAULT
//        var y = 470f
//        lines.forEach { (label, values) ->
//            canvas.drawText(label, 40f, y, paint)
//            canvas.drawText(values.first, 500f, y, paint)
//            canvas.drawText(values.second, 850f, y, paint)
//            y += 50
//        }
//
//        canvas.drawText("Intellismart Representative Name: $repName", 40f, y + 50, paint)
//        canvas.drawText("Helpline No.: 19124", 40f, y + 100, paint)
//
//        if (!vm.sign1.value.isNullOrEmpty() || !vm.sign2.value.isNullOrEmpty()) {
//            val consumerBitmap = BitmapFactory.decodeFile(vm.sign1.value)
//            val repBitmap = BitmapFactory.decodeFile(vm.sign2.value)
//
//            consumerBitmap?.let {
//                val resized = Bitmap.createScaledBitmap(it, 250, 100, false)
//                canvas.drawText("Consumer Signature:", 40f, y + 200, paint)
//                canvas.drawBitmap(resized, 280f, y + 120, null)
//            }
//
//            repBitmap?.let {
//                val resized = Bitmap.createScaledBitmap(it, 250, 100, false)
//                canvas.drawText("Representative Signature:", 650f, y + 200, paint)
//                canvas.drawBitmap(resized, 1000f, y + 120, null)
//            }
//        }
//
//        pdfDocument.finishPage(page)
//
//        val file = File(context.filesDir, "meter_replacement_note.pdf")
//        FileOutputStream(file).use {
//            pdfDocument.writeTo(it)
//        }
//        pdfDocument.close()
//        return file
//    }
//
//    fun renderPdfToBitmap(file: File): Bitmap? {
//        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//        val pdfRenderer = PdfRenderer(fileDescriptor)
//        val page = pdfRenderer.openPage(0)
//
//        val bitmap = createBitmap(page.width, page.height)
//        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//
//        page.close()
//        pdfRenderer.close()
//        fileDescriptor.close()
//
//        return bitmap
//    }
}