package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.dataClasses.Consumer
import com.apc.smartinstallation.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConsumerDetailsFrag : Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
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
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ConsumerDetails(vm.consumer.value)!!
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ConsumerDetails(consumer: Consumer) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier.border(2.dp, Color.Black),
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = Color(0xFF3A7BD5),
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    title = {
                        Text(
                            "Consumer Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                )
            },
            bottomBar = {
                BottomAppBar {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { navController.navigate(ConsumerDetailsFragDirections.actionConsumerDetailsFragToOldMeterCaptureFrag()) },
                            modifier = Modifier
                                .padding(2.dp)
                                .weight(1f)
                        ) {
                            Text("Consumer\nInformation")
                        }

                        Button(
                            onClick = { navController.navigate(ConsumerDetailsFragDirections.actionConsumerDetailsFragToConInfoCaptureFrag()) },
                            modifier = Modifier
                                .padding(2.dp)
                                .weight(1f)
                        ) {
                            Text("Meter\nInstallation")
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF3A7BD5),

                                Color(0xFFB9B9B9),

                                //         Color(0xFFFF9800),
                                //  Color(0xFFFFFFFF),
                                Color(0xFFFFFFFF)

                            ), // Gradient colors
                            startY = 0.0f,
                            //         endY = 1000.0f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between items
                ) {
                    // Reusable composable for displaying a heading-content pair
                    @Composable
                    fun InfoItem(heading: String, content: String) {
                        Column(
                            modifier = Modifier
                                .padding(4.dp)

                                // .fillMaxWidth()
                                .weight(1f)
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = heading,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = content,

                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    fontSize = 16.sp

                                )
                            )
                        }
                    }

                    // Using the InfoItem composable for each field
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()

                    ){
                        InfoItem("Name:", consumer.name)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        InfoItem("Account ID:", consumer.acct_id.toString())
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        InfoItem("Address:", consumer.address)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        InfoItem("Mobile No. :", consumer.mobile)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        InfoItem("Circle:", "")
                        InfoItem("Division Code:", "")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        InfoItem("Sub-Division Code:", "")
                        InfoItem("Sub-Station Code:", "")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        InfoItem("Status:", consumer.status)
                        InfoItem("Location:", consumer.landmark)
                    }

                    Column(

                        modifier = Modifier
                            .fillMaxWidth()
                            //     .padding(8.dp)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        )
                        Row {
                            ClickableText(
                                text = AnnotatedString("Click for location"),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline,

                                    ),
                                onClick = {
                                    val intent =
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://www.google.com/maps?q=${consumer.latitude},${consumer.longitude}")
                                        )
                                    mContext.startActivity(intent)
                                }

                            )
                            Image(
                                painter = painterResource(id = android.R.drawable.ic_menu_mylocation), // Replace with your drawable resource
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .width(16.dp)
                                    .padding(8.dp) // Adjust size as needed
                            )

                        }
                    }


                    Spacer(modifier = Modifier.weight(1f)) // Push the button to the bottom


                }
            }

        }
    }
}