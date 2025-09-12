package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apc.smartinstallation.R
import com.apc.smartinstallation.api.MeterReadingRequest
import com.apc.smartinstallation.dataClasses.CiMeterRequest
import com.apc.smartinstallation.databinding.FragCiActionBinding
import com.apc.smartinstallation.vm.MainViewModel
import com.apc.smartinstallation.dataClasses.ocr.response.OcrResult
import com.apc.smartinstallation.dataClasses.retrofit.RetrofitClient
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OldMeterCaptureFrag: Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
    private lateinit var binding: FragCiActionBinding
    private val vm: MainViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragCiActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val info = mContext.packageManager.getPackageInfo(mContext.packageName, 0)
        val versionCode = info.versionName
        binding.verTv.text = "v$versionCode"
        binding.latLongEt.setText("${vm.lat.value},${vm.long.value}")

        // Initialize OCR results if empty
        if (vm.ocrResults.value?.isEmpty() == true) {
            val ocrResult1 = OcrResult("","", "", "", 1, "", "", "", "","","",0, "Old Meter (KWH)")
            val ocrResult2 = OcrResult("","", "", "", 1, "", "", "", "","","",0, "New Meter (KWH)")
            vm.ocrResults.value?.add(ocrResult1)
            vm.ocrResults.value?.add(ocrResult2)
        } else {
            // Populate UI with existing data
            vm.ocrResults.value?.forEachIndexed { index, ocrResult ->
                if (index == 0) {
                    binding.oldMeterMakes.setText(ocrResult.ocr_meter_make)
                    binding.oMNoEt.setText(ocrResult.ocr_mno)
                }
            }
        }

        // Sync old meter number to ViewModel
        binding.oMNoEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                vm.ocrResults.value?.get(0)?.ocr_mno = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

// Sync old meter make to ViewModel
        binding.oldMeterMakes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                vm.ocrResults.value?.get(0)?.ocr_meter_make = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // Set up RecyclerView
        val subList = vm.ocrResults.value?.toList()?.subList(0,1) ?: emptyList()
        val adapter1 = OcrResultsAdapter(subList, vm, navController)
        binding.readingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.readingsRecyclerView.adapter = adapter1

        // Submit button click handler
        binding.subBt.setOnClickListener {
            // Update ViewModel with old meter data
            vm.ocrResults.value?.get(0)?.apply {
                ocr_mno = binding.oMNoEt.text.toString()
                ocr_meter_make = binding.oldMeterMakes.text.toString()
                manual_reading = manual_reading ?: ocr_reading
            }
            validateAndSubmitData()
        }
    }

    private fun validateAndSubmitData() {
        // Get the old meter data
        val oldMeter = vm.ocrResults.value?.get(0)?.apply {
            if (manual_reading.isNullOrBlank()) {
                manual_reading = ocr_reading
            }
        }

        oldMeter?.ocr_mno = binding.oMNoEt.text.toString()
        oldMeter?.ocr_meter_make = binding.oldMeterMakes.text.toString()

        // Validate required fields
        if (binding.oMNoEt.text.toString().isBlank()) {
            Toast.makeText(mContext, "Please enter meter number", Toast.LENGTH_SHORT).show()
            return
        }

        if (oldMeter?.ocr_reading.isNullOrBlank() && oldMeter?.manual_reading.isNullOrBlank()) {
            Toast.makeText(mContext, "Please capture or enter meter reading", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare request data for CI portal
        val request = CiMeterRequest(
            acct_id =  "123123123",
            organization = "APDCL",
            old_meter_image = oldMeter?.img_path ?: "",
            old_meter_number = binding.oMNoEt.text.toString(),
            old_meter_model = binding.oldMeterMakes.text.toString().ifBlank { "UNKNOWN" },
            old_meter_reading = oldMeter?.manual_reading?.ifBlank { oldMeter.ocr_reading } ?: "0",
            old_meter_parameter = "kWh", // Default parameter as shown in curl example
            old_meter_phase = "3" // Convert to String as required by CI portal
        )

        // Show loading indicator
        binding.subBt.isEnabled = false
        binding.subBt.text = "Submitting..."

        // Submit data to CI portal
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.saveToCiPortal(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(mContext, "Data saved to CI portal successfully", Toast.LENGTH_SHORT).show()
                        // Navigate to next screen
                        navController.navigate(R.id.action_oldMeterCaptureFrag_to_consumerDetailsFrag)
                    } else {  navController.navigate(R.id.action_oldMeterCaptureFrag_to_consumerDetailsFrag)
                        Toast.makeText(
                            mContext,
                            "CI portal submission successful: ${body?.message ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        mContext,
                        "CI portal error: ${response.code()} - ${errorBody ?: "No details"}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("CI_PORTAL_ERROR", "Error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                Toast.makeText(
                    mContext,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("SubmitError", "Exception: ${e.message}", e)
            } finally {
                // Reset button state
                withContext(Dispatchers.Main) {
                    binding.subBt.isEnabled = true
                    binding.subBt.text = "Submit"
                }
            }
        }
    }

    class OcrResultsAdapter(
        private val readings: List<OcrResult>,
        private val vm: MainViewModel,
        private val navController: NavController
    ) : RecyclerView.Adapter<OcrResultsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val unitText: TextView = view.findViewById(R.id.unit)
            val valueText: TextView = view.findViewById(R.id.unitValueTv)
            val mnoText: TextView = view.findViewById(R.id.mnTv)
            val makeTv: TextView = view.findViewById(R.id.meterMakeTv)
            val readingLL: LinearLayout = view.findViewById(R.id.ocrReadingLL)
            val manReadingEt: EditText = view.findViewById(R.id.manValueEt)
            val expText: TextView = view.findViewById(R.id.unitExpTv)
            val readingImage: ImageView = view.findViewById(R.id.unitIv)
            val unitCard: CardView = view.findViewById(R.id.unitCard)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ocr_result_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = readings.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = readings[position]
            holder.unitText.text = item.register
            holder.valueText.text = item.ocr_reading
            holder.expText.text = if (item.ocr_exception_code == 22)
                "${item.ocr_exception_msg} (${item.ocr_unit})" else item.ocr_exception_msg
            holder.makeTv.text = item.ocr_meter_make
            holder.mnoText.text = if (item.ocr_mno.isEmpty()) "" else "# ${item.ocr_mno}"

            if (item.ocr_exception_code >= 1) {
                holder.readingLL.visibility = View.VISIBLE
                if (item.ocr_exception_code > 1) {
                    holder.expText.visibility = View.VISIBLE
                }
            }

            // Set manual reading if exists
            item.manual_reading?.takeIf { it.isNotBlank() }?.let {
                holder.manReadingEt.setText(it)
            }

            holder.manReadingEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    vm.ocrResults.value?.get(holder.adapterPosition)?.manual_reading = s.toString()
                }
            })

            if (item.img_path.isNotEmpty()) {
                val url = item.img_path.replace(
                    "/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/",
                    "http://195.35.20.141:3100/images/"
                )
                Glide.with(holder.itemView.context).load(url).into(holder.readingImage)
            }

            holder.unitCard.setOnClickListener {
                // Set values for OLD meter capture
                vm.curType.value = 0  // 0 for old meter
                vm.capStage.value = 1 // 1 for old meter stage

                // Navigate to camera fragment
                try {
                    navController.navigate(R.id.action_oldMeterCaptureFrag_to_captureFrag)
                } catch (e: Exception) {
                    Log.e("Navigation", "Camera navigation failed: ${e.message}")
                    Toast.makeText(holder.itemView.context, "Failed to open camera", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}