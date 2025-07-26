package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.databinding.FragOutputBinding
import com.apc.smartinstallation.vm.MainViewModel
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OutputFrag: Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragOutputBinding
    private lateinit var navController: NavController
    private lateinit var url:String
    private val vm: MainViewModel by activityViewModels()





    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragOutputBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController= Navigation.findNavController(view)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        val formatted = current.format(formatter)
        val args=vm.res
        binding.readingTv.text="Meter Reading : ${args.value?.meter_reading} \n Meter Number: ${args.value?.meter_no}"
        val info = mContext.packageManager.getPackageInfo(
            mContext.packageName, 0
        )
        val versionCode = info.versionName
        binding.verTv.text="v$versionCode"

        val url=args.value?.image_path?.replace("/home/jbvnl/be/jbvnl_backend/src/ocr/images_power/","https://api.png-suvidha.in/api/images/")

        Glide.with(this).load(url).into(binding.imgIv)


        binding.numberTv.text = "ACC ID: ${args.value?.ca_no}"
        //    binding.numberTv.visibility=View.GONE
        binding.locTv.text="Lat.: ,Long.: "
        //  binding.locTv.visibility=View.VISIBLE
        binding.latLongTv.text="Date & Time : $formatted \nLocation: "
        binding.btnBack.setOnClickListener {

        }



        //  imgIv.setImageBitmap(bp)

        binding.rejBt.setOnClickListener {

        }
        binding.conBt.setOnClickListener {
                // Simulate processing and return result
          //      Handler(Looper.getMainLooper()).postDelayed({
            val res=Gson().toJson(vm.res.value)
            Log.d("SDK>>>",res.toString())
             //       OcrLauncher.sendResultBack(res)
                    requireActivity().finish()
                    // finish()
           //     }, 2000)



        }


    }


}