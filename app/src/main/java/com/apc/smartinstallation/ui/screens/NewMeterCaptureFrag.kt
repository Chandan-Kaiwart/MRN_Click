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
import androidx.datastore.core.IOException
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apc.smartinstallation.R
import com.apc.smartinstallation.databinding.FragActionBinding
import com.apc.smartinstallation.vm.MainViewModel
import com.apc.smartinstallation.dataClasses.ocr.response.OcrResult
import com.apc.smartinstallation.dataClasses.retrofit.RetrofitClient
import com.bumptech.glide.Glide
import  com.apc.smartinstallation.dataClasses.MiMeterRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewMeterCaptureFrag:Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
    private lateinit var binding: FragActionBinding
    private val vm:MainViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragActionBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController=Navigation.findNavController(view)
        val info = mContext.packageManager.getPackageInfo(
            mContext.packageName, 0
        )
        val versionCode = info.versionName
        binding.verTv.setText("v$versionCode")

        if(vm.ocrResults.value?.size==0){
            val ocrResult1 = OcrResult("","", "", "",  1, "", "", "",  "","","",0, "Old Meter (KWH)")
            val ocrResult2 = OcrResult("","", "", "", 1, "", "", "",  "","","",0, "New Meter (KWH)")
            vm.ocrResults.value?.add(ocrResult1)
            vm.ocrResults.value?.add(ocrResult2)
        }
        else{
            vm.ocrResults.value?.forEachIndexed { index, ocrResult ->
                if(index==0){
                    binding.oldMeterMakes.setText(ocrResult.ocr_meter_make)
                    binding.oMNoEt.setText(ocrResult.ocr_mno)
                }
                if(index==1){
                    binding.newMeterMakes.setText(ocrResult.ocr_meter_make)
                    binding.nMNoEt.setText(ocrResult.ocr_mno)
                }
            }
        }

        val subList=vm.ocrResults.value?.toList()?.subList(1,2)!!

        val adapter1 = OcrResultsAdapter(
            subList,
            vm,
            navController
        )
        binding.readingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.readingsRecyclerView.adapter = adapter1

        binding.oldMeterMakes.visibility=View.GONE
        binding.oMNoEt.visibility=View.GONE

        binding.subBt.setOnClickListener {
            vm.ocrResults.value?.forEachIndexed { index, ocrResult ->
                if(index==0) {
                    vm.ocrResults.value?.get(index)?.ocr_mno = binding.oMNoEt.text.toString()
                    vm.ocrResults.value?.get(index)?.ocr_meter_make = binding.oldMeterMakes.text.toString()
                }
                else if(index==1){
                    vm.ocrResults.value?.get(index)?.ocr_mno = binding.nMNoEt.text.toString()
                    vm.ocrResults.value?.get(index)?.ocr_meter_make = binding.newMeterMakes.text.toString()
                }
            }
            validateAndSubmitData()
            navController.navigate(R.id.action_newMeterCaptureFrag_to_mrnFrag)
        }
    }

    private fun validateAndSubmitData() {
        val meterNumber = binding.nMNoEt.text.toString()
        if (meterNumber.isBlank()) {
            Toast.makeText(mContext, "Meter number required", Toast.LENGTH_SHORT).show()
            return
        }

        // Additional validation
        val oldMeterNumber = binding.oMNoEt.text.toString()
        val oldMeterMake = binding.oldMeterMakes.text.toString()
        val newMeterMake = binding.newMeterMakes.text.toString()

        if (oldMeterNumber.isBlank()) {
            Toast.makeText(mContext, "Old meter number required", Toast.LENGTH_SHORT).show()
            return
        }

        if (newMeterMake.isBlank()) {
            Toast.makeText(mContext, "New meter make required", Toast.LENGTH_SHORT).show()
            return
        }

        val oldMeter = vm.ocrResults.value?.getOrNull(0)
        val newMeter = vm.ocrResults.value?.getOrNull(1)

        if (oldMeter == null || newMeter == null) {
            Toast.makeText(mContext, "Meter data not available", Toast.LENGTH_SHORT).show()
            return
        }

        binding.subBt.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.saveToMiPortal(
                        acctId = "123123123", // Use correct parameter name
                        organization = "APDCL",
                        oldMeterImage = oldMeter.img_path,
                        oldMeterModel = oldMeterMake,
                        oldMeterNumber = oldMeterNumber,
                        oldMeterReading = oldMeter.manual_reading.takeIf { it.isNotBlank() }
                            ?: oldMeter.ocr_reading.takeIf { it.isNotBlank() } ?: "0",
                        oldMeterPhase = "3",
                        newMeterImage = newMeter.img_path,
                        newMeterModel = newMeterMake,
                        newMeterNumber = meterNumber,
                        newMeterReading = newMeter.manual_reading.takeIf { it.isNotBlank() }
                            ?: newMeter.ocr_reading.takeIf { it.isNotBlank() } ?: "0",
                        newMeterPhase = "3",
                        newMeterBoxSeal = "CES12BE9398",
                        newMeterSeal = "CES12BE9398",
                        mrnPdf = "CES12BE9398"
                    )
                }

                // Handle response on Main thread
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            if (body.success) {
                                navController.navigate(
                                    NewMeterCaptureFragDirections.actionNewMeterCaptureFragToMrnFrag()
                                )
                            } else {
                                Toast.makeText(
                                    mContext,
                                    body.message ?: "Action failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } ?: run {
                            Toast.makeText(mContext, "Empty response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("SERVER_ERROR", "Code ${response.code()}: $error")
                        Toast.makeText(mContext, "Server error occurred", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: java.io.IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(mContext, "Network unavailable", Toast.LENGTH_SHORT).show()
                }
            } catch (e: retrofit2.HttpException) {
                withContext(Dispatchers.Main) {
                    Log.e("HTTP_ERROR", "HTTP error: ${e.code()}", e)
                    Toast.makeText(mContext, "Network error: ${e.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: com.google.gson.JsonParseException) {
                withContext(Dispatchers.Main) {
                    Log.e("JSON_ERROR", "JSON parsing failed", e)
                    Toast.makeText(mContext, "Data format error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MI_PORTAL_ERROR", "Failed to submit to MI portal", e)
                    Toast.makeText(mContext, "Failed to submit data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.subBt.isEnabled = true
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
            holder.expText.text = if(item.ocr_exception_code==22)
                "${item.ocr_exception_msg} (${item.ocr_unit})" else item.ocr_exception_msg
            holder.makeTv.text = item.ocr_meter_make
            holder.mnoText.text = if(item.ocr_mno.isEmpty()) "" else "# ${item.ocr_mno}"

            if(item.ocr_exception_code>=1){
                holder.readingLL.visibility=View.VISIBLE
                if(item.ocr_exception_code>1){
                    holder.expText.visibility=View.VISIBLE
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

            if(item.img_path.isNotEmpty()){
                val url = item.img_path.replace(
                    "/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/",
                    "http://195.35.20.141:3100/images/"
                )
                Glide.with(holder.itemView.context).load(url).into(holder.readingImage)
            }

            holder.unitCard.setOnClickListener {
                vm.curType.value = 1  // 1 for new meter
                vm.capStage.value = 2 // 2 for new meter stage
                Log.d("CLICK>>>","NEW")

                try {
                    navController.navigate(R.id.action_newMeterCaptureFrag_to_captureFrag)
                } catch (e: Exception) {
                    Log.e("Navigation", "Camera navigation failed: ${e.message}")
                    Toast.makeText(holder.itemView.context, "Failed to open camera", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}