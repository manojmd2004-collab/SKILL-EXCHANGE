package com.mindmatrix.skillexchange.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mindmatrix.skillexchange.model.NeedPost
import com.mindmatrix.skillexchange.model.PostStatus
import com.mindmatrix.skillexchange.model.SwapOffer
import com.mindmatrix.skillexchange.viewmodel.SkillExchangeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillBoardScreen(navController: NavController, viewModel: SkillExchangeViewModel) {
    val posts by viewModel.needPosts.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var selectedSkillFilter by remember { mutableStateOf("All") }
    val skills = listOf("All", "Plumber", "Electrician", "Carpenter", "Mechanic")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skill Board") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_post") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Post")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            // Filter
            ScrollableTabRow(
                selectedTabIndex = skills.indexOf(selectedSkillFilter),
                edgePadding = 0.dp
            ) {
                skills.forEachIndexed { index, skill ->
                    Tab(
                        selected = selectedSkillFilter == skill,
                        onClick = { selectedSkillFilter = skill },
                        text = { Text(skill) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val filteredPosts = if (selectedSkillFilter == "All") posts else posts.filter { it.skillRequired == selectedSkillFilter }
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredPosts) { post ->
                    PostCard(post = post, onClick = {
                        viewModel.loadOffersForPost(post.postId)
                        navController.navigate("post_detail/${post.postId}")
                    })
                }
            }
        }
    }
}

@Composable
fun PostCard(post: NeedPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Need: ${post.skillRequired}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.description)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "By: ${post.authorName}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Status: ${post.status.name}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController, viewModel: SkillExchangeViewModel) {
    var description by remember { mutableStateOf("") }
    var skillRequired by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create Need Post") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = skillRequired,
                onValueChange = { skillRequired = it },
                label = { Text("Skill Required (e.g., Plumber)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (e.g., Need help with leaking roof)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (description.isNotBlank() && skillRequired.isNotBlank()) {
                        viewModel.addNeedPost(description, skillRequired)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post Need")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavController, viewModel: SkillExchangeViewModel, postId: String) {
    val posts by viewModel.needPosts.collectAsState()
    val offers by viewModel.offersForCurrentPost.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val post = posts.find { it.postId == postId }

    var offerMessage by remember { mutableStateOf("") }
    var offerHours by remember { mutableStateOf("1") }

    if (post == null) return

    Scaffold(
        topBar = { TopAppBar(title = { Text("Post Details") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Text(text = "Need: ${post.skillRequired}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(text = post.description, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Posted by: ${post.authorName}")
            Text(text = "Status: ${post.status}")
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(text = "Offers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(offers) { offer ->
                    OfferCard(
                        offer = offer,
                        isPostAuthor = currentUser?.userId == post.authorId,
                        onConfirmSwap = { viewModel.confirmSwap(offer.offerId) }
                    )
                }
            }

            if (currentUser?.userId != post.authorId && post.status == PostStatus.OPEN) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = offerMessage,
                    onValueChange = { offerMessage = it },
                    label = { Text("Make an offer (e.g., I'll do woodwork for roof repair)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = offerHours,
                    onValueChange = { if (it.all { char -> char.isDigit() }) offerHours = it },
                    label = { Text("Estimated Hours to work") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Button(
                    onClick = {
                        val hours = offerHours.toIntOrNull() ?: 1
                        if (offerMessage.isNotBlank()) {
                            viewModel.makeOffer(postId, offerMessage, hours)
                            offerMessage = ""
                            offerHours = "1"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Submit Offer")
                }
            }
        }
    }
}

@Composable
fun OfferCard(offer: SwapOffer, isPostAuthor: Boolean, onConfirmSwap: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Offerer: ${offer.offererName} (${offer.offeredSkill})", fontWeight = FontWeight.SemiBold)
            Text(text = "Message: ${offer.message}")
            Text(text = "Hours Offered: ${offer.hours}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Status: ${offer.status.name}")
                if (isPostAuthor && offer.status == com.mindmatrix.skillexchange.model.OfferStatus.PENDING) {
                    Button(onClick = onConfirmSwap) {
                        Text("Confirm Swap")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: SkillExchangeViewModel) {
    val user by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            user?.let { u ->
                Text(text = u.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "Skill Profile: ${u.skill}", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Trust Score", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "${u.trustScore}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Based on successful swaps", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Skill Points", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "${u.skillPoints}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "1 Hour = 1 Point", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: SkillExchangeViewModel) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Login") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (error.isNotBlank()) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.login(name, password) { success ->
                    if (success) {
                        navController.navigate("board") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        error = "Invalid credentials"
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Sign In") }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("signup") }) { Text("Don't have an account? Sign Up") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController, viewModel: SkillExchangeViewModel) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Sign Up") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = skill, onValueChange = { skill = it }, label = { Text("Skill (e.g., Plumber)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (name.isNotBlank() && password.isNotBlank() && skill.isNotBlank()) {
                    viewModel.signup(name, password, skill) { success ->
                        if (success) {
                            navController.navigate("board") {
                                popUpTo("signup") { inclusive = true }
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Create Account") }
        }
    }
}
