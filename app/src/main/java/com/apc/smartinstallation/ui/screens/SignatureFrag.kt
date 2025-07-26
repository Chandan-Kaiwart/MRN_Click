package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.databinding.FragSignaturePadBinding
import com.apc.smartinstallation.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class SignatureFrag : Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
    private val vm: MainViewModel by activityViewModels()
    private lateinit var binding: FragSignaturePadBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragSignaturePadBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val signaturePad1 = binding.signaturePad1
        val signaturePad2 = binding.signaturePad2

        binding.submitButton.setOnClickListener{
            if (!signaturePad1.isEmpty && !signaturePad2.isEmpty) {
                val file1 = bitmapToFile(mContext,signaturePad1.signatureBitmap)
                val file2 = bitmapToFile(mContext,signaturePad2.signatureBitmap)
                vm.sign1.value = file1.absolutePath
                vm.sign2.value = file2.absolutePath

                Toast.makeText(mContext, "Signatures saved!", Toast.LENGTH_SHORT).show()
                navController.navigate(SignatureFragDirections.actionSignatureFragToMrnPdfFragmentNew())
            } else {
                Toast.makeText(mContext, "Both signatures are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun bitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "sign_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }
    }


    @Composable
    fun TestUi() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Text(
                text = "Hello Android",
                modifier = Modifier.padding(innerPadding)
            )
        }

    }




