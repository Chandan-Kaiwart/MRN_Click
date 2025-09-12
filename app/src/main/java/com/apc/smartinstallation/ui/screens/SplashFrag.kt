package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SplashFrag: Fragment() {
    private lateinit var mContext: Context
    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    SplashScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        //   Toast.makeText(mContext,"here",Toast.LENGTH_LONG).show()
        //  navController.navigate(R.id.action_landingFrag_to_loginFrag)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TestUi() {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text ("Smart Installation") })

            }
        ) { innerPadding ->
            Column(
                modifier = Modifier

                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Enable scrolling
                verticalArrangement = Arrangement.Top,
            ) {

            }
        }
    }
    @Composable
    fun SplashScreen() {
        LaunchedEffect(Unit) {
            delay(5000000)
            //         navController.navigate(LandingFragDirections.actionLandingFragToChatFrag())

            navController.navigate(SplashFragDirections.actionSplashFragToLoginFrag())
// Delay for 3000 milliseconds (3 seconds)
        }
        Scaffold(
            bottomBar =
                {
                    BottomAppBar(


                        containerColor = Color(0xFF3A7BD5)
                    ){
                        Box(Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("An App by Assetplus Consulting", textAlign = TextAlign.Center,
                                textDecoration = TextDecoration.Underline, color = Color.White,
                                style = TextStyle(fontSize = 18.sp, textAlign = TextAlign.Center)
                            )
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
                            .align(Alignment.Center)
                        .padding(8.dp)
                    /*      .paint(
                              painterResource(id = R.drawable.baseline_trending_up_24),
                              contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                          )*/,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Abstract background effect using a decorative shape
                    /*    Canvas(
                        modifier = Modifier
                            .size(250.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = size.minDimension / 2
                        )
                    }*/
                    Card(
                        onClick = {
                            navController.navigate(SplashFragDirections.actionSplashFragToLoginFrag())
                        },

                        modifier = Modifier
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)

                        // Padding around the text container

                    ) {
                        Text(

                            text = "INTELLISMART", // Replace with your title
                            fontSize = 32.sp, // Increased font size for better visibility
                            fontWeight = FontWeight.ExtraBold, // Bold text for emphasis

                            modifier = Modifier
                                .padding(8.dp),
                            //      .shadow(4.dp, shape = RectangleShape, clip = false), // Add subtle shadow for a 3D effect
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                letterSpacing = 1.5.sp, // Spacing for better readability
                                fontFamily = FontFamily.Monospace // Apply a serif font for a more formal feel
                            )
                        )

                    }
                    Spacer(modifier = Modifier.height(120.dp))

                    Image(
                        alignment = Alignment.Center,
                        painter = painterResource(id = R.drawable.intelismart_logo), // Replace with your image resource
                        contentDescription = "Splash Image",
                        modifier = Modifier
                            //   .padding(4.dp)
                            .fillMaxWidth()
                            .scale(1.6f)
                            //  .size(250.dp) // Adjust size as needed
                            .clip(CircleShape) // Circular image shape
                            .border(4.dp, Color.White, CircleShape) // Border around the image
                            .shadow(8.dp, CircleShape) // Add shadow to the image
                    )
                    Card(

                        modifier = Modifier
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFF3F51B5))
                        // Padding around the text container

                    ) {
                        Text(
                            text = "Smart Installation", // Replace with your title
                            fontSize = 24.sp, // Increased font size for better visibility
                            fontWeight = FontWeight.ExtraBold, // Bold text for emphasis

                            modifier = Modifier
                                .padding(8.dp),
                            //      .shadow(4.dp, shape = RectangleShape, clip = false), // Add subtle shadow for a 3D effect
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                letterSpacing = 1.5.sp, // Spacing for better readability
                                fontFamily = FontFamily.Monospace // Apply a serif font for a more formal feel
                            )
                        )

                    }
                    Card(

                        modifier = Modifier
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFF3F51B5))
                        // Padding around the text container

                    ) {
                        Text(
                            text = "AI Driven QC Smart Meter Installation App", // Replace with your title
                            fontSize = 18.sp, // Increased font size for better visibility
                            fontWeight = FontWeight.ExtraBold, // Bold text for emphasis

                            modifier = Modifier
                                .padding(8.dp),
                            //      .shadow(4.dp, shape = RectangleShape, clip = false), // Add subtle shadow for a 3D effect
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                letterSpacing = 1.5.sp, // Spacing for better readability
                                fontFamily = FontFamily.Monospace // Apply a serif font for a more formal feel
                            )
                        )

                    }



                }
            }
        }
    }


}