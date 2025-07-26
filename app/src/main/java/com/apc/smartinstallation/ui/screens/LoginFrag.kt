package com.apc.smartinstallation.ui.screens

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apc.smartinstallation.R
import com.apc.smartinstallation.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFrag : Fragment() {
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
                    LoginScreen()
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
    fun LoginScreen() {
        val username = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val iconList = listOf(
            R.drawable.outline_contact_support_24,
            R.drawable.outline_video_library_24,
            R.drawable.outline_apk_document_24
        )
        val titleList = listOf("Help", "Tutorial", "Manual")
        val loginState by vm.loginState.collectAsState()
        val userState by vm.userState.collectAsState()

        when (userState) {
            "-1" -> {
                // Show loading spinner while user state is being loaded
                CircularProgressIndicator()
            }

            else -> {
                if (userState == null) {
                    Log.d("loginState", "LoginScreen: null")

                    // User is not logged in, navigate to LoginScreen
                    //  navController.navigate(LoginFragDirections.actionLoginFragToHomeFrag())
                } else {
                    Log.d("loginState", "LoginScreen: ${userState.toString()}")
                    navController.navigate(LoginFragDirections.actionLoginFragToConListFrag())
                    vm.reInitLogin()
                    // User is logged in, navigate to HomeScreen
                    //  vm.getAssignedConsumers(userState!!)
                }
            }
        }

        when (loginState) {
            is MainViewModel.LoginEvent.Loading -> {
                CircularProgressIndicator() // Show loading indicator
            }

            is MainViewModel.LoginEvent.Success -> {
                val data = (loginState as MainViewModel.LoginEvent.Success).resultText
                //       Toast.makeText(mContext, "${data?.userDetails?.Username}", Toast.LENGTH_LONG).show()

                Text(text = "Welcome, ${data?.username}!") // Display success message
                        vm.saveUser(data!!)



                //   vm.reInitLogin()

            }

            is MainViewModel.LoginEvent.Failure -> {
                Toast.makeText(
                    mContext,
                    (loginState as MainViewModel.LoginEvent.Failure).errorText.toString(),
                    Toast.LENGTH_LONG
                ).show()

                val error = (loginState as MainViewModel.LoginEvent.Failure).errorText
                Text(text = "Error: $error") // Display error message
            }

            is MainViewModel.LoginEvent.Empty -> {
                //      Toast.makeText(mContext,"empty", Toast.LENGTH_LONG).show()

                // Display the initial login form
                Text(text = "Empty") // Display error message


            }
        }
        Scaffold { innerPadding ->
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
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(64.dp))

                    // Logo Image
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        border = BorderStroke(2.dp, Color.Black),

                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        //     elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Image on the left
                            Image(
                                painter = painterResource(id = R.drawable.intelismart_logo), // Replace with your image resource
                                contentDescription = "Image",
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )

                            // Heading on the right
                            Text(
                                text = "Intellismart",
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(4.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp, // Increased font size for better visibility
                                fontWeight = FontWeight.ExtraBold, // Bold text for emphasis


                                //      .shadow(4.dp, shape = RectangleShape, clip = false), // Add subtle shadow for a 3D effect
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    letterSpacing = 1.5.sp, // Spacing for better readability
                                    fontFamily = FontFamily.Monospace // Apply a serif font for a more formal feel
                                )
                            )


                        }
                    }
                    // Title
                    Card(

                        modifier = Modifier
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color.Black)
                        // Padding around the text container

                    ) {
                        Text(
                            text = "LOGIN", // Replace with your title
                            fontSize = 36.sp, // Increased font size for better visibility
                            fontWeight = FontWeight.ExtraBold, // Bold text for emphasis

                            modifier = Modifier
                                .padding(16.dp),
                            //      .shadow(4.dp, shape = RectangleShape, clip = false), // Add subtle shadow for a 3D effect
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                letterSpacing = 1.5.sp, // Spacing for better readability
                                fontFamily = FontFamily.Monospace // Apply a serif font for a more formal feel
                            )
                        )
                    }
                    // Email Text Field


                    TextField(
                        trailingIcon = {
                            IconButton(onClick = { /* Handle icon click action */ }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle, // Use any icon from Icons
                                    contentDescription = "Search Icon"
                                )
                            }
                        },
                        value = username.value,
                        onValueChange = { username.value = it },
                        label = {
                            Text(
                                "Username",
                                style = TextStyle(
                                    fontSize = 18.sp, // Increased font size for better visibility
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                ),
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 18.sp, // Increased font size for better visibility
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        ),
                        modifier = Modifier
                            .padding(16.dp) // Padding around the TextField
                            .background(
                                Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ) // Yellow background with rounded corners
                            .border(
                                width = 2.dp, // Bold border width
                                color = Color.Black, // Border color
                                shape = RoundedCornerShape(8.dp) // Rounded corners for border
                            )
                            .padding(8.dp), // Inner padding for the TextField
                       /* colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent, // Set to transparent to use custom background color
                            focusedIndicatorColor = Color.Transparent, // Remove default indicator
                            unfocusedIndicatorColor = Color.Transparent // Remove default indicator
                        )*/
                    )

                    // Password Text Field

                    TextField(
                        trailingIcon = {
                            IconButton(onClick = { /* Handle icon click action */ }) {
                                Icon(
                                    imageVector = Icons.Default.Lock, // Use any icon from Icons
                                    contentDescription = "Search Icon"
                                )
                            }
                        },
                        value = password.value,
                        textStyle = TextStyle(
                            fontSize = 18.sp, // Increased font size for better visibility
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        ),
                        onValueChange = { password.value = it },
                        label = {
                            Text(
                                "Password", style = TextStyle(
                                    fontSize = 18.sp, // Increased font size for better visibility
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .padding(16.dp) // Padding around the TextField
                            .background(
                                Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ) // Yellow background with rounded corners
                            .border(
                                width = 2.dp, // Bold border width
                                color = Color.Black, // Border color
                                shape = RoundedCornerShape(8.dp) // Rounded corners for border
                            )
                            .padding(8.dp), // Inner padding for the TextField
                     /*   colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent, // Set to transparent to use custom background color
                            focusedIndicatorColor = Color.Transparent, // Remove default indicator
                            unfocusedIndicatorColor = Color.Transparent // Remove default indicator
                        )*/
                    )


                    // Login Button
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = Color(0xFF3F51B5)
                        ),
                        border = BorderStroke(2.dp, Color.White),

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),

                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            vm.loginUser(
                                    username.value,
                                    password.value
                            )
                        },

                        ) {
                        Text(text = "SUBMIT", fontSize = 24.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly // Space between cards
                    ) {
                        repeat(3) { index ->
                            Card(
                                border = BorderStroke(2.dp, Color.Black),

                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp), // Add some padding between cards
                                //   elevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth() // Make the column fill the width of the card
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally, // Center contents horizontally
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Image in each card
                                    Image(
                                        painter = painterResource(id = iconList[index]), // Replace with your image resource
                                        contentDescription = "Card Image",
                                        modifier = Modifier
                                            .size(75.dp) // Set image size
                                            .padding(bottom = 4.dp)
                                    )
                                    // Text in each card
                                    Text(
                                        text = titleList[index],
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color.Black
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