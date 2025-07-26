package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.R
import com.apc.smartinstallation.dataClasses.Consumer
import com.apc.smartinstallation.dataClasses.UploadMeterReadingImageReq
import com.apc.smartinstallation.dataClasses.User
import com.apc.smartinstallation.dataClasses.login.LoginRes
import com.apc.smartinstallation.vm.MainViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.LocalDate
import androidx.core.net.toUri

@AndroidEntryPoint
class ConListFrag : Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController
    private val vm: MainViewModel by activityViewModels()
    private lateinit var userObj:LoginRes

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ConsumerList()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        userObj= LoginRes(true,"", listOf(""),"","")

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
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    fun ConsumerList() {
        val user by vm.userState.collectAsState()
        val consListState by vm.consListState.collectAsState()
        val geocodeState by vm.geocodeState.collectAsState()
        val getState by vm.meterReadingListState.collectAsState()
        val uploadState by vm.uploadMeterReadingImageState.collectAsState()

        var savedList= arrayListOf<UploadMeterReadingImageReq>()
        var totalSize by remember { mutableIntStateOf(0) }
        val categories = listOf("All", "CI", "MI")


        // Handle Fetch State
        when (getState) {
            is MainViewModel.MeterReadingEvent.Loading -> CircularProgressIndicator()
            is MainViewModel.MeterReadingEvent.GetSuccess -> {
                savedList = (getState as MainViewModel.MeterReadingEvent.GetSuccess).readings as ArrayList<UploadMeterReadingImageReq>

            }

            is MainViewModel.MeterReadingEvent.Failure -> {
                Log.d("READING>>", (getState as MainViewModel.MeterReadingEvent.Failure).errorText)

                Text(
                    "Error: ${(getState as MainViewModel.MeterReadingEvent.Failure).errorText}",
                    color = Color.Red
                )
            }

            else -> {}
        }

        when (uploadState) {
            is MainViewModel.UploadMeterReadingImageEvent.Success -> {



            }

            is MainViewModel.UploadMeterReadingImageEvent.Loading -> {
                //     pd.show(childFragmentManager,"show")

                Dialog(
                    onDismissRequest = {
                        savedList.size<=0
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(White, shape = RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)  // Increased padding to prevent overflow
                        ) {
                            CircularProgressIndicator(
                                progress = { ((totalSize-savedList.size).toFloat() / totalSize.toFloat()) },
                                modifier = Modifier
                                    .size(40.dp)  // Fixed size to ensure it stays within the box
                                    .weight(1f)
                                    .padding(4.dp),
                            )

                        }
                    }
                }


            }

            is MainViewModel.UploadMeterReadingImageEvent.Failure -> {

                Toast.makeText(mContext, "Upload Error", Toast.LENGTH_SHORT).show()

            }

            MainViewModel.UploadMeterReadingImageEvent.Empty -> {

            }
        }


        when (geocodeState) {
            is MainViewModel.GeocodeEvent.Success -> {
                Log.d("Geocode>>", "Success")
                val geocode =
                    (geocodeState as MainViewModel.GeocodeEvent.Success).resultText
                val result = geocode?.results?.get(0)
                val conLat = result?.geometry?.location?.lat
                val conLong = result?.geometry?.location?.lng

                //   val uri = "https://www.google.com/maps/dir/?api=1&origin=$lat,$long&destination=$conLat,$conLong"
                val uri =
                    "https://www.google.com/maps/dir/?api=1&origin=current location&destination=$conLat,$conLong"

                val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
                startActivity(intent)


            }

            is MainViewModel.GeocodeEvent.Loading -> {



            }

            is MainViewModel.GeocodeEvent.Failure -> {

                Toast.makeText(mContext, "Geocode Error", Toast.LENGTH_SHORT).show()

            }

            MainViewModel.GeocodeEvent.Empty -> {

            }
        }

        var wps = "optimize:true|"


        when (user) {
            "-1" -> {
                // Show loading spinner while user state is being loaded
                CircularProgressIndicator()
            }

            else -> {
                if (user == null) {
                    Log.d("USER>>", "NULL")
                    // User is not logged in, navigate to LoginScreen

                    try {
                        navController.navigate(ConListFragDirections.actionConListFragToLoginFrag())
                    } catch (e: Exception) {
                        //    vm2.clearUser()
                    }

                } else {
                    Log.d("USER>>", user.toString())

                    userObj = Gson().fromJson(user, LoginRes::class.java)
                    vm.getAssignedConsumers(userObj.username)

                    //vm.getAssignedConsumers(userObj.Username, "CALL")

                }
            }
        }

        var selectedCategory by remember { mutableStateOf("All") }

        // Combine the two StateFlows



        Scaffold(
            topBar = {
                TopAppBar(
                    actions = {
                        if (user != null && user != "-1") {


                            IconButton(onClick = {
                                vm.reInitAssignedConsList()
                                vm.getAssignedConsumers(userObj.username)
                                //    vm1.clearUser()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = {

                            vm.clearUser()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }

                    },


                    modifier = Modifier.border(2.dp, Color.Black),
                    //BorderStroke(2.dp, Color.Black),
                    title = {
                        Column {
                            //   if (user != null && user != "-1")

                            Text(

                                fontSize = 18.sp,
                                text = "${userObj.username}\n(Meter Reader)",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)


                            )


                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = Color(0xFF3A7BD5),
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

        ) { innerPadding ->
            Column {

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



                    LazyColumn(
                        modifier = Modifier
                           .fillMaxSize(),

                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp),
                    ) {


                        when {
                            consListState is MainViewModel.GetAssignedConsumersEvent.Success -> {
                                //       vm.reInitServerCallLogs()
                                //     vm.reInitAssignedConsList()
                                val consumers = (consListState as MainViewModel.GetAssignedConsumersEvent.Success).resultText!!.data




                                // Join lists by the common property "MOBILE_NO"


                                if (consumers.isNotEmpty()) {
                                    /*
                                                                        val locs = consumers.map {
                                                                            if (it.LATITUDE != null) "${it.LATITUDE},${it.LONGTIUDE}" else it.address
                                                                        }*/

                                    val locs = consumers.map {
                                        it.address
                                    }
                                    val m=if(locs.size>24)24 else locs.size-1
                                    locs.slice(0..m).forEach {
                                        if (!it.isNullOrEmpty())
                                            wps += "$it|"
                                    }
                                    // Map to categorize items
                                 /*   val categorizedItems = mapOf(
                                        "All" to consumers,
                                        "Unbilled" to consumers,

                                        //      "Not Visited" to consumers.filter { it.callingStatus == 0 },
                                        "AI Billed" to consumers.filter { it.status == 2 },
                                        "Manual Billed" to consumers.filter { it.status == 1 },
                                        //    "Category4" to consumers.filter { it.callingStatus == "Category4" }
                                    )*/
                                    item {
                                        Column {
                                            LazyRow(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            ) {
                                                items(categories.size) { category ->
                                                    FilterChip(
                                                        category = categories[category],
                                                        isSelected = selectedCategory == categories[category],
                                                        onClick = { selectedCategory = categories[category] }
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                            }
                                            // ChipGroup equivalent
                                      /*      FlowRow(
                                                modifier = Modifier
                                                    .fillMaxWidth()

                                                    .padding(1.dp),
                                                //      maxItemsInEachRow = 2,
                                                //  horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                categorizedItems.keys.forEach { category ->
                                                    FilterChip(
                                                        selected = selectedFilter == category,
                                                        onClick = { selectedFilter = category },
                                                        label = {
                                                            Box( // Add a Box to handle alignment properly
                                                                modifier = Modifier.fillMaxWidth(), // Ensure full width for centering
                                                                contentAlignment = Alignment.Center // Center content within the Box
                                                            ) {
                                                                Text(
                                                                    text = "$category (${categorizedItems[category]!!.size})",
                                                                    fontSize = 14.sp,
                                                                    textAlign = TextAlign.Center, // Center text horizontally
                                                                    style = TextStyle(
                                                                        color = Color.Black,
                                                                        fontWeight = FontWeight.SemiBold
                                                                    )
                                                                )
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .padding(1.dp)
                                                            .weight(1f)
                                                        //   .fillMaxHeight()
                                                    )

                                                }
                                            }*/
                                        }
                                    }

                                    items(consumers.size) { index ->
                                        //    val con=consumers[index]

                                        ConsumerItem(
                                            consumer = consumers[index],
                                            index
                                        )
                                    }
                                    //  vm.reInitAssignedConsList()
                                }
                            }


                            consListState is MainViewModel.GetAssignedConsumersEvent.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            consListState is MainViewModel.GetAssignedConsumersEvent.Failure -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Failed to load consumers",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }

                            consListState is MainViewModel.GetAssignedConsumersEvent.Empty -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No consumers available",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ConsumerItem(consumer: Consumer, idx: Int) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Card content
            Card(
                onClick = {
                    vm.consumer.value = consumer

                    //     navController.navigate(R.id.action_homeFrag_to_consumerDetailFrag,bundle)
                    try {
                        navController.navigate(
                            ConListFragDirections.actionConListFragToConsumerDetailsFrag(
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    //      vm1.consumer.value=consumer
                    //     vm1.inspectionData.value?.conId = consumer.id
                    //    navController.navigate(R.id.action_customerListFrag_to_actionListFrag)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                border = BorderStroke(2.dp, Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {

                    Text(
                        text = "${idx + 1}). ${consumer.name.toString()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Start,

                        )
                    Text(
                        text = "ACC. ID: ${consumer.acct_id}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Center,

                        )
                    Text(
                        text = "Address: ${consumer.address}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(4.dp),
                        textAlign = TextAlign.Left,

                        )
                }
            }

            /*
                        if (consumer.LATITUDE != null && consumer.LONGTIUDE != null && (consumer.LATITUDE!! > 0 && consumer.LONGTIUDE!! > 0) || consumer.ADDRESS?.isNotEmpty() == true)
                            Card(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(8.dp)// Align it to the center-right
                                    .offset(x = 24.dp, y = 0.dp)
                                    .clip(RoundedCornerShape(8.dp)) // Circular image shape
                                    .border(
                                        2.dp,
                                        Color.Black,
                                        RoundedCornerShape(8.dp)
                                    ) // Border around the image
                                    .shadow(2.dp, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (consumer.LATITUDE != null && consumer.LONGTIUDE != null && consumer.LATITUDE!! > 0 && consumer.LONGTIUDE!! > 0) {
                                            //         val uri = "https://www.google.com/maps/dir/?api=1&origin=$lat,$long&destination=${consumer.LATITUDE},${consumer.LONGTIUDE}"
                                            val uri =
                                                "https://www.google.com/maps/dir/?api=1&origin=current location&destination=${consumer.LATITUDE},${consumer.LONGTIUDE}"

                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                            startActivity(intent)
                                        } else
                                            vm1.geocode(
                                                "AIzaSyDtj4Bwn_vqj0Dq7B--q51phjr39jYYAKA",
                                                consumer.ADDRESS!!
                                            )

                                        //    uri = "https://www.google.com/maps/dir/?api=1&origin=$lat,$long&destination=${Uri.encode(consumer.ADDRESS)}"

                                        *//* val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                             intent.setPackage("com.google.android.apps.maps")
                             if (intent.resolveActivity(mContext.packageManager) != null) {
                                 startActivity(intent)
                             } else {
                                 Toast.makeText(mContext, "Google Maps app is not installed.", Toast.LENGTH_SHORT).show()
                             }*//*


                        },// Offset to float half outside the main card
                    // Optional border
                    shape = RoundedCornerShape(8.dp), // Circular shape to mimic a FAB
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Elevation to float it above the card
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Share",
                        tint = Color.Unspecified,// Prevent automatic tinting

                        modifier = Modifier
                            .size(40.dp) // Icon size
                        // Padding inside the card
                    )
                }*/

        }
    }
    @Composable
    fun FilterChip(category: String, isSelected: Boolean, onClick: () -> Unit) {
        Surface(
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .clickable { onClick() }
                .shadow(2.dp, RoundedCornerShape(20.dp))
        ) {
            Text(
                text = category,
                color = if (isSelected) Color.White else Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}