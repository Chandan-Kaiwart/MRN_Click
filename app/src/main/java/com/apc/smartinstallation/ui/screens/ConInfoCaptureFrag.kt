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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apc.smartinstallation.R
import com.apc.smartinstallation.databinding.FragActionBinding
import com.apc.smartinstallation.vm.MainViewModel
import com.apc.smartinstallation.dataClasses.ocr.response.OcrResult
import com.apc.smartinstallation.databinding.FragCiActionBinding
import com.bumptech.glide.Glide


class ConInfoCaptureFrag:Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
    private lateinit var binding: FragCiActionBinding
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
        binding=FragCiActionBinding.inflate(inflater,container,false)
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
        binding.latLongEt.setText(vm.lat.value.toString()+","+vm.long.value.toString())

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
                    //     binding.newMeterMakes.setText(ocrResult.ocr_meter_make)
                    //   binding.nMNoEt.setText(ocrResult.ocr_mno)

                }
            }

        }
        val subList=vm.ocrResults.value?.toList()?.subList(0,1)!!


        val adapter1 = OcrResultsAdapter(subList,vm,navController)
        binding.readingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.readingsRecyclerView.adapter = adapter1
        //    binding.newMeterMakes.visibility=View.GONE
        //    binding.nMNoEt.visibility=View.GONE

        binding.subBt.setOnClickListener {
            vm.ocrResults.value?.forEachIndexed { index, ocrResult ->
                if(index==0) {
                    vm.ocrResults.value?.get(index)?.ocr_mno = binding.oMNoEt.text.toString()
                    vm.ocrResults.value?.get(index)?.ocr_meter_make = binding.oldMeterMakes.text.toString()
                }
                else if(index==1){
                    //  vm.ocrResults.value?.get(index)?.ocr_mno = binding.nMNoEt.text.toString()
                    //  vm.ocrResults.value?.get(index)?.ocr_meter_make = binding.newMeterMakes.text.toString()

                }

            }

            navController.navigate(ConInfoCaptureFragDirections.actionConInfoCaptureFragToNewMeterCaptureFrag())


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
            holder.expText.text = if(item.ocr_exception_code==22) item.ocr_exception_msg + " ("+ item.ocr_unit + ")" else item.ocr_exception_msg
            holder.makeTv.text = item.ocr_meter_make
            holder.mnoText.text = if(item.ocr_mno.isEmpty()) "" else "# ${item.ocr_mno}"
            if(item.ocr_exception_code>=1){
                holder.readingLL.visibility=View.VISIBLE
                if(item.ocr_exception_code>1){
                    holder.expText.visibility=View.VISIBLE
                }
            }
            holder.manReadingEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Called *before* the text changes
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Called *while* the text is changing
                }

                override fun afterTextChanged(s: Editable?) {
                    // Called *after* the text has changed
                    Log.d("EditText", "User typed: ${s.toString()}")
                    vm.ocrResults.value?.get(holder.adapterPosition)?.manual_reading=s.toString()
                }
            })



            if(item.img_path.isNotEmpty()){
                val url= item.img_path.replace("/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/","http://195.35.20.141:3100/images/")
                Glide.with(holder.itemView.context).load(url).into(holder.readingImage)
            }
            holder.unitCard.setOnClickListener {
                Log.d("CLICK>>>","OLD")
                vm.curType.value=0
                vm.capStage.value=1

                navController.navigate(OldMeterCaptureFragDirections.actionOldMeterCaptureFragToCaptureFrag())            }

        }
    }


}